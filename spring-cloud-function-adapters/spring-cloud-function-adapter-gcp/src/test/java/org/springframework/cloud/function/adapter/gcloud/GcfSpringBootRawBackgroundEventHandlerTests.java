/*
 * Copyright 2020-2020 the original author or authors.
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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.cloud.functions.RawBackgroundFunction;
import com.google.gson.Gson;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link RawBackgroundFunction} function adapter for Google Cloud
 * Functions.
 *
 * @author Mike Eltsufin
 */
public class GcfSpringBootRawBackgroundEventHandlerTests {

	private static final Gson gson = new Gson();

	private static final String DROPPED_LOG_PREFIX = "Dropping background function result: ";

	@Rule
	public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

	@Test
	public void testHelloWorldSupplier() throws Exception {
		testBackgroundFunction(HelloWorldSupplier.class, null, "Hello World!", null);
	}

	@Test
	public void testJsonInputFunction() throws Exception {
		testBackgroundFunction(JsonInputFunction.class, new IncomingRequest("hello"),
				"Thank you for sending the message: hello", null);
	}

	@Test
	public void testJsonInputOutputFunction() throws Exception {
		testBackgroundFunction(JsonInputOutputFunction.class, new IncomingRequest("hello"),
				new OutgoingResponse("Thank you for sending the message: hello"), null);
	}

	@Test
	public void testJsonInputConsumer() throws Exception {
		testBackgroundFunction(JsonInputConsumer.class, new IncomingRequest("hello"), null,
				"Thank you for sending the message: hello");
	}

	private <I, O> void testBackgroundFunction(Class<?> configurationClass, I input, O expectedResult,
			String expectedSysOut) {
		GcfSpringBootBackgroundEventHandler handler = new GcfSpringBootBackgroundEventHandler(configurationClass);

		handler.accept(gson.toJson(input), null);

		// verify function sysout statements
		if (expectedSysOut != null) {
			assertThat(systemOutRule.getLog()).contains(expectedSysOut);
		}

		// verify that if function had a return type, it was logged as being dropped
		if (expectedResult != null) {
			assertThat(systemOutRule.getLog()).contains(DROPPED_LOG_PREFIX + gson.toJson(expectedResult));
		}
		else {
			assertThat(systemOutRule.getLog()).doesNotContain(DROPPED_LOG_PREFIX);
		}
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

	@Configuration
	@Import({ ContextFunctionCatalogAutoConfiguration.class })
	protected static class JsonInputOutputFunction {

		@Bean
		public Function<IncomingRequest, OutgoingResponse> function() {
			return (in) -> new OutgoingResponse("Thank you for sending the message: " + in.message);
		}

	}

	@Configuration
	@Import({ ContextFunctionCatalogAutoConfiguration.class })
	protected static class JsonInputConsumer {

		@Bean
		public Consumer<IncomingRequest> function() {
			return (in) -> System.out.println("Thank you for sending the message: " + in.message);
		}

	}

	private static class IncomingRequest {

		String message;

		IncomingRequest(String message) {
			this.message = message;
		}

	}

	private static class OutgoingResponse {

		String message;

		OutgoingResponse(String message) {
			this.message = message;
		}

	}

}
