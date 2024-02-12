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

package org.springframework.ai.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.speech.SpeechOptions;

/**
 * @author Ahmed Yousri
 */
@JsonInclude(Include.NON_NULL)
public class OpenAiSpeechOptions implements SpeechOptions {

	// @formatter:off
	/**
	 *
	 * One of the available TTS models
	 */
	private @JsonProperty("model") String model;
	/**
	 *
	 * The text to generate audio for.
	 */
	private @JsonProperty("input") String input;

	/**
	 * The voice to use when generating the audio.
	 */
	private @JsonProperty("voice") String voice;

	/**
	 * The format to audio in.
	 */
	private @JsonProperty("response_format") String responseFormat;

	/**
	 * The speed of the generated audi.
	 */
	private @JsonProperty("speed") Float speed;


	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		protected OpenAiSpeechOptions options;

		public Builder() {
			this.options = new OpenAiSpeechOptions();
		}

		public Builder(OpenAiSpeechOptions options) {
			this.options = options;
		}

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
			return this.options;
		}

	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getVoice() {
		return voice;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}

	public String getResponseFormat() {
		return responseFormat;
	}

	public void setResponseFormat(String responseFormat) {
		this.responseFormat = responseFormat;
	}

	public Float getSpeed() {
		return speed;
	}

	public void setSpeed(Float speed) {
		this.speed = speed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result + ((voice == null) ? 0 : voice.hashCode());
		result = prime * result + ((responseFormat == null) ? 0 : responseFormat.hashCode());
		result = prime * result + ((speed == null) ? 0 : speed.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OpenAiSpeechOptions other = (OpenAiSpeechOptions) obj;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		if (voice == null) {
			if (other.voice != null)
				return false;
		} else if (!voice.equals(other.voice))
			return false;
		if (responseFormat == null) {
			if (other.responseFormat != null)
				return false;
		} else if (!responseFormat.equals(other.responseFormat))
			return false;
		if (speed == null) {
			if (other.speed != null)
				return false;
		} else if (!speed.equals(other.speed))
			return false;
		return true;
	}

}
