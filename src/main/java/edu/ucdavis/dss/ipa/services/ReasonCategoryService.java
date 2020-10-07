package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.ReasonCategory;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ReasonCategoryService {
    List<ReasonCategory> findAll();
}
