package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Workgroup;

@Validated
public interface TagService {
	Tag save(@NotNull @Valid Tag tag);

	Tag getOneById(Long id);

	List<Tag> searchTags(String query, Workgroup workgroup);

	Tag archiveById(Long id);

	/**
	 * Finds or Creates a track based on trackName and workgroup. If found the track will be unarchived
	 * and then returned.
	 * @param workgroup
	 * @param tagName
     * @param tagColor
	 * @return
     */
	Tag findOrCreateByWorkgroupAndName(Workgroup workgroup, String tagName, String tagColor);

}
