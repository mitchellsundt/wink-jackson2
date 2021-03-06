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

package org.apache.wink.client.internal.handlers;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.HandlerContext;
import org.apache.wink.client.handlers.InputStreamAdapter;
import org.apache.wink.client.handlers.OutputStreamAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for GZip encoding for requests and responses
 */
public class GzipHandler implements ClientHandler {
	
    private static Logger logger = LoggerFactory.getLogger(GzipHandler.class);

    public ClientResponse handle(ClientRequest request, HandlerContext context) throws Exception {
        request.getHeaders().add("Accept-Encoding", "gzip"); //$NON-NLS-1$ //$NON-NLS-2$
        if (request.getEntity() != null) {
            request.getHeaders().add("Content-Encoding", "gzip"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        context.addInputStreamAdapter(new GzipAdapter());
        context.addOutputStreamAdapter(new GzipAdapter());
        return context.doChain(request);
    }

    private static class GzipAdapter implements InputStreamAdapter, OutputStreamAdapter {

        public OutputStream adapt(OutputStream os, ClientRequest request) throws IOException {
            return new GZIPOutputStream(os);
        }

        public InputStream adapt(InputStream is, ClientResponse response) throws IOException {
            String header = response.getHeaders().getFirst("Content-Encoding"); //$NON-NLS-1$
            if (header != null && header.equalsIgnoreCase("gzip")) { //$NON-NLS-1$

            	// if we have content-length, then use it to figure out the GZIP body size.
            	String clHeader = response.getHeaders().getFirst("Content-Length");
            	if ( clHeader != null ) {
            		try {
                    	int length = Integer.parseInt(clHeader);
                    	// Transfer bytes from in to out
                    	byte[] data = new byte[length];
                    	BufferedInputStream bi = new BufferedInputStream(is);
                    	int len;
                    	int start = 0;
                    	int end = length;
                    	int check = 0;
                    	while ((end != 0) && ((len = bi.read(data, start, end)) >= 0)) {
                    		end -= len;
                    		start += len;
                    		check = start + end;
                    		if (check != length) {
                    			logger.trace("check != length: check =" + check + " start=" + start + " end=" + end + " length=" +  length);
                    		}
                    	}
                    	bi.close();

                		ByteArrayInputStream bis = new ByteArrayInputStream(data);
                		return new GZIPInputStream(bis);
            		} catch ( NumberFormatException e) {
            			// ignore...
            		} catch (Exception e) {
            		  e.printStackTrace();
            		}
            	}
            	
            	BufferedInputStream bi = new BufferedInputStream(is);
        		return new GZIPInputStream(bi);
            }
            return is;
        }

    }
}
