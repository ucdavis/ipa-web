package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport;

import com.amazonaws.services.s3.model.ObjectMetadata;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportView;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories.WorkloadSummaryReportViewFactory;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import edu.ucdavis.dss.ipa.utilities.S3Service;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;

@RestController
public class WorkloadSummaryReportController {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger("edu.ucdavis.ipa");

    @Inject
    WorkloadSummaryReportViewFactory workloadSummaryReportViewFactory;
    @Inject
    S3Service s3Service;
    @Inject
    UserService userService;
    @Inject
    EmailService emailService;
    @Inject
    Authorizer authorizer;
    @Inject
    Authorization authorization;

    @Value("${IPA_URL_API}")
    String ipaUrlApi;

    @Value("${IPA_URL_FRONTEND}")
    String ipaUrlFrontend;

    @RequestMapping(value = "/api/workloadSummaryReport/{workgroupId}/years/{year}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public WorkloadSummaryReportView getWorkloadSummaryReportView(
        @PathVariable long workgroupId, @PathVariable long year) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        return workloadSummaryReportViewFactory.createWorkloadSummaryReportView(workgroupId, year);
    }

    @RequestMapping(value = "/api/workloadSummaryReport/{workgroupIds}/years/{year}/generateExcel", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> generateExcel(@PathVariable List<Long> workgroupIds,
                                             @PathVariable long year,
                                             HttpServletRequest httpRequest) {

        for (long workgroupId : workgroupIds) {
            authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");
        }

        String url =
            ipaUrlApi + "/download/workloadSummaryReport/" + StringUtils.join(workgroupIds, ",") +
                "/years/" + year + "/excel";
        String salt = RandomStringUtils.randomAlphanumeric(16).toUpperCase();

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        Map<String, String> map = new HashMap<>();
        map.put("redirect", url + "/" + salt + "/" + UrlEncryptor.encrypt(salt, ipAddress));

        return map;
    }

    @RequestMapping(value = "/download/workloadSummaryReport/{workgroupId}/years/{year}/excel/{salt}/{encrypted}")
    public View downloadExcel(@PathVariable long[] workgroupId, @PathVariable long year,
                              @PathVariable String salt, @PathVariable String encrypted,
                              HttpServletRequest httpRequest, HttpServletResponse httpResponse)
        throws ParseException {
        long TIMEOUT = 30L; // In seconds

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        boolean isValidUrl = UrlEncryptor.validate(salt, encrypted, ipAddress, TIMEOUT);

        if (isValidUrl) {
            return workloadSummaryReportViewFactory
                .createWorkloadSummaryReportExcelView(workgroupId, year);
        } else {
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            return null;
        }
    }

    @RequestMapping(value = "/api/workloadSummaryReport/{workgroupId}/years/{year}/generateMultiple", method = RequestMethod.GET)
    public ResponseEntity generateMultipleDepartments(@PathVariable long workgroupId,
                                                      @PathVariable long year) {
        // overwrite with empty file to update modified time
        s3Service.upload("Workload_Summary_Report.xlsx", new byte[0]);

        long[] workgroupIds =
            authorization.getUserRoles().stream().filter(ur -> ur.getRole().getName().equals("academicPlanner")).map(
                UserRole::getWorkgroupIdentification).mapToLong(Long::longValue).toArray();

        for (long id : workgroupIds) {
            authorizer.hasWorkgroupRoles(id, "academicPlanner", "reviewer");
        }

        User user = userService.getOneByLoginId(authorization.getRealUserLoginId());
        String downloadUrl = ipaUrlFrontend + "/summary/" + workgroupId + "/" + year + "?mode=download";

        CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return workloadSummaryReportViewFactory.createWorkloadSummaryReportBytes(workgroupIds, year);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            )
            .thenAccept(bytes ->
            {
                if (bytes == null) {
                    System.err.println("Unable to fetch workload data. Deleting partial file");
                    s3Service.delete("Workload_Summary_Report.xlsx");
                }

                System.out.println("Finished generating file. Uploading to S3");
                try {
                    s3Service.upload("Workload_Summary_Report.xlsx",
                        bytes.get());

                    if (user != null) {
                    System.out.println("Upload completed, sending email to " + user.getEmail());

                    emailService.send(user.getEmail(), "Your download is ready - " + downloadUrl,
                        "IPA Workload Summary Report Download", true);
                }
                } catch (InterruptedException e) {
                    System.out.println("Upload failed");
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    System.out.println("Upload failed");
                    e.printStackTrace();
                }
            });

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/api/workloadSummaryReport/{workgroupId}/years/{year}/downloadMultiple", method = RequestMethod.POST)
    public ResponseEntity downloadMultipleDepartments(@PathVariable long workgroupId, @PathVariable long year) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        byte[] bytes = s3Service.download("Workload_Summary_Report.xlsx");
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocumsent.spreadsheetml.sheet"))
            .contentLength(resource.contentLength())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
            .body(resource);
    }

    @RequestMapping(value = "/api/workloadSummaryReport/download/status", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Object> getDownloadStatus() {
        authorizer.isAuthorized();

        ObjectMetadata metadata = s3Service.getMetadata("Workload_Summary_Report.xlsx");
        if (metadata != null) {
            Map<String, Object> md = new HashMap<>();
            md.put("lastModified", metadata.getLastModified().getTime());
            md.put("contentLength", metadata.getContentLength());

            return md;
        }
        return null;
    }
}
