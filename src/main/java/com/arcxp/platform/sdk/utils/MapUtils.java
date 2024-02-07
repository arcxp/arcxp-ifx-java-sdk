package com.arcxp.platform.sdk.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class MapUtils {
    public static ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .build();
    }
}
