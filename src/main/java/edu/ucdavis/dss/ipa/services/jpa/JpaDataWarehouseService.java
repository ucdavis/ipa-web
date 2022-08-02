package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.dw.dto.DwCensus;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.DataWarehouseService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class JpaDataWarehouseService implements DataWarehouseService {
    @Inject DataWarehouseRepository dwRepository;

    /**
     * @param courses
     * @return { "AST": ["AST-001-001": 100], "PHY": [...] }
     */
    @Override
    public Map<String, Map<String, Long>> generateCourseCensusMap(List<Course> courses) {
        Map<String, HashSet<String>> courseMap = new HashMap<>();

        for (Course c : courses) {
            if (!courseMap.containsKey(c.getSubjectCode())) {
                courseMap.put(c.getSubjectCode(), new HashSet<>());
            }

            courseMap.get(c.getSubjectCode()).add(c.getCourseNumber());
        }

        List<CompletableFuture<List<DwCensus>>> courseCensusFutures = new ArrayList<>();

        for (String subjectCode : courseMap.keySet()) {
            courseCensusFutures.addAll(courseMap.get(subjectCode).stream().map(
                courseNumber -> CompletableFuture.supplyAsync(
                    () -> dwRepository.getCensusBySubjectCodeAndCourseNumber(subjectCode, courseNumber))).collect(
                Collectors.toList())
            );
        }

        List<DwCensus> censusList =
            courseCensusFutures.stream().map(CompletableFuture::join).flatMap(Collection::stream)
                .filter(c -> "CURRENT".equals(c.getSnapshotCode())).collect(Collectors.toList());

        Map<String, Map<String, Long>> censusMap = new HashMap<>();

        for (DwCensus census : censusList) {
            String termCode = census.getTermCode();
            String courseKey =
                census.getSubjectCode() + "-" + census.getCourseNumber() + "-" + census.getSequencePattern();

            censusMap.computeIfAbsent(termCode, k -> new HashMap<>());

            if (censusMap.get(termCode).get(courseKey) == null) {
                censusMap.get(termCode).put(courseKey, census.getCurrentEnrolledCount());
            } else {
                censusMap.get(termCode)
                    .put(courseKey, censusMap.get(termCode).get(courseKey) + census.getCurrentEnrolledCount());
            }
        }

        return censusMap;
    }
}
