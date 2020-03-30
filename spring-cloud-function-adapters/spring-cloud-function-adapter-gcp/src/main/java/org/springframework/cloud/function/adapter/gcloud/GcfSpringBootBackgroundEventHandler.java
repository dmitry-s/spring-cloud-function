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

import com.google.cloud.functions.Context;
import com.google.cloud.functions.RawBackgroundFunction;
import org.reactivestreams.Publisher;

import org.springframework.cloud.function.context.AbstractSpringFunctionAdapterInitializer;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;

import reactor.core.publisher.Mono;

/**
 * Implementation of {@link RawBackgroundFunction} for Google Cloud Function (GCF). This
 * is the Spring Cloud Function adapter for GCF background functions.
 *
 * @author Mike Eltsufin
 */
public class GcfSpringBootBackgroundEventHandler extends AbstractSpringFunctionAdapterInitializer<Context>
		implements RawBackgroundFunction {

	private final Gson gson = new Gson();

	public GcfSpringBootBackgroundEventHandler() {
		super();
	}

	public GcfSpringBootBackgroundEventHandler(Class<?> configurationClass) {
		super(configurationClass);
	}

	/**
	 * The implementation of a GCF {@link RawBackgroundFunction} that will be used as the
	 * entrypoint from GCF background functions.
	 */
	@Override
	public void accept(String json, Context context) {
		Thread.currentThread().setContextClassLoader(GcfSpringBootBackgroundEventHandler.class.getClassLoader());

		initialize(context);

		Publisher<?> input;
		if (getInputType() == Void.class) {
			input = Mono.empty();
		}
		else {
			input = Mono.just(gson.fromJson(json, getInputType()));
		}

		this.apply(input);
	}

}
