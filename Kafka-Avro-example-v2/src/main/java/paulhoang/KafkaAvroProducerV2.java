/*
 * Copyright 2021 Smarsh Inc.
 */

package paulhoang;

import com.paulhoang.User;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.util.Properties;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaAvroProducerV2 {

  //REMOVED THE RECIEVE EMAILS FROM USER AND ADDED expired and age fields as optional
  public static void main(String[] args) {
    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
    properties.setProperty("acks", "all");
    properties.setProperty("retries", "10");

    properties.setProperty("schema.registry.url", "http://localhost:8081");

    properties.setProperty("key.serializer", StringSerializer.class.getName());
    properties.setProperty("value.serializer", KafkaAvroSerializer.class.getName());

    Producer<String, User> producer = new KafkaProducer<>(properties);


    User.Builder userBuilder = User.newBuilder();
    final User user = userBuilder
        .setFirstName("Paul")
        .setLastName("Hoang")
        .setUsername("paul.hoang")
        .setEmail("myemail")
        .setCreatedDate(1000L)
        .setLastLogInDate(1000L)
        .setExpired(true).build();

    ProducerRecord<String, User> producerRecord = new ProducerRecord<>("user-topic", user);
    producer.send(producerRecord, new Callback() {
      @Override
      public void onCompletion(RecordMetadata recordMetadata, Exception e) {
        if(e == null) {
          System.out.println("Data sent");
          System.out.println(recordMetadata);
        } else {
          e.printStackTrace();
        }
      }
    });

    producer.flush();
    producer.close();
  }

}
