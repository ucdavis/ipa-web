package edu.ucdavis.dss.ipa.services;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;

import java.util.List;

@Validated
public interface TeachingCallReceiptService {

	public TeachingCallReceipt findOneById(Long id);

	public TeachingCallReceipt save(@NotNull @Valid TeachingCallReceipt teachingCallReceipt);

	public void sendNotificationsByWorkgroupId(Long workgroupId);

	public TeachingCallReceipt create(TeachingCallReceipt teachingCallReceipt);

	public List<TeachingCallReceipt> createMany(List<Long> instructorIds, TeachingCallReceipt teachingCallReceipt);
}
