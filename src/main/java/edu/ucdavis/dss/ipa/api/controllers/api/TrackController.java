package edu.ucdavis.dss.ipa.api.controllers.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.TagService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.utilities.UserLogger;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;

@RestController
public class TrackController {
	@Inject
	TagService tagService;
	@Inject WorkgroupService workgroupService;
	@Inject
	CourseService courseService;
	@Inject CurrentUser currentUser;

	@PreAuthorize("hasPermission(#id, 'track', 'academicCoordinator')")
	@RequestMapping(value = "/api/tracks/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Tag trackById(@PathVariable Long Id) {
		return this.tagService.getOneById(Id);
	}

	@PreAuthorize("hasPermission(#id, 'workgroup', 'academicCoordinator')"
			+ "or hasPermission(#id, 'workgroup', 'senateInstructor') or hasPermission(#id, 'workgroup', 'federationInstructor')")
	@RequestMapping(value ="/api/workgroups/{id}/tracks", method = RequestMethod.GET)
	@ResponseBody
	public List<Tag> getTracksByWorkgroupId(@PathVariable long id, HttpServletResponse httpResponse) {
		Workgroup workgroup = workgroupService.findOneById(id);

		if(workgroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		httpResponse.setStatus(HttpStatus.OK.value());
		return workgroup.getActiveTracks();
	}

	@PreAuthorize("hasPermission(#id, 'track', 'academicCoordinator')")
	@RequestMapping(value = "/api/tracks/{id}/courseOfferingGroups", method = RequestMethod.GET)
	@ResponseBody
	public List<Course> courseOfferingGroupsByTrackId(@PathVariable Long Id) {
		return this.courseService.findByTrackId(Id);
	}

	@PreAuthorize("hasPermission('*', 'academicCoordinator')")
	@RequestMapping(value = "/api/tracks/search", method = RequestMethod.GET)
	@ResponseBody
	public List<Tag> searchTracks (@RequestParam(value = "q", required = false) String query, @RequestParam(value = "workgroupId", required = false) Long workgroupId ) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		List<Tag> tags = new ArrayList<Tag>();

		tags = tagService.searchTags(query, workgroup);

		return tags;
	}

	@PreAuthorize("hasPermission(#id, 'track', 'academicCoordinator')")
	@RequestMapping(value = "/api/tracks/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteTrack(@PathVariable Long id, HttpServletResponse httpResponse) {
		Tag tag = this.tagService.getOneById(id);
		if (tag == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		this.tagService.archiveById(id);
		UserLogger.log(currentUser, "Deleted tag '" + tag.getName() + "' from workgroup " + tag.getWorkgroup().getName());
		httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
	}


	@PreAuthorize("hasPermission(#workgroupId, 'workgroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/tracks", method = RequestMethod.POST)
	@ResponseBody
	public Tag createTrack(@RequestParam(value = "workgroupId", required = false) String workgroupId, @RequestParam(value = "trackName", required = false) String trackName, HttpServletResponse httpResponse) {
		Workgroup workgroup = workgroupService.findOneById(Long.parseLong(workgroupId));

		Tag tag = new Tag();
		tag.setWorkgroup(workgroup);
		tag.setName(trackName);

		this.tagService.save(tag);
		
		UserLogger.log(currentUser, "Created tag '" + trackName + "' in workgroup " + workgroup.getName());

		return tag;
	}

	@PreAuthorize("hasPermission(#id, 'track', 'academicCoordinator')")
	@RequestMapping(value = "/api/tracks/{id}", method = RequestMethod.POST)
	@ResponseBody
	public Tag updateTrack(@RequestBody Tag newTag, @PathVariable Long id,
						   HttpServletResponse httpResponse_p) {
		Tag tag = tagService.getOneById(id);
		UserLogger.log(currentUser, "Renamed tag from '" + tag.getName() + "' to '" + newTag.getName() +"' in workgroup " + tag.getWorkgroup().getName());
		tag.setName(newTag.getName());

		httpResponse_p.setStatus(HttpStatus.OK.value());

		return tagService.save(tag);
	}
}
