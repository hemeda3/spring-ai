/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ai.speech;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for {@link SpeechClient}.
 *
 * @author Ahmed Yousri
 */
class SpeechClientTests {

	@Test
	void speechRequestReturnsResponseCorrectly() {
		SpeechClient mockClient = Mockito.mock(SpeechClient.class);
		SpeechResponse mockResponse = Mockito.mock(SpeechResponse.class);
		Speech mockSpeech = Mockito.mock(Speech.class);
		byte[] expectedOutput = new byte[1024];

		when(mockSpeech.getOutput()).thenReturn(expectedOutput);
		when(mockResponse.getResult()).thenReturn(mockSpeech);
		when(mockClient.call(any(SpeechRequest.class))).thenReturn(mockResponse);

		// Assuming this is the correct way to trigger the call that leads to `getResult()`
		SpeechRequest mockSpeechRequest = Mockito.mock(SpeechRequest.class);
		SpeechResponse response = mockClient.call(mockSpeechRequest);

		// This forces the verification of getResult() call
		byte[] actualOutput = response.getResult().getOutput();

		assertThat(actualOutput).isEqualTo(expectedOutput);
		verify(mockClient, times(1)).call(eq(mockSpeechRequest));
		verify(mockClient, times(1)).call(isA(SpeechRequest.class));
		verify(mockSpeech, times(1)).getOutput();
		verify(response, times(1)).getResult();
		verifyNoMoreInteractions(mockClient, mockSpeech, response);
	}

}
