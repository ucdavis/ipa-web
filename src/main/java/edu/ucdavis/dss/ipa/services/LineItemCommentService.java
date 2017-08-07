package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.LineItemComment;
import org.springframework.validation.annotation.Validated;

@Validated
public interface LineItemCommentService {
    LineItemComment create(LineItemComment lineItemCommentDTO);
}