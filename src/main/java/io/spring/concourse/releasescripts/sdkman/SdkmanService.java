/*
 * Copyright 2012-2023 the original author or authors.
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

package io.spring.concourse.releasescripts.sdkman;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Central class for interacting with SDKMAN's API.
 *
 * @author Madhura Bhave
 * @author Brian Clozel
 */
@Component
public class SdkmanService {

	private static final Logger logger = LoggerFactory.getLogger(SdkmanService.class);

	private static final String SDKMAN_URL = "https://vendors.sdkman.io/";

	private static final String DOWNLOAD_BASE_URL = "https://repo.maven.apache.org/maven2/";

	private final RestTemplate restTemplate;

	private final SdkmanProperties properties;

	private final String CONSUMER_KEY_HEADER = "Consumer-Key";

	private final String CONSUMER_TOKEN_HEADER = "Consumer-Token";

	public SdkmanService(RestTemplateBuilder builder, SdkmanProperties properties) {
		this.restTemplate = builder.build();
		this.properties = properties;
	}

	public void publish(String version, boolean makeDefault) {
		release(version);
		if (makeDefault) {
			makeDefault(version);
		}
		broadcast(version);
	}

	private void broadcast(String version) {
		String url = this.properties.getBroadcastUrl();
		BroadcastRequest broadcastRequest = new BroadcastRequest(this.properties.getCandidate(), version,
				(url != null) ? String.format(url, version) : null);
		RequestEntity<BroadcastRequest> broadcastEntity = RequestEntity.post(URI.create(SDKMAN_URL + "announce/struct"))
			.header(this.CONSUMER_KEY_HEADER, this.properties.getConsumerKey())
			.header(this.CONSUMER_TOKEN_HEADER, this.properties.getConsumerToken())
			.contentType(MediaType.APPLICATION_JSON)
			.body(broadcastRequest);
		this.restTemplate.exchange(broadcastEntity, String.class);
		logger.debug("Broadcast complete");
	}

	private void makeDefault(String version) {
		logger.debug("Making this version the default");
		Request request = new Request(this.properties.getCandidate(), version);
		RequestEntity<Request> requestEntity = RequestEntity.put(URI.create(SDKMAN_URL + "default"))
			.header(this.CONSUMER_KEY_HEADER, this.properties.getConsumerKey())
			.header(this.CONSUMER_TOKEN_HEADER, this.properties.getConsumerToken())
			.contentType(MediaType.APPLICATION_JSON)
			.body(request);
		this.restTemplate.exchange(requestEntity, String.class);
		logger.debug("Make default complete");
	}

	private void release(String version) {
		Artifact artifact = Artifact.parseCoordinates(this.properties.getArtifact());
		ReleaseRequest releaseRequest = new ReleaseRequest(this.properties.getCandidate(), version,
				DOWNLOAD_BASE_URL + artifact.buildArtifactPath(version));
		RequestEntity<ReleaseRequest> releaseEntity = RequestEntity.post(URI.create(SDKMAN_URL + "release"))
			.header(this.CONSUMER_KEY_HEADER, this.properties.getConsumerKey())
			.header(this.CONSUMER_TOKEN_HEADER, this.properties.getConsumerToken())
			.contentType(MediaType.APPLICATION_JSON)
			.body(releaseRequest);
		this.restTemplate.exchange(releaseEntity, String.class);
		logger.debug("Release complete");
	}

	static class Request {

		private final String candidate;

		private final String version;

		public Request(String candidate, String version) {
			this.candidate = candidate;
			this.version = version;
		}

		public String getCandidate() {
			return this.candidate;
		}

		public String getVersion() {
			return this.version;
		}

	}

	static class ReleaseRequest extends Request {

		private final String url;

		public ReleaseRequest(String candidate, String version, String url) {
			super(candidate, version);
			this.url = url;
		}

		public String getUrl() {
			return this.url;
		}

	}

	static class BroadcastRequest extends Request {

		private final String url;

		public BroadcastRequest(String candidate, String version, String url) {
			super(candidate, version);
			this.url = url;
		}

		public String getUrl() {
			return this.url;
		}

	}

	static class Artifact {

		private final String groupId;

		private final String artifactId;

		private String packaging = "jar";

		private String classifier = "";

		public Artifact(String groupId, String artifactId) {
			this.groupId = groupId;
			this.artifactId = artifactId;
		}

		static Artifact parseCoordinates(String coordinates) {
			String[] split = coordinates.split(":");
			Artifact artifact = new Artifact(split[0], split[1]);
			if (split.length > 2) {
				artifact.setPackaging(split[3]);
			}
			if (split.length > 3) {
				artifact.setClassifier(split[4]);
			}
			return artifact;
		}

		public String getGroupId() {
			return this.groupId;
		}

		public String getArtifactId() {
			return this.artifactId;
		}

		public String getPackaging() {
			return this.packaging;
		}

		public void setPackaging(String packaging) {
			this.packaging = packaging;
		}

		public String getClassifier() {
			return this.classifier;
		}

		public void setClassifier(String classifier) {
			this.classifier = classifier;
		}

		public String buildArtifactPath(String version) {
			StringBuilder builder = new StringBuilder();
			builder.append(this.groupId.replace('.', '/')).append('/');
			builder.append(this.artifactId).append('/').append(version).append('/');
			builder.append(this.artifactId).append('-').append(version);
			if (StringUtils.hasText(this.classifier)) {
				builder.append('-').append(this.classifier);
			}
			builder.append('.').append(this.packaging);
			return builder.toString();
		}

	}

}
