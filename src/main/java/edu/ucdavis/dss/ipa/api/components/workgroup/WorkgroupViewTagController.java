package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.ipa.api.components.workgroup.views.factories.WorkgroupViewFactory;
import edu.ucdavis.dss.ipa.entities.Tag;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.authorization.TagAuthorizer;
import edu.ucdavis.dss.ipa.services.TagService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class WorkgroupViewTagController {

    @Inject WorkgroupViewFactory workgroupViewFactory;
    @Inject TagService tagService;
    @Inject WorkgroupService workgroupService;
    @Inject TagAuthorizer tagAuthorizer;

    @PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
    @RequestMapping(value = "/api/workgroupView/{workgroupId}/tags", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Tag addTag(@PathVariable Long workgroupId, @RequestBody Tag tag, HttpServletResponse httpResponse) {
        tagAuthorizer.authorize(tag, workgroupId);

        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        return tagService.findOrCreateByWorkgroupAndName(workgroup, tag.getName());
    }

    @PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
    @RequestMapping(value = "/api/workgroupView/{workgroupId}/tags/{tagId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public Tag updateTag(@PathVariable Long workgroupId, @PathVariable long tagId,
                         @RequestBody Tag tag, HttpServletResponse httpResponse) {
        Tag editedTag = tagService.getOneById(tagId);
        editedTag.setName(tag.getName());
        return tagService.save(editedTag);
    }

    @PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
    @RequestMapping(value = "/api/workgroupView/{workgroupId}/tags/{tagId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Tag archiveTag(@PathVariable Long workgroupId, @PathVariable long tagId, HttpServletResponse httpResponse) {
        return tagService.archiveById(tagId);
    }
}
