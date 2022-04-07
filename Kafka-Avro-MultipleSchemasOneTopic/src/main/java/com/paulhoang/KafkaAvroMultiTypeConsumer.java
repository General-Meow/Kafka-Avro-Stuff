/*
 * Copyright 2021 Smarsh Inc.
 */

package com.paulhoang;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Properties;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * This producer uses multiple specific record types that are some way related to the domain and
 * puts those instances into the same topic WITH avro support
 */
public class KafkaAvroMultiTypeConsumer {

  public static final String COMBINED_TOPIC = "combined-topic";

  public static void main(String[] args) {
    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
    properties.setProperty("group.id", "myConsumer-v2");
    properties.setProperty("auto.offset.reset", "earliest");

    //NOTE I've changed the port here as i changed the docker compose file as there was a clash with another app locally
    properties.setProperty("schema.registry.url", "http://localhost:8091");

    properties.setProperty("key.deserializer", StringDeserializer.class.getName());
    properties.setProperty("value.deserializer", KafkaAvroDeserializer.class.getName());

    properties.setProperty("specific.avro.reader", "true");

    //NOTE: THIS IS THE IMPORTANT PART FOR MULTIPLE SCHEMAS IN ONE TOPIC: "value.subject.name.strategy"
    //Should be in versions 5.0+
    properties.setProperty(VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class.getName());

    final Consumer<String, SpecificRecord> combinedConsumer = new KafkaConsumer<>(properties);
    combinedConsumer.subscribe(Collections.singleton(COMBINED_TOPIC));

    while (true) {
      final ConsumerRecords<String, SpecificRecord> pollResult = combinedConsumer.poll(500L);
      for (ConsumerRecord<String, SpecificRecord> record : pollResult) {
        final SpecificRecord value = record.value();
        if (value instanceof User) {
          User user = (User) value;
          System.out.println(MessageFormat.format("User data received: {0}", user));
        } else if (value instanceof Profile) {
          Profile profile = (Profile) value;
          System.out.println(MessageFormat.format("User data received: {0}", profile));
        } else {
          throw new RuntimeException("Uhoh unknown type of data: " + value.getClass());
        }
      }
    }

  }

}
