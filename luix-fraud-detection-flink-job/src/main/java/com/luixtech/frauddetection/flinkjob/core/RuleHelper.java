package com.luixtech.frauddetection.flinkjob.core;

import com.luixtech.frauddetection.common.rule.Rule;
import com.luixtech.frauddetection.common.rule.RuleCommand;
import com.luixtech.frauddetection.common.rule.RuleType;
import com.luixtech.frauddetection.common.transaction.Transaction;
import com.luixtech.frauddetection.flinkjob.core.accumulator.*;
import com.luixtech.frauddetection.flinkjob.utils.FieldsExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.accumulators.SimpleAccumulator;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapState;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/* Collection of helper methods for Rules. */
@Slf4j
public class RuleHelper {
    public static void handleRule(BroadcastState<String, RuleCommand> broadcastState, RuleCommand ruleCommand) throws Exception {
        switch (ruleCommand.getCommand()) {
            case ADD:
                // merge rule
                broadcastState.put(ruleCommand.getRule().getId(), ruleCommand);
                break;
            case DELETE:
                broadcastState.remove(ruleCommand.getRule().getId());
                break;
            case DELETE_ALL:
                Iterator<Map.Entry<String, RuleCommand>> entriesIterator = broadcastState.iterator();
                while (entriesIterator.hasNext()) {
                    Map.Entry<String, RuleCommand> ruleEntry = entriesIterator.next();
                    broadcastState.remove(ruleEntry.getKey());
                    log.info("Removed {}", ruleEntry.getValue());
                }
                break;
        }
    }

    /* Picks and returns a new accumulator, based on the Rule's aggregator function type. */
    public static SimpleAccumulator<BigDecimal> getAggregator(Rule rule) {
        switch (rule.getAggregator()) {
            case COUNT:
                return new BigDecimalCounter();
            case SUM:
                return new BigDecimalAdder();
            case AVG:
                return new BigDecimalAverageAccumulator();
            case MAX:
                return new BigDecimalMaximum();
            case MIN:
                return new BigDecimalMinimum();
            default:
                throw new RuntimeException(
                        "Unsupported aggregation function type: " + rule.getAggregator());
        }
    }

    /**
     * Evaluates this rule by comparing provided value with rules' limit based on limit operator type.
     *
     * @param comparisonValue value to be compared with the limit
     */
    public static boolean evaluate(Rule rule, Transaction inputRecord,
                                   MapState<Long, Set<Transaction>> windowState) throws Exception {
        return RuleType.MATCHING == rule.determineType()
                ? evaluateMatchingRule(rule, inputRecord)
                : evaluateAggregatingRule(rule, inputRecord, windowState);
    }

    private static boolean evaluateMatchingRule(Rule rule, Transaction inputRecord) throws IllegalAccessException, NoSuchFieldException {
        if (StringUtils.isNotEmpty(rule.getExpectedValue())) {
            return rule.getExpectedValue().equals(FieldsExtractor.getFieldValAsString(inputRecord, rule.getFieldName()));
        }

        return FieldsExtractor.isFieldValSame(inputRecord, rule.getFieldName(), rule.getExpectedFieldName());
    }

    private static boolean evaluateAggregatingRule(Rule rule, Transaction inputRecord,
                                                   MapState<Long, Set<Transaction>> windowState) throws Exception {
        Long windowStartTime = inputRecord.getCreatedTime() - TimeUnit.MINUTES.toMillis(rule.getWindowMinutes());

        // Calculate the aggregate value
        SimpleAccumulator<BigDecimal> aggregator = RuleHelper.getAggregator(rule);
        for (Long stateCreatedTime : windowState.keys()) {
            if (isStateValueInWindow(stateCreatedTime, windowStartTime, inputRecord.getCreatedTime())) {
                Set<Transaction> transactionsInWindow = windowState.get(stateCreatedTime);
                for (Transaction t : transactionsInWindow) {
                    BigDecimal aggregatedValue = FieldsExtractor.getBigDecimalByName(t, rule.getAggregateFieldName());
                    aggregator.add(aggregatedValue);
                }
            }
        }
        BigDecimal comparisonValue = aggregator.getLocalValue();
        rule.setActualAggregatedValue(comparisonValue);

        switch (rule.getOperator()) {
            case EQUAL:
                return comparisonValue.compareTo(rule.getLimit()) == 0;
            case NOT_EQUAL:
                return comparisonValue.compareTo(rule.getLimit()) != 0;
            case GREATER:
                return comparisonValue.compareTo(rule.getLimit()) > 0;
            case LESS:
                return comparisonValue.compareTo(rule.getLimit()) < 0;
            case GREATER_EQUAL:
                return comparisonValue.compareTo(rule.getLimit()) >= 0;
            case LESS_EQUAL:
                return comparisonValue.compareTo(rule.getLimit()) <= 0;
            default:
                throw new RuntimeException("Unknown operator: " + rule.getOperator());
        }
    }

    private static boolean isStateValueInWindow(Long stateCreatedTime, Long windowStartTime, long currentEventTime) {
        return stateCreatedTime >= windowStartTime && stateCreatedTime <= currentEventTime;
    }
}
