package io.spring.concourse.releasescripts.sonatype;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.springframework.http.client.ClientHttpRequest;
import org.springframework.test.web.client.RequestMatcher;

class AnyOfRequestMatcher implements RequestMatcher {

	private final Object monitor = new Object();

	private final Set<RequestMatcher> candidates;

	AnyOfRequestMatcher(Set<RequestMatcher> candidates) {
		this.candidates = candidates;
	}

	@Override
	public void match(ClientHttpRequest request) throws IOException, AssertionError {
		synchronized (this.monitor) {
			Iterator<RequestMatcher> iterator = this.candidates.iterator();
			while (iterator.hasNext()) {
				try {
					iterator.next().match(request);
					iterator.remove();
					return;
				}
				catch (AssertionError ex) {
					// Continue
				}
			}
			throw new AssertionError("No matching request matcher was found for request to '" + request.getURI() + "'");
		}
	}

	public Set<RequestMatcher> getCandidates() {
		return this.candidates;
	}

}
