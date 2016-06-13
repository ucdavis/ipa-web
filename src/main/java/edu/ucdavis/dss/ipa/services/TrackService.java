package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Track;
import edu.ucdavis.dss.ipa.entities.Workgroup;

@Validated
public interface TrackService {
	Track saveTrack(@NotNull @Valid Track track);

	Track findOneById(Long id);

	List<CourseOfferingGroup> getCourseOfferingGroupsByTrackId(Long id);

	List<Track> searchTracks(String query, Workgroup workgroup);

	void archiveTrackByTrackId(Long id);

	/**
	 * Finds or Creates a track based on trackName and workgroup. If found the track will be unarchived
	 * and then returned.
	 * @param workgroup
	 * @param trackName
     * @return
     */
	Track findOrCreateTrackByWorkgroupAndTrackName(Workgroup workgroup, String trackName);

}
