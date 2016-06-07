package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;

public interface TeachingCallReceiptRepository extends CrudRepository<TeachingCallReceipt, Long> {

	TeachingCallReceipt findOneByTeachingCallIdAndInstructorId(Long teachingCallId, long instructorId);

}
