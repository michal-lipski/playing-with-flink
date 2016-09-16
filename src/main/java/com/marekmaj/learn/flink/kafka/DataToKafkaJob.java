package com.marekmaj.learn.flink.kafka;


import com.dataartisans.flinktraining.exercises.datastream_java.datatypes.TaxiRide;
import com.dataartisans.flinktraining.exercises.datastream_java.sources.TaxiRideSource;
import com.dataartisans.flinktraining.exercises.datastream_java.utils.GeoUtils;
import com.dataartisans.flinktraining.exercises.datastream_java.utils.TaxiRideSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer09;

public class DataToKafkaJob {

    public static void main(String[] args) throws Exception {
        String path = ParameterTool.fromArgs(args).getRequired("data");

        // set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        DataStream<TaxiRide> rides = env.addSource(new TaxiRideSource(path, 0, 60));

        DataStream<TaxiRide> filtered = rides
                .filter(taxiRide -> GeoUtils.isInNYC(taxiRide.startLon, taxiRide.startLat))
                .filter(taxiRide -> GeoUtils.isInNYC(taxiRide.endLon, taxiRide.endLat));

        filtered.addSink(new FlinkKafkaProducer09<>(KafkaProps.broker, KafkaProps.inputTopic, new TaxiRideSchema()));

        env.execute("Flink to Kafka data producer");
    }
}
