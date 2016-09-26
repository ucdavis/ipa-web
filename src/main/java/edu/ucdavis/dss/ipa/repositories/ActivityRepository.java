package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Activity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityRepository extends CrudRepository<Activity, Long> {

	void deleteAllBySectionId(long sectionId);

    List<Activity> findBySection_SectionGroup_Id(long sectionGroupId);

    List<Activity> findBySectionGroupId(long sectionGroupId);

    @Query( " SELECT DISTINCT a" +
            " FROM Course c, SectionGroup sg, Section s, Schedule sch, Activity a" +
            " WHERE sg.course = c" +
            " AND s.sectionGroup = sg" +
            " AND c.schedule = sch" +
            " AND sch.year = :year" +
            " AND sch.workgroup.id = :workgroupId" +
            " AND sg.termCode = :termCode" +
            " AND (" +
            "        a.sectionGroup = sg" +
            "    OR  a.section = s" +
            " )" +
            " AND (s.visible = true OR s.visible IS NULL)")
    List<Activity> findVisibleByWorkgroupIdAndYearAndTermCode(
            @Param("workgroupId") long workgroupId,
            @Param("year") long year,
            @Param("termCode") String termCode);

    @Query( " SELECT DISTINCT a" +
            " FROM Course c, SectionGroup sg, Section s, Schedule sch, Activity a" +
            " WHERE sg.course = c" +
            " AND s.sectionGroup = sg" +
            " AND c.schedule = sch" +
            " AND sch.year = :year" +
            " AND sch.workgroup.id = :workgroupId" +
            " AND sg.termCode = :termCode" +
            " AND (" +
            "        a.sectionGroup = sg" +
            "    OR  a.section = s" +
            " )")
    List<Activity> findByWorkgroupIdAndYearAndTermCode(
            @Param("workgroupId") long workgroupId,
            @Param("year") long year,
            @Param("termCode") String termCode);
}
