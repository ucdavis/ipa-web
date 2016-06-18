package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Workgroup;

@Validated
public interface TagService {
	Tag saveTrack(@NotNull @Valid Tag tag);

	Tag findOneById(Long id);

	List<Course> getCourseOfferingGroupsByTrackId(Long id);

	List<Tag> searchTracks(String query, Workgroup workgroup);

	Tag archiveTrackByTrackId(Long id);

	/**
	 * Finds or Creates a track based on trackName and workgroup. If found the track will be unarchived
	 * and then returned.
	 * @param workgroup
	 * @param trackName
     * @return
     */
	Tag findOrCreateTrackByWorkgroupAndTrackName(Workgroup workgroup, String trackName);

	List<Tag> getTagsByCourseId(Long courseId);

}
