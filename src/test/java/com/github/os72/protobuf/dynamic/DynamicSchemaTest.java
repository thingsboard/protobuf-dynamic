/*
 * Copyright 2015 protobuf-dynamic developers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.os72.protobuf.dynamic;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.DynamicMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DynamicSchemaTest {
    /**
     * testBasic - basic usage
     */
    @Test
    public void testBasic() throws Exception {
        log.info("--- testBasic ---");

        // Create dynamic schema
        DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
        schemaBuilder.setName("PersonSchemaDynamic.proto");
        schemaBuilder.setSyntax("proto3");

        MessageDefinition msgDef = MessageDefinition.newBuilder("Person") // message Person
                .addField("optional", "int32", "id", 1)        // optional int32 id = 1
                .addField("optional", "string", "name", 2)        // optional string name = 2
                .addField(null, "string", "email", 3)    // string email = 3
                .build();

        schemaBuilder.addMessageDefinition(msgDef);
        DynamicSchema schema = schemaBuilder.build();
        log.info("testBasic schema: {}", schema);

        basicValidation(schema);
    }

    /**
     * testParseFromProtoString - basic usage
     */
    @Test
    public void testBasicWithSchemaParseFromProtoString() throws Exception {
        log.info("--- testBasicWithSchemaParseFromProtoString ---");

        // .proto definition as a string
        String proto = """
        syntax = "proto3";
        package test;

        message Person {
            optional int32 id = 1;
            optional string name = 2;
            string email = 3;
        }
        """;

        DynamicSchema schema = DynamicSchema.parseFromProtoString(proto, "PersonSchemaDynamic.proto");
        log.info("testBasicWithSchemaParseFromProtoString schema: {}", schema);

       basicValidation(schema);
    }

    private static void basicValidation(DynamicSchema schema) {
        // Create dynamic message from schema
        DynamicMessage.Builder msgBuilder = schema.newMessageBuilder("Person");
        Descriptor msgDesc = msgBuilder.getDescriptorForType();
        DynamicMessage msgWithoutIdValue = msgBuilder
                .setField(msgDesc.findFieldByName("name"), "Alan Turing")
                .setField(msgDesc.findFieldByName("email"), "at@sis.gov.uk")
                .build();
        log.info("testBasic msgWithoutIdValue: {}", msgWithoutIdValue);

        DynamicMessage msgWithIdValue = msgBuilder
                .setField(msgDesc.findFieldByName("id"), 1)
                .setField(msgDesc.findFieldByName("name"), "Alan Turing")
                .setField(msgDesc.findFieldByName("email"), "at@sis.gov.uk")
                .build();
        log.info("testBasic msgWithIdValue: {}", msgWithIdValue);

        DynamicMessage msgWithDefaultIdAndNameValue = msgBuilder
                .setField(msgDesc.findFieldByName("id"), 0)
                .setField(msgDesc.findFieldByName("name"), "")
                .setField(msgDesc.findFieldByName("email"), "at@sis.gov.uk")
                .build();
        log.info("testBasic msgWithDefaultIdAndNameValue: {}", msgWithDefaultIdAndNameValue);

        // Create data object traditional way using generated code
        PersonSchema.Person person = PersonSchema.Person.newBuilder()
                .setName("Alan Turing")
                .setEmail("at@sis.gov.uk")
                .build();

        // Create data object traditional way using generated code
        PersonSchema.Person person2 = PersonSchema.Person.newBuilder()
                .setId(1)
                .setName("Alan Turing")
                .setEmail("at@sis.gov.uk")
                .build();

        // Create data object traditional way using generated code
        PersonSchema.Person person3 = PersonSchema.Person.newBuilder()
                .setId(0)
                .setName("")
                .setEmail("at@sis.gov.uk")
                .build();

        // Should be equivalent
        Assert.assertEquals(person.toString(), msgWithoutIdValue.toString());

        // Should be equivalent
        Assert.assertEquals(person2.toString(), msgWithIdValue.toString());

        // Should be equivalent
        Assert.assertEquals(person3.toString(), msgWithDefaultIdAndNameValue.toString());
    }

    /**
     * testOneof - oneof usage
     */
    @Test
    public void testOneof() throws Exception {
        log.info("--- testOneof ---");

        // Create dynamic schema
        DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
        schemaBuilder.setName("PersonSchemaDynamic.proto");
        schemaBuilder.setSyntax("proto3");

        // message Person
        MessageDefinition msgDef = MessageDefinition.newBuilder("Person")
                .addOneof("address")                                    // oneof address
                .addField("string", "home_addr", 4)        // string home_addr = 4
                .addField("string", "work_addr", 5)        // string work_addr = 5
                .msgDefBuilder()
                .addField("optional", "int32", "id", 1)        // optional int32 id = 1
                .addField("optional", "string", "name", 2)        // optional string name = 2
                .addField(null, "string", "email", 3)    // string email = 3
                .build();
        schemaBuilder.addMessageDefinition(msgDef);

        DynamicSchema schema = schemaBuilder.build();
        log.info("testOneof schema: {}", schema);

        oneOfValidation(schema);
    }

    /**
     * testOneofWithSchemaParseFromProtoString - oneof usage
     */
    @Test
    public void testOneofWithSchemaParseFromProtoString() throws Exception {
        log.info("--- testOneofWithSchemaParseFromProtoString ---");

        // .proto definition with oneof block
        String proto = """
                syntax = "proto3";
                package test;
                
                message Person {
                    oneof address {
                        string home_addr = 4;
                        string work_addr = 5;
                    }
                
                    int32 id = 1;
                    string name = 2;
                    string email = 3;
                }
                """;

        // Build schema from string
        DynamicSchema schema = DynamicSchema.parseFromProtoString(proto, "person.proto");
        log.info("testOneofWithSchemaParseFromProtoString schema: {}", schema);

    }

    private static void oneOfValidation(DynamicSchema schema) {
        // Create dynamic message from schema
        DynamicMessage.Builder msgBuilder = schema.newMessageBuilder("Person");
        Descriptor msgDesc = msgBuilder.getDescriptorForType();
        DynamicMessage msg = msgBuilder
                .setField(msgDesc.findFieldByName("id"), 0)
                .setField(msgDesc.findFieldByName("name"), "")
                .setField(msgDesc.findFieldByName("work_addr"), "85 Albert Embankment")
                .build();
        log.info("testOneof msg: {}", msg);

        // Create data object traditional way using generated code
        PersonSchema.Person person = PersonSchema.Person.newBuilder()
                .setId(0)
                .setName("")
                .setWorkAddr("85 Albert Embankment")
                .build();

        // Should be equivalent
        Assert.assertEquals(person.toString(), msg.toString());
    }

    /**
     * testAdvanced - nested messages, enums, default values, repeated fields
     */
    @Test
    public void testAdvanced() throws Exception {
        log.info("--- testAdvanced ---");

        // Create dynamic schema
        DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
        schemaBuilder.setName("PersonSchemaDynamic.proto");
        schemaBuilder.setSyntax("proto3");

        EnumDefinition enumDefPhoneType = EnumDefinition.newBuilder("PhoneType") // enum PhoneType
                .addValue("MOBILE", 0)    // MOBILE = 0
                .addValue("HOME", 1)    // HOME = 1
                .addValue("WORK", 2)    // WORK = 2
                .build();

        MessageDefinition msgDefPhoneNumber = MessageDefinition.newBuilder("PhoneNumber") // message PhoneNumber
                .addField(null, "string", "number", 1)                        // string number = 1
                .addField(null, "PhoneType", "type", 2)    // PhoneType type = 2
                .build();

        MessageDefinition msgDefPerson = MessageDefinition.newBuilder("Person") // message Person
                .addEnumDefinition(enumDefPhoneType)                                    // enum PhoneType (nested)
                .addMessageDefinition(msgDefPhoneNumber)                                // message PhoneNumber (nested)
                .addField("optional", "int32", "id", 1)                // int32 id = 1
                .addField("optional", "string", "name", 2)                // string name = 2
                .addField(null, "string", "email", 3)            // string email = 3
                .addField("repeated", "PhoneNumber", "phone", 4)    // repeated PhoneNumber phone = 4
                .build();

        schemaBuilder.addMessageDefinition(msgDefPerson);
        DynamicSchema schema = schemaBuilder.build();
        log.info("testAdvanced schema: {}", schema);

        testAdvanced(schema);
    }

    /**
     * testAdvancedWithSchemaParseFromProtoString - nested messages, enums, default values, repeated fields
     */
    @Test
    public void testAdvancedWithSchemaParseFromProtoString() throws Exception {
        log.info("--- testAdvancedWithSchemaParseFromProtoString ---");

        String proto = """
        syntax = "proto3";
        package test;

        message Person {
          enum PhoneType {
            MOBILE = 0;
            HOME = 1;
            WORK = 2;
          }

          message PhoneNumber {
            string number = 1;
            PhoneType type = 2;
          }

          optional int32 id = 1;
          optional string name = 2;
          string email = 3;
          repeated PhoneNumber phone = 4;
        }
        """;

        DynamicSchema schema = DynamicSchema.parseFromProtoString(proto, "PersonSchemaDynamic.proto");
        log.info("testAdvancedWithSchemaParseFromProtoString schema: {}", schema);

        testAdvanced(schema);
    }

    private static void testAdvanced(DynamicSchema schema) {
        // Create dynamic message from schema
        Descriptor phoneDesc = schema.getMessageDescriptor("Person.PhoneNumber");
        DynamicMessage phoneMsg1 = schema.newMessageBuilder("Person.PhoneNumber")
                .setField(phoneDesc.findFieldByName("number"), "+44-111")
                .build();
        DynamicMessage phoneMsg2 = schema.newMessageBuilder("Person.PhoneNumber")
                .setField(phoneDesc.findFieldByName("number"), "+44-222")
                .setField(phoneDesc.findFieldByName("type"), schema.getEnumValue("Person.PhoneType", "WORK"))
                .build();

        Descriptor personDesc = schema.getMessageDescriptor("Person");
        DynamicMessage personMsg = schema.newMessageBuilder("Person")
                .setField(personDesc.findFieldByName("id"), 0)
                .setField(personDesc.findFieldByName("name"), "Alan Turing")
                .addRepeatedField(personDesc.findFieldByName("phone"), phoneMsg1)
                .addRepeatedField(personDesc.findFieldByName("phone"), phoneMsg2)
                .build();
        log.info("testAdvanced personMsg: {}", personMsg);

        PersonSchema.Person.PhoneNumber personPhone1 = PersonSchema.Person.PhoneNumber.newBuilder().setNumber("+44-111").build();
        PersonSchema.Person.PhoneNumber personPhone2 = PersonSchema.Person.PhoneNumber.newBuilder().setNumber("+44-222").setType(PersonSchema.Person.PhoneType.WORK).build();
        List<PersonSchema.Person.PhoneNumber> phoneNumbers = new ArrayList<PersonSchema.Person.PhoneNumber>();
        phoneNumbers.add(personPhone1);
        phoneNumbers.add(personPhone2);

        PersonSchema.Person person = PersonSchema.Person.newBuilder()
                .setId(0)
                .setName("Alan Turing")
                .addAllPhone(phoneNumbers)
                .build();

        Assert.assertEquals(person.toString(), personMsg.toString());

        phoneMsg1 = (DynamicMessage) personMsg.getRepeatedField(personDesc.findFieldByName("phone"), 0);
        phoneMsg2 = (DynamicMessage) personMsg.getRepeatedField(personDesc.findFieldByName("phone"), 1);

        String phoneNumber1 = (String) phoneMsg1.getField(phoneDesc.findFieldByName("number"));
        String phoneNumber2 = (String) phoneMsg2.getField(phoneDesc.findFieldByName("number"));

        EnumValueDescriptor phoneType1 = (EnumValueDescriptor) phoneMsg1.getField(phoneDesc.findFieldByName("type"));
        EnumValueDescriptor phoneType2 = (EnumValueDescriptor) phoneMsg2.getField(phoneDesc.findFieldByName("type"));

        log.info("{}, {}", phoneNumber1, phoneType1.getName());
        log.info("{}, {}", phoneNumber2, phoneType2.getName());

        Assert.assertEquals("+44-111", phoneNumber1);
        Assert.assertEquals("MOBILE", phoneType1.getName()); // [default = MOBILE]

        Assert.assertEquals("+44-222", phoneNumber2);
        Assert.assertEquals("WORK", phoneType2.getName());
    }

    /**
     * testSchemaMerge - schema merging
     */
    @Test
    public void testSchemaMerge() throws Exception {
        log.info("--- testSchemaMerge ---");

        DynamicSchema.Builder schemaBuilder1 = DynamicSchema.newBuilder().setName("Schema1.proto").setPackage("package1");
        schemaBuilder1.addMessageDefinition(MessageDefinition.newBuilder("Msg1").build());

        DynamicSchema.Builder schemaBuilder2 = DynamicSchema.newBuilder().setName("Schema2.proto").setPackage("package2");
        schemaBuilder2.addMessageDefinition(MessageDefinition.newBuilder("Msg2").build());

        schemaBuilder1.addSchema(schemaBuilder2.build());
        DynamicSchema schema1 = schemaBuilder1.build();
        log.info("testSchemaMerge schema1: {}", schema1);

        // schema1 should contain both Msg1 and Msg2
        Assert.assertNotNull(schema1.getMessageDescriptor("Msg1"));
        Assert.assertNotNull(schema1.getMessageDescriptor("Msg2"));

        DynamicSchema.Builder schemaBuilder3 = DynamicSchema.newBuilder().setName("Schema3.proto").setPackage("package3");
        schemaBuilder3.addMessageDefinition(MessageDefinition.newBuilder("Msg1").build()); // Msg1 to force collision
        schemaBuilder1.addSchema(schemaBuilder3.build());
        schema1 = schemaBuilder1.build();
        log.info("testSchemaMerge schema1: {}", schema1);

        // Msg1 now ambiguous, must fully qualify name (package1, package3); Msg2 still unique
        Assert.assertNull(schema1.getMessageDescriptor("Msg1"));
        Assert.assertNotNull(schema1.getMessageDescriptor("Msg2"));
        Assert.assertNotNull(schema1.getMessageDescriptor("package1.Msg1"));
        Assert.assertNotNull(schema1.getMessageDescriptor("package2.Msg2"));
        Assert.assertNotNull(schema1.getMessageDescriptor("package3.Msg1"));

        // Trying to add duplicate name (fully qualified) should throw exception
        IllegalArgumentException ex = null;
        try {
            schemaBuilder1.addSchema(schemaBuilder3.build());
            schema1 = schemaBuilder1.build();
        } catch (IllegalArgumentException e) {
            log.error("expected: ", e);
            ex = e;
        }
        Assert.assertNotNull(ex);
    }

    /**
     * testSchemaSerialization - serialization, deserialization, protoc output parsing
     */
    @Test
    public void testSchemaSerialization() throws Exception {
        log.info("--- testSchemaSerialization ---");

        // Read protoc compiler output (deserialize)
        DynamicSchema schema1 = DynamicSchema.parseFrom(new FileInputStream("src/test/resources/PersonSchema.desc"));
        log.info("PersonSchema.desc: {}", schema1);

        byte[] descBuf = schema1.toByteArray(); // serialize
        DynamicSchema schema2 = DynamicSchema.parseFrom(descBuf); // deserialize

        // Should be equivalent
        Assert.assertEquals(schema1.toString(), schema2.toString());
    }

    /**
     * testSchemaDependency - nested dependencies (imports)
     */
    @Test
    public void testSchemaDependency() throws Exception {
        log.info("--- testSchemaDependency ---");

        // Read protoc compiler output (deserialize)
        DynamicSchema schema1 = DynamicSchema.parseFrom(new FileInputStream("src/test/resources/Schema1.desc"));
        log.info("Schema1.desc: {}", schema1);

        // schema1 should contain all imported types
        Assert.assertNotNull(schema1.getMessageDescriptor("Msg1"));
        Assert.assertNotNull(schema1.getMessageDescriptor("Msg2"));
        Assert.assertNotNull(schema1.getMessageDescriptor("Msg3"));
        Assert.assertNotNull(schema1.getMessageDescriptor("Person"));
        Assert.assertNotNull(schema1.getMessageDescriptor("Person.PhoneNumber"));
        Assert.assertNotNull(schema1.getEnumDescriptor("Person.PhoneType"));
    }

    /**
     * testSchemaDependencyNoImports - missing nested dependencies (imports)
     */
    @Test
    public void testSchemaDependencyNoImports() throws Exception {
        log.info("--- testSchemaDependencyNoImports ---");

        // Trying to parse schema descriptor with missing dependencies should throw exception
        IllegalArgumentException ex = null;
        try {
            // Read protoc compiler output (deserialize)
            DynamicSchema schema1 = DynamicSchema.parseFrom(new FileInputStream("src/test/resources/Schema1_no_imports.desc"));
            log.info("Schema1_no_imports.desc: {}", schema1);
        } catch (IllegalArgumentException e) {
            log.error("expected: ", e);
            ex = e;
        }
        Assert.assertNotNull(ex);
    }
}
