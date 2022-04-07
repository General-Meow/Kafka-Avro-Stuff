### Kafka Avro

#### Types

- There are a number of simple types that avro supports, they are effectively the same as java:
    - null
    - boolean
    - int (32 signed bit)
    - long (64 signed bit)
    - float
    - double
    - byte
    - String
- An example of a schema file using a type:

```json
{
  'type': 'string'
}
```

- Avro also supports `complex types`, these are:
    - Enums //field
      example: `{ 'type': 'enum', 'name': 'customer_status', 'symbols': ['GOLD', 'SILVER', 'BRONZE']}`
    - Arrays // `{ 'type': 'array', 'name': 'customerEmails', 'items': 'string'}`
    - Maps // `{ 'type': 'map', 'name': 'secretQuestions', 'values': 'string'}`
    - Unions // allows multiple types in a field e.g. strings, ints, booleans, use an array of types
      in the 'type field'
    - References to other schema's as a type

- Enums, once set, you cant change the values in the symbols array if you want to maintain
  compatability
- Arrays, you need to provide the `items` property to define the type of elements in the array
- Map, key value pair where the key is a string, you need to provide the `values` property to define
  the type of values in the map
- Unions, if a default is provided, it must be the same type as the first element of types in that
  field e.g. `{'type': ['string', 'int'], 'default': 'five '}`

- In addition to the types above, avro supports `logical types`, these are:
    - `decimals` (bytes)
    - `date` (int) days since epoch Jan 1st 1970
    - `time-millis` (long) millis since the start of the day
    - `timestamp-millis` (long) millis since epoch
- To use one of these logical types, just add the property `logicalType` in the `Field`
  e.g. `{"name":"birthday", "type": "long", "logicalType": "timestamp-millis"} `
- The decimal type is very important. You should always use it if you want to store or calculate
  exact numbers, e.g. calculating cart money. As somethings you get floating point arthimatic errors
  with floats/doubles e.g. 1.999999999991
    - Also to note, as `decimals` are stored as bytes, you cannot print the value out from the json,
      as it would be giberish
    - It's still new, so not all libraries support it for now.
    - You can use a String for now and convert that to your needed value if required

### Schemas

- In Avro, the schemas are called `Avro Record Schema`
- These are defined in json
- Some of the common fields in the record schema are:

```json
{
  'name': 'xxx',
  // name of the schema
  'namespace': '',
  // like the java package
  'doc': 'xxx',
  // any documentation
  'aliases': 'xxx',
  // any other name for this schema
  'fields': [
    // an array of json object describing fields
    {
      'name',
      // name of the field
      'doc',
      // documentation for the field
      'type',
      // the type of the field
      'default'
      // optional default value for the field
    }
  ]
}
```

- Heres an example of a record schema

```json
{
  'name': 'Customer',
  'namespace': 'com.paulhoang.customer',
  'doc': 'The customer schema',
  'fields': [
    {
      'name': 'first_name',
      'type': 'string',
      'doc': 'the first name of the customer'
    },
    {
      'name': 'last_name',
      'type': 'string',
      'doc': 'the last name of the customer'
    },
    {
      'name': 'age',
      'type': 'int',
      'doc': 'the age of the customer'
    },
    {
      'name': 'height',
      'type': 'float',
      'doc': 'the height of the customer in cm'
    },
    {
      'name': 'weight',
      'type': 'float',
      'doc': 'the weight of the customer in kg'
    },
    {
      'name': 'emails_on',
      'type': 'boolean',
      'doc': 'whether or not to email them ',
      default: true
    }
  ]
}
```

#### Creating Avro Objects via Java

- `GenericRecord`, `SpecificRecord` `Avro Reflection` are three ways to create valid avro records/objects
- The `GenericRecord` interface is the simplest way to create an Avro object
    - It is done by referencing an avro schema file or
    - By Strings containing the schema
    - Not the best way to use avro has Runtime failures can occur
    - Good for when you want to write generic code based on property names rather than classes and their fields, think MongoDB `Document` types and their properties
- The `SpecificRecord` is the preferred method to create avro objects
  - Will require build tool plugins to read avro schemas and generate avro compliant classes (look at the `pom.xml` plugins)
  - In this case we're using `avro-maven-plugin` plugin to generate the classes defined in the schema
  - Also the `build-helper-maven-plugin` plugin is used to mark directories as sources so that intellij can find the generated classes
  - Using `SpecificRecord` will give you typed classes so that you can use setter methods to set values and in doing so, you get compile time errors instead of runtime ones
- When using the `DataFileWriter` class to persist the avro records, it stores it in bytes
  - It will be similar with Kafka, in that we write the avro records in byte form direct to a kafka topic + a schema registry
- `Avro Reflection` allows you to create the Java class & object first, then generates the schema from the object

```java

class GenericExample {

  public static void main(String[] args) {

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
    try (DataFileReader<Record> fileRecordReader = new DataFileReader<>(studentRecord, reader)) {
      for (Record record : fileRecordReader) {
        System.out.println("Record from file:");
        System.out.println(record);

        //you can do things with the record
        System.out.println("getting the first name of the record: " + record.get("firstName"));
        //will return null if property does not exist
        System.out.println(
            "getting the non existing property of the record: " + record.get("blah"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
```

```java
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
```

```java
import com.paulhoang.Student;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.reflect.Nullable;
import org.apache.avro.reflect.ReflectDatumWriter;

class ReflectionExample {

  class Student {

    private String firstName;
    @Nullable
    private String nickName;
    //...
  }

  public static void main(String[] args) {
    Schema schema = ReflectData.get().getSchema(Student.class);
    File file = new File("student-reflection.avro");
    DatumWriter<Student> writer = new ReflectDatumWriter<>(com.paulhoang.Student.class);
    DataFileWriter<com.paulhoang.Student> dataWriter = new DataFileWriter<>(writer);
    dataWriter.append(new com.paulhoang.Student(...));
  }
}

```

