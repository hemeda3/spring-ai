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

package org.springframework.ai.speech;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents an audio data structure with functionality to manage audio content.
 */
public class Audio {

	// Audio data
	private byte[] data;

	public static Audio NULL = null;

	/**
	 * Constructs a new Audio instance with the specified data.
	 * @param data the audio data
	 */
	public Audio(byte[] data) {
		this.setData(data);
	}

	/**
	 * Gets the audio data.
	 * @return the audio data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Sets the audio data.
	 * @param data the audio data to set
	 */
	public void setData(byte[] data) {
		this.data = Objects.requireNonNull(data, "Audio data cannot be null");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Audio))
			return false;
		Audio audio = (Audio) o;
		return Objects.equals(data, audio.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	@Override
	public String toString() {
		return "Audio{data=" + Arrays.toString(data) + "}";
	}

}
