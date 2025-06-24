protobuf-dynamic
================

Protocol Buffers Dynamic Schema - create protobuf schemas programmatically.  
Available on Maven Central (original project):
* https://repo.maven.apache.org/maven2/com/github/os72/protobuf-dynamic/1.0.1/
* https://repo.maven.apache.org/maven2/com/github/os72/protobuf-dynamic/0.9.5/

[![Maven Central](https://img.shields.io/badge/maven%20central-1.0.1-brightgreen.svg)](http://search.maven.org/#artifactdetails|com.github.os72|protobuf-dynamic|1.0.1|)
[![Maven Central](https://img.shields.io/badge/maven%20central-0.9.5-brightgreen.svg)](http://search.maven.org/#artifactdetails|com.github.os72|protobuf-dynamic|0.9.5|)

---

This fork is maintained by [ThingsBoard](https://github.com/thingsboard), with updated Java version support and build improvements.

Library to simplify working with the Protocol Buffers reflection mechanism, no protoc compiler required.  
Supports the major protobuf features: primitive types, complex and nested types, labels, default values, etc.

Features:
* Dynamic schema creation at runtime
* Dynamic message creation from schema
* Schema merging
* Schema serialization, deserialization
* Schema parsing from protoc compiler output
* Compatible with `protobuf-java` 3.25.x
* Requires Java 17+

See the Protocol Buffers site for details: https://github.com/google/protobuf

---

Protocol Buffers Dynamic Schema (ThingsBoard Fork)  
Available on Maven Central:

* https://repo.maven.apache.org/maven2/org/thingsboard/protobuf-dynamic/1.0.4TB/
* https://repo.maven.apache.org/maven2/org/thingsboard/protobuf-dynamic/1.0.5TB/

[![Maven Central](https://img.shields.io/badge/maven%20central-1.0.4TB-brightgreen.svg)](https://search.maven.org/artifact/org.thingsboard/protobuf-dynamic/1.0.4TB)
[![Maven Central](https://img.shields.io/badge/maven%20central-1.0.5TB-brightgreen.svg)](https://search.maven.org/artifact/org.thingsboard/protobuf-dynamic/1.0.5TB)

---

#### Usage
```java
// Create dynamic schema representing device telemetry
DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
schemaBuilder.setName("Telemetry.proto");
schemaBuilder.setPackage("org.thingsboard.telemetry");
schemaBuilder.setSyntax("proto3");

MessageDefinition telemetryMsg = MessageDefinition.newBuilder("Telemetry")
        .addField(null, "string", "serialNumber", 1)         // string serialNumber = 1
        .addField(null, "int64", "ts", 2)                    // int64 ts = 2
        .addField("optional", "double", "temperature", 3)    // optional double temperature = 3
        .addField("optional", "double", "humidity", 4)       // optional double humidity = 4
        .build();

schemaBuilder.addMessageDefinition(telemetryMsg);
DynamicSchema schema = schemaBuilder.build();

// Create dynamic message
DynamicMessage.Builder msgBuilder = schema.newMessageBuilder("Telemetry");
Descriptor msgDesc = msgBuilder.getDescriptorForType();
DynamicMessage message = msgBuilder
        .setField(msgDesc.findFieldByName("serialNumber"), "SN-0011223344")
        .setField(msgDesc.findFieldByName("ts"), System.currentTimeMillis())
        .setField(msgDesc.findFieldByName("temperature"), 23.5)
        .setField(msgDesc.findFieldByName("humidity"), 60.2)
        .build();
```

### addField(...) Behavior in proto3

Passing `null` as the label in `.addField(null, ...)` results in the field being defined without any label like optional, repeated, or required.

In proto3, this means:

 - The field is treated as implicitly optional, per the proto3 default.
 - The field does not support presence tracking — calling `hasField()` will return false unless the field is part of a synthetic oneof.

This fork supports explicit presence tracking in proto3:

 - If `.addField("optional", ...)` is used:
   - The field is marked with `proto3_optional = true`.
   - It is added to a synthetic oneof group internally.

Presence tracking becomes available: `hasField()` will work.

This makes it possible to model both:

 - Required-in-practice fields like `serialNumber`, `ts` (without `optional`, not tracked via presence).
 - Optional-with-tracking fields like `temperature`, `humidity` (explicit optional label, tracked).

Please refer to the official [How To Implement Field Presence for Proto3](https://github.com/protocolbuffers/protobuf/blob/main/docs/implementing_proto3_presence.md) for more details.

#### Maven dependency
```xml
<dependency>
    <groupId>org.thingsboard</groupId>
    <artifactId>protobuf-dynamic</artifactId>
    <version>1.0.4TB</version>
</dependency>
```

```xml
<dependency>
    <groupId>org.thingsboard</groupId>
    <artifactId>protobuf-dynamic</artifactId>
    <version>1.0.5TB</version>
</dependency>
```
