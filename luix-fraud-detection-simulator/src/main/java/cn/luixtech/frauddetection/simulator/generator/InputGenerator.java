package cn.luixtech.frauddetection.simulator.generator;

import cn.luixtech.dae.common.input.Input;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.SplittableRandom;
import java.util.function.Consumer;

@Slf4j
public class InputGenerator extends AbstractInputGenerator {

    private       long       lastPayeeIdBeneficiaryIdTriggered = System.currentTimeMillis();
    private       long       lastBeneficiaryIdTriggered        = System.currentTimeMillis();
    private final BigDecimal beneficiaryLimit                  = new BigDecimal(10000000);
    private final BigDecimal payeeBeneficiaryLimit             = new BigDecimal(20000000);

    public InputGenerator(Consumer<Input> inputProducer, int maxRecordsPerSecond) {
        super(inputProducer, maxRecordsPerSecond);
    }

    @Override
    protected Input randomOne(SplittableRandom rnd, Long eventTime) {
        Input input = super.randomOne(rnd, eventTime);
        long now = System.currentTimeMillis();
        if (now - lastBeneficiaryIdTriggered > 8000 + rnd.nextInt(5000)) {
            input.getRecord().put("paymentAmount", beneficiaryLimit.add(new BigDecimal(rnd.nextInt(1000000))));
            this.lastBeneficiaryIdTriggered = System.currentTimeMillis();
        }
        if (now - lastPayeeIdBeneficiaryIdTriggered > 12000 + rnd.nextInt(10000)) {
            input.getRecord().put("paymentAmount", payeeBeneficiaryLimit.add(new BigDecimal(rnd.nextInt(1000000))));
            this.lastPayeeIdBeneficiaryIdTriggered = System.currentTimeMillis();
        }
        return input;
    }
}
