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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CloudFunctionMain {
	public static void main(String[] args) {
		SpringApplication.run(CloudFunctionMain.class, args);
	}

	@Bean
	public Function<String, String> uppercase() {
		return input -> input.toUpperCase();
	}

	@Bean
	public Function<Foo, Bar> foobar() {
		return input -> new Bar(input.value);
	}
}

class Foo {
	String value;

	Foo(String value) {
		this.value = value;
	}
}

class Bar {
	String value;

	Bar(String value) {
		this.value = value;
	}
}
