/*
 * Copyright 2012-2021 the original author or authors.
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

package io.spring.concourse.releasescripts.sonatype;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} for Sonatype.
 *
 * @author Madhura Bhave
 */
@ConfigurationProperties(prefix = "sonatype")
public class SonatypeProperties {

	private String username;

	private String password;

	/**
	 * URL of the Nexus instance used to publish releases.
	 */
	private String url;

	/**
	 * ID of the staging profile used to publish releases.
	 */
	private String stagingProfileId;

	/**
	 * Name of the staging profile used to publish releases.
	 */
	private String stagingProfile;

	/**
	 * Time between requests made to determine if the closing of a staging repository has
	 * completed.
	 */
	private Duration pollingInterval = Duration.ofSeconds(15);

	/**
	 * Number of threads used to upload artifacts to the staging repository.
	 */
	private int uploadThreads = 8;

	/**
	 * Regular expression patterns of artifacts to exclude.
	 */
	private List<String> exclude = Arrays.asList("build-info\\.json");

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Deprecated
	@DeprecatedConfigurationProperty(replacement = "sonatype.stating-profile")
	public String getStagingProfileId() {
		return this.stagingProfileId;
	}

	public void setStagingProfileId(String stagingProfileId) {
		this.stagingProfileId = stagingProfileId;
	}

	public String getStagingProfile() {
		return this.stagingProfile;
	}

	public void setStagingProfile(String stagingProfile) {
		this.stagingProfile = stagingProfile;
	}

	public Duration getPollingInterval() {
		return this.pollingInterval;
	}

	public void setPollingInterval(Duration pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	public int getUploadThreads() {
		return this.uploadThreads;
	}

	public void setUploadThreads(int uploadThreads) {
		this.uploadThreads = uploadThreads;
	}

	public List<String> getExclude() {
		return this.exclude;
	}

	public void setExclude(List<String> exclude) {
		this.exclude = exclude;
	}

}
