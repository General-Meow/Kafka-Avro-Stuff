/*
 * Copyright 2021 Smarsh Inc.
 */

package com.paulhoang.generic;

import com.paulhoang.Student;
import java.io.File;
import java.io.IOException;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

public class SpecificRecordExample {

  public static void main(String[] args) {
    Student.Builder studentBuilder = Student.newBuilder();
    final Student student = studentBuilder
        .setFirstName("Paul")
        .setLastName("Hoang")
        .setAge(36)
        .setCampus("Westminster")
        .build();

    //WRITE SPECIFIC RECORD TO FILE
    DatumWriter<Student> writer = new SpecificDatumWriter<>(
        Student.class);//we don't have a schema but the generated class
    try (DataFileWriter<Student> fileWriter = new DataFileWriter<>(writer)) {
      fileWriter.create(student.getSchema(), new File("student-specific.avro"));
      fileWriter.append(student);
    } catch (IOException e) {
      e.printStackTrace();
    }

    //READ RECORD FROM AVRO FILE
    DatumReader<Student> reader = new SpecificDatumReader<>(Student.class);
    try (DataFileReader<Student> fileReader = new DataFileReader<>(
        new File("student-specific.avro"), reader)) {
      for (Student student1 : fileReader) {
        System.out.println(student1);

        //we can even get properties of a student in a type safe way
        System.out.println(student1.getFirstName());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
