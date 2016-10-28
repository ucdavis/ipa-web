package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Section;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionRepository extends CrudRepository<Section, Long> {

	Section findById(Long id);

    /**
     * Finds sections that do have sections and those sections have the
     * visible flag set to true or Null
     * @param workgroupId
     * @param year
     * @param termCode
     * @return
     */
    @Query( " SELECT s" +
            " FROM Course c, SectionGroup sg, Section s, Schedule sch" +
            " WHERE sg.course = c" +
            " AND s.sectionGroup = sg" +
            " AND c.schedule = sch" +
            " AND sch.year = :year" +
            " AND sch.workgroup.id = :workgroupId" +
            " AND sg.termCode = :termCode" +
            " AND (s.visible = true OR s.visible IS NULL)")
    List<Section> findByWorkgroupIdAndYearAndTermCode(
            @Param("workgroupId") long workgroupId,
            @Param("year") long year,
            @Param("termCode") String termCode);
}
