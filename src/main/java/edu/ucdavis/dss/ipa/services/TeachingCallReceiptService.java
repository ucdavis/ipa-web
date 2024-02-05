package edu.ucdavis.dss.ipa.services;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;

import java.util.List;

@Validated
public interface TeachingCallReceiptService {

	TeachingCallReceipt findOneById(Long id);

	TeachingCallReceipt findOneByScheduleIdAndInstructorId(Long scheduleId, Long instructorId);

	TeachingCallReceipt save(@NotNull @Valid TeachingCallReceipt teachingCallReceipt);

	List<TeachingCallReceipt> saveAll(List<TeachingCallReceipt> teachingCallReceipts);

	void sendNotificationsByWorkgroupId(Long workgroupId);

	TeachingCallReceipt create(TeachingCallReceipt teachingCallReceipt);

	List<TeachingCallReceipt> createOrUpdateMany(List<Long> instructorIds, TeachingCallReceipt teachingCallReceipt);

	boolean delete(Long id);

	void lockExpiredReceipts();
}
