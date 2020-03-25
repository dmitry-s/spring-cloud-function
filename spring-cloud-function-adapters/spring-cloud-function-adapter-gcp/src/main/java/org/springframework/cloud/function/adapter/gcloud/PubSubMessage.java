package org.springframework.cloud.function.adapter.gcloud;

import java.util.Map;

class PubSubMessage {
	String data;
	Map<String, String> attributes;
	String messageId;
	String publishTime;

	public PubSubMessage(String data, Map<String, String> attributes, String messageId, String publishTime) {
		this.data = data;
		this.attributes = attributes;
		this.messageId = messageId;
		this.publishTime = publishTime;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}
}
