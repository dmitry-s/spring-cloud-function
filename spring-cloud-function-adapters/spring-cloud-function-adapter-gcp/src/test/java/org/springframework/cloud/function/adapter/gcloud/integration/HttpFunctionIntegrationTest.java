/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.cloud.function.adapter.gcloud.integration;

import java.util.function.Function;

import org.junit.Rule;
import org.junit.Test;

import org.springframework.cloud.function.adapter.gcloud.GcfSpringBootHttpRequestHandler;
import org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * Integration tests for GCF Http Functions.
 *
 * @author Daniel Zou
 * @author Mike Eltsufin
 */
public class HttpFunctionIntegrationTest {

	@Rule
	public CloudFunctionServer cloudFunctionServer =
		new CloudFunctionServer(GcfSpringBootHttpRequestHandler.class, CloudFunctionMain.class);

	@Test
	public void test() {
		cloudFunctionServer.test(null, "hello", "HELLO");
	}

	@Configuration
	@Import({ContextFunctionCatalogAutoConfiguration.class})
	protected static class CloudFunctionMain {

		@Bean
		public Function<String, String> uppercase() {
			return input -> input.toUpperCase();
		}
	}
}
