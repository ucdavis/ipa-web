package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.SchedulingNote;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.repositories.SchedulingNoteRepository;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.services.SchedulingNoteService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.UserService;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class JpaSchedulingNoteService implements SchedulingNoteService {
    @Inject
    SchedulingNoteRepository schedulingNoteRepository;
    @Inject
    UserService userService;
    @Inject
    SectionGroupService sectionGroupService;
    @Inject
    Authorization authorization;

    @Override
    public SchedulingNote create(SchedulingNote schedulingNoteDTO) {
        SchedulingNote schedulingNote = new SchedulingNote();

        User user = userService.getOneByLoginId(authorization.getLoginId());

        schedulingNote.setSectionGroup(schedulingNoteDTO.getSectionGroup());
        schedulingNote.setUser(user);
        schedulingNote.setAuthorName(user.getDisplayName());
        schedulingNote.setMessage(schedulingNoteDTO.getMessage());

        schedulingNote = this.schedulingNoteRepository.save(schedulingNote);

        return schedulingNote;
    }
}
