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

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;

/**
 * Tests for {@link ArtifactoryService} with a project.
 *
 * @author Madhura Bhave
 * @author Brian Clozel
 */
@RestClientTest(value = ArtifactoryService.class,
		properties = { "artifactory.url=https://repo.spring.io", "artifactory.project=test" })
@EnableConfigurationProperties(ArtifactoryProperties.class)
class ArtifactoryServiceWithProjectTests extends AbstractArtifactoryServiceTests {

	ArtifactoryServiceWithProjectTests() {
		super("?project=test");
	}

}