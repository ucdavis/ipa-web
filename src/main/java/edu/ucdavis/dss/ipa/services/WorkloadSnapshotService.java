package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.WorkloadSnapshot;
import java.util.List;

public interface WorkloadSnapshotService {
    List<WorkloadSnapshot> findByWorkgroupIdAndYear(long workgroupId, long year);

    WorkloadSnapshot create(long workgroupId, long budgetScenarioId);

    void deleteByBudgetScenarioId(long budgetScenarioId);
}
