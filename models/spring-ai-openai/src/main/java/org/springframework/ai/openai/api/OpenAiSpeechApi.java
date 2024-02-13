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
package org.springframework.ai.openai.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.openai.api.OpenAiApi.OpenAiApiClientErrorException;
import org.springframework.ai.openai.api.OpenAiApi.OpenAiApiException;
import org.springframework.ai.openai.api.OpenAiApi.ResponseError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * OpenAI Speech API.
 *
 * @see <a href=
 * "https://platform.openai.com/docs/api-reference/audio">https://platform.openai.com/docs/api-reference/audio</a>
 */
public class OpenAiSpeechApi {

	private static final String DEFAULT_BASE_URL = "https://api.openai.com";

	public static final String DEFAULT_SPEECH_MODEL = "tts-1";

	private final RestClient restClient;

	private final ObjectMapper objectMapper;

	/**
	 * Create a new OpenAI Audio api with base URL set to https://api.openai.com
	 * @param openAiToken OpenAI apiKey.
	 */
	public OpenAiSpeechApi(String openAiToken) {
		this(DEFAULT_BASE_URL, openAiToken, RestClient.builder());
	}

	public OpenAiSpeechApi(String baseUrl, String openAiToken, RestClient.Builder restClientBuilder) {

		this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Consumer<HttpHeaders> jsonContentHeaders = headers -> {
			headers.setBearerAuth(openAiToken);
			headers.setContentType(MediaType.APPLICATION_JSON);
		};

		var responseErrorHandler = new ResponseErrorHandler() {

			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				return response.getStatusCode().isError();
			}

			@Override
			public void handleError(ClientHttpResponse response) throws IOException {
				if (response.getStatusCode().isError()) {
					if (response.getStatusCode().is4xxClientError()) {
						throw new OpenAiApiClientErrorException(String.format("%s - %s",
								response.getStatusCode().value(),
								OpenAiSpeechApi.this.objectMapper.readValue(response.getBody(), ResponseError.class)));
					}
					throw new OpenAiApiException(String.format("%s - %s", response.getStatusCode().value(),
							OpenAiSpeechApi.this.objectMapper.readValue(response.getBody(), ResponseError.class)));
				}
			}
		};

		this.restClient = restClientBuilder.baseUrl(baseUrl)
			.defaultHeaders(jsonContentHeaders)
			.defaultStatusHandler(responseErrorHandler)
			.build();
	}

	// @formatter:off
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record OpenAiSpeechRequest (
		@JsonProperty("input") String prompt,
		@JsonProperty("model") String model,
		@JsonProperty("voice") String voice,
		@JsonProperty("response_format") String responseFormat,
		@JsonProperty("speed") Float speed)

	{

		public OpenAiSpeechRequest(String prompt, String model) {
			this(prompt, model, null, null, null);
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Data(
			@JsonProperty("data") byte[] data,
			@JsonProperty("revised_prompt") String revisedPrompt) {
	}
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record OpenAiSpeechResponse(
			@JsonProperty("data") Data data){
	}
	// @formatter:onn

	public ResponseEntity<OpenAiSpeechResponse> createSpeech(OpenAiSpeechRequest openAiSpeechRequest) {
		Assert.notNull(openAiSpeechRequest, "Speech request cannot be null.");
		Assert.hasLength(openAiSpeechRequest.prompt(), "Prompt cannot be empty.");

		var responseEntity = this.restClient.post()
			.uri("v1/audio/speech")
			.body(openAiSpeechRequest)
			.accept(MediaType.APPLICATION_OCTET_STREAM)
			.retrieve()
			.toEntity(byte[].class);

		HttpHeaders headers = new HttpHeaders();
		headers.addAll(responseEntity.getHeaders());
		OpenAiSpeechResponse speechResponse = new OpenAiSpeechResponse(
				new Data(responseEntity.getBody(), openAiSpeechRequest.prompt));
		return new ResponseEntity<>(speechResponse, headers, responseEntity.getStatusCode());

	}

}
