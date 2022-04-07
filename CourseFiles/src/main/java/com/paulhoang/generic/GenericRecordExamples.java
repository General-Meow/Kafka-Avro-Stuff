/*
 * Copyright 2021 Smarsh Inc.
 */

package com.paulhoang.generic;

import java.io.File;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;

public class GenericRecordExamples {

  public static void main(String[] args) {
    //my schema
    /*
    {
      "type": "record",
      "namespace": "com.paulhoang",
      "name": "Student",
      "doc": "Schema that represents a student",
      "fields": [
        { "name": "firstName", "type": "string", "doc": "Students first name"},
        { "name": "lastName", "type": "string", "doc": "Students last name"},
        { "name": "age", "type": "int", "doc": "Students age"},
        { "name": "campus", "type": "string", "doc": "Students main campus", "default": "London"}
      ]
    }
     */
    Parser parser = new Parser();

    //CREATE THE SCHEMA
    final Schema schema = parser.parse("{\n"
        + "      \"type\": \"record\",\n"
        + "      \"namespace\": \"com.paulhoang\",\n"
        + "      \"name\": \"Student\",\n"
        + "      \"doc\": \"Schema that represents a student\",\n"
        + "      \"fields\": [\n"
        + "        { \"name\": \"firstName\", \"type\": \"string\", \"doc\": \"Students first name\"},\n"
        + "        { \"name\": \"lastName\", \"type\": \"string\", \"doc\": \"Students last name\"},\n"
        + "        { \"name\": \"age\", \"type\": \"int\", \"doc\": \"Students age\"},\n"
        + "        { \"name\": \"campus\", \"type\": \"string\", \"doc\": \"Students main campus\", \"default\": \"London\"}\n"
        + "      ]\n"
        + "    }"); //you can parse a file or a string here

    //CREATE RECORDS
    GenericRecordBuilder studentBuilder1 = new GenericRecordBuilder(schema);
    studentBuilder1.set("firstName", "bob");
    studentBuilder1.set("lastName", "meeesiks");
    studentBuilder1.set("age", 30);
    studentBuilder1.set("campus", "Liverpool");

    final Record studentRecord1 = studentBuilder1.build();

    System.out.println(studentRecord1);

    GenericRecordBuilder studentBuilder2 = new GenericRecordBuilder(schema);
    studentBuilder2.set("firstName", "John");
    studentBuilder2.set("lastName", "Doe");
    studentBuilder2.set("age", 40);

    final Record studentRecord2 = studentBuilder2.build();

    GenericRecordBuilder studentBuilder3 = new GenericRecordBuilder(schema);
    studentBuilder3.set("lastName", "Doe");
    studentBuilder3.set("age", 50);
    //THIS WILL FAIL AS MISSING FIRST NAME WHICH IS NEEDED ON THE SCHEMA
    //final Record studentRecord3 = studentBuilder3.build();


    System.out.println(studentRecord2);

    //WRITE RECORDS TO FILE, this file will now represent an avro record
    DatumWriter<Record> writer = new GenericDatumWriter<>(schema);
    try (DataFileWriter<Record> dataFileWriter = new DataFileWriter<>(writer)) {
      System.out.println("Writing to file");
      dataFileWriter.create(schema, new File("student-generic.avro"));
      dataFileWriter.append(studentRecord1);
    } catch (IOException e) {
      e.printStackTrace();
    }

    //READ THE AVRO RECORD FILE
    final File studentRecord = new File("student-generic.avro");
    DatumReader<Record> reader = new GenericDatumReader<>();
    try(DataFileReader<Record> fileRecordReader = new DataFileReader<>(studentRecord, reader)) {
      for (Record record : fileRecordReader) {
        System.out.println("Record from file:");
        System.out.println(record);

        //you can do things with the record
        System.out.println("getting the first name of the record: " + record.get("firstName"));
        //will return null if property does not exist
        System.out.println("getting the non existing property of the record: " + record.get("blah"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
