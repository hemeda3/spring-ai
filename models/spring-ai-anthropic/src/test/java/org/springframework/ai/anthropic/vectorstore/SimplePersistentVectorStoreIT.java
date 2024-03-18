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
package org.springframework.ai.anthropic.vectorstore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.reader.JsonMetadataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SimplePersistentVectorStoreIT {

	@Value("file:src/test/resources/data/acme/bikes.json")
	private Resource bikesJsonResource;

	@Autowired
	private EmbeddingClient embeddingClient;

	@Test
	void persist(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path workingDir) {
		JsonReader jsonReader = new JsonReader(bikesJsonResource, new ProductMetadataGenerator(), "price", "name",
				"shortDescription", "description", "tags");
		List<Document> documents = jsonReader.get();
		SimpleVectorStore vectorStore = new SimpleVectorStore(this.embeddingClient);
		vectorStore.add(documents);

		File tempFile = new File(workingDir.toFile(), "temp.txt");
		vectorStore.save(tempFile);
		assertThat(tempFile).isNotEmpty();
		assertThat(tempFile).content().contains("Velo 99 XR1 AXS");
		SimpleVectorStore vectorStore2 = new SimpleVectorStore(this.embeddingClient);

		vectorStore2.load(tempFile);
		List<Document> similaritySearch = vectorStore2.similaritySearch("Velo 99 XR1 AXS");
		assertThat(similaritySearch).isNotEmpty();
		assertThat(similaritySearch.get(0).getMetadata()).containsEntry("name", "Velo 99 XR1 AXS");

	}

	public class ProductMetadataGenerator implements JsonMetadataGenerator {

		@Override
		public Map<String, Object> generate(Map<String, Object> jsonMap) {
			return Map.of("name", jsonMap.get("name"));
		}

	}

}
