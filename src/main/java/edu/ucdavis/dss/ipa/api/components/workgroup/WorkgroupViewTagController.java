package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.ipa.entities.Tag;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.TagService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

@RestController
public class WorkgroupViewTagController {
    @Inject TagService tagService;
    @Inject WorkgroupService workgroupService;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/workgroupView/{workgroupId}/tags", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Tag addTag(@PathVariable Long workgroupId, @RequestBody Tag tag) {
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        return tagService.findOrCreateByWorkgroupAndName(workgroup, tag.getName(), tag.getColor());
    }

    @RequestMapping(value = "/api/workgroupView/{workgroupId}/tags/{tagId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public Tag updateTag(@PathVariable Long workgroupId, @PathVariable long tagId,
                         @RequestBody Tag tag) {
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        Tag editedTag = tagService.getOneById(tagId);
        editedTag.setName(tag.getName());
        editedTag.setColor(tag.getColor());
        return tagService.save(editedTag);
    }

    @RequestMapping(value = "/api/workgroupView/{workgroupId}/tags/{tagId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Tag archiveTag(@PathVariable Long workgroupId, @PathVariable long tagId) {
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        return tagService.archiveById(tagId);
    }
}
