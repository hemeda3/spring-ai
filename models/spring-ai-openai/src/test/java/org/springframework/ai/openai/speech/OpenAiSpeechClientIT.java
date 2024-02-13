/*
 * Copyright 2024-2024 the original author or authors.
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
package org.springframework.ai.openai.speech;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.openai.OpenAiTestConfiguration;
import org.springframework.ai.openai.testutils.AbstractIT;
import org.springframework.ai.speech.*;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = OpenAiTestConfiguration.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class OpenAiSpeechClientIT extends AbstractIT {

	@Test
	void speechAsUrlTest() {
		var optionsSpeech = SpeechOptionsBuilder.builder()
			.withModel("tts-1")
			.withSpeed(1.0f)
			.withVoice("alloy")
			.build();

		// EMPTY PROMPT
		var instructions = """
				""";

		SpeechPrompt speechPrompt = new SpeechPrompt(instructions, optionsSpeech);

		SpeechResponse speechResponse = openaiSpeechClient.call(speechPrompt);

		assertThat(speechResponse.getResults()).hasSize(1);

		SpeechResponseMetadata speechResponseMetadata = speechResponse.getMetadata();
		assertThat(speechResponseMetadata).isNotNull();

		var generation = speechResponse.getResult();
		Audio audio = generation.getOutput();

		System.out.println(audio);
		assertThat(audio.getData()).isNotEmpty();
		assertThat(audio.getData()).hasSizeGreaterThan(0);

	}

	@Test()
	@DisplayName("IllegalArgumentException: Prompt cannot be empty")
	void speechWithEmptyPromptThrow() {
		var optionsSpeech = SpeechOptionsBuilder.builder()
			.withModel("tts-1")
			.withSpeed(1.0f)
			.withVoice("alloy")
			.build();
		var instructions = """
				""";

		SpeechPrompt speechPrompt = new SpeechPrompt(instructions, optionsSpeech);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			openaiSpeechClient.call(speechPrompt);
		});
		assertThat(exception.getMessage()).isEqualTo("Prompt cannot be empty.");

	}

}
