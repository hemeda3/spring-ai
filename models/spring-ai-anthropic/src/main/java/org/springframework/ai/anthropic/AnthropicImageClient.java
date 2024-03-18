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

import org.springframework.ai.anthropic.api.AnthropicImageApi;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.image.ImageResponseMetadata;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.anthropic.metadata.AnthropicImageGenerationMetadata;
import org.springframework.ai.anthropic.metadata.AnthropicImageResponseMetadata;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

/**
 * AnthropicImageClient is a class that implements the ImageClient interface. It provides a
 * client for calling the OpenAI image generation API.
 *
 * @author Mark Pollack
 * @author Christian Tzolov
 * @since 0.8.0
 */
public class AnthropicImageClient implements ImageClient {

	private final static Logger logger = LoggerFactory.getLogger(AnthropicImageClient.class);

	private org.springframework.ai.anthropic.AnthropicImageOptions defaultOptions;

	private final AnthropicImageApi anthropicImageApi;

	public final RetryTemplate retryTemplate;

	public AnthropicImageClient(AnthropicImageApi anthropicImageApi) {
		this(anthropicImageApi, org.springframework.ai.anthropic.AnthropicImageOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public AnthropicImageClient(AnthropicImageApi anthropicImageApi, org.springframework.ai.anthropic.AnthropicImageOptions defaultOptions,
								RetryTemplate retryTemplate) {
		Assert.notNull(anthropicImageApi, "AnthropicImageApi must not be null");
		Assert.notNull(defaultOptions, "defaultOptions must not be null");
		Assert.notNull(retryTemplate, "retryTemplate must not be null");
		this.anthropicImageApi = anthropicImageApi;
		this.defaultOptions = defaultOptions;
		this.retryTemplate = retryTemplate;
	}

	public org.springframework.ai.anthropic.AnthropicImageOptions getDefaultOptions() {
		return this.defaultOptions;
	}

	@Override
	public ImageResponse call(ImagePrompt imagePrompt) {
		return this.retryTemplate.execute(ctx -> {

			String instructions = imagePrompt.getInstructions().get(0).getText();

			AnthropicImageApi.OpenAiImageRequest imageRequest = new AnthropicImageApi.OpenAiImageRequest(instructions,
					AnthropicImageApi.DEFAULT_IMAGE_MODEL);

			if (this.defaultOptions != null) {
				imageRequest = ModelOptionsUtils.merge(this.defaultOptions, imageRequest,
						AnthropicImageApi.OpenAiImageRequest.class);
			}

			if (imagePrompt.getOptions() != null) {
				imageRequest = ModelOptionsUtils.merge(toOpenAiImageOptions(imagePrompt.getOptions()), imageRequest,
						AnthropicImageApi.OpenAiImageRequest.class);
			}

			// Make the request
			ResponseEntity<AnthropicImageApi.OpenAiImageResponse> imageResponseEntity = this.anthropicImageApi
				.createImage(imageRequest);

			// Convert to org.springframework.ai.model derived ImageResponse data type
			return convertResponse(imageResponseEntity, imageRequest);
		});
	}

	private ImageResponse convertResponse(ResponseEntity<AnthropicImageApi.OpenAiImageResponse> imageResponseEntity,
			AnthropicImageApi.OpenAiImageRequest openAiImageRequest) {
		AnthropicImageApi.OpenAiImageResponse imageApiResponse = imageResponseEntity.getBody();
		if (imageApiResponse == null) {
			logger.warn("No image response returned for request: {}", openAiImageRequest);
			return new ImageResponse(List.of());
		}

		List<ImageGeneration> imageGenerationList = imageApiResponse.data().stream().map(entry -> {
			return new ImageGeneration(new Image(entry.url(), entry.b64Json()),
					new AnthropicImageGenerationMetadata(entry.revisedPrompt()));
		}).toList();

		ImageResponseMetadata openAiImageResponseMetadata = AnthropicImageResponseMetadata.from(imageApiResponse);
		return new ImageResponse(imageGenerationList, openAiImageResponseMetadata);
	}

	/**
	 * Convert the {@link ImageOptions} into {@link org.springframework.ai.anthropic.AnthropicImageOptions}.
	 * @param runtimeImageOptions the image options to use.
	 * @return the converted {@link org.springframework.ai.anthropic.AnthropicImageOptions}.
	 */
	private org.springframework.ai.anthropic.AnthropicImageOptions toOpenAiImageOptions(ImageOptions runtimeImageOptions) {
		org.springframework.ai.anthropic.AnthropicImageOptions.Builder openAiImageOptionsBuilder = org.springframework.ai.anthropic.AnthropicImageOptions.builder();
		if (runtimeImageOptions != null) {
			// Handle portable image options
			if (runtimeImageOptions.getN() != null) {
				openAiImageOptionsBuilder.withN(runtimeImageOptions.getN());
			}
			if (runtimeImageOptions.getModel() != null) {
				openAiImageOptionsBuilder.withModel(runtimeImageOptions.getModel());
			}
			if (runtimeImageOptions.getResponseFormat() != null) {
				openAiImageOptionsBuilder.withResponseFormat(runtimeImageOptions.getResponseFormat());
			}
			if (runtimeImageOptions.getWidth() != null) {
				openAiImageOptionsBuilder.withWidth(runtimeImageOptions.getWidth());
			}
			if (runtimeImageOptions.getHeight() != null) {
				openAiImageOptionsBuilder.withHeight(runtimeImageOptions.getHeight());
			}
			// Handle OpenAI specific image options
			if (runtimeImageOptions instanceof org.springframework.ai.anthropic.AnthropicImageOptions) {
				org.springframework.ai.anthropic.AnthropicImageOptions runtimeAnthropicImageOptions = (org.springframework.ai.anthropic.AnthropicImageOptions) runtimeImageOptions;
				if (runtimeAnthropicImageOptions.getQuality() != null) {
					openAiImageOptionsBuilder.withQuality(runtimeAnthropicImageOptions.getQuality());
				}
				if (runtimeAnthropicImageOptions.getStyle() != null) {
					openAiImageOptionsBuilder.withStyle(runtimeAnthropicImageOptions.getStyle());
				}
				if (runtimeAnthropicImageOptions.getUser() != null) {
					openAiImageOptionsBuilder.withUser(runtimeAnthropicImageOptions.getUser());
				}
			}
		}
		return openAiImageOptionsBuilder.build();
	}

}
