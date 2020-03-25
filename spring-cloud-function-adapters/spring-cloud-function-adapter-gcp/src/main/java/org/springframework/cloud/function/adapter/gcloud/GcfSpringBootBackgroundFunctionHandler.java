package org.springframework.cloud.function.adapter.gcloud;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

public class GcfSpringBootBackgroundFunctionHandler<O>  extends AbstractGcfHandler<O>  implements BackgroundFunction<PubSubMessage> {
	public GcfSpringBootBackgroundFunctionHandler(Class<?> configurationClass) {
		super(configurationClass);
	}

	public GcfSpringBootBackgroundFunctionHandler() {
		super();
	}

	@Override
	public void accept(PubSubMessage pubSubMessage, Context context) {
		Flux.from(apply(extract(toMessageIfNeeded(pubSubMessage)))).blockLast();
	}

	private Object toMessageIfNeeded(PubSubMessage pubSubMessage) {
		if (functionAcceptsMessage()) {
			return new GenericMessage<>(toOptionalIfEmpty(pubSubMessage.getData()), getHeaders(pubSubMessage));
		}
		return toOptionalIfEmpty(pubSubMessage.getData());
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

