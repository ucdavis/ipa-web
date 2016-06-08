package edu.ucdavis.dss.ipa.web.controllers.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Track;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.CourseOfferingGroupService;
import edu.ucdavis.dss.ipa.services.TrackService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.utilities.UserLogger;
import edu.ucdavis.dss.ipa.web.helpers.CurrentUser;

@RestController
public class TrackController {
	@Inject TrackService trackService;
	@Inject WorkgroupService workgroupService;
	@Inject CourseOfferingGroupService courseOfferingGroupService;
	@Inject CurrentUser currentUser;

	@PreAuthorize("hasPermission(#id, 'track', 'academicCoordinator')")
	@RequestMapping(value = "/api/tracks/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Track trackById(@PathVariable Long Id) {
		return this.trackService.findOneById(Id);
	}

	@PreAuthorize("hasPermission(#id, 'workgroup', 'academicCoordinator')"
			+ "or hasPermission(#id, 'workgroup', 'senateInstructor') or hasPermission(#id, 'workgroup', 'federationInstructor')")
	@RequestMapping(value ="/api/workgroups/{id}/tracks", method = RequestMethod.GET)
	@ResponseBody
	public List<Track> getTracksByWorkgroupId(@PathVariable long id, HttpServletResponse httpResponse) {
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
	public List<CourseOfferingGroup> courseOfferingGroupsByTrackId(@PathVariable Long Id) {
		return this.trackService.getCourseOfferingGroupsByTrackId(Id);
	}

	@PreAuthorize("hasPermission('*', 'academicCoordinator')")
	@RequestMapping(value = "/api/tracks/search", method = RequestMethod.GET)
	@ResponseBody
	public List<Track> searchTracks (@RequestParam(value = "q", required = false) String query, @RequestParam(value = "workgroupId", required = false) Long workgroupId ) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		List<Track> tracks = new ArrayList<Track>();

		tracks = trackService.searchTracks(query, workgroup);

		return tracks;
	}

	@PreAuthorize("hasPermission(#id, 'track', 'academicCoordinator')")
	@RequestMapping(value = "/api/tracks/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteTrack(@PathVariable Long id, HttpServletResponse httpResponse) {
		Track track = this.trackService.findOneById(id);
		if (track == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		this.trackService.archiveTrackByTrackId(id);
		UserLogger.log(currentUser, "Deleted track '" + track.getName() + "' from workgroup " + track.getWorkgroup().getName());
		httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
	}


	@PreAuthorize("hasPermission(#workgroupId, 'workgroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/tracks", method = RequestMethod.POST)
	@ResponseBody
	public Track createTrack(@RequestParam(value = "workgroupId", required = false) String workgroupId, @RequestParam(value = "trackName", required = false) String trackName, HttpServletResponse httpResponse) {
		Workgroup workgroup = workgroupService.findOneById(Long.parseLong(workgroupId));

		Track track = new Track();
		track.setWorkgroup(workgroup);
		track.setName(trackName);

		this.trackService.saveTrack(track);
		
		UserLogger.log(currentUser, "Created track '" + trackName + "' in workgroup " + workgroup.getName());

		return track;
	}

	@PreAuthorize("hasPermission(#id, 'track', 'academicCoordinator')")
	@RequestMapping(value = "/api/tracks/{id}", method = RequestMethod.POST)
	@ResponseBody
	public Track updateTrack( @RequestBody Track newTrack, @PathVariable Long id, 
			HttpServletResponse httpResponse_p) {
		Track track = trackService.findOneById(id);
		UserLogger.log(currentUser, "Renamed track from '" + track.getName() + "' to '" + newTrack.getName() +"' in workgroup " + track.getWorkgroup().getName());
		track.setName(newTrack.getName());

		httpResponse_p.setStatus(HttpStatus.OK.value());

		return trackService.saveTrack(track);
	}
}
