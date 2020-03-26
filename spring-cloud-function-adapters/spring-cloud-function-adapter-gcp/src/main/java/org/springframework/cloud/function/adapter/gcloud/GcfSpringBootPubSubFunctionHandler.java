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

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import reactor.core.publisher.Flux;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GcfSpringBootPubSubFunctionHandler<O> extends GcfHandler<O>
		implements BackgroundFunction<PubSubMessage> {

	public GcfSpringBootPubSubFunctionHandler(Class<?> configurationClass) {
		super(configurationClass);
	}

	public GcfSpringBootPubSubFunctionHandler() {
		super();
	}

	@Override
	public void accept(PubSubMessage pubSubMessage, Context context) {
		Flux.from(apply(extract(toMessageIfNeeded(pubSubMessage)))).blockLast();
	}

	private Object toMessageIfNeeded(PubSubMessage pubSubMessage) {
		String data = new String(Base64.getDecoder().decode(pubSubMessage.getData()), UTF_8);
		if (functionAcceptsMessage()) {
			return new GenericMessage<>(toOptionalIfEmpty(data), getHeaders(pubSubMessage));
		}
		return toOptionalIfEmpty(data);
	}

	private Map<String, Object> getHeaders(PubSubMessage pubSubMessage) {
		Map<String, Object> headers = new HashMap<String, Object>();

		if (pubSubMessage.getAttributes() != null) {
			headers.putAll(pubSubMessage.getAttributes());
		}

		if (pubSubMessage.getMessageId() != null) {
			headers.put("messageId", pubSubMessage.getMessageId());
		}

		if (pubSubMessage.getPublishTime() != null) {
			headers.put("publishTime", pubSubMessage.getPublishTime());
		}

		return new MessageHeaders(headers);
	}

}
