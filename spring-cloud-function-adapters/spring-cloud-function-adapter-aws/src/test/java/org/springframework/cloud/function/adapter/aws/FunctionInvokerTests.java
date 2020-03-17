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

package org.springframework.cloud.function.adapter.aws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Oleg Zhurakousky
 *
 */
public class FunctionInvokerTests {

	String sampleLBEvent = "{" +
			"    \"requestContext\": {" +
			"        \"elb\": {" +
			"            \"targetGroupArn\": \"arn:aws:elasticloadbalancing:region:123456789012:targetgroup/my-target-group/6d0ecf831eec9f09\"" +
			"        }" +
			"    }," +
			"    \"httpMethod\": \"GET\"," +
			"    \"path\": \"/\"," +
			"    \"headers\": {" +
			"        \"accept\": \"text/html,application/xhtml+xml\"," +
			"        \"accept-language\": \"en-US,en;q=0.8\"," +
			"        \"content-type\": \"text/plain\"," +
			"        \"cookie\": \"cookies\"," +
			"        \"host\": \"lambda-846800462-us-east-2.elb.amazonaws.com\"," +
			"        \"user-agent\": \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6)\"," +
			"        \"x-amzn-trace-id\": \"Root=1-5bdb40ca-556d8b0c50dc66f0511bf520\"," +
			"        \"x-forwarded-for\": \"72.21.198.66\"," +
			"        \"x-forwarded-port\": \"443\"," +
			"        \"x-forwarded-proto\": \"https\"" +
			"    }," +
			"    \"isBase64Encoded\": false," +
			"    \"body\": \"request_body\"" +
			"}";

	String sampleKinesisEvent = "{" +
			"    \"Records\": [" +
			"        {" +
			"            \"kinesis\": {" +
			"                \"kinesisSchemaVersion\": \"1.0\"," +
			"                \"partitionKey\": \"1\"," +
			"                \"sequenceNumber\": \"49590338271490256608559692538361571095921575989136588898\"," +
			"                \"data\": \"SGVsbG8sIHRoaXMgaXMgYSB0ZXN0Lg==\"," +
			"                \"approximateArrivalTimestamp\": 1545084650.987" +
			"            }," +
			"            \"eventSource\": \"aws:kinesis\"," +
			"            \"eventVersion\": \"1.0\"," +
			"            \"eventID\": \"shardId-000000000006:49590338271490256608559692538361571095921575989136588898\"," +
			"            \"eventName\": \"aws:kinesis:record\"," +
			"            \"invokeIdentityArn\": \"arn:aws:iam::123456789012:role/lambda-role\"," +
			"            \"awsRegion\": \"us-east-2\"," +
			"            \"eventSourceARN\": \"arn:aws:kinesis:us-east-2:123456789012:stream/lambda-stream\"" +
			"        }," +
			"        {" +
			"            \"kinesis\": {" +
			"                \"kinesisSchemaVersion\": \"1.0\"," +
			"                \"partitionKey\": \"1\"," +
			"                \"sequenceNumber\": \"49590338271490256608559692540925702759324208523137515618\"," +
			"                \"data\": \"VGhpcyBpcyBvbmx5IGEgdGVzdC4=\"," +
			"                \"approximateArrivalTimestamp\": 1545084711.166" +
			"            }," +
			"            \"eventSource\": \"aws:kinesis\"," +
			"            \"eventVersion\": \"1.0\"," +
			"            \"eventID\": \"shardId-000000000006:49590338271490256608559692540925702759324208523137515618\"," +
			"            \"eventName\": \"aws:kinesis:record\"," +
			"            \"invokeIdentityArn\": \"arn:aws:iam::123456789012:role/lambda-role\"," +
			"            \"awsRegion\": \"us-east-2\"," +
			"            \"eventSourceARN\": \"arn:aws:kinesis:us-east-2:123456789012:stream/lambda-stream\"" +
			"        }" +
			"    ]" +
			"}";

	@SuppressWarnings("rawtypes")
//	@Test
	public void testLBStringMessageEvent() throws Exception {
		System.setProperty("MAIN_CLASS", GenericConfiguration.class.getName());
		System.setProperty("spring.cloud.function.definition", "echoStringMessage");
		FunctionInvoker invoker = new FunctionInvoker();

		InputStream targetStream = new ByteArrayInputStream(this.sampleLBEvent.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		invoker.handleRequest(targetStream, output, null);

		String result = new String(output.toByteArray(), StandardCharsets.UTF_8);

		ObjectMapper mapper = new ObjectMapper();
		Map responseMap = mapper.readValue(result, Map.class);
		assertThat(responseMap.get("statusCode")).isEqualTo(200);
		assertThat(responseMap.get("statusDescription")).isEqualTo("200 OK");
	}

//	@Test
	public void testKinesisStringMessageEvent() throws Exception {
		System.setProperty("MAIN_CLASS", GenericConfiguration.class.getName());
		System.setProperty("spring.cloud.function.definition", "echoStringMessage");
		FunctionInvoker invoker = new FunctionInvoker();

		InputStream targetStream = new ByteArrayInputStream(this.sampleKinesisEvent.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		invoker.handleRequest(targetStream, output, null);

		String result = new String(output.toByteArray(), StandardCharsets.UTF_8);
		assertThat(result).isEqualTo(this.sampleKinesisEvent);
	}

//	@Test
	public void testKinesisStringEvent() throws Exception {
		System.setProperty("MAIN_CLASS", GenericConfiguration.class.getName());
		System.setProperty("spring.cloud.function.definition", "echoString");
		FunctionInvoker invoker = new FunctionInvoker();

		InputStream targetStream = new ByteArrayInputStream(this.sampleKinesisEvent.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		invoker.handleRequest(targetStream, output, null);

		String result = new String(output.toByteArray(), StandardCharsets.UTF_8);
		System.out.println(result);
		assertThat(result).isEqualTo(this.sampleKinesisEvent);
	}


	@Test
	public void testKinesisEvent() throws Exception {
		System.setProperty("MAIN_CLASS", KinesisConfiguration.class.getName());
		System.setProperty("spring.cloud.function.definition", "echoKinesisEvent");
		FunctionInvoker invoker = new FunctionInvoker();

		InputStream targetStream = new ByteArrayInputStream(this.sampleKinesisEvent.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		invoker.handleRequest(targetStream, output, null);

		String result = new String(output.toByteArray(), StandardCharsets.UTF_8);
		System.out.println(result);
		assertThat(result).contains("\"sequenceNumber\":\"49590338271490256608559692538361571095921575989136588898\"");
	}

	@EnableAutoConfiguration
	@Configuration
	public static class GenericConfiguration {

		@Bean
		public Function<Message<String>, Message<String>> echoStringMessage() {
			return v -> {
				System.out.println("Received: " + v);
				return v;
			};
		}

		@Bean
		public Function<String, String> echoString() {
			return v -> {
				System.out.println("Received: " + v);
				return v;
			};
		}
	}


	@EnableAutoConfiguration
	@Configuration
	public static class KinesisConfiguration {

		@Bean
		public Function<KinesisEvent, KinesisEvent> echoKinesisEvent() {
			return v -> {
				System.out.println("Received: " + v);
				return v;
			};
		}
	}
}
