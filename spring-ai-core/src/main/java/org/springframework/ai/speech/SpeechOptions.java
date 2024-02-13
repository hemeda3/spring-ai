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

import org.springframework.ai.model.ModelOptions;

public interface SpeechOptions extends ModelOptions {

	/**
	 * Gets the model used for speech generation. This method returns the identifier of
	 * the AI model that processes the speech synthesis request.
	 * @return the model identifier
	 */
	String getModel();

	/**
	 * Gets the voice selected for speech synthesis. This method returns the voice type or
	 * character used in the synthesized speech.
	 * @return the voice identifier
	 */
	String getVoice();

	/**
	 * Gets the format of the generated speech. This method specifies the format in which
	 * the synthesized speech is delivered (e.g., MP3, WAV).
	 * @return the response format
	 */
	String getResponseFormat();

	/**
	 * Gets the speed of the generated speech. This method controls the playback speed of
	 * the synthesized speech.
	 * @return the speed of the speech
	 */
	Float getSpeed();

}
