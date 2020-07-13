package edu.ucdavis.dss.ipa.services;

import static edu.ucdavis.dss.ipa.entities.enums.BudgetSummary.*;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.enums.BudgetSummary;
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
    public static Map<String, Map<BudgetSummary, BigDecimal>> calculateTermTotals(Budget budget, List<SectionGroupCost> sectionGroupCosts, List<String> termCodes) {
        Map<String, Map<BudgetSummary, BigDecimal>> termTotals = new HashMap<>();

        for (String termCode : termCodes) {
            Map<BudgetSummary, BigDecimal> budgetCountMap = generateBudgetCountMap();
            termTotals.put(termCode, budgetCountMap);
        }

        // add another map for yearly total
        Map<BudgetSummary, BigDecimal> budgetCountMap = generateBudgetCountMap();
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
            termTotals.get(sectionGroupCost.getTermCode()).put(UNITS_OFFERED, termTotals.get(sectionGroupCost.getTermCode()).get(UNITS_OFFERED).add(calculateUnits(sectionGroupCost)));

            if (isGrad(sectionGroupCost)) {
                termTotals.get(sectionGroupCost.getTermCode()).put(SCH_GRAD, termTotals.get(sectionGroupCost.getTermCode()).get(SCH_GRAD).add(calculateSCH(sectionGroupCost)));
                termTotals.get("combined").put(SCH_GRAD, termTotals.get("combined").get(SCH_GRAD).add(calculateSCH(sectionGroupCost)));
            } else {
                termTotals.get(sectionGroupCost.getTermCode()).put(SCH_UNDERGRAD, termTotals.get(sectionGroupCost.getTermCode()).get(SCH_UNDERGRAD).add(calculateSCH(sectionGroupCost)));
                termTotals.get("combined").put(SCH_UNDERGRAD, termTotals.get("combined").get(SCH_UNDERGRAD).add(calculateSCH(sectionGroupCost)));
            }

            termTotals.get("combined").put(TA_COUNT, termTotals.get("combined").get(TA_COUNT).add(taCount));
            termTotals.get("combined").put(TA_COST, termTotals.get("combined").get(TA_COST).add(baseTaCost.multiply(taCount)));
            termTotals.get("combined").put(READER_COUNT, termTotals.get("combined").get(READER_COUNT).add(readerCount));
            termTotals.get("combined").put(READER_COST, termTotals.get("combined").get(READER_COST).add(baseReaderCost.multiply(readerCount)));
            termTotals.get("combined").put(UNITS_OFFERED, termTotals.get("combined").get(UNITS_OFFERED).add(calculateUnits(sectionGroupCost)));

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

    private static boolean isGrad(SectionGroupCost sectionGroupCost) {
        return Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) >= 200;
    }

    private static BigDecimal calculateUnits(SectionGroupCost sectionGroupCost) {
        // if sectionGroupCost has unitsHigh, it is a variable unit course, use unitsVariable if exist, otherwise, use zero for the variable unit course
        if ((sectionGroupCost.getUnitsHigh() == null ? 0 : sectionGroupCost.getUnitsHigh()) > 0) {
            return sectionGroupCost.getUnitsVariable() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getUnitsVariable()));
        } else {
            return sectionGroupCost.getUnitsLow() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getUnitsLow()));
        }
    }

    private static BigDecimal calculateSCH(SectionGroupCost sectionGroupCost) {
        return new BigDecimal(String.valueOf(sectionGroupCost.getEnrollment())).multiply(calculateUnits(sectionGroupCost));
    }

//    public static Map<BudgetSummary, BigDecimal> calculateYearTotals(Map<String, Map<BudgetSummary, BigDecimal>> termTotals) {
//        Map<BudgetSummary, BigDecimal> yearTotals = generateBudgetCountMap();
//
//        for (Map.Entry<String, Map<BudgetSummary, BigDecimal>> termTotal : termTotals.entrySet()) {
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

    private static Map<BudgetSummary, BigDecimal> generateBudgetCountMap() {
        Map<BudgetSummary, BigDecimal> budgetCountMap = Stream.of(
            new SimpleEntry<>(TA_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(TA_COST, BigDecimal.ZERO),
            new SimpleEntry<>(READER_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(READER_COST, BigDecimal.ZERO),
            new SimpleEntry<>(LOWER_DIV_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(UPPER_DIV_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(GRAD_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(LOWER_DIV_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(UPPER_DIV_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(GRAD_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(UNITS_OFFERED, BigDecimal.ZERO),
            new SimpleEntry<>(SCH_UNDERGRAD, BigDecimal.ZERO),
            new SimpleEntry<>(SCH_GRAD, BigDecimal.ZERO)
            )
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

        return budgetCountMap;
    }
}
