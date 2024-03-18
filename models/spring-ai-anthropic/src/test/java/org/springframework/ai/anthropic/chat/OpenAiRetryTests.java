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
package org.springframework.ai.anthropic.chat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.anthropic.*;
import org.springframework.ai.anthropic.api.AnthropicApi;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.image.ImageMessage;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.anthropic.AnthropicChatClient;
import org.springframework.ai.anthropic.api.AnthropicApi.ChatCompletion;
import org.springframework.ai.anthropic.api.AnthropicApi.ChatCompletionChunk;
import org.springframework.ai.anthropic.api.AnthropicApi.ChatCompletionFinishReason;
import org.springframework.ai.anthropic.api.AnthropicApi.ChatCompletionMessage;
import org.springframework.ai.anthropic.api.AnthropicApi.ChatCompletionMessage.Role;
import org.springframework.ai.anthropic.api.AnthropicApi.ChatCompletionRequest;
import org.springframework.ai.anthropic.api.AnthropicApi.Embedding;
import org.springframework.ai.anthropic.api.AnthropicApi.EmbeddingList;
import org.springframework.ai.anthropic.api.AnthropicApi.EmbeddingRequest;
import org.springframework.ai.anthropic.api.AnthropicImageApi;
import org.springframework.ai.anthropic.api.AnthropicImageApi.Data;
import org.springframework.ai.anthropic.api.AnthropicImageApi.AnthropicImageRequest;
import org.springframework.ai.anthropic.api.AnthropicImageApi.AnthropicImageResponse;
import org.springframework.ai.anthropic.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.anthropic.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

