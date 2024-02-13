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

import org.springframework.ai.model.ModelRequest;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SpeechPrompt implements ModelRequest<List<SpeechMessage>> {

	private final List<SpeechMessage> messages;

	private SpeechOptions speechModelOptions;

	public SpeechPrompt(List<SpeechMessage> messages) {
		this.messages = messages;
	}

	public SpeechPrompt(List<SpeechMessage> messages, SpeechOptions speechModelOptions) {
		this.messages = messages;
		this.speechModelOptions = speechModelOptions;
	}

	public SpeechPrompt(SpeechMessage speechMessage, SpeechOptions speechOptions) {
		this(Collections.singletonList(speechMessage), speechOptions);
	}

	public SpeechPrompt(String instructions, SpeechOptions speechOptions) {
		this(new SpeechMessage(instructions), speechOptions);
	}

	public SpeechPrompt(String instructions) {
		this(new SpeechMessage(instructions), SpeechOptionsBuilder.builder().build());
	}

	@Override
	public List<SpeechMessage> getInstructions() {
		return messages;
	}

	@Override
	public SpeechOptions getOptions() {
		return speechModelOptions;
	}

	@Override
	public String toString() {
		return "NewSpeechPrompt{" + "messages=" + messages + ", speechModelOptions=" + speechModelOptions + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SpeechPrompt that))
			return false;
		return Objects.equals(messages, that.messages) && Objects.equals(speechModelOptions, that.speechModelOptions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(messages, speechModelOptions);
	}

}
