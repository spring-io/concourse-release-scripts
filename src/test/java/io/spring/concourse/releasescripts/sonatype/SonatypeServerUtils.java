package io.spring.concourse.releasescripts.sonatype;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import io.spring.concourse.releasescripts.ReleaseInfo;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Test utility for mocking Sonatype server behavior.
 *
 * @author Brian Clozel
 */
public class SonatypeServerUtils {

	public static RequestMatcher requestTestArtifact() {
		return requestTo(String.format(
				"/service/local/repositories/releases/content/org/example/test/test-artifact/%s/test-artifact-%s.jar.sha1",
				"1.1.0.RELEASE", "1.1.0.RELEASE"));
	}

	public static String setupStagingProfile(MockRestServiceServer server) {
		server.expect(requestTo("/service/local/staging/profiles"))
				.andExpect(header("Accept", "application/json, application/*+json"))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(getResource("profiles.json")));
		return "2e3ed47dc2510";
	}

	public static Set<RequestMatcher> generateUploadRequests(Path artifactsRoot, String stagingRepositoryId)
			throws IOException {
		return Files.walk(artifactsRoot).filter(Files::isRegularFile).map(artifactsRoot::relativize)
				.filter((artifact) -> !"build-info.json".equals(artifact.toString()))
				.map((artifact) -> requestTo(
						"/service/local/staging/deployByRepositoryId/" + stagingRepositoryId + "/" + artifact))
				.collect(Collectors.toSet());
	}

	public static String setupStagingRepositoryCreation(MockRestServiceServer server, String stagingProfileId) {
		String stagedRepositoryId = "example-6789";
		server.expect(requestTo("/service/local/staging/profiles/" + stagingProfileId + "/start"))
				.andExpect(method(HttpMethod.POST)).andExpect(header("Content-Type", "application/json"))
				.andExpect(header("Accept", "application/json, application/*+json"))
				.andExpect(jsonPath("$.data.description").value("example-build-1"))
				.andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
						.body("{\"data\":{\"stagedRepositoryId\":\"" + stagedRepositoryId
								+ "\", \"description\":\"example-build\"}}"));
		return stagedRepositoryId;
	}

	public static void attemptFinishStagingRepository(MockRestServiceServer server, String stagingProfileId,
			String stagingRepositoryId, boolean closed) {
		String closeStatus = closed ? "closed" : "open";
		server.expect(requestTo("/service/local/staging/profiles/" + stagingProfileId + "/finish"))
				.andExpect(method(HttpMethod.POST)).andExpect(header("Content-Type", "application/json"))
				.andExpect(header("Accept", "application/json, application/*+json"))
				.andRespond(withStatus(HttpStatus.CREATED));
		server.expect(ExpectedCount.times(2), requestTo("/service/local/staging/repository/" + stagingRepositoryId))
				.andExpect(method(HttpMethod.GET)).andExpect(header("Accept", "application/json, application/*+json"))
				.andRespond(withSuccess().contentType(MediaType.APPLICATION_JSON)
						.body("{\"type\":\"open\", \"transitioning\":true}"));
		server.expect(requestTo("/service/local/staging/repository/" + stagingRepositoryId))
				.andExpect(method(HttpMethod.GET)).andExpect(header("Accept", "application/json, application/*+json"))
				.andRespond(withSuccess().contentType(MediaType.APPLICATION_JSON)
						.body("{\"type\":\"" + closeStatus + "\", \"transitioning\":false}"));
	}

	private static ClassPathResource getResource(String path) {
		return new ClassPathResource(path, SonatypeServerUtils.class);
	}

	public static ReleaseInfo getReleaseInfo() {
		ReleaseInfo releaseInfo = new ReleaseInfo();
		releaseInfo.setBuildName("example-build");
		releaseInfo.setBuildNumber("example-build-1");
		releaseInfo.setVersion("1.1.0.RELEASE");
		releaseInfo.setGroupId("example");
		releaseInfo.setMarkerArtifact(
				ReleaseInfo.MarkerArtifact.fromCoordinates("org.example.test:test-artifact:1.1.0.RELEASE"));
		return releaseInfo;
	}

}
