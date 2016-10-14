package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.SectionRepository;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.TermService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaSectionService implements SectionService {

	@Inject SectionRepository sectionRepository;
	@Inject CourseService courseService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject TermService termService;

	@Override
	public Section save(Section section) {
		if (isLocked(section)) return null;

		return sectionRepository.save(section);
	}

	@Override
	@Transactional
	public boolean delete(Long id) {
		Section section = this.getOneById(id);
		if (isLocked(section)) return false;

		this.sectionRepository.delete(id);
		return true;
	}

	@Override
	public Section getOneById(Long id) {
		return this.sectionRepository.findById(id);
	}

	@Override
	@Transactional
	public boolean deleteByCourseIdAndSequence(Long courseId, String sequence) {
		boolean deletedAll = true;
		Course course = this.courseService.getOneById(courseId);

		for (SectionGroup sg : course.getSectionGroups()) {
			List<Section> toDelete = new ArrayList<>();
			for (Section section : sg.getSections()) {
				if (section.getSequenceNumber().equals(sequence)) {
					toDelete.add(section);
				}
			}
			for (Section section : toDelete) {
				if (this.delete(section.getId())) {
					sg.getSections().remove(section);
				} else {
					deletedAll = false;
				}
			}
		}

		return deletedAll;
	}

	@Override
	public boolean updateSequencesByCourseId(Long courseId, String oldSequence, String newSequence) {
		boolean renamedAll = true;
		Course course = this.courseService.getOneById(courseId);

		for (SectionGroup sectionGroup : course.getSectionGroups()) {
			for (Section section : sectionGroup.getSections()) {
				if (section.getSequenceNumber().equals(oldSequence)) {
					if (isLocked(section)) {
						renamedAll = false;
						break;
					} else {
						section.setSequenceNumber(newSequence);
					}
				}
			}
		}

		if (renamedAll) {
			this.courseService.save(course);
		}

		return renamedAll;
	}

	private boolean isLocked(Section section) {
		SectionGroup sectionGroup = section.getSectionGroup();
		if (sectionGroup == null) { return true; }

		Course course = sectionGroup.getCourse();
		Term term = termService.getOneByTermCode(sectionGroup.getTermCode());

		if (course == null) { return true; }
		ScheduleTermState termState = this.scheduleTermStateService.createScheduleTermState(term);

		if (termState != null && termState.scheduleTermLocked()) {
			ExceptionLogger.logAndMailException(
					this.getClass().getName(),
					new UnsupportedOperationException("Term " + term.getTermCode() + " is locked")
					);
			return true;
		}

		return false;
	}

}
