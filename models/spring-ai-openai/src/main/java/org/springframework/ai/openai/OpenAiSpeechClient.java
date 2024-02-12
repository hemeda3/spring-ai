package org.springframework.ai.openai;/*
										* Copyright 2023-2023 the original author or authors.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.OpenAiApiException;
import org.springframework.ai.openai.metadata.OpenAiSpeechResponseMetadata;
import org.springframework.ai.openai.metadata.support.OpenAiResponseHeaderExtractor;
import org.springframework.ai.speech.*;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Objects;

/**
 * {@link SpeechClient} implementation for {@literal OpenAI} backed by
 * {@link OpenAiApi}.
 *
 * @author Ahmed Yousri
 * @see SpeechClient
 * @see OpenAiApi
 */
public class OpenAiSpeechClient implements SpeechClient {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private OpenAiSpeechOptions defaultOptions = OpenAiSpeechOptions.builder()
			.withModel("tts-1")
			.withResponseFormat("mp3")
			.withSpeed(1.0f)
			.withVoice("alloy")
			.build();

	public final RetryTemplate retryTemplate = RetryTemplate.builder()
		.maxAttempts(10)
		.retryOn(OpenAiApiException.class)
		.exponentialBackoff(Duration.ofMillis(2000), 5, Duration.ofMillis(3 * 60000))
		.build();

	private final OpenAiApi openAiApi;

	public OpenAiSpeechClient(OpenAiApi openAiApi) {
		Assert.notNull(openAiApi, "OpenAiApi must not be null");
		this.openAiApi = openAiApi;
	}

	public OpenAiSpeechClient withDefaultOptions(OpenAiSpeechOptions options) {
		this.defaultOptions = options;
		return this;
	}

	@Override
	public SpeechResponse call(SpeechRequest request) {

		return this.retryTemplate.execute(ctx -> {

			OpenAiApi.SpeechRequest requestBody = createRequestBody(request);
				ResponseEntity<OpenAiApi.SpeechResponse> SpeechEntity = this.openAiApi
					.textToSpeechEntityJson(requestBody);
				var speech = SpeechEntity.getBody();

				if (speech == null) {
					logger.warn("No speech response returned for speechRequest: {}", request);
				}

				RateLimit rateLimits = OpenAiResponseHeaderExtractor.extractAiResponseHeaders(SpeechEntity);

				return new SpeechResponse(convertResponse(speech),
						new OpenAiSpeechResponseMetadata(rateLimits));


		});
	}

	private OpenAiApi.SpeechRequest createRequestBody(SpeechRequest speechRequest) {

		OpenAiApi.SpeechRequest request = new OpenAiApi.SpeechRequest();

		if (this.defaultOptions != null) {
			request = ModelOptionsUtils.merge(request, this.defaultOptions, OpenAiApi.SpeechRequest.class);
		}

		if (speechRequest.getOptions() != null) {
			if (speechRequest.getOptions() instanceof SpeechOptions runtimeOptions) {
				OpenAiSpeechOptions updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(runtimeOptions,
						SpeechOptions.class, OpenAiSpeechOptions.class);
				request = ModelOptionsUtils.merge(updatedRuntimeOptions, request, OpenAiApi.SpeechRequest.class);
			}
			else {
				throw new IllegalArgumentException("Prompt options are not of type SpeechOptions: "
						+ speechRequest.getOptions().getClass().getSimpleName());
			}
		}

		return request ;
	}

	private Speech convertResponse(OpenAiApi.SpeechResponse speechResponse) {
		return new Speech(Objects.requireNonNull(speechResponse).audio());
	}


}
