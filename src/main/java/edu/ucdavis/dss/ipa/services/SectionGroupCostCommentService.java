package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.SectionGroupCostComment;
import org.springframework.validation.annotation.Validated;

@Validated
public interface SectionGroupCostCommentService {
    SectionGroupCostComment create(SectionGroupCostComment sectionGroupCostCommentDTO);
}