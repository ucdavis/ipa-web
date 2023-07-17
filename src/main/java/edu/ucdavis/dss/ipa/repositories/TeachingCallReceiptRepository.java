package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;

import java.util.List;

public interface TeachingCallReceiptRepository extends CrudRepository<TeachingCallReceipt, Long> {

  TeachingCallReceipt findByInstructorIdAndScheduleId(Long instructorId, long scheduleId);

    List<TeachingCallReceipt> findByLockedFalseAndLockAfterDueDateTrue();

    List<TeachingCallReceipt> findByUnlockedAtNotNull();
}
