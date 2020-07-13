package edu.ucdavis.dss.ipa.services;

import static edu.ucdavis.dss.ipa.entities.enums.BudgetCount.*;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.enums.BudgetCount;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class BudgetCalculationService {
    /**
     * @return {
     *     termCode: {
     *         TA_COUNT: BigDecimal,
     *         TA_COST: BigDecimal,
     *         ...
     *     }
     * }
     */
    public static Map<String, Map<BudgetCount, BigDecimal>> calculateTermTotals(Budget budget, List<SectionGroupCost> sectionGroupCosts, List<String> termCodes) {
        Map<String, Map<BudgetCount, BigDecimal>> termTotals = new HashMap<>();

        for (String termCode : termCodes) {
            Map<BudgetCount, BigDecimal> budgetCountMap = generateBudgetCountMap();
            termTotals.put(termCode, budgetCountMap);
        }

        // add another map for yearly total
        Map<BudgetCount, BigDecimal> budgetCountMap = generateBudgetCountMap();
        termTotals.put("combined", budgetCountMap);

        // new BigDecimal(String) preferred over BigDecimal.valueOf(double)?
        // new BigDecimal(String.valueOf()) = 7303.83
        // new BigDecimal(float) = 7303.830078125
        // BigDecimal.valueOf(float) = 7303.830078125
        BigDecimal baseTaCost = new BigDecimal(String.valueOf((budget.getTaCost())));
        BigDecimal baseReaderCost = new BigDecimal(String.valueOf((budget.getReaderCost())));

        for (SectionGroupCost sectionGroupCost : sectionGroupCosts) {
            BigDecimal taCount = sectionGroupCost.getTaCount() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getTaCount()));
            BigDecimal readerCount = sectionGroupCost.getReaderCount() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getReaderCount()));

            termTotals.get(sectionGroupCost.getTermCode()).put(TA_COUNT, termTotals.get(sectionGroupCost.getTermCode()).get(TA_COUNT).add(taCount));
            termTotals.get(sectionGroupCost.getTermCode()).put(TA_COST, termTotals.get(sectionGroupCost.getTermCode()).get(TA_COST).add(baseTaCost.multiply(taCount)));
            termTotals.get(sectionGroupCost.getTermCode()).put(READER_COUNT, termTotals.get(sectionGroupCost.getTermCode()).get(READER_COUNT).add(readerCount));
            termTotals.get(sectionGroupCost.getTermCode()).put(READER_COST, termTotals.get(sectionGroupCost.getTermCode()).get(READER_COST).add(baseReaderCost.multiply(readerCount)));

            termTotals.get("combined").put(TA_COUNT, termTotals.get("combined").get(TA_COUNT).add(taCount));
            termTotals.get("combined").put(TA_COST, termTotals.get("combined").get(TA_COST).add(baseTaCost.multiply(taCount)));
            termTotals.get("combined").put(READER_COUNT, termTotals.get("combined").get(READER_COUNT).add(readerCount));
            termTotals.get("combined").put(READER_COST, termTotals.get("combined").get(READER_COST).add(baseReaderCost.multiply(readerCount)));

            if (Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) >= 200) {
                termTotals.get(sectionGroupCost.getTermCode()).put(GRAD_OFFERINGS, termTotals.get(sectionGroupCost.getTermCode()).get(GRAD_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get(sectionGroupCost.getTermCode()).put(GRAD_SEATS, termTotals.get(sectionGroupCost.getTermCode()).get(GRAD_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));

                termTotals.get("combined").put(GRAD_OFFERINGS, termTotals.get("combined").get(GRAD_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get("combined").put(GRAD_SEATS, termTotals.get("combined").get(GRAD_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));
            } else if (Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) > 99) {
                termTotals.get(sectionGroupCost.getTermCode()).put(UPPER_DIV_OFFERINGS, termTotals.get(sectionGroupCost.getTermCode()).get(UPPER_DIV_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get(sectionGroupCost.getTermCode()).put(UPPER_DIV_SEATS, termTotals.get(sectionGroupCost.getTermCode()).get(UPPER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));

                termTotals.get("combined").put(UPPER_DIV_OFFERINGS, termTotals.get("combined").get(UPPER_DIV_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get("combined").put(UPPER_DIV_SEATS, termTotals.get("combined").get(UPPER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));
            } else {
                termTotals.get(sectionGroupCost.getTermCode()).put(LOWER_DIV_OFFERINGS, termTotals.get(sectionGroupCost.getTermCode()).get(LOWER_DIV_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get(sectionGroupCost.getTermCode()).put(LOWER_DIV_SEATS, termTotals.get(sectionGroupCost.getTermCode()).get(LOWER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));

                termTotals.get("combined").put(LOWER_DIV_OFFERINGS, termTotals.get("combined").get(LOWER_DIV_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get("combined").put(LOWER_DIV_SEATS, termTotals.get("combined").get(LOWER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));
            }
        }

        return termTotals;
    }

//    public static Map<BudgetCount, BigDecimal> calculateYearTotals(Map<String, Map<BudgetCount, BigDecimal>> termTotals) {
//        Map<BudgetCount, BigDecimal> yearTotals = generateBudgetCountMap();
//
//        for (Map.Entry<String, Map<BudgetCount, BigDecimal>> termTotal : termTotals.entrySet()) {
//            yearTotals.put(TA_COUNT, yearTotals.get(TA_COUNT).add(termTotal.getValue().get(TA_COUNT)));
//            yearTotals.put(TA_COST, yearTotals.get(TA_COST).add(termTotal.getValue().get(TA_COST)));
//            yearTotals.put(READER_COUNT, yearTotals.get(READER_COUNT).add(termTotal.getValue().get(READER_COUNT)));
//            yearTotals.put(READER_COST, yearTotals.get(READER_COST).add(termTotal.getValue().get(READER_COST)));
//            yearTotals.put(LOWER_DIV_OFFERINGS, yearTotals.get(LOWER_DIV_OFFERINGS).add(termTotal.getValue().get(LOWER_DIV_OFFERINGS)));
//            yearTotals.put(UPPER_DIV_OFFERINGS, yearTotals.get(UPPER_DIV_OFFERINGS).add(termTotal.getValue().get(UPPER_DIV_OFFERINGS)));
//            yearTotals.put(GRAD_OFFERINGS, yearTotals.get(GRAD_OFFERINGS).add(termTotal.getValue().get(GRAD_OFFERINGS)));
//            yearTotals.put(LOWER_DIV_SEATS, yearTotals.get(LOWER_DIV_SEATS).add(termTotal.getValue().get(LOWER_DIV_SEATS)));
//            yearTotals.put(UPPER_DIV_SEATS, yearTotals.get(UPPER_DIV_SEATS).add(termTotal.getValue().get(UPPER_DIV_SEATS)));
//            yearTotals.put(GRAD_SEATS, yearTotals.get(GRAD_SEATS).add(termTotal.getValue().get(GRAD_SEATS)));
//        }
//
//        return yearTotals;
//    }

    private static Map<BudgetCount, BigDecimal> generateBudgetCountMap() {
        Map<BudgetCount, BigDecimal> budgetCountMap = Stream.of(
            new SimpleEntry<>(TA_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(TA_COST, BigDecimal.ZERO),
            new SimpleEntry<>(READER_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(READER_COST, BigDecimal.ZERO),
            new SimpleEntry<>(LOWER_DIV_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(UPPER_DIV_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(GRAD_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(LOWER_DIV_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(UPPER_DIV_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(GRAD_SEATS, BigDecimal.ZERO)
            )
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

        return budgetCountMap;
    }
}
