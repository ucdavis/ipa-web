package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport;

import com.amazonaws.services.s3.model.ObjectMetadata;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportView;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories.WorkloadSummaryReportViewFactory;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.WorkloadAssignment;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkloadAssignmentService;
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
    @Inject
    WorkloadSummaryReportViewFactory workloadSummaryReportViewFactory;
    @Inject
    WorkloadAssignmentService workloadAssignmentService;

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
    public List<WorkloadAssignment> getWorkloadSummaryReportView(
        @PathVariable long workgroupId, @PathVariable long year) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        return workloadAssignmentService.generateWorkloadAssignments(workgroupId, year);
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
        authorizer.isDeansOffice();
        final String fileName = year + "_Workload_Summary_Report.xlsx";

        // overwrite with empty file to update modified time
        s3Service.upload(fileName, new byte[0]);

        long[] workgroupIds =
            authorization.getUserRoles().stream().filter(ur -> ur.getRole().getName().equals("academicPlanner")).map(
                UserRole::getWorkgroupIdentification).mapToLong(Long::longValue).toArray();


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
                    // Create failed. Clean up partial file.
                    s3Service.delete(fileName);
                }

                try {
                    s3Service.upload(fileName, bytes.get());

                    if (user != null) {
                        emailService.send(user.getEmail(), "Your download is ready - " + downloadUrl,
                            "IPA Workload Summary Report Download", true);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/api/workloadSummaryReport/{workgroupId}/years/{year}/downloadMultiple", method = RequestMethod.POST)
    public ResponseEntity downloadMultipleDepartments(@PathVariable long workgroupId, @PathVariable long year) {
        authorizer.isDeansOffice();

        byte[] bytes = s3Service.download(year + "_Workload_Summary_Report.xlsx");
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocumsent.spreadsheetml.sheet"))
            .contentLength(resource.contentLength())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
            .body(resource);
    }

    @RequestMapping(value = "/api/workloadSummaryReport/{workgroupId}/years/{year}/generateHistoricalExcel", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> generateHistoricalExcel(@PathVariable long workgroupId,
                                                       @PathVariable long year,
                                                       HttpServletRequest httpRequest) {

        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        String url =
            ipaUrlApi + "/download/workloadSummaryReport/" + workgroupId +
                "/years/" + year + "/historical/excel";
        String salt = RandomStringUtils.randomAlphanumeric(16).toUpperCase();

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        Map<String, String> map = new HashMap<>();
        map.put("redirect", url + "/" + salt + "/" + UrlEncryptor.encrypt(salt, ipAddress));

        return map;
    }

    @RequestMapping(value = "/download/workloadSummaryReport/{workgroupId}/years/{year}/historical/excel/{salt}/{encrypted}")
    public View downloadHistoricalExcel(@PathVariable long workgroupId, @PathVariable long year,
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
                .createHistoricalWorkloadExcelView(workgroupId, year);
        } else {
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            return null;
        }
    }

    @RequestMapping(value = "/api/workloadSummaryReport/years/{year}/download/status", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Object> getDownloadStatus(@PathVariable long year) {
        authorizer.isDeansOffice();

        ObjectMetadata metadata = s3Service.getMetadata(year + "_Workload_Summary_Report.xlsx");
        if (metadata != null) {
            Map<String, Object> md = new HashMap<>();
            md.put("lastModified", metadata.getLastModified().getTime());
            md.put("contentLength", metadata.getContentLength());

            return md;
        }
        return null;
    }
}
