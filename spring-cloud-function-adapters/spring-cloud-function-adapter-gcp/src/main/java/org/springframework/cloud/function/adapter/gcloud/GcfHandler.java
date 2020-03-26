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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.cloud.functions.Context;
import com.google.cloud.functions.HttpResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import org.springframework.cloud.function.context.AbstractSpringFunctionAdapterInitializer;
import org.springframework.messaging.Message;

public class GcfHandler<O> extends AbstractSpringFunctionAdapterInitializer<Context> {

	public GcfHandler(Class<?> configurationClass) {
		super(configurationClass);
		init();
	}

	public GcfHandler() {
		super();
		init();
	}

	public void init() {
		Thread.currentThread().setContextClassLoader(GcfSpringBootHttpRequestHandler.class.getClassLoader());
		initialize(null);
	}

	Object toOptionalIfEmpty(String requestBody) {
		return requestBody.isEmpty() ? Optional.empty() : requestBody;
	}

	protected boolean functionAcceptsMessage() {
		return this.getInspector().isMessage(function());
	}

	@SuppressWarnings("unchecked")
	protected <T> T result(Object input, Publisher<?> output, HttpResponse resp) {
		List<T> result = new ArrayList<>();
		for (Object value : Flux.from(output).toIterable()) {
			result.add((T) convertOutputAndHeaders(value, resp));
		}
		if (isSingleValue(input) && result.size() == 1) {
			return result.get(0);
		}
		return (T) result;
	}

	private boolean isSingleValue(Object input) {
		return !(input instanceof Collection);
	}

	Flux<?> extract(Object input) {
		if (input instanceof Collection) {
			return Flux.fromIterable((Iterable<?>) input);
		}
		return Flux.just(input);
	}

	protected O convertOutputAndHeaders(Object output, HttpResponse resp) {
		if (output instanceof Message) {
			Message<?> message = (Message<?>) output;
			for (Map.Entry<String, Object> entry : message.getHeaders().entrySet()) {
				Object values = entry.getValue();
				if (values instanceof List) {
					for (Object value : (List) values) {
						if (value != null) {
							resp.appendHeader(entry.getKey(), value.toString());
						}
					}
				}
				else if (values != null) {
					resp.appendHeader(entry.getKey(), values.toString());
				}
			}
			return (O) message.getPayload();
		}
		else {
			return (O) output;
		}
	}

	boolean returnsOutput() {
		return !this.getInspector().getOutputType(function()).equals(Void.class);
	}

}
