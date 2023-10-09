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

package io.spring.concourse.releasescripts.artifactory;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} for an Artifactory server.
 *
 * @author Madhura Bhave
 * @author Brian Clozel
 */
@ConfigurationProperties(prefix = "artifactory")
public class ArtifactoryProperties {

	private String url = "https://repo.spring.io";

	private String username;

	private String password;

	private final Repository repository = new Repository();

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

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

	public Repository getRepository() {
		return this.repository;
	}

	public static class Repository {

		/**
		 * Repository name for staging versions
		 */
		private String staging = "libs-staging-local";

		/**
		 * Repository name for milestone versions
		 */
		private String milestone = "libs-milestone-local";

		/**
		 * Repository name for release candidate versions
		 */
		private String releaseCandidate = "libs-milestone-local";

		/**
		 * Repository name for release versions
		 */
		private String release = "libs-release-local";

		public String getStaging() {
			return this.staging;
		}

		public void setStaging(String staging) {
			this.staging = staging;
		}

		public String getMilestone() {
			return this.milestone;
		}

		public void setMilestone(String milestone) {
			this.milestone = milestone;
		}

		public String getReleaseCandidate() {
			return this.releaseCandidate;
		}

		public void setReleaseCandidate(String releaseCandidate) {
			this.releaseCandidate = releaseCandidate;
		}

		public String getRelease() {
			return this.release;
		}

		public void setRelease(String release) {
			this.release = release;
		}

	}

}