#### Evolving schemas

- Avro helps with changes to requirements and data by allowing schema evolution
- Schema evolution is very important and it must be done in a non-breaking way (breaking for existing clients)
- In general, there are 4 different kinds of schema evolution
  - `Backwards` = where a newer schema can be used to read older data. e.g. v2 schema can read v1 data. This is when you've updated a consumer first to use the new schema while still consuming old data
  - `Forwards` = where an old schema can be used to read new data. e.g. v1 schema can read v2 data. This is typically important for clients using old schemas that don't change often. Where you only change your producing service and not the consuming service
  - `Full` = where the schema can be both backwards and forwards compatible, this is what you want all the time
  - `Breaking` = where it doesn't support any of the above

- You can achieve backwards by not deleting any old fields and any new ones could have default values
- You can achieve forwards by adding new data, any fields not in the old schema will be ignored
- Breaking can be done by adding or removing elements from an ENUM, changing types of a field, renaming fields
- Some tips on writing schemas
  - Make PK's required with no defaults
  - Provide default values for fields that could potentially be removed in the future (removing fields that have default values is ok)
  - Be very careful with enum types as they can't change (add or remove)
  - Never rename fields
  - Always give default values
  - Never delete required fields 
- Steps to evolving schemas
  - Forwards compatible change (more common): update your producer to V2 (consumers will be able to read with V1 schema), then update the consumers to V2
  - Backwards compatible change (less common): update your consumers to V2 (consumer will be able to read V1 data) then update the producer to V2

#### How does it work?

- When sending a message with Avro...
1. Your producer will first send the schema to the `Schema Registry`
2. Then your producer will send the data to the Kafka broker / topic
3. The consumer of the topic will then get the `schema` from the `Schema Registry`
4. Then the consumer will read the data off the topic, validating the data with the schema

##### How does it work? More Details

- The `KafkaAvroSerializer` does all of the following work and the `KafkaAvroDeserializer` does the reading bit
1. When the data is sent to kafka, the Java app will first send the schema to the registry
   1. If the registry doesn't already have it, it stores it and responds with a schema ID
   2. If it already exists, it just returns the schema ID
2. The Java program then prepares the message to be sent to Kafka
   1. It removes the schema record from the message
   2. Adds a `magic byte` to the beginning of the message. This byte a value that represents the schema version number
   3. Adds the schema id after the byte
   4. Then adds the `Avro content` (the message body) then sends it

- Doing all the above allows the consumer to work out the version and what schema to use to process the message
- Doing this also shrinks down the message as your not copying the entire `Avro schema` onto all messages but only a reference using an ID

#### Schema Registry

- Opensource project
- The features of the Confluent Schema registry
  - A central place to store all the avro schemas
  - Enforce backwards, forwards, full compatability
  - Decrease the size of the message payloads by sending the schema to the schema registry and the data to the broker, same with the consumer
  - Allows you to get, add, remove, update schemas through a REST API
  - 

#### Work and pipelines

- If possible use `SpecificRecords` this will generate the java classes for you
- In CICD pipelines, you should try and validate the `avro schema` file `.avsc` with the schema registry
- If its valid, build the project to generate the Java `SpecificRecord` classes/jars
- Upload the jars to a registry so that other consumers could use it


#### Confluent REST proxy

- Not all languages/framewors/libraries have good support for Avro
- So Confluent made the REST proxy to allow users to send avro compliant data to kafka via the proxy
  - It works by sending the kafka data with the schema to the proxy by HTTP POST and the proxy will then send the data onto kafka
  - The consumers do the same thing but use HTTP GET with the REST proxy which will also retrieve the data from kafka
- The proxy is actually integrated with the schema registry so its not another component to install and maintain
- Using the proxy means using HTTP which will mean there are performance implications. Some 3-4X slower
- When making a REST request to the proxy, you will need to provide a `Content-Type` and `Accept` header too.
- Both headers should have the format:
  - `application/vnd.kafka.<EMBEDDED_FORMAT>.<API_VERSION>+<SERIALIZATION_FORMAT>`
  - Where `EMBEDDED_FORMAT` is `json`, `binary` or `avro`
  - The API version should always be `V2`. `V1` was pre kafka 0.8 so dont use that
  - `SERIALIZATION_FORMAT` should always be `json`
- There are a number of operations you can do with the REST proxy but you can't do everything yet
  - GET topics @ `/topics`
  - GET specific topic @ `/topics/<TOPIC_NAME>`
  - With the `landoop/fast-data-dev` docker image running, you can make requests to `http://localhost:8082`
- When sending data using the REST proxy, you have 3 choices when it comes to data format
  - binary (in base64)
  - json
  - avro (json encoded)
- Batching is also supported in produce request


#### Misc

- `.avsc` are the avro schema files which is basically json files
- You can read avro record files by using the avro tools
  - Download it from maven central (its a jar) and put it as an alias e.g. `alias avrotools=java -jar /Users/paul/Dev/Software/avro-tools-1.10.2/avro-tools-1.10.2.jar`
  - run it like: `avrotools from tojson --pretty student-generic.avro` this will read the file and output to json
  - You can also get the schema too with the `getschema` option
- Download the `Confluent Platform` binary for its avro tools such as
  - `kafka-avro-console-consumer`
  - `kafka-avro-console-producer`