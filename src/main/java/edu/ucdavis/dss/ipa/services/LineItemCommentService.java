package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.LineItemComment;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface LineItemCommentService {
    LineItemComment create(LineItemComment lineItemCommentDTO);

    List<LineItemComment> findByLineItems(List<LineItem> lineItems);
}