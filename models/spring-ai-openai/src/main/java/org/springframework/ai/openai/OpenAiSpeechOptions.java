/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ai.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.speech.SpeechOptions;

/**
 * OpenAI Speech API options. This class encapsulates options for the speech generation
 * request to OpenAI's API, allowing customization of the speech synthesis process.
 *
 * @author Mark Pollack
 * @author Christian Tzolov
 * @since 0.8.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiSpeechOptions implements SpeechOptions {

	/**
	 * The model to use for speech generation. This selects the specific AI model that
	 * OpenAI will use to generate the speech.
	 */
	@JsonProperty("model")
	private String model;

	/**
	 * The text input to generate speech for. This is the text content that will be
	 * converted into speech by the OpenAI service.
	 */
	@JsonProperty("input")
	private String input;

	/**
	 * The voice to use when generating the speech. Specifies the desired voice type or
	 * character for the synthesized speech.
	 */
	@JsonProperty("voice")
	private String voice;

	/**
	 * The format in which the generated speech is returned. Defines the audio format of
	 * the generated speech (e.g., MP3, WAV).
	 */
	@JsonProperty("response_format")
	private String responseFormat;

	/**
	 * The speed of the generated speech. Controls the playback speed of the generated
	 * speech audio.
	 */
	@JsonProperty("speed")
	private Float speed;

	// Builder pattern for constructing OpenAiSpeechOptions instances
	public static class Builder {

		private final OpenAiSpeechOptions options = new OpenAiSpeechOptions();

		public Builder withModel(String model) {
			options.model = model;
			return this;
		}

		public Builder withInput(String input) {
			options.input = input;
			return this;
		}

		public Builder withVoice(String voice) {
			options.voice = voice;
			return this;
		}

		public Builder withResponseFormat(String responseFormat) {
			options.responseFormat = responseFormat;
			return this;
		}

		public Builder withSpeed(Float speed) {
			options.speed = speed;
			return this;
		}

		public OpenAiSpeechOptions build() {
			return options;
		}

	}

	// Factory method to initiate the builder
	public static Builder builder() {
		return new Builder();
	}

	// Getters (and setters if mutable properties are desired)

	public String getModel() {
		return model;
	}

	public String getInput() {
		return input;
	}

	public String getVoice() {
		return voice;
	}

	public String getResponseFormat() {
		return responseFormat;
	}

	public Float getSpeed() {
		return speed;
	}

}
