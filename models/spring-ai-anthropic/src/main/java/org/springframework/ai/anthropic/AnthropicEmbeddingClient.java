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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.AbstractEmbeddingClient;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.anthropic.api.AnthropicApi.EmbeddingList;
import org.springframework.ai.anthropic.api.AnthropicApi.Usage;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

/**
 * Open AI Embedding Client implementation.
 *
 * @author Christian Tzolov
 */
public class AnthropicEmbeddingClient extends AbstractEmbeddingClient {

	private static final Logger logger = LoggerFactory.getLogger(AnthropicEmbeddingClient.class);

	private final org.springframework.ai.anthropic.AnthropicEmbeddingOptions defaultOptions;

	private final RetryTemplate retryTemplate;

	private final AnthropicApi anthropicApi;

	private final MetadataMode metadataMode;

	/**
	 * Constructor for the AnthropicEmbeddingClient class.
	 * @param anthropicApi The AnthropicApi instance to use for making API requests.
	 */
	public AnthropicEmbeddingClient(AnthropicApi anthropicApi) {
		this(anthropicApi, MetadataMode.EMBED);
	}

	/**
	 * Initializes a new instance of the AnthropicEmbeddingClient class.
	 * @param anthropicApi The AnthropicApi instance to use for making API requests.
	 * @param metadataMode The mode for generating metadata.
	 */
	public AnthropicEmbeddingClient(AnthropicApi anthropicApi, MetadataMode metadataMode) {
		this(anthropicApi, metadataMode,
				org.springframework.ai.anthropic.AnthropicEmbeddingOptions.builder().withModel(AnthropicApi.DEFAULT_EMBEDDING_MODEL).build(),
				RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	/**
	 * Initializes a new instance of the AnthropicEmbeddingClient class.
	 * @param anthropicApi The AnthropicApi instance to use for making API requests.
	 * @param metadataMode The mode for generating metadata.
	 * @param anthropicEmbeddingOptions The options for OpenAi embedding.
	 */
	public AnthropicEmbeddingClient(AnthropicApi anthropicApi, MetadataMode metadataMode,
									org.springframework.ai.anthropic.AnthropicEmbeddingOptions anthropicEmbeddingOptions) {
		this(anthropicApi, metadataMode, anthropicEmbeddingOptions, RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	/**
	 * Initializes a new instance of the AnthropicEmbeddingClient class.
	 * @param anthropicApi - The AnthropicApi instance to use for making API requests.
	 * @param metadataMode - The mode for generating metadata.
	 * @param options - The options for OpenAI embedding.
	 * @param retryTemplate - The RetryTemplate for retrying failed API requests.
	 */
	public AnthropicEmbeddingClient(AnthropicApi anthropicApi, MetadataMode metadataMode, org.springframework.ai.anthropic.AnthropicEmbeddingOptions options,
									RetryTemplate retryTemplate) {
		Assert.notNull(anthropicApi, "OpenAiService must not be null");
		Assert.notNull(metadataMode, "metadataMode must not be null");
		Assert.notNull(options, "options must not be null");
		Assert.notNull(retryTemplate, "retryTemplate must not be null");

		this.anthropicApi = anthropicApi;
		this.metadataMode = metadataMode;
		this.defaultOptions = options;
		this.retryTemplate = retryTemplate;
	}

	@Override
	public List<Double> embed(Document document) {
		Assert.notNull(document, "Document must not be null");
		return this.embed(document.getFormattedContent(this.metadataMode));
	}

	@SuppressWarnings("unchecked")
	@Override
	public EmbeddingResponse call(EmbeddingRequest request) {

		return this.retryTemplate.execute(ctx -> {

			AnthropicApi.EmbeddingRequest<List<String>> apiRequest = (this.defaultOptions != null)
					? new AnthropicApi.EmbeddingRequest<>(request.getInstructions(),
							this.defaultOptions.getModel(), this.defaultOptions.getEncodingFormat(),
							this.defaultOptions.getUser())
					: new AnthropicApi.EmbeddingRequest<>(request.getInstructions(),
							AnthropicApi.DEFAULT_EMBEDDING_MODEL);

			if (request.getOptions() != null && !EmbeddingOptions.EMPTY.equals(request.getOptions())) {
				apiRequest = ModelOptionsUtils.merge(request.getOptions(), apiRequest,
						AnthropicApi.EmbeddingRequest.class);
			}

			EmbeddingList<AnthropicApi.Embedding> apiEmbeddingResponse = this.anthropicApi.embeddings(apiRequest).getBody();

			if (apiEmbeddingResponse == null) {
				logger.warn("No embeddings returned for request: {}", request);
				return new EmbeddingResponse(List.of());
			}

			var metadata = generateResponseMetadata(apiEmbeddingResponse.model(), apiEmbeddingResponse.usage());

			List<Embedding> embeddings = apiEmbeddingResponse.data()
				.stream()
				.map(e -> new Embedding(e.embedding(), e.index()))
				.toList();

			return new EmbeddingResponse(embeddings, metadata);

		});
	}

	private EmbeddingResponseMetadata generateResponseMetadata(String model, Usage usage) {
		EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata();
		metadata.put("model", model);
		metadata.put("prompt-tokens", usage.promptTokens());
		metadata.put("completion-tokens", usage.completionTokens());
		metadata.put("total-tokens", usage.totalTokens());
		return metadata;
	}

}
