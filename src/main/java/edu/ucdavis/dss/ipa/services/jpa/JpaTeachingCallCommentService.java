package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.TeachingCallComment;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;
import edu.ucdavis.dss.ipa.repositories.TeachingCallCommentRepository;
import edu.ucdavis.dss.ipa.services.TeachingCallCommentService;
import edu.ucdavis.dss.ipa.services.TeachingCallReceiptService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class JpaTeachingCallCommentService implements TeachingCallCommentService {
    @Inject TeachingCallReceiptService teachingCallReceiptService;
    @Inject TeachingCallCommentRepository teachingCallCommentRepository;

    @Override
    public TeachingCallComment create(TeachingCallComment teachingCallCommentDTO) {
        TeachingCallComment teachingCallComment = new TeachingCallComment();

        TeachingCallReceipt teachingCallReceipt = teachingCallReceiptService.findOneById(teachingCallCommentDTO.getTeachingCallReceipt().getId());

        teachingCallComment.setTeachingCallReceipt(teachingCallReceipt);
        teachingCallComment.setComment(teachingCallCommentDTO.getComment());

        teachingCallComment = this.teachingCallCommentRepository.save(teachingCallComment);

        return teachingCallComment;
    }
}
