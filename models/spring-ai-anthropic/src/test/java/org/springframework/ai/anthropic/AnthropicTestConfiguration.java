/*
 * Copyright 2023 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ai.anthropic;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.anthropic.api.AnthropicImageApi;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootConfiguration
public class AnthropicTestConfiguration {

	@Bean
	public AnthropicApi AnthropicApi() {
		return new AnthropicApi(getApiKey());
	}

	@Bean
	public AnthropicImageApi AnthropicImageApi() {
		return new AnthropicImageApi(getApiKey());
	}

	private String getApiKey() {
		String apiKey = System.getenv("Anthropic_API_KEY");
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException(
					"You must provide an API key.  Put it in an environment variable under the name Anthropic_API_KEY");
		}
		return apiKey;
	}

	@Bean
	public AnthropicChatClient AnthropicChatClient(AnthropicApi api) {
		AnthropicChatClient AnthropicChatClient = new AnthropicChatClient(api);
		return AnthropicChatClient;
	}


	@Bean
	public AnthropicImageClient AnthropicImageClient(AnthropicImageApi imageApi) {
		AnthropicImageClient AnthropicImageClient = new AnthropicImageClient(imageApi);
		// AnthropicImageClient.setModel("foobar");
		return AnthropicImageClient;
	}

	@Bean
	public EmbeddingClient AnthropicEmbeddingClient(AnthropicApi api) {
		return new AnthropicEmbeddingClient(api);
	}

}
