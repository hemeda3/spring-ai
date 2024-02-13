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

package org.springframework.ai.speech;

public class SpeechOptionsBuilder {

	private class SpeechModelOptionsImpl implements SpeechOptions {

		private String model;

		private String voice;

		private String responseFormat;

		private Float speed;

		@Override
		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		@Override
		public String getVoice() {
			return voice;
		}

		public void setVoice(String voice) {
			this.voice = voice;
		}

		@Override
		public String getResponseFormat() {
			return responseFormat;
		}

		public void setResponseFormat(String responseFormat) {
			this.responseFormat = responseFormat;
		}

		@Override
		public Float getSpeed() {
			return speed;
		}

		public void setSpeed(Float speed) {
			this.speed = speed;
		}

	}

	private final SpeechModelOptionsImpl options = new SpeechModelOptionsImpl();

	private SpeechOptionsBuilder() {
	}

	public static SpeechOptionsBuilder builder() {
		return new SpeechOptionsBuilder();
	}

	public SpeechOptionsBuilder withModel(String model) {
		options.setModel(model);
		return this;
	}

	public SpeechOptionsBuilder withVoice(String voice) {
		options.setVoice(voice);
		return this;
	}

	public SpeechOptionsBuilder withResponseFormat(String responseFormat) {
		options.setResponseFormat(responseFormat);
		return this;
	}

	public SpeechOptionsBuilder withSpeed(Float speed) {
		options.setSpeed(speed);
		return this;
	}

	public SpeechOptions build() {
		return options;
	}

}