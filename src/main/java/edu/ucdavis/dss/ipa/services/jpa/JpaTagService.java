package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Tag;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.TrackRepository;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TagService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaTagService implements TagService {
	@Inject TrackRepository trackRepository;
	@Inject ScheduleService scheduleService;

	@Override
	@Transactional
	public Tag save(Tag tag)
	{
		return this.trackRepository.save(tag);
	}

	@Override
	public Tag getOneById(Long id) {
		return this.trackRepository.findOne(id);
	}

	@Override
	public List<Tag> searchTags(String query, Workgroup workgroup) {

		List<Tag> results = new ArrayList<Tag>();
		List<Tag> tags = new ArrayList<Tag>();

		tags = workgroup.getTags();
		
		for (Tag tag : tags) {
			if(tag.getName().toLowerCase().contains(query.toLowerCase())) {
				results.add(tag);
			}
		}
		
		return results;
	}

	@Override
	public Tag archiveById(Long id) {
		
		Tag tag = this.trackRepository.findOne(id);

		// Remove this tag from COGs only in active schedules
		for (Course cog : tag.getCourses()){
			boolean isHistorical = scheduleService.isScheduleClosed(cog.getSchedule().getId());
			if (!isHistorical) {
				List<Tag> tags = cog.getTags();
				tags.remove(tag);
				cog.setTags(tags);
			}
		}

		tag.setArchived(true);
		return this.trackRepository.save(tag);
	}

	@Override
	public Tag findOrCreateByWorkgroupAndName(Workgroup workgroup, String tagName, String tagColor) {
		if (workgroup == null) return null;

		Tag tag = this.trackRepository.findOneByWorkgroupIdAndName(workgroup.getId(), tagName);

		if (tag == null) {
			tag = new Tag();
			tag.setWorkgroup(workgroup);
			tag.setName(tagName);
			tag.setColor(tagColor);
			tag = this.trackRepository.save(tag);
		} else if (tag.isArchived()) {
			tag.setArchived(false);
			tag.setColor(tagColor);
			tag = this.trackRepository.save(tag);
		}

		return tag;
	}

	@Override
	public List<Tag> findByWorkgroupId(long workgroupId) {
		return trackRepository.findByWorkgroupId(workgroupId);
	}

}
