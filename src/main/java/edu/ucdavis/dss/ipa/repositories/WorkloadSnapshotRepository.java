package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.WorkloadSnapshot;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface WorkloadSnapshotRepository extends CrudRepository<WorkloadSnapshot, Long> {
    WorkloadSnapshot findById(long id);

    List<WorkloadSnapshot> findByWorkgroupIdAndYear(long workgroupId, long year);

    WorkloadSnapshot findByBudgetScenarioId(long budgetScenarioId);
}
