/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.function.adapter.gcloud;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import org.reactivestreams.Publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

/**
 * Implementation of HttpFunction for Google Cloud Function.
 *
 * @param <O> input type
 * @author Dmitry Solomakha
 */
public class GcfSpringBootHttpRequestHandlerOriginal<O> extends GcfHandler<O> implements HttpFunction {

	public GcfSpringBootHttpRequestHandlerOriginal(Class<?> configurationClass) {
		super(configurationClass);
	}

	public GcfSpringBootHttpRequestHandlerOriginal() {
		super();
	}

	@Autowired
	private ObjectMapper mapper;

	@Override
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
		Publisher<?> output = apply(extract(convert(httpRequest)));
		BufferedWriter writer = httpResponse.getWriter();
		Object result = result(httpRequest, output, httpResponse);
		if (returnsOutput()) {
			writer.write(mapper.writeValueAsString(result));
			writer.flush();
		}
		httpResponse.setStatusCode(200);
	}

	private Object convert(HttpRequest event) throws IOException {
		BufferedReader br = event.getReader();
		StringBuilder sb = new StringBuilder();

		char[] buffer = new char[1024 * 4];
		int n;
		while (-1 != (n = br.read(buffer))) {
			sb.append(buffer, 0, n);
		}

		String requestBody = sb.toString();
		if (functionAcceptsMessage()) {
			return new GenericMessage<>(toOptionalIfEmpty(requestBody), getHeaders(event));
		}
		return toOptionalIfEmpty(requestBody);
	}

	private MessageHeaders getHeaders(HttpRequest event) {
		Map<String, Object> headers = new HashMap<String, Object>();

		if (event.getHeaders() != null) {
			headers.putAll(event.getHeaders());
		}
		if (event.getQueryParameters() != null) {
			headers.putAll(event.getQueryParameters());
		}
		if (event.getUri() != null) {
			headers.put("path", event.getPath());
		}

		if (event.getMethod() != null) {
			headers.put("httpMethod", event.getMethod());
		}

		return new MessageHeaders(headers);
	}

}
