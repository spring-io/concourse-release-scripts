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

package io.spring.concourse.releasescripts;

import java.util.Arrays;

import io.spring.concourse.releasescripts.artifactory.payload.BuildInfoResponse;

import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Properties corresponding to the release.
 *
 * @author Madhura Bhave
 * @author Brian Clozel
 */
public class ReleaseInfo {

	private String buildName;

	private String buildNumber;

	private String groupId;

	private String version;

	private MarkerArtifact markerArtifact;

	public static ReleaseInfo from(BuildInfoResponse.BuildInfo buildInfo) {
		ReleaseInfo info = new ReleaseInfo();
		PropertyMapper propertyMapper = PropertyMapper.get();
		propertyMapper.from(buildInfo.getName()).to(info::setBuildName);
		propertyMapper.from(buildInfo.getNumber()).to(info::setBuildNumber);
		String[] moduleInfo = StringUtils.delimitedListToStringArray(buildInfo.getModules()[0].getId(), ":");
		propertyMapper.from(moduleInfo[0]).to(info::setGroupId);
		propertyMapper.from(moduleInfo[2]).to(info::setVersion);
		String markerArtifact = Arrays.stream(buildInfo.getModules())
			.filter(module -> Arrays.stream(module.getArtifacts())
				.anyMatch(artifact -> artifact.getType().equals("jar")))
			.map(BuildInfoResponse.Module::getId)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException(
					"Could not find Jar module in build info: " + buildInfo.getNumber()));
		info.setMarkerArtifact(MarkerArtifact.fromCoordinates(markerArtifact));
		return info;
	}

	public String getBuildName() {
		return this.buildName;
	}

	public void setBuildName(String buildName) {
		this.buildName = buildName;
	}

	public String getBuildNumber() {
		return this.buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public MarkerArtifact getMarkerArtifact() {
		return markerArtifact;
	}

	public void setMarkerArtifact(MarkerArtifact markerArtifact) {
		this.markerArtifact = markerArtifact;
	}

	public static class MarkerArtifact {

		private final String groupId;

		private final String artifactId;

		private final String version;

		public static MarkerArtifact fromCoordinates(String coordinates) {
			String[] split = coordinates.split(":");
			Assert.state(split.length == 3, "Invalid artifact coordinates: " + coordinates);
			String groupId = split[0];
			String artifactId = split[1];
			String version = split[2];
			return new MarkerArtifact(groupId, artifactId, version);
		}

		MarkerArtifact(String groupId, String artifactId, String version) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
		}

		public String getGroupId() {
			return this.groupId;
		}

		public String getArtifactId() {
			return this.artifactId;
		}

		public String getVersion() {
			return this.version;
		}

		@Override
		public String toString() {
			return "MarkerArtifact{" + this.groupId + ':' + this.artifactId + ':' + this.version + "}";
		}

	}

}
