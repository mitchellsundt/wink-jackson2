/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.common.internal.providers.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.providers.entity.csv.CsvDeserializerProvider;
import org.apache.wink.common.internal.providers.entity.csv.CsvSerializerProvider;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.csv.CsvDeserializer;
import org.apache.wink.common.model.csv.CsvSerializer;

/**
 * 
 */
public class CsvProvidersTest extends TestCase {

    public final static String lineSeparator = System.getProperty("line.separator");

    /**
     * tests CSV Representation <tt>to</tt> operation
     * 
     * @throws Exception
     */
    public void testTo() throws Exception {
        // CollectionResource<String> resource = new
        // CollectionResource<String>();
        CsvSerializer csvSerializer = new CsvSerializer() {

            List<String[]> csvText =
                                       new ArrayList<String[]>(
                                                               Arrays
                                                                   .asList(new String[] {"Id",
                                                                               "Name",
                                                                               "Description",
                                                                               "Assigned To",
                                                                               "Severity", "Status"},
                                                                           new String[] {
                                                                               "2",
                                                                               "Infinite Loop",
                                                                               "\"When making target which depends on it self, the application freezes in an infinite loop\"",
                                                                               "developer2",
                                                                               "2-high", "New"},
                                                                           new String[] {
                                                                               "3",
                                                                               "RSS feed component throws exception",
                                                                               "When I open the policy manager home page" + lineSeparator
                                                                                   + " (after I create business policy and perform some validations) I see the error.",
                                                                               "developer3",
                                                                               "4-minor", "New"},
                                                                           new String[] {
                                                                               "1",
                                                                               "Localization problem",
                                                                               "While running the application," + lineSeparator
                                                                                   + "an exception that localization key is not found was thrown.",
                                                                               "developer,1",
                                                                               "2-high", "New"}));

            public Iterator<String[]> getEntities() {
                return csvText.iterator();
            }
        };

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        MultivaluedMapImpl<String, Object> httpHeaders = new MultivaluedMapImpl<String, Object>();
        new CsvSerializerProvider().writeTo(csvSerializer,
                                            csvSerializer.getClass(),
                                            null,
                                            null,
                                            MediaTypeUtils.CSV_TYPE,
                                            httpHeaders,
                                            os);

        String result = new String(os.toByteArray(), "UTF-8");

        String expected =
            "Id,Name,Description,Assigned To,Severity,Status" + lineSeparator
                + "2,Infinite Loop,\"\"\"When making target which depends on it self, the application freezes in an infinite loop\"\"\",developer2,2-high,New"
                + lineSeparator
                + "3,RSS feed component throws exception,\"When I open the policy manager home page"
                + lineSeparator
                + " (after I create business policy and perform some validations) I see the error.\",developer3,4-minor,New"
                + lineSeparator
                + "1,Localization problem,\"While running the application,"
                + lineSeparator
                + "an exception that localization key is not found was thrown.\",\"developer,1\",2-high,New"
                + lineSeparator;
        assertEquals(expected, result);
        assertEquals("attachment; filename=\"representation.csv\"", httpHeaders
            .getFirst(CsvSerializerProvider.CONTENT_DISPOSITION_HEADER));
    }

    public static class InnerCsvDeserializer implements CsvDeserializer {

        final List<String[]> result = new ArrayList<String[]>();

        public void addEntity(String[] row) {
            result.add(row);

        }

    }

    /**
     * tests CSV Representation <tt>from</tt> operation
     * 
     * @throws Exception
     */
    public void testFrom() throws Exception {

        String source =
            "Id,Name,Description,Assigned To,Severity,Status" + lineSeparator
                + "2,Infinite Loop,\"\"\"When making target which depends on it self, the application freezes in an infinite loop\"\"\",developer2,2-high,New"
                + lineSeparator
                + "3,RSS feed component throws exception,When I open the policy manager home page (after I create business policy and perform some validations) I see the error.,developer3,4-minor,New"
                + lineSeparator
                + "1,Localization problem,\"While running the application,"
                + lineSeparator
                + "an exception that localization key is not found was thrown.\",\"developer,1\",2-high,New"
                + lineSeparator;

        List<String[]> expected =
            new ArrayList<String[]>(
                                    Arrays
                                        .asList(new String[] {"Id", "Name", "Description",
                                                    "Assigned To", "Severity", "Status"},
                                                new String[] {
                                                    "2",
                                                    "Infinite Loop",
                                                    "\"When making target which depends on it self, the application freezes in an infinite loop\"",
                                                    "developer2", "2-high", "New"},
                                                new String[] {
                                                    "3",
                                                    "RSS feed component throws exception",
                                                    "When I open the policy manager home page (after I create business policy and perform some validations) I see the error.",
                                                    "developer3", "4-minor", "New"},
                                                new String[] {
                                                    "1",
                                                    "Localization problem",
                                                    "While running the application," + lineSeparator
                                                        + "an exception that localization key is not found was thrown.",
                                                    "developer,1", "2-high", "New"}));

        InnerCsvDeserializer csvDeserializer =
            new CsvDeserializerProvider<InnerCsvDeserializer>()
                .readFrom(InnerCsvDeserializer.class,
                          null,
                          null,
                          MediaTypeUtils.CSV_TYPE,
                          new MultivaluedMapImpl<String, String>(),
                          new ByteArrayInputStream(source.getBytes()));

        for (int i = 0; i < expected.size(); ++i) {
            assertTrue(Arrays.deepEquals(expected.get(i), csvDeserializer.result.get(i)));
        }
    }

}
