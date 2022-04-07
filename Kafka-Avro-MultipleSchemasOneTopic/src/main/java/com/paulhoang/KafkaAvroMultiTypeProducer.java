/*
 * Copyright 2021 Smarsh Inc.
 */

package com.paulhoang;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * This producer uses multiple specific record types that are some way related to the domain and
 * puts those instances into the same topic WITH avro support
 */
public class KafkaAvroMultiTypeProducer {

  public static final String COMBINED_TOPIC = "combined-topic";

  public static void main(String[] args) {
    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
    properties.setProperty("acks", "all");
    properties.setProperty("retries", "10");

    properties.setProperty("schema.registry.url", "http://localhost:8081");

    properties.setProperty("key.serializer", StringSerializer.class.getName());
    properties.setProperty("value.serializer", KafkaAvroSerializer.class.getName());

    //NOTE: THIS IS THE IMPORTANT PART FOR MULTIPLE SCHEMAS IN ONE TOPIC: "value.subject.name.strategy"
    //Should be in versions 5.0+
    properties.setProperty(VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class.getName());

    Producer<String, User> userProducer = new KafkaProducer<>(properties);
    final User user = createUser();
    ProducerRecord<String, User> producerRecord = new ProducerRecord<>(COMBINED_TOPIC, user);
    userProducer.send(producerRecord, (recordMetadata, e) -> {
      if (e == null) {
        System.out.println("User data sent");
        System.out.println(recordMetadata);
      } else {
        e.printStackTrace();
      }
    });

    userProducer.flush();
    userProducer.close();

    Producer<String, Profile> profileProducer = new KafkaProducer<>(properties);
    final Profile profile = createProfile();
    ProducerRecord<String, Profile> profileProducerRecord = new ProducerRecord<>(COMBINED_TOPIC,
        profile);
    profileProducer.send(profileProducerRecord, (recordMetadata, e) -> {
      if (e == null) {
        System.out.println("Profile data sent");
        System.out.println(recordMetadata);
      } else {
        e.printStackTrace();
      }
    });
    profileProducer.flush();
    profileProducer.close();
  }

  private static Profile createProfile() {
    final Profile profile = Profile.newBuilder()
        .setImageUrl("https://imgurl.com/blah")
        .setDepartment("engineering")
        .setJobTitle("a developer")
        .build();
    return profile;
  }

  private static User createUser() {
    User.Builder userBuilder = User.newBuilder();
    final User user = userBuilder
        .setFirstName("Paul")
        .setLastName("Hoang")
        .setUsername("paul.hoang")
        .setEmail("myemail")
        .setCreatedDate(1000L)
        .setLastLogInDate(10L)
        .build();
    return user;
  }

}
