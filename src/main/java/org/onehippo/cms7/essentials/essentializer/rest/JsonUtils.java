/*
 * Copyright 2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.cms7.essentials.essentializer.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class JsonUtils {
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    public static final ObjectMapper JSON = new ObjectMapper();
    public static final byte[] EMPTY_BYTES = new byte[0];

    static {
        JSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSON.enable(SerializationFeature.INDENT_OUTPUT);
        JSON.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    private JsonUtils() {
    }



    public static <T> String toJson(final T object) {
        if (object == null) {
            return "";
        }

        try {
            return JSON.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON error", e);
        }
        return "";
    }

    public static <T> byte[] toBytesJson(final T object) {
        if (object == null) {
            return EMPTY_BYTES;
        }

        try {
            return JSON.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error("JSON error", e);
        }
        return EMPTY_BYTES;
    }


    public static <T> T fromJson(final byte[] value, Class<T> clazz) {

        try {
            return JSON.readValue(value, clazz);
        } catch (Exception e) {
            log.error("JSON error", e);
        }
        return null;
    }


    public static <T> T fromJson(final String value, Class<T> clazz) {

        try {
            return JSON.readValue(value, clazz);
        } catch (Exception e) {
            log.error("JSON error (see message below)", e);
            log.error("{}", value);
        }
        return null;
    }

}
