/*
 * Copyright 2021 Smarsh Inc.
 */

package com.paulhoang;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class KafkaAvroConsumer {

  public static void main(String[] args) {

    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", "localhost:9092");
    properties.setProperty("group.id", "myConsumer");
    properties.setProperty("auto.offset.reset", "earliest");

    properties.setProperty("key.deserializer", StringDeserializer.class.getName());
    properties.setProperty("value.deserializer", KafkaAvroDeserializer.class.getName());
    properties.setProperty("schema.registry.url", "http://localhost:8081");
    properties.setProperty("specific.avro.reader", "true");

    KafkaConsumer<String, User> consumer = new KafkaConsumer<String, User>(properties);
    consumer.subscribe(Collections.singleton("user-topic"));

    while(true) {
      final ConsumerRecords<String, User> results = consumer.poll(500L);
      for (ConsumerRecord<String, User> result : results) {
        System.out.println("Got data: ");
        System.out.println("Key is: " + result.key());
        System.out.println("Value is: " + result.value());
      }
    }
  }
}
