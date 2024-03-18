package com.luixtech.frauddetection.flinkjob.input.sourcecreator.impl;

import com.luixtech.frauddetection.common.input.InputRecord;
import com.luixtech.frauddetection.flinkjob.generator.JsonGeneratorWrapper;
import com.luixtech.frauddetection.flinkjob.generator.TransactionsGenerator;
import com.luixtech.frauddetection.flinkjob.core.Arguments;
import com.luixtech.frauddetection.flinkjob.input.sourcecreator.SourceCreator;
import com.luixtech.utilities.serviceloader.annotation.SpiName;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

@SpiName("input-record-" + Arguments.CHANNEL_SOCKET)
public class InputRecordSourceSocketCreator implements SourceCreator {
    @Override
    public DataStreamSource<String> create(StreamExecutionEnvironment env, Arguments arguments) {
        int inputRecordsPerSecond = arguments.recordsPerSecond;
        JsonGeneratorWrapper<InputRecord> generatorSource = new JsonGeneratorWrapper<>(new TransactionsGenerator(inputRecordsPerSecond));
        return env.addSource(generatorSource);
    }
}