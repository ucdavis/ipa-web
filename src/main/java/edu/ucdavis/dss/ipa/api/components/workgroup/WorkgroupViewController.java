package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupView;
import edu.ucdavis.dss.ipa.api.components.workgroup.views.factories.WorkgroupViewFactory;
import edu.ucdavis.dss.ipa.entities.Track;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.TrackService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class WorkgroupViewController {

	@Inject WorkgroupViewFactory workgroupViewFactory;
	@Inject TrackService trackService;
	@Inject WorkgroupService workgroupService;

	@PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/workgroupView/{workgroupCode}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public WorkgroupView getWorkgroupViewByCode(@PathVariable String workgroupCode, HttpServletResponse httpResponse) {
		return workgroupViewFactory.createWorkgroupView(workgroupCode);
	}

	@PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/workgroupView/{workgroupCode}/tags", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public Track addTag(@PathVariable String workgroupCode, @RequestBody Track tag, HttpServletResponse httpResponse) {
		Workgroup workgroup = workgroupService.findOneByCode(workgroupCode);
		return trackService.findOrCreateTrackByWorkgroupAndTrackName(workgroup, tag.getName());
	}

	@PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/workgroupView/{workgroupCode}/tags/{tagId}", method = RequestMethod.PUT, produces="application/json")
	@ResponseBody
	public Track updateTag(@PathVariable String workgroupCode, @PathVariable long tagId,
						   @RequestBody Track tag, HttpServletResponse httpResponse) {
		Track editedTag = trackService.findOneById(tagId);
		editedTag.setName(tag.getName());
		return trackService.saveTrack(editedTag);
	}

	@PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/workgroupView/{workgroupCode}/tags/{tagId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public void deleteTag(@PathVariable String workgroupCode, @PathVariable long tagId, HttpServletResponse httpResponse) {
		trackService.archiveTrackByTrackId(tagId);
	}

}
