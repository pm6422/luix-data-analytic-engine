/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luixtech.frauddetection.simulator.datasource;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.SplittableRandom;
import java.util.function.Consumer;

@Slf4j
public class TransactionsGenerator extends AbstractTransactionsGenerator {

    private       long       lastPayeeIdBeneficiaryIdTriggered = System.currentTimeMillis();
    private       long       lastBeneficiaryIdTriggered        = System.currentTimeMillis();
    private final BigDecimal beneficiaryLimit                  = new BigDecimal(10000000);
    private final BigDecimal payeeBeneficiaryLimit             = new BigDecimal(20000000);

    public TransactionsGenerator(Consumer<Transaction> consumer, int maxRecordsPerSecond) {
        super(consumer, maxRecordsPerSecond);
    }

    @Override
    protected Transaction randomEvent(SplittableRandom rnd) {
        Transaction transaction = super.randomEvent(rnd);
        long now = System.currentTimeMillis();
        if (now - lastBeneficiaryIdTriggered > 8000 + rnd.nextInt(5000)) {
            transaction.setPaymentAmount(beneficiaryLimit.add(new BigDecimal(rnd.nextInt(1000000))));
            this.lastBeneficiaryIdTriggered = System.currentTimeMillis();
        }
        if (now - lastPayeeIdBeneficiaryIdTriggered > 12000 + rnd.nextInt(10000)) {
            transaction.setPaymentAmount(payeeBeneficiaryLimit.add(new BigDecimal(rnd.nextInt(1000000))));
            this.lastPayeeIdBeneficiaryIdTriggered = System.currentTimeMillis();
        }
        return transaction;
    }
}
