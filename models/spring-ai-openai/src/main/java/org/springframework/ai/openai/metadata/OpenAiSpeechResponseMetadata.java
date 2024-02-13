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

package org.springframework.ai.openai.metadata;

import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.speech.SpeechResponseMetadata;
import org.springframework.ai.openai.api.OpenAiSpeechApi;
import org.springframework.util.Assert;

public class OpenAiSpeechResponseMetadata implements SpeechResponseMetadata {

	RateLimit rateLimits;

	public static OpenAiSpeechResponseMetadata from(OpenAiSpeechApi.OpenAiSpeechResponse openAiSpeechResponse,
			RateLimit rateLimits) {
		Assert.notNull(openAiSpeechResponse, "OpenAiSpeechResponse must not be null");

		return new OpenAiSpeechResponseMetadata(rateLimits);
	}

	protected OpenAiSpeechResponseMetadata(RateLimit rateLimits) {
		this.rateLimits = rateLimits;
	}

	@Override
	public RateLimit getRateLimit() {
		return this.rateLimits;
	}

}
