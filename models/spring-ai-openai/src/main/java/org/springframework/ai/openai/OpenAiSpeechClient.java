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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiSpeechApi;
import org.springframework.ai.openai.metadata.OpenAiSpeechResponseMetadata;
import org.springframework.ai.openai.metadata.support.OpenAiResponseHeaderExtractor;
import org.springframework.ai.speech.*;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * OpenAiSpeechClient is a class that implements the SpeechClient interface. It provides a
 * client for calling the OpenAI speech generation API.
 *
 * @author Mark Pollack
 * @author Christian Tzolov
 * @since 0.8.0
 */
public class OpenAiSpeechClient implements SpeechClient {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private OpenAiSpeechOptions defaultOptions;

	private final OpenAiSpeechApi openAiSpeechApi;

	public final RetryTemplate retryTemplate = RetryTemplate.builder()
		.maxAttempts(10)
		.retryOn(OpenAiApi.OpenAiApiException.class)
		.exponentialBackoff(Duration.ofMillis(2000), 5, Duration.ofMillis(3 * 60000))
		.withListener(new RetryListener() {
			public <T extends Object, E extends Throwable> void onError(RetryContext context,
					RetryCallback<T, E> callback, Throwable throwable) {
				logger.warn("Retry error. Retry count:" + context.getRetryCount(), throwable);
			};
		})
		.build();

	public OpenAiSpeechClient(OpenAiSpeechApi openAiSpeechApi) {
		Assert.notNull(openAiSpeechApi, "OpenAiSpeechApi must not be null");
		this.openAiSpeechApi = openAiSpeechApi;
	}

	public OpenAiSpeechOptions getDefaultOptions() {
		return this.defaultOptions;
	}

	public OpenAiSpeechClient withDefaultOptions(OpenAiSpeechOptions defaultOptions) {
		this.defaultOptions = defaultOptions;
		return this;
	}

	@Override
	public SpeechResponse call(SpeechPrompt speechPrompt) {
		return this.retryTemplate.execute(ctx -> {

			String instructions = speechPrompt.getInstructions().get(0).getText();

			OpenAiSpeechApi.OpenAiSpeechRequest speechRequest = new OpenAiSpeechApi.OpenAiSpeechRequest(instructions,
					OpenAiSpeechApi.DEFAULT_SPEECH_MODEL);

			if (this.defaultOptions != null) {
				speechRequest = ModelOptionsUtils.merge(this.defaultOptions, speechRequest,
						OpenAiSpeechApi.OpenAiSpeechRequest.class);
			}

			if (speechPrompt.getOptions() != null) {
				speechRequest = ModelOptionsUtils.merge(toOpenAiSpeechOptions(speechPrompt.getOptions()), speechRequest,
						OpenAiSpeechApi.OpenAiSpeechRequest.class);
			}

			// Make the request
			ResponseEntity<OpenAiSpeechApi.OpenAiSpeechResponse> speechResponseEntity = this.openAiSpeechApi
				.createSpeech(speechRequest);

			RateLimit rateLimits = OpenAiResponseHeaderExtractor.extractAiResponseHeaders(speechResponseEntity);

			return convertResponse(speechResponseEntity, speechRequest, rateLimits);
		});
	}

	private SpeechResponse convertResponse(ResponseEntity<OpenAiSpeechApi.OpenAiSpeechResponse> speechResponseEntity,
			OpenAiSpeechApi.OpenAiSpeechRequest openAiSpeechRequest, RateLimit rateLimits) {
		OpenAiSpeechApi.OpenAiSpeechResponse speechApiResponse = speechResponseEntity.getBody();
		if (speechApiResponse == null) {
			logger.warn("No speech response returned for request: {}", openAiSpeechRequest);
			return new SpeechResponse(SpeechGeneration.NULL);
		}

		var speechGeneration = new SpeechGeneration(new Audio(speechApiResponse.data().data()));
		SpeechResponseMetadata openAiSpeechResponseMetadata = OpenAiSpeechResponseMetadata.from(speechApiResponse,
				rateLimits);
		return new SpeechResponse(speechGeneration, openAiSpeechResponseMetadata);
	}

	/**
	 * Convert the {@link SpeechOptions} into {@link OpenAiSpeechOptions}.
	 * @param defaultOptions the speech options to use.
	 * @return the converted {@link OpenAiSpeechOptions}.
	 */
	private OpenAiSpeechOptions toOpenAiSpeechOptions(SpeechOptions runtimeSpeechOptions) {
		OpenAiSpeechOptions.Builder openAiSpeechOptionsBuilder = OpenAiSpeechOptions.builder();
		if (runtimeSpeechOptions != null) {
			// Handle portable speech options
			if (runtimeSpeechOptions.getModel() != null) {
				openAiSpeechOptionsBuilder.withModel(runtimeSpeechOptions.getModel());
			}
			if (runtimeSpeechOptions.getVoice() != null) {
				openAiSpeechOptionsBuilder.withVoice(runtimeSpeechOptions.getVoice());
			}
			if (runtimeSpeechOptions.getSpeed() != null) {
				openAiSpeechOptionsBuilder.withSpeed(runtimeSpeechOptions.getSpeed());
			}
			if (runtimeSpeechOptions.getResponseFormat() != null) {
				openAiSpeechOptionsBuilder.withResponseFormat(runtimeSpeechOptions.getResponseFormat());
			}

			// Handle OpenAI specific speech options
			if (runtimeSpeechOptions instanceof OpenAiSpeechOptions) {
				OpenAiSpeechOptions runtimeOpenAiSpeechOptions = (OpenAiSpeechOptions) runtimeSpeechOptions;
				if (runtimeOpenAiSpeechOptions.getInput() != null) {
					openAiSpeechOptionsBuilder.withInput(runtimeOpenAiSpeechOptions.getInput());
				}
			}
		}
		return openAiSpeechOptionsBuilder.build();
	}

}
