/*
 * Copyright 2023 the original author or authors.
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
import org.springframework.ai.speech.metadata.SpeechMetadata;
import org.springframework.lang.Nullable;

import java.util.Objects;


public class Speech implements ModelResult<byte[]> {

	private byte[] audio;

	private SpeechMetadata speechMetadata;

	public Speech(byte[] audio) {
		this.audio = audio;
	}

	@Override
	public byte[] getOutput() {
		return this.audio;
	}

	@Override
	public SpeechMetadata getMetadata() {
		SpeechMetadata chatGenerationMetadata = this.speechMetadata;
		return speechMetadata != null ? speechMetadata : SpeechMetadata.NULL;
	}

	public Speech withSpeechMetadata(@Nullable SpeechMetadata speechMetadata) {
		this.speechMetadata = speechMetadata;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Speech that))
			return false;
		return Objects.equals(audio, that.audio) && Objects.equals(speechMetadata, that.speechMetadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(audio, speechMetadata);
	}

	@Override
	public String toString() {
		return "Speech{" + "text=" + audio + ", speechMetadata=" + speechMetadata + '}';
	}

}
