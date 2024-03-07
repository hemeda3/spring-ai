package org.springframework.ai.openai.audio.speech;

public class SpeechMessage {

	private String text;

	public SpeechMessage(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}