package edu.ucdavis.dss.ipa.services;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;

import java.util.List;

@Validated
public interface TeachingCallReceiptService {

	TeachingCallReceipt findOneById(Long id);

	TeachingCallReceipt save(@NotNull @Valid TeachingCallReceipt teachingCallReceipt);

	void sendNotificationsByWorkgroupId(Long workgroupId);

	TeachingCallReceipt create(TeachingCallReceipt teachingCallReceipt);

	List<TeachingCallReceipt> createMany(List<Long> instructorIds, TeachingCallReceipt teachingCallReceipt);

	boolean delete(Long id);
}
