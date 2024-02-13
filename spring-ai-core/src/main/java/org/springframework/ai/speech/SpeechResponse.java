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

import org.springframework.ai.model.ModelResponse;

import java.util.List;
import java.util.Objects;

public class SpeechResponse implements ModelResponse<SpeechGeneration> {

	private final SpeechResponseMetadata speechResponseMetadata;

	private final SpeechGeneration speechGenerations;

	public SpeechResponse(SpeechGeneration generations) {
		this(generations, SpeechResponseMetadata.NULL);
	}

	public SpeechResponse(SpeechGeneration generations, SpeechResponseMetadata speechResponseMetadata) {
		this.speechResponseMetadata = speechResponseMetadata;
		this.speechGenerations = generations;
	}

	@Override
	public SpeechGeneration getResult() {
		return speechGenerations;
	}

	@Override
	public List<SpeechGeneration> getResults() {
		return List.of(speechGenerations);
	}

	@Override
	public SpeechResponseMetadata getMetadata() {
		return speechResponseMetadata;
	}

	@Override
	public String toString() {
		return "SpeechResponse{" + "speechResponseMetadata=" + speechResponseMetadata + ", speechGenerations="
				+ speechGenerations + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SpeechResponse that))
			return false;
		return Objects.equals(speechResponseMetadata, that.speechResponseMetadata)
				&& Objects.equals(speechGenerations, that.speechGenerations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(speechResponseMetadata, speechGenerations);
	}

}
