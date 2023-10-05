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

import javax.validation.constraints.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} for SDKMAN.
 *
 * @author Madhura Bhave
 * @author Brian Clozel
 */
@ConfigurationProperties(prefix = "sdkman")
@Validated
public class SdkmanProperties {

	private String consumerKey;

	private String consumerToken;

	private String candidate;

	@Pattern(regexp = "[a-z.]+:[a-z\\-]+:[^:]+(:[a-z.]+(:[a-z]+)?)?")
	private String artifact;

	private String broadcastUrl;

	public String getConsumerKey() {
		return this.consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerToken() {
		return this.consumerToken;
	}

	public void setConsumerToken(String consumerToken) {
		this.consumerToken = consumerToken;
	}

	public String getCandidate() {
		return this.candidate;
	}

	public void setCandidate(String candidate) {
		this.candidate = candidate;
	}

	public String getArtifact() {
		return this.artifact;
	}

	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	public String getBroadcastUrl() {
		return this.broadcastUrl;
	}

	public void setBroadcastUrl(String broadcastUrl) {
		this.broadcastUrl = broadcastUrl;
	}

}
