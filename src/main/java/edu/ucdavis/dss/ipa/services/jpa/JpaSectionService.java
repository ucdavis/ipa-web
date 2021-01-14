package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.ActivityRepository;
import edu.ucdavis.dss.ipa.repositories.SectionRepository;
import edu.ucdavis.dss.ipa.repositories.SyncActionRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;

@Service
public class JpaSectionService implements SectionService {

	@Inject SectionRepository sectionRepository;
	@Inject SyncActionRepository syncActionRepository;
	@Inject	ActivityRepository activityRepository;

	@Override
	public Section save(@Valid Section section) {
		return sectionRepository.save(section);
	}

	@Override
	@Transactional
	public boolean delete(Long id) {
		this.sectionRepository.delete(id);
		return true;
	}

	@Override
	public Section getOneById(Long id) {
		return this.sectionRepository.findById(id);
	}

	/**
	 * Based on a new course sequencePattern, will recalculate and save a new section sequenceNumber
	 * @param sectionId
	 * @param newSequencePattern
     * @return
     */
	@Override
	public Section updateSequenceNumber(Long sectionId, String newSequencePattern) {
		Section section = this.getOneById(sectionId);

		if (newSequencePattern == null || newSequencePattern.length() == 0 || section == null) {
			return null;
		}

		Character firstChar = null;
		boolean isNumeric = true;

		firstChar = newSequencePattern.charAt(0);
		if (Character.isLetter(firstChar)) {
			isNumeric = false;
		} else {
			return null;
		}

		if (isNumeric) {
			section.setSequenceNumber(newSequencePattern);
		} else {
			String newSequenceNumber = firstChar + section.getSequenceNumber().substring(1);
			section.setSequenceNumber(newSequenceNumber);
		}

		return this.save(section);
	}

	@Override
	public List<Section> findVisibleByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode) {
		return sectionRepository.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
	}

	@Override
	public Section findOrCreateBySectionGroupAndSequenceNumber(SectionGroup sectionGroup, String sequenceNumber) {
		if (sectionGroup == null) {
			return null;
		}

		Section section = sectionRepository.findBySectionGroupIdAndSequenceNumber(sectionGroup.getId(), sequenceNumber);

		if (section == null) {
			section = new Section();
			section.setSequenceNumber(sequenceNumber);
			section.setSectionGroup(sectionGroup);
			section = sectionRepository.save(section);
		}

		return section;
	}

	@Override
	public List<Section> findVisibleByWorkgroupIdAndYear(long workgroupId, long year) {
		return sectionRepository.findVisibleByWorkgroupIdAndYear(workgroupId, year);
	}

	/**
	 * Returns true if the sequenceNumber of the section is unique within the sectionGroup
	 * @param section
	 * @return
     */
	public boolean hasValidSequenceNumber (Section section) {
		String potentialSequenceNumber = section.getSequenceNumber();

		if (section.getSectionGroup() == null) {
			return false;
		}

		for (Section slotSection : section.getSectionGroup().getSections()) {
			if (slotSection.getSequenceNumber().equals(potentialSequenceNumber) && section.getId() != slotSection.getId()) {
				return false;
			}
		}

		return true;
	}

	@Override
	@Transactional
	public void deleteWithCascade(Section section) {
		for(SyncAction syncAction : section.getSyncActions()){
			sectionRepository.deleteById(syncAction.getId());
		}
		for(Activity activity : section.getActivities()){
			System.err.println("Deleting activity " + activity.getId());
			activityRepository.deleteById(activity.getId());
		}
		sectionRepository.deleteById(section.getId());
	}
}
