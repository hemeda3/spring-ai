package org.springframework.ai.speech.metadata;

import org.springframework.ai.model.ResultMetadata;

public interface SpeechMetadata extends ResultMetadata {

	SpeechMetadata NULL = SpeechMetadata.create();

	/**
	 * Factory method used to construct a new {@link SpeechMetadata}
	 * @return a new {@link SpeechMetadata}
	 */
	static SpeechMetadata create() {
		return new SpeechMetadata() {
		};
	}

}
