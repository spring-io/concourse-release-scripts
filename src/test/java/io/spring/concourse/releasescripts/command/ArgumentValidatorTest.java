package io.spring.concourse.releasescripts.command;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests for {@link ArgumentValidator}.
 *
 * @author Abel Salgado Romero
 */
public class ArgumentValidatorTest {

	@Test
	public void shouldFailWhenArgumentsAreLessThanRequired() {
		ArgumentValidator argumentValidator = new ArgumentValidator("$ONE", "$TWO", "$THREE");

		Throwable failure = catchThrowable(() -> argumentValidator.validate(Arrays.asList("first", "second")));

		assertThat(failure).isInstanceOf(IllegalArgumentException.class).hasMessage("Missing argument(s): $THREE");
	}

	@Test
	public void shouldNotFailWhenArgumentsAreMoreThanRequired() {
		ArgumentValidator argumentValidator = new ArgumentValidator("one");

		argumentValidator.validate(Arrays.asList("first", "second"));
	}

	@Test
	public void shouldFailAndReportAllMissingArguments() {
		ArgumentValidator argumentValidator = new ArgumentValidator("$ONE", "$TWO", "$THREE");

		Throwable failure = catchThrowable(() -> argumentValidator.validate(Collections.emptyList()));

		assertThat(failure).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Missing argument(s): $ONE, $TWO, $THREE");
	}

}
