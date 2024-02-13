/*
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

package org.springframework.ai.openai.speech;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.speech.SpeechGeneration;
import org.springframework.ai.speech.SpeechPrompt;
import org.springframework.ai.speech.SpeechResponse;
import org.springframework.ai.speech.SpeechResponseMetadata;
import org.springframework.ai.openai.OpenAiSpeechClient;
import org.springframework.ai.openai.api.OpenAiSpeechApi;
import org.springframework.ai.openai.metadata.support.OpenAiApiResponseHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author John Blum
 * @author Christian Tzolov
 * @since 0.7.0
 */
@RestClientTest(OpenAiSpeechClientWithSpeechResponseMetadataTests.Config.class)
public class OpenAiSpeechClientWithSpeechResponseMetadataTests {

	private static String TEST_API_KEY = "sk-1234567890";

	@Autowired
	private OpenAiSpeechClient openAiSpeechClient;

	@Autowired
	private MockRestServiceServer server;

	@AfterEach
	void resetMockServer() {
		server.reset();
	}

	@Test
	void aiResponseContainsSpeechResponseMetadata() {

		prepareMock();

		SpeechPrompt prompt = new SpeechPrompt("Create an speech of a mini golden doodle dog.");

		SpeechResponse response = this.openAiSpeechClient.call(prompt);

		System.out.println(response.toString());

		assertThat(response).isNotNull();
		SpeechGeneration speechGenerations = response.getResult();
		assertThat(speechGenerations).isNotNull();

		SpeechResponseMetadata speechResponseMetadata = response.getMetadata();

		assertThat(speechResponseMetadata).isNotNull();

		SpeechResponseMetadata responseMetadata = response.getMetadata();

		assertThat(responseMetadata).isNotNull();
		byte[] audioBytes = response.getResult().getOutput().getData();
		assertThat(audioBytes).hasSizeGreaterThan(0);

		var requestLimit = speechResponseMetadata.getRateLimit();
		Long requestsLimit = requestLimit.getRequestsLimit();
		Long tokensLimit = requestLimit.getTokensLimit();
		Long tokensRemaining = requestLimit.getTokensRemaining();
		Long requestsRemaining = requestLimit.getRequestsRemaining();
		Duration requestsReset = requestLimit.getRequestsReset();
		assertThat(requestsLimit).isNotNull();
		assertThat(requestsLimit).isEqualTo(4000L);
		assertThat(tokensLimit).isEqualTo(725000L);
		assertThat(tokensRemaining).isEqualTo(112358L);
		assertThat(requestsRemaining).isEqualTo(999L);
		assertThat(requestsReset).isEqualTo(Duration.parse("PT64H15M29S"));

	}

	private void prepareMock() {

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set(OpenAiApiResponseHeaders.REQUESTS_LIMIT_HEADER.getName(), "4000");
		httpHeaders.set(OpenAiApiResponseHeaders.REQUESTS_REMAINING_HEADER.getName(), "999");
		httpHeaders.set(OpenAiApiResponseHeaders.REQUESTS_RESET_HEADER.getName(), "2d16h15m29s");
		httpHeaders.set(OpenAiApiResponseHeaders.TOKENS_LIMIT_HEADER.getName(), "725000");
		httpHeaders.set(OpenAiApiResponseHeaders.TOKENS_REMAINING_HEADER.getName(), "112358");
		httpHeaders.set(OpenAiApiResponseHeaders.TOKENS_RESET_HEADER.getName(), "27h55s451ms");
		httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		server.expect(requestTo("v1/audio/speech"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_API_KEY))
			.andRespond(withSuccess("Audio bytes as string", MediaType.APPLICATION_OCTET_STREAM).headers(httpHeaders));

	}

	@SpringBootConfiguration
	static class Config {

		@Bean
		public OpenAiSpeechApi speechGenerationApi(RestClient.Builder builder) {
			return new OpenAiSpeechApi("", TEST_API_KEY, builder);
		}

		@Bean
		public OpenAiSpeechClient openAiSpeechClient(OpenAiSpeechApi openAiSpeechApi) {
			return new OpenAiSpeechClient(openAiSpeechApi);
		}

	}

}
