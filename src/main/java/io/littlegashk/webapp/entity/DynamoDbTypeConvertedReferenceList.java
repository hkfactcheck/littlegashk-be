/*
 * Copyright 2016-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package io.littlegashk.webapp.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@DynamoDBTypeConverted(converter= DynamoDbTypeConvertedReferenceList.Converter.class)
@DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface DynamoDbTypeConvertedReferenceList {


    static final class Converter<T> implements DynamoDBTypeConverter<String, List<Reference>> {

        private static final ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        @Override
        public final String convert(final List<Reference> object) {
            try {
                return mapper.writeValueAsString(object);
            } catch (final Exception e) {
                throw new DynamoDBMappingException("Unable to write object to JSON", e);
            }
        }

        @Override
        public final List<Reference> unconvert(final String object) {
            try {
                return mapper.readValue(object, new TypeReference<List<Reference>>(){});
            } catch (final Exception e) {
                throw new DynamoDBMappingException("Unable to read JSON string", e);
            }
        }
    }

}

