package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.GraduateStudentRepository;
import edu.ucdavis.dss.ipa.services.GraduateStudentService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
public class JpaGraduateStudentService implements GraduateStudentService {
	@Inject GraduateStudentRepository graduateStudentRepository;
	@Inject WorkgroupService workgroupService;
	@Override
	public GraduateStudent saveGraduateStudent(GraduateStudent graduateStudent) {
		return graduateStudentRepository.save(graduateStudent);
	}

	@Override
	public GraduateStudent findOneById(Long id) {
		return graduateStudentRepository.findById(id);
	}

	@Override
	public GraduateStudent findOneByLoginId(String loginId) {
		return graduateStudentRepository.findByLoginId(loginId);
	}

	@Override
	public List<GraduateStudent> getAllTeachingAssistantGraduateStudentsByWorkgroupId(Long workgroupId) {
		List<GraduateStudent> teachingAssistants = new ArrayList<GraduateStudent>();
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		for (UserRole userRole : workgroup.getUserRoles()) {
			if(userRole.getRoleToken().equals("teachingAssistant") ) {
				GraduateStudent graduateStudent = this.findOneByLoginId(userRole.getUser().getLoginId());
				
				if (graduateStudent != null && !teachingAssistants.contains(graduateStudent) ) {
					teachingAssistants.add(graduateStudent);
				}
			}
		}
		return teachingAssistants;
	}

}
