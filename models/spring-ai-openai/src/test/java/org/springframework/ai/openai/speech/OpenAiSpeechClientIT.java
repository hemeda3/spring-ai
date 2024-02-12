package org.springframework.ai.openai.speech;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.openai.OpenAiTestConfiguration;
import org.springframework.ai.openai.testutils.AbstractIT;
import org.springframework.ai.speech.SpeechOptions;
import org.springframework.ai.speech.SpeechOptionsBuilder;
import org.springframework.ai.speech.SpeechRequest;
import org.springframework.ai.speech.SpeechResponse;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest(classes = OpenAiTestConfiguration.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiSpeechClientIT extends AbstractIT {

	@Test
	void speechTest() {
		SpeechOptions speechOptions = SpeechOptionsBuilder.builder()
				.withVoice("shimmer")
				.withSpeed(1.0f)
				.withResponseFormat("mp3")
				.withModel("tts-1-hd")
				.withInput("Today is a wonderful day to build something people love!")
				.build();
		SpeechRequest speechRequest = new SpeechRequest(speechOptions);
		SpeechResponse response = openAiSpeechClient.call(speechRequest);
		byte[] audioBytes = response.getResult().getOutput();
		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput()).isNotEmpty();
		assertThat(audioBytes).hasSizeGreaterThan(0);

		System.out.println(response.getMetadata().getRateLimit().toString());
		System.out.println(response.getMetadata().getRateLimit().getTokensLimit());
		System.out.println(response.getMetadata().getRateLimit().getRequestsLimit());

		try {
			saveMp3ToFile(audioBytes, "aac1.mp3");
		} catch (IOException e) {
			System.err.println("Error saving MP3 file: " + e.getMessage());
		}

	}


	public static void saveMp3ToFile(byte[] audioBytes, String filePath) throws IOException {
		// Convert the string file path to a Path object
		Path path = Paths.get(filePath);

		// Write the byte array to the file
		Files.write(path, audioBytes);

		System.out.println("Audio saved to " + filePath);
	}
}
