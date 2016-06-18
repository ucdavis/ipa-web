package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.ipa.api.components.workgroup.views.factories.WorkgroupViewFactory;
import edu.ucdavis.dss.ipa.entities.Tag;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.TagService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class WorkgroupViewTagController {

    @Inject WorkgroupViewFactory workgroupViewFactory;
    @Inject
    TagService tagService;
    @Inject WorkgroupService workgroupService;

    @PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
    @RequestMapping(value = "/api/workgroupView/{workgroupCode}/tags", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Tag addTag(@PathVariable String workgroupCode, @RequestBody Tag tag, HttpServletResponse httpResponse) {
        Workgroup workgroup = workgroupService.findOneByCode(workgroupCode);
        return tagService.findOrCreateTrackByWorkgroupAndTrackName(workgroup, tag.getName());
    }

    @PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
    @RequestMapping(value = "/api/workgroupView/{workgroupCode}/tags/{tagId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public Tag updateTag(@PathVariable String workgroupCode, @PathVariable long tagId,
                         @RequestBody Tag tag, HttpServletResponse httpResponse) {
        Tag editedTag = tagService.findOneById(tagId);
        editedTag.setName(tag.getName());
        return tagService.saveTrack(editedTag);
    }

    @PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
    @RequestMapping(value = "/api/workgroupView/{workgroupCode}/tags/{tagId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Tag archiveTag(@PathVariable String workgroupCode, @PathVariable long tagId, HttpServletResponse httpResponse) {
        return tagService.archiveTrackByTrackId(tagId);
    }
}
