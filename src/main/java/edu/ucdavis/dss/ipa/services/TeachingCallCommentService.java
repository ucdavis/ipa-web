package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.TeachingCallComment;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TeachingCallCommentService {
    TeachingCallComment create(TeachingCallComment teachingCallCommentDTO);
}
