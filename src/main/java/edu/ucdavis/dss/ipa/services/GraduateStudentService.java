package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.GraduateStudent;

@Validated
public interface GraduateStudentService {
	GraduateStudent saveGraduateStudent(@NotNull @Valid GraduateStudent graduateStudent);

	GraduateStudent findOneById(Long id);

	GraduateStudent findOneByLoginId(String loginId);
	
	List<GraduateStudent> getAllTeachingAssistantGraduateStudentsByWorkgroupId(Long workgroupId);
}
