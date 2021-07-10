package io.spring.concourse.releasescripts.command;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Simple argument validator. It expects arguments to be in a fixed order and position.
 *
 * @author Andy Wilkinson
 */
public class ArgumentValidator {

	private final List<String> expectedArguments;

	public ArgumentValidator(String... expectedArgumentsNames) {
		Assert.notEmpty(expectedArgumentsNames, "arguments must not be null or empty");
		this.expectedArguments = Arrays.asList(expectedArgumentsNames);
	}

	public void validate(List<String> args) {
		if (args.size() < expectedArguments.size() && args.size() != expectedArguments.size()) {
			throw new IllegalArgumentException("Missing argument(s): " + joinFrom(expectedArguments, args.size()));
		}
	}

	private String joinFrom(List<String> expectedArguments, int from) {
		StringJoiner stringJoiner = new StringJoiner(", ");
		expectedArguments.subList(from, expectedArguments.size()).forEach(stringJoiner::add);
		return stringJoiner.toString();
	}

}
