package cn.luixtech.dae.flinkjob.accumulator;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.api.common.accumulators.Accumulator;
import org.apache.flink.api.common.accumulators.SimpleAccumulator;

import java.math.BigDecimal;

/**
 * An accumulator that finds the minimum {@code BigDecimal} value.
 *
 * <p>Supports numbers less than Double.MAX_VALUE.
 */
@PublicEvolving
public class BigDecimalMinAccumulator implements SimpleAccumulator<BigDecimal> {

    private static final long       serialVersionUID = 1L;
    private              BigDecimal min              = BigDecimal.valueOf(Double.MAX_VALUE);
    private final        BigDecimal limit            = BigDecimal.valueOf(Double.MAX_VALUE);

    public BigDecimalMinAccumulator() {
    }

    public BigDecimalMinAccumulator(BigDecimal value) {
        this.min = value;
    }

    // ------------------------------------------------------------------------
    //  Accumulator
    // ------------------------------------------------------------------------

    @Override
    public void add(BigDecimal value) {
        if (value.compareTo(limit) > 0) {
            throw new IllegalArgumentException(
                    "BigDecimalMinimum accumulator only supports values less than Double.MAX_VALUE");
        }
        this.min = min.min(value);
    }

    @Override
    public BigDecimal getLocalValue() {
        return this.min;
    }

    @Override
    public void merge(Accumulator<BigDecimal, BigDecimal> other) {
        this.min = min.min(other.getLocalValue());
    }

    @Override
    public void resetLocal() {
        this.min = BigDecimal.valueOf(Double.MAX_VALUE);
    }

    @Override
    public BigDecimalMinAccumulator clone() {
        BigDecimalMinAccumulator clone = new BigDecimalMinAccumulator();
        clone.min = this.min;
        return clone;
    }

    // ------------------------------------------------------------------------
    //  Utilities
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "BigDecimal " + this.min;
    }
}
