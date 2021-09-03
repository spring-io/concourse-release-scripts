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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link SonatypeService}.
 *
 * @author Madhura Bhave
 * @author Brian Clozel
 */
@RestClientTest(components = SonatypeService.class,
		properties = { "sonatype.url=https://nexus.example.org", "sonatype.username=spring",
				"sonatype.stagingProfile=org.example", "sonatype.password=secret" })
@EnableConfigurationProperties(SonatypeProperties.class)
class SonatypeServiceTests {

	@Autowired
	private SonatypeService service;

	@Autowired
	private MockRestServiceServer server;

	@AfterEach
	void tearDown() {
		this.server.reset();
	}

	@Test
	void publishWhenAlreadyPublishedShouldNotPublish() {
		this.server.expect(SonatypeServerUtils.requestTestArtifact()).andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess().body("ce8d8b6838ecceb68962b9150b18682f4237ccf71".getBytes()));
		Path artifactsRoot = new File("src/test/resources/io/spring/concourse/releasescripts/sonatype/artifactory-repo")
				.toPath();
		this.service.publish(SonatypeServerUtils.getReleaseInfo(), artifactsRoot);
		this.server.verify();
	}

	@Test
	void publishWithSuccessfulClose() throws IOException {
		this.server.expect(SonatypeServerUtils.requestTestArtifact()).andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));
		String stagingProfileId = SonatypeServerUtils.setupStagingProfile(this.server);
		String stagingRepositoryId = SonatypeServerUtils.setupStagingRepositoryCreation(this.server, stagingProfileId);

		Path artifactsRoot = new File("src/test/resources/io/spring/concourse/releasescripts/sonatype/artifactory-repo")
				.toPath();

		Set<RequestMatcher> uploads = SonatypeServerUtils.generateUploadRequests(artifactsRoot, stagingRepositoryId);

		AnyOfRequestMatcher uploadRequestsMatcher = anyOf(uploads);
		assertThat(uploadRequestsMatcher.getCandidates()).hasSize(150);
		this.server.expect(ExpectedCount.times(150), uploadRequestsMatcher).andExpect(method(HttpMethod.PUT))
				.andRespond(withSuccess());

		SonatypeServerUtils.attemptFinishStagingRepository(server, stagingProfileId, stagingRepositoryId, true);

		this.server.expect(requestTo("/service/local/staging/bulk/promote")).andExpect(method(HttpMethod.POST))
				.andExpect(header("Content-Type", "application/json"))
				.andExpect(header("Accept", "application/json, application/*+json"))
				.andExpect(jsonPath("$.data.description").value("Releasing example-build-1"))
				.andExpect(jsonPath("$.data.autoDropAfterRelease").value(true))
				.andExpect(jsonPath("$.data.stagedRepositoryIds")
						.value(equalTo(Collections.singletonList(stagingRepositoryId))))
				.andRespond(withSuccess());
		this.service.publish(SonatypeServerUtils.getReleaseInfo(), artifactsRoot);
		this.server.verify();
		assertThat(uploadRequestsMatcher.getCandidates()).hasSize(0);
	}

	@Test
	void publishWithCloseFailureDueToRuleViolations() throws IOException {
		this.server.expect(SonatypeServerUtils.requestTestArtifact()).andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));
		String stagingProfileId = SonatypeServerUtils.setupStagingProfile(this.server);
		String stagingRepositoryId = SonatypeServerUtils.setupStagingRepositoryCreation(this.server, stagingProfileId);
		Path artifactsRoot = new File("src/test/resources/io/spring/concourse/releasescripts/sonatype/artifactory-repo")
				.toPath();
		Set<RequestMatcher> uploads = SonatypeServerUtils.generateUploadRequests(artifactsRoot, stagingRepositoryId);

		AnyOfRequestMatcher uploadRequestsMatcher = anyOf(uploads);
		assertThat(uploadRequestsMatcher.getCandidates()).hasSize(150);

		this.server.expect(ExpectedCount.times(150), uploadRequestsMatcher).andExpect(method(HttpMethod.PUT))
				.andRespond(withSuccess());

		SonatypeServerUtils.attemptFinishStagingRepository(server, stagingProfileId, stagingRepositoryId, false);

		this.server.expect(requestTo("/service/local/staging/repository/" + stagingRepositoryId + "/activity"))
				.andExpect(method(HttpMethod.GET)).andExpect(header("Accept", "application/json, application/*+json"))
				.andRespond(withSuccess().contentType(MediaType.APPLICATION_JSON)
						.body(getResource("stagingFailureActivity.json")));
		assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> this.service.publish(SonatypeServerUtils.getReleaseInfo(), artifactsRoot))
				.withMessage("Close failed");
		this.server.verify();
		assertThat(uploadRequestsMatcher.getCandidates()).hasSize(0);
	}

	private static ClassPathResource getResource(String path) {
		return new ClassPathResource(path, SonatypeServiceTests.class);
	}

	private AnyOfRequestMatcher anyOf(Set<RequestMatcher> candidates) {
		return new AnyOfRequestMatcher(candidates);
	}

}
