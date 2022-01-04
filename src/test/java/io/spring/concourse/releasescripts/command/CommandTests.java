package io.spring.concourse.releasescripts.command;

import org.junit.jupiter.api.Test;

import org.springframework.boot.ApplicationArguments;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Command}
 *
 * @author Brian Clozel
 */
class CommandTests {

	@Test
	void shouldRemoveCommandSuffix() {
		TestCommand testCommand = new TestCommand();
		assertThat(testCommand.getName()).isEqualTo("test");
	}

	@Test
	void shouldKeepUpperCaseLettersWithinWord() {
		CamelCaseCommand camelCaseCommand = new CamelCaseCommand();
		assertThat(camelCaseCommand.getName()).isEqualTo("camelCase");
	}

	static class TestCommand implements Command {

		@Override
		public void run(ApplicationArguments args) throws Exception {

		}

	}

	static class CamelCaseCommand implements Command {

		@Override
		public void run(ApplicationArguments args) throws Exception {

		}

	}

}