/**
 * @author Christian Tzolov
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class AnthropicRetryTests {

	private class TestRetryListener implements RetryListener {

		int onErrorRetryCount = 0;

		int onSuccessRetryCount = 0;

		@Override
		public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
			onSuccessRetryCount = context.getRetryCount();
		}

		@Override
		public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
				Throwable throwable) {
			onErrorRetryCount = context.getRetryCount();
		}

	}

	private TestRetryListener retryListener;

	private RetryTemplate retryTemplate;

	private @Mock AnthropicApi anthropicApi;

	private @Mock AnthropicAudioApi AnthropicAudioApi;

	private @Mock AnthropicImageApi anthropicImageApi;

	private AnthropicChatClient chatClient;

	private AnthropicEmbeddingClient embeddingClient;

	private AnthropicAudioTranscriptionClient audioTranscriptionClient;

	private AnthropicImageClient imageClient;

	@BeforeEach
	public void beforeEach() {
		retryTemplate = RetryUtils.DEFAULT_RETRY_TEMPLATE;
		retryListener = new TestRetryListener();
		retryTemplate.registerListener(retryListener);

		chatClient = new AnthropicChatClient(anthropicApi, AnthropicChatOptions.builder().build(), null, retryTemplate);
		embeddingClient = new AnthropicEmbeddingClient(anthropicApi, MetadataMode.EMBED,
				AnthropicEmbeddingOptions.builder().build(), retryTemplate);
		audioTranscriptionClient = new AnthropicAudioTranscriptionClient(AnthropicAudioApi,
				AnthropicAudioTranscriptionOptions.builder()
					.withModel("model")
					.withResponseFormat(TranscriptResponseFormat.JSON)
					.build(),
				retryTemplate);
		imageClient = new AnthropicImageClient(anthropicImageApi, AnthropicImageOptions.builder().build(), retryTemplate);
	}

	@Test
	public void AnthropicChatTransientError() {

		var choice = new ChatCompletion.Choice(ChatCompletionFinishReason.STOP, 0,
				new ChatCompletionMessage("Response", Role.ASSISTANT), null);
		ChatCompletion expectedChatCompletion = new ChatCompletion("id", List.of(choice), 666l, "model", null, null,
				new AnthropicApi.Usage(10, 10, 10));

		when(anthropicApi.chatCompletionEntity(isA(ChatCompletionRequest.class)))
			.thenThrow(new TransientAiException("Transient Error 1"))
			.thenThrow(new TransientAiException("Transient Error 2"))
			.thenReturn(ResponseEntity.of(Optional.of(expectedChatCompletion)));

		var result = chatClient.call(new Prompt("text"));

		assertThat(result).isNotNull();
		assertThat(result.getResult().getOutput().getContent()).isSameAs("Response");
		assertThat(retryListener.onSuccessRetryCount).isEqualTo(2);
		assertThat(retryListener.onErrorRetryCount).isEqualTo(2);
	}

	@Test
	public void AnthropicChatNonTransientError() {
		when(anthropicApi.chatCompletionEntity(isA(ChatCompletionRequest.class)))
				.thenThrow(new RuntimeException("Non Transient Error"));
		assertThrows(RuntimeException.class, () -> chatClient.call(new Prompt("text")));
	}

	@Test
	public void AnthropicChatStreamTransientError() {

		var choice = new ChatCompletionChunk.ChunkChoice(ChatCompletionFinishReason.STOP, 0,
				new ChatCompletionMessage("Response", Role.ASSISTANT), null);
		ChatCompletionChunk expectedChatCompletion = new ChatCompletionChunk("id", List.of(choice), 666l, "model", null,
				null);

		when(anthropicApi.chatCompletionStream(isA(ChatCompletionRequest.class)))
			.thenThrow(new TransientAiException("Transient Error 1"))
			.thenThrow(new TransientAiException("Transient Error 2"))
			.thenReturn(Flux.just(expectedChatCompletion));

		var result = chatClient.stream(new Prompt("text"));

		assertThat(result).isNotNull();
		assertThat(result.collectList().block().get(0).getResult().getOutput().getContent()).isSameAs("Response");
		assertThat(retryListener.onSuccessRetryCount).isEqualTo(2);
		assertThat(retryListener.onErrorRetryCount).isEqualTo(2);
	}

	@Test
	public void AnthropicChatStreamNonTransientError() {
		when(anthropicApi.chatCompletionStream(isA(ChatCompletionRequest.class)))
				.thenThrow(new RuntimeException("Non Transient Error"));
		assertThrows(RuntimeException.class, () -> chatClient.stream(new Prompt("text")));
	}

	@Test
	public void AnthropicEmbeddingTransientError() {

		EmbeddingList<Embedding> expectedEmbeddings = new EmbeddingList<>("list",
				List.of(new Embedding(0, List.of(9.9, 8.8))), "model", new AnthropicApi.Usage(10, 10, 10));

		when(anthropicApi.embeddings(isA(EmbeddingRequest.class))).thenThrow(new TransientAiException("Transient Error 1"))
			.thenThrow(new TransientAiException("Transient Error 2"))
			.thenReturn(ResponseEntity.of(Optional.of(expectedEmbeddings)));

		var result = embeddingClient
			.call(new org.springframework.ai.embedding.EmbeddingRequest(List.of("text1", "text2"), null));

		assertThat(result).isNotNull();
		assertThat(result.getResult().getOutput()).isEqualTo(List.of(9.9, 8.8));
		assertThat(retryListener.onSuccessRetryCount).isEqualTo(2);
		assertThat(retryListener.onErrorRetryCount).isEqualTo(2);
	}

	@Test
	public void AnthropicEmbeddingNonTransientError() {
		when(anthropicApi.embeddings(isA(EmbeddingRequest.class)))
				.thenThrow(new RuntimeException("Non Transient Error"));
		assertThrows(RuntimeException.class, () -> embeddingClient
				.call(new org.springframework.ai.embedding.EmbeddingRequest(List.of("text1", "text2"), null)));
	}

	@Test
	public void AnthropicAudioTranscriptionTransientError() {

		var expectedResponse = new StructuredResponse("nl", 6.7f, "Transcription Text", List.of(), List.of());

		when(AnthropicAudioApi.createTranscription(isA(TranscriptionRequest.class), isA(Class.class)))
			.thenThrow(new TransientAiException("Transient Error 1"))
			.thenThrow(new TransientAiException("Transient Error 2"))
			.thenReturn(ResponseEntity.of(Optional.of(expectedResponse)));

		AudioTranscriptionResponse result = audioTranscriptionClient
			.call(new AudioTranscriptionPrompt(new ClassPathResource("speech/jfk.flac")));

		assertThat(result).isNotNull();
		assertThat(result.getResult().getOutput()).isEqualTo(expectedResponse.text());
		assertThat(retryListener.onSuccessRetryCount).isEqualTo(2);
		assertThat(retryListener.onErrorRetryCount).isEqualTo(2);
	}

	@Test
	public void AnthropicAudioTranscriptionNonTransientError() {
		when(AnthropicAudioApi.createTranscription(isA(TranscriptionRequest.class), isA(Class.class)))
				.thenThrow(new RuntimeException("Transient Error 1"));
		assertThrows(RuntimeException.class, () -> audioTranscriptionClient
				.call(new AudioTranscriptionPrompt(new ClassPathResource("speech/jfk.flac"))));
	}

	@Test
	public void AnthropicImageTransientError() {

		var expectedResponse = new AnthropicImageResponse(678l, List.of(new Data("url678", "b64", "prompt")));

		when(anthropicImageApi.createImage(isA(AnthropicImageRequest.class)))
			.thenThrow(new TransientAiException("Transient Error 1"))
			.thenThrow(new TransientAiException("Transient Error 2"))
			.thenReturn(ResponseEntity.of(Optional.of(expectedResponse)));

		var result = imageClient.call(new ImagePrompt(List.of(new ImageMessage("Image Message"))));

		assertThat(result).isNotNull();
		assertThat(result.getResult().getOutput().getUrl()).isEqualTo("url678");
		assertThat(retryListener.onSuccessRetryCount).isEqualTo(2);
		assertThat(retryListener.onErrorRetryCount).isEqualTo(2);
	}

	@Test
	public void AnthropicImageNonTransientError() {
		when(anthropicImageApi.createImage(isA(AnthropicImageRequest.class)))
				.thenThrow(new RuntimeException("Transient Error 1"));
		assertThrows(RuntimeException.class,
				() -> imageClient.call(new ImagePrompt(List.of(new ImageMessage("Image Message")))));
	}

}
