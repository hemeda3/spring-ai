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

import org.springframework.ai.model.ModelResult;

public class SpeechGeneration implements ModelResult<Audio> {

	private SpeechGenerationMetadata speechGenerationMetadata;

	public static SpeechGeneration NULL = new SpeechGeneration(Audio.NULL) {
	};

	private final Audio audio;

	public SpeechGeneration(Audio audio) {
		this.audio = audio;
	}

	public SpeechGeneration(Audio audio, SpeechGenerationMetadata speechGenerationMetadata) {
		this.audio = audio;
		this.speechGenerationMetadata = speechGenerationMetadata;
	}

	@Override
	public Audio getOutput() {
		return audio;
	}

	@Override
	public SpeechGenerationMetadata getMetadata() {
		return speechGenerationMetadata;
	}

	@Override
	public String toString() {
		return "SpeechGeneration{" + "speechGenerationMetadata=" + speechGenerationMetadata + ", audio=" + audio + '}';
	}

}
