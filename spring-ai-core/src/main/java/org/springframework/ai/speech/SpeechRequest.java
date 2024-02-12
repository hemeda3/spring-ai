package org.springframework.ai.speech;

import org.springframework.ai.model.ModelOptions;
import org.springframework.ai.model.ModelRequest;
import org.springframework.core.io.Resource;

public class SpeechRequest implements ModelRequest<String> {


	private final SpeechOptions speechOptions;

	public SpeechRequest(SpeechOptions modelOptions) {
		this.speechOptions = modelOptions;
	}

	@Override
	public String getInstructions() {
		return null;
	}

	@Override
	public ModelOptions getOptions() {
		return speechOptions;
	}

}
