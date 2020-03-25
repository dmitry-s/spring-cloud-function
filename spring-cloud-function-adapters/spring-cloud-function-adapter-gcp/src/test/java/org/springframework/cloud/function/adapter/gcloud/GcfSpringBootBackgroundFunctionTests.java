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

import com.google.gson.Gson;
import org.junit.After;
import org.junit.Test;
import org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cloud.function.adapter.gcloud.GcfSpringBootHttpRequestHandlerTests.*;

/**
 * @author Dmitry Solomakha
 */
public class GcfSpringBootBackgroundFunctionTests {
	private GcfSpringBootBackgroundFunctionHandler<?> handler = null;
	public static final Gson GSON = new Gson();

	<O> GcfSpringBootBackgroundFunctionHandler<O> handler(Class<?> config) {
		GcfSpringBootBackgroundFunctionHandler<O> handler = new GcfSpringBootBackgroundFunctionHandler<>(config);

		this.handler = handler;
		return handler;
	}

	@Test
	public void testWithBody() {
		GcfSpringBootBackgroundFunctionHandler<Foo> handler = handler(FunctionConfig.class);

		Foo foo = new Foo("foo");
		PubSubMessage psMessage = new PubSubMessage(GSON.toJson(foo), null, null, null);
		handler.accept(psMessage, null);

		assertThat(FunctionConfig.argument).isEqualTo(foo);
	}

	@Test
	public void testWithBodyMessage() {
		GcfSpringBootBackgroundFunctionHandler<Foo> handler = handler(FunctionMessageConfig.class);

		Foo foo = new Foo("foo");
		HashMap<String, String> attributes = new HashMap<>();
		attributes.put("attribute", "value");

		PubSubMessage psMessage = new PubSubMessage(GSON.toJson(foo), attributes, "id", "2020-02-01");
		handler.accept(psMessage, null);

		assertThat(FunctionMessageConfig.argument.getHeaders().get("messageId")).isEqualTo("id");
		assertThat(FunctionMessageConfig.argument.getHeaders().get("publishTime")).isEqualTo("2020-02-01");
		assertThat(FunctionMessageConfig.argument.getHeaders().get("attribute")).isEqualTo("value");
		assertThat(FunctionMessageConfig.argument.getPayload()).isEqualTo(foo);
	}

	@After
	public void close() {
		if (this.handler != null) {
			this.handler.close();
		}
	}

	@Configuration
	@Import({ContextFunctionCatalogAutoConfiguration.class})
	protected static class FunctionConfig {
		static Foo argument;

		@Bean
		public Consumer<Foo> function() {
			return (foo -> {
				argument = foo;
			});
		}
	}

	@Configuration
	@Import({ContextFunctionCatalogAutoConfiguration.class})
	protected static class FunctionMessageConfig {
		static Message<Foo> argument;

		@Bean
		public Consumer<Message<Foo>> function() {
			return (message -> {
				argument = message;
			});
		}
	}
}



