package com.example.Deserializer;

import com.example.Dto.Features;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class JSONValueDeserializationSchema implements DeserializationSchema<Features> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void open(InitializationContext context) throws Exception {
        DeserializationSchema.super.open(context);
    }

    @Override
    public Features deserialize(byte[] bytes) throws IOException {
        return objectMapper.readValue(bytes, Features.class);
    }

    @Override
    public boolean isEndOfStream(Features Features) {
        return false;
    }

    @Override
    public TypeInformation<Features> getProducedType() {
        return TypeInformation.of(Features.class);
    }
}
