package org.infraRpcExample.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimeZone;

import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ObjectMapperConfig {

    private final static Logger logger = LoggerFactory.getLogger(ObjectMapperConfig.class);

    @Getter
    private static final ObjectMapper snakeObjectMapper;

    @Getter
    private static final ObjectMapper commonObjectMapper;

    static {
        commonObjectMapper = new ObjectMapper();

        snakeObjectMapper = new ObjectMapper();

        snakeObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        commonObjectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                .configure(REQUIRE_SETTERS_FOR_GETTERS, true)
                .setTimeZone(TimeZone.getDefault());
    }

    public static String toCommonJsonString(Object object) {
        try {
            return commonObjectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Object json deserialization exception.", e);
        }
    }

    /**
     * serialize to json byte
     *
     * @param obj object
     * @param <T> object type
     * @return byte array
     */
    public static <T> byte[] toCommonJsonByteArray(T obj) {
        if (obj == null) {
            return null;
        }
        String json = "";
        try {
            json = toCommonJsonString(obj);
        } catch (Exception e) {
            logger.error("json serialize exception.", e);
        }

        return json.getBytes(UTF_8);
    }

    public static <T> T parseCommonObject(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return commonObjectMapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("parse object exception!", e);
        }
        return null;
    }

}
