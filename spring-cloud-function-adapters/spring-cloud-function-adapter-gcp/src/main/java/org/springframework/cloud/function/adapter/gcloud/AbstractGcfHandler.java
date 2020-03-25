package org.springframework.cloud.function.adapter.gcloud;

import com.google.cloud.functions.Context;
import com.google.cloud.functions.HttpResponse;
import org.reactivestreams.Publisher;
import org.springframework.cloud.function.context.AbstractSpringFunctionAdapterInitializer;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.util.*;

public class AbstractGcfHandler<O> extends AbstractSpringFunctionAdapterInitializer<Context> {
	public AbstractGcfHandler(Class<?> configurationClass) {
		super(configurationClass);
		init();
	}

	public AbstractGcfHandler() {
		super();
		init();
	}

	public void init() {
		Thread.currentThread()
			.setContextClassLoader(GcfSpringBootHttpRequestHandler.class.getClassLoader());
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
