package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BudgetScenarioRepository extends CrudRepository<BudgetScenario, Long> {
    BudgetScenario findById(Long id);

    BudgetScenario findByBudgetIdAndName(long id, String budgetScenarioName);

    @Query( " SELECT DISTINCT bs" +
        " FROM Schedule s, Workgroup w, Budget b, BudgetScenario bs" +
        " WHERE bs.budget = b" +
        " AND b.schedule = s" +
        " AND s.workgroup = w" +
        " AND w.id = :workgroupId" +
        " AND s.year = :year")
    List<BudgetScenario> findbyWorkgroupIdAndYear(@Param("workgroupId") long workgroupId, @Param("year") long year);

    @Query( " SELECT DISTINCT bs" +
        " FROM Schedule s, Workgroup w, Budget b, BudgetScenario bs" +
        " WHERE bs.budget = b" +
        " AND bs.fromLiveData = :fromLiveData" +
        " AND b.schedule = s" +
        " AND s.workgroup = w" +
        " AND w.id = :workgroupId" +
        " AND s.year = :year")
    BudgetScenario findbyWorkgroupIdAndYearAndFromLiveData(long workgroupId, long year, boolean fromLiveData);
}
