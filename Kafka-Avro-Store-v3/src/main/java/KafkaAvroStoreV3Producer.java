/*
 * Copyright 2021 Smarsh Inc.
 */


import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import locations.events.stores.store;
import locations.events.stores.storeevekeys;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.LocalDate;
import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY;

/**
 * This producer uses multiple specific record types that are some way related to the domain and
 * puts those instances into the same topic WITH avro support
 */
public class KafkaAvroStoreV3Producer {

    public static final String STORES_TOPIC = "stores-v3";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:31090");
        properties.setProperty("acks", "all");
        properties.setProperty("retries", "10");

        properties.setProperty("schema.registry.url", "http://localhost:8081");

        properties.setProperty("key.serializer", KafkaAvroSerializer.class.getName());
        properties.setProperty("value.serializer", KafkaAvroSerializer.class.getName());

        //NOTE: THIS IS THE IMPORTANT PART FOR MULTIPLE SCHEMAS IN ONE TOPIC: "value.subject.name.strategy"
        //Should be in versions 5.0+
        properties.setProperty(VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class.getName());

        Producer<storeevekeys, store> storeProducer = new KafkaProducer<>(properties);
        final store store = createStore();
        final storeevekeys storeevekeys = createStoreevekeys();
        ProducerRecord<storeevekeys, store> producerRecord = new ProducerRecord<>(STORES_TOPIC,storeevekeys ,store);
        storeProducer.send(producerRecord, (recordMetadata, e) -> {
            if (e == null) {
                System.out.println("Store data sent");
                System.out.println(recordMetadata);
            } else {
                e.printStackTrace();
            }
        });

        storeProducer.flush();
        storeProducer.close();
    }

    private static store createStore() {
        store.Builder storeBuilder = store.newBuilder();
        final store store = storeBuilder
                .setPlaceId("abc123")
                .setBrand("Argos")
                .setLegacySainsburysStoreCode("None")
                .setLegacyArgosStoreCode("4000")
                .setAnaNumber("None")
                .setStoreType("None")
                .setName("TestStore")
                .setDisplayName("TestStore")
                .setParentStore("None")
                .setCurrentStatus("open")
                .setFutureStatus("None")
                .setOpenDate(LocalDate.now())
                .setCloseDate(LocalDate.now())
                .setStoreOpeningMessage("None")
                .setCurrencyCode("GDP")
                .setVatRegion("UK")
                .setPhone("None")
                .setManager("None")
                .setStoreLocatorUrl("https://www.argos.co.uk/stores/4000-TestStore")
                .setRegionId("None")
                .setRegion("None")
                .setZoneId("None")
                .setZone("None")
                .setSellingSquareFeet("None")
                .setTotalSquareFeet("None")
                .build();

        return store;
    }

    private static storeevekeys createStoreevekeys() {
        storeevekeys.Builder storeevekeysBuilder = storeevekeys.newBuilder();
        final storeevekeys msgKey = storeevekeysBuilder
                .setPlaceId("abc123")
                .setSubject("store")
                .setBrand("Argos")
                .setExtra("None")
                .build();

        return msgKey;
    }
}


