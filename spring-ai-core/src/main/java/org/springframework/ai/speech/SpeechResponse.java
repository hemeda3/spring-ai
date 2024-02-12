package org.springframework.ai.speech;

import org.springframework.ai.model.ModelResponse;
import org.springframework.ai.speech.metadata.SpeechResponseMetadata;

import java.util.Arrays;
import java.util.List;

public class SpeechResponse implements ModelResponse<Speech> {

	private Speech speech;

	private SpeechResponseMetadata speechResponseMetadata;

	public SpeechResponse(Speech speech) {
		this(speech, SpeechResponseMetadata.NULL);
	}

	public SpeechResponse(Speech speech, SpeechResponseMetadata speechResponseMetadata) {
		this.speech = speech;
		this.speechResponseMetadata = speechResponseMetadata;
	}

	@Override
	public Speech getResult() {
		return speech;
	}

	@Override
	public List<Speech> getResults() {
		return Arrays.asList(speech);
	}

	@Override
	public SpeechResponseMetadata getMetadata() {
		return speechResponseMetadata;
	}

}
