/*
 * Copyright 2019-2019 the original author or authors.
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

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Solomakha
 * @author Mike Eltsufin
 */
public class GcfSpringBootHttpRequestHandler2Tests {

	public static final Gson gson = new Gson();

	@Test
	public void testHelloWorldSupplier() throws Exception {
		testFunction(HelloWorldSupplier.class, null, "Hello World!");
	}

	@Test
	public void testJsonInputFunction() throws Exception {
		testFunction(JsonInputFunction.class, new IncomingRequest("hello"),
			"Thank you for sending the message: hello");
	}

	private <I, O> void testFunction(Class<?> configurationClass, I input, O expectedOutput) throws Exception {
		GcfSpringBootHttpRequestHandler2 handler = new GcfSpringBootHttpRequestHandler2(configurationClass);

		HttpRequest request = Mockito.mock(HttpRequest.class);

		if (input != null) {
			when(request.getReader()).thenReturn(new BufferedReader(new StringReader(gson.toJson(input))));
		}

		StringWriter writer = new StringWriter();
		HttpResponse response = new HttpResponseImpl(new BufferedWriter(writer));

		handler.service(request, response);

		assertThat(writer.toString()).isEqualTo(gson.toJson(expectedOutput));
	}

	@Configuration
	@Import({ ContextFunctionCatalogAutoConfiguration.class })
	protected static class HelloWorldSupplier {
		@Bean
		public Supplier<String> supplier() {
			return () -> "Hello World!";
		}
	}

	@Configuration
	@Import({ ContextFunctionCatalogAutoConfiguration.class })
	protected static class JsonInputFunction {
		@Bean
		public Function<IncomingRequest, String> function() {
			return (in) -> "Thank you for sending the message: " + in.message;
		}
	}

	class IncomingRequest {
		String message;

		public IncomingRequest(String message) {
			this.message = message;
		}
	}

}
