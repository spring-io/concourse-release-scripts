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

import java.util.Base64;
import java.util.stream.Stream;

import io.spring.concourse.releasescripts.ReleaseInfo;
import io.spring.concourse.releasescripts.ReleaseType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Base class for {@link ArtifactoryService} tests.
 *
 * @author Madhura Bhave
 * @author Brian Clozel
 */
@RestClientTest(value = ArtifactoryService.class, properties = "artifactory.url=https://repo.spring.io")
@EnableConfigurationProperties(ArtifactoryProperties.class)
abstract class AbstractArtifactoryServiceTests {

	private final String urlSuffix;

	@Autowired
	private ArtifactoryService service;

	@Autowired
	private ArtifactoryProperties properties;

	@Autowired
	private MockRestServiceServer server;

	AbstractArtifactoryServiceTests(String urlSuffix) {
		this.urlSuffix = urlSuffix;
	}

	@AfterEach
	void tearDown() {
		this.server.reset();
	}

	@ParameterizedTest
	@MethodSource("promotionArguments")
	void promoteWhenSuccessful(ReleaseType releaseType, String targetRepository) {
		this.server
			.expect(requestTo(
					"https://repo.spring.io/api/build/promote/example-build/example-build-1" + this.urlSuffix))
			.andExpect(method(HttpMethod.POST))
			.andExpect(
					content().json("{\"status\": \"staged\", \"sourceRepo\": \"libs-staging-local\", \"targetRepo\": \""
							+ targetRepository + "\"}"))
			.andExpect(header("Authorization", "Basic " + Base64.getEncoder()
				.encodeToString(String.format("%s:%s", this.properties.getUsername(), this.properties.getPassword())
					.getBytes())))
			.andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
			.andRespond(withSuccess());
		this.service.promote(releaseType, getReleaseInfo());
		this.server.verify();
	}

	static Stream<Arguments> promotionArguments() {
		return Stream.of(Arguments.of(ReleaseType.MILESTONE, "libs-milestone-local"),
				Arguments.of(ReleaseType.RELEASE_CANDIDATE, "libs-milestone-local"),
				Arguments.of(ReleaseType.RELEASE, "libs-release-local"));
	}

	@Test
	void promoteWhenArtifactsAlreadyPromoted() {
		this.server
			.expect(requestTo(
					"https://repo.spring.io/api/build/promote/example-build/example-build-1" + this.urlSuffix))
			.andRespond(withStatus(HttpStatus.CONFLICT));
		this.server.expect(requestTo("https://repo.spring.io/api/build/example-build/example-build-1" + this.urlSuffix))
			.andRespond(withJsonFrom("build-info-response.json"));
		this.service.promote(ReleaseType.RELEASE, getReleaseInfo());
		this.server.verify();
	}

	@Test
	void promoteWhenCheckForArtifactsAlreadyPromotedForbidden() {
		this.server
			.expect(requestTo(
					"https://repo.spring.io/api/build/promote/example-build/example-build-1" + this.urlSuffix))
			.andRespond(withStatus(HttpStatus.CONFLICT));
		this.server.expect(requestTo("https://repo.spring.io/api/build/example-build/example-build-1" + this.urlSuffix))
			.andRespond(withStatus(HttpStatus.FORBIDDEN));
		assertThatExceptionOfType(HttpClientErrorException.class)
			.isThrownBy(() -> this.service.promote(ReleaseType.RELEASE, getReleaseInfo()));
		this.server.verify();
	}

	@Test
	void promoteWhenCheckForArtifactsAlreadyPromotedMissingStatuses() {
		this.server
			.expect(requestTo(
					"https://repo.spring.io/api/build/promote/example-build/example-build-1" + this.urlSuffix))
			.andRespond(withStatus(HttpStatus.CONFLICT));
		this.server.expect(requestTo("https://repo.spring.io/api/build/example-build/example-build-1" + this.urlSuffix))
			.andRespond(withJsonFrom("not-staged-build-info-response.json"));
		assertThatExceptionOfType(HttpClientErrorException.class)
			.isThrownBy(() -> this.service.promote(ReleaseType.RELEASE, getReleaseInfo()));
		this.server.verify();
	}

	@Test
	void promoteWhenPromotionFails() {
		this.server
			.expect(requestTo(
					"https://repo.spring.io/api/build/promote/example-build/example-build-1" + this.urlSuffix))
			.andRespond(withStatus(HttpStatus.CONFLICT));
		this.server.expect(requestTo("https://repo.spring.io/api/build/example-build/example-build-1" + this.urlSuffix))
			.andRespond(withJsonFrom("staged-build-info-response.json"));
		assertThatExceptionOfType(HttpClientErrorException.class)
			.isThrownBy(() -> this.service.promote(ReleaseType.RELEASE, getReleaseInfo()));
		this.server.verify();
	}

	private ReleaseInfo getReleaseInfo() {
		ReleaseInfo releaseInfo = new ReleaseInfo();
		releaseInfo.setBuildName("example-build");
		releaseInfo.setBuildNumber("example-build-1");
		return releaseInfo;
	}

	private DefaultResponseCreator withJsonFrom(String path) {
		return withSuccess(getClassPathResource(path), MediaType.APPLICATION_JSON);
	}

	private ClassPathResource getClassPathResource(String path) {
		return new ClassPathResource(path, getClass());
	}

}