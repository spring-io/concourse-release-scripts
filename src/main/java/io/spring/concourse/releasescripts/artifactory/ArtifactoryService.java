/*
 * Copyright 2012-2024 the original author or authors.
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

import io.spring.concourse.releasescripts.ReleaseInfo;
import io.spring.concourse.releasescripts.ReleaseType;
import io.spring.concourse.releasescripts.artifactory.payload.BuildInfoResponse;
import io.spring.concourse.releasescripts.artifactory.payload.BuildInfoResponse.Status;
import io.spring.concourse.releasescripts.artifactory.payload.PromotionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Central class for interacting with Artifactory's REST API.
 *
 * @author Madhura Bhave
 * @author Brian Clozel
 */
@Component
public class ArtifactoryService {

	private static final Logger logger = LoggerFactory.getLogger(ArtifactoryService.class);

	private static final String PROMOTION_URL = "/api/build/promote/";

	private static final String BUILD_INFO_URL = "/api/build/";

	private final String rootUri;

	private final Repositories repositories;

	private final RestTemplate restTemplate;

	private final String project;

	public ArtifactoryService(RestTemplateBuilder builder, ArtifactoryProperties artifactoryProperties) {
		String username = artifactoryProperties.getUsername();
		String password = artifactoryProperties.getPassword();
		if (StringUtils.hasLength(username)) {
			builder = builder.basicAuthentication(username, password);
		}
		this.rootUri = artifactoryProperties.getUrl();
		this.project = artifactoryProperties.getProject();
		ArtifactoryProperties.Repository repository = artifactoryProperties.getRepository();
		this.repositories = new Repositories(repository.getStaging(), repository.getMilestone(),
				repository.getReleaseCandidate(), repository.getRelease());
		this.restTemplate = builder.build();
	}

	/**
	 * Move artifacts to a target repository in Artifactory.
	 * @param releaseType the release type
	 * @param releaseInfo the release information
	 */
	public void promote(ReleaseType releaseType, ReleaseInfo releaseInfo) {
		PromotionRequest request = getPromotionRequest(this.repositories.forReleaseType(releaseType));
		String buildName = releaseInfo.getBuildName();
		String buildNumber = releaseInfo.getBuildNumber();
		logger.info("Promoting " + buildName + "/" + buildNumber + " to " + request.getTargetRepo());
		RequestEntity<PromotionRequest> requestEntity = RequestEntity
			.post(this.rootUri + PROMOTION_URL + buildName + "/" + buildNumber
					+ ((this.project != null) ? ("?project=" + this.project) : ""))
			.contentType(MediaType.APPLICATION_JSON)
			.body(request);
		try {
			this.restTemplate.exchange(requestEntity, String.class);
			logger.debug("Promotion complete");
		}
		catch (HttpClientErrorException ex) {
			boolean isAlreadyPromoted = isAlreadyPromoted(buildName, buildNumber, request.getTargetRepo());
			if (isAlreadyPromoted) {
				logger.info("Already promoted.");
			}
			else {
				logger.info("Promotion failed.");
				throw ex;
			}
		}
	}

	private boolean isAlreadyPromoted(String buildName, String buildNumber, String targetRepo) {
		try {
			logger.debug("Checking if already promoted");
			ResponseEntity<BuildInfoResponse> entity = this.restTemplate
				.getForEntity(
						this.rootUri + BUILD_INFO_URL + buildName + "/" + buildNumber
								+ ((this.project != null) ? ("?project=" + this.project) : ""),
						BuildInfoResponse.class);
			Status[] statuses = entity.getBody().getBuildInfo().getStatuses();
			Status status = (statuses != null) ? statuses[0] : null;
			if (status == null) {
				logger.debug("Returned no status object");
				return false;
			}
			logger.debug("Returned repository " + status.getRepository() + " expecting " + targetRepo);
			return status.getRepository().equals(targetRepo);
		}
		catch (HttpClientErrorException ex) {
			logger.debug("Client error, assuming not promoted");
			return false;
		}
	}

	private PromotionRequest getPromotionRequest(String targetRepo) {
		return new PromotionRequest("staged", this.repositories.staging(), targetRepo);
	}

	private record Repositories(String staging, String milestone, String releaseCandidate, String release) {

		String forReleaseType(ReleaseType releaseType) {
			return switch (releaseType) {
				case MILESTONE -> milestone;
				case RELEASE_CANDIDATE -> releaseCandidate;
				case RELEASE -> release;
			};
		}
	}

}
