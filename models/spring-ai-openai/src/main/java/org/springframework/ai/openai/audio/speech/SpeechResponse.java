package org.springframework.ai.openai.audio.speech;

import org.springframework.ai.model.ModelResponse;
import org.springframework.ai.openai.metadata.audio.OpenAiAudioSpeechResponseMetadata;

import java.util.Collections;
import java.util.List;

/**
 * @author Ahmed Yousri
 */

public class SpeechResponse implements ModelResponse<Speech> {

	private final Speech speech;

	private final OpenAiAudioSpeechResponseMetadata speechResponseMetadata;

	public SpeechResponse(Speech speech) {
		this(speech, OpenAiAudioSpeechResponseMetadata.NULL);
	}

	public SpeechResponse(Speech speech, OpenAiAudioSpeechResponseMetadata speechResponseMetadata) {
		this.speech = speech;
		this.speechResponseMetadata = speechResponseMetadata;
	}

	@Override
	public Speech getResult() {
		return speech;
	}

	@Override
	public List<Speech> getResults() {
		return Collections.singletonList(speech);
	}

	@Override
	public OpenAiAudioSpeechResponseMetadata getMetadata() {
		return speechResponseMetadata;
	}

}