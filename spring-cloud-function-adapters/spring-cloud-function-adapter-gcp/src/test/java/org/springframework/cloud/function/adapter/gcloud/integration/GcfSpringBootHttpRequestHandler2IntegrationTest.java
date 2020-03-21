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

import com.google.gson.Gson;
import org.junit.Rule;
import org.junit.Test;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.function.adapter.gcloud.GcfSpringBootHttpRequestHandler2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for GCF Http Functions.
 *
 * @author Daniel Zou
 * @author Mike Eltsufin
 */
public class GcfSpringBootHttpRequestHandler2IntegrationTest {

	private static final int PORT = 7777;

	private static final Gson gson = new Gson();

	@Rule
	public CloudFunctionServer cloudFunctionServer =
		new CloudFunctionServer(PORT, GcfSpringBootHttpRequestHandler2.class, CloudFunctionMain.class);

	@Test
	public void testUppercase() {
		test("uppercase", "hello", "HELLO");
	}

	@Test
	public void testFooBar() {
		test("foobar", new Foo("Hi"), new Bar("Hi"));
	}

	private <I, O> void test(String function, I request, O expectedResponse) {
		TestRestTemplate testRestTemplate = new TestRestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.set("spring.function", function);

		ResponseEntity<String> response = testRestTemplate.postForEntity(
			"http://localhost:" + PORT,
			new HttpEntity<>(gson.toJson(request), headers), String.class);

		assertThat(response.getBody()).isEqualTo(gson.toJson(expectedResponse));
	}
}
