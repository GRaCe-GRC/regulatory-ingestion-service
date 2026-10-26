package io.github.gracegrc.regulatoryingestionservice.boot.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.gracegrc.regulatoryingestionservice.boot.RegulatoryApiConfig;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFileRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFileResponseDto;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@RestClientTest(RegulatoryApiClient.class)
@Import(RegulatoryApiConfig.class)
@TestPropertySource(properties = "regulatory-api.base-url=http://regulatory-api.test")
class RegulatoryApiClientFilesTest {

	private static final UUID ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID FRAMEWORK_ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final String FILE_JSON = "{\"id\":\"" + ID + "\",\"frameworkId\":\"" + FRAMEWORK_ID
			+ "\",\"title\":\"CRR consolidated text\",\"sourceUrl\":\"https://example.org/crr.pdf\","
			+ "\"versionLabel\":\"2024-07\",\"publicationDate\":\"2024-07-09\","
			+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"admin-console\"}";

	private static final RegulatoryApiFileResponseDto FILE = new RegulatoryApiFileResponseDto(ID, FRAMEWORK_ID,
			"CRR consolidated text", "https://example.org/crr.pdf", "2024-07", LocalDate.parse("2024-07-09"),
			Instant.parse("2026-10-01T10:15:30Z"), "admin-console");

	private static final RegulatoryApiFileRequestDto REQUEST = new RegulatoryApiFileRequestDto(FRAMEWORK_ID,
			"CRR consolidated text", "https://example.org/crr.pdf", "2024-07", LocalDate.parse("2024-07-09"),
			"admin-console");

	@Autowired
	private RegulatoryApiClient regulatoryApiClient;

	@Autowired
	private MockRestServiceServer server;

	@AfterEach
	void verifyAllRequestsWereMade() {
		server.verify();
	}

	@Test
	void createFilePostsTheRequestAndReturnsTheIdFromTheLocation() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/files"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("{\"frameworkId\":\"" + FRAMEWORK_ID + "\",\"title\":\"CRR consolidated text\","
					+ "\"sourceUrl\":\"https://example.org/crr.pdf\",\"versionLabel\":\"2024-07\","
					+ "\"publicationDate\":\"2024-07-09\",\"createdBy\":\"admin-console\"}"))
			.andRespond(withStatus(HttpStatus.CREATED)
				.location(URI.create("http://regulatory-api.test/api/v1/files/" + ID)));

		assertThat(regulatoryApiClient.createFile(REQUEST)).isEqualTo(ID);
	}

	@Test
	void createFileWithoutALocationThrows() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/files")).andRespond(withStatus(HttpStatus.CREATED));

		assertThatThrownBy(() -> regulatoryApiClient.createFile(REQUEST)).isInstanceOf(RestClientException.class)
			.hasMessage("The Regulatory API did not return the location of the new file");
	}

	@Test
	void findAllFilesWithoutAFrameworkReturnsTheList() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/files"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[" + FILE_JSON + "]", MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findAllFiles(null)).containsExactly(FILE);
	}

	@Test
	void findAllFilesWithAFrameworkSendsTheFilter() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/files?frameworkId=" + FRAMEWORK_ID))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[" + FILE_JSON + "]", MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findAllFiles(FRAMEWORK_ID)).containsExactly(FILE);
	}

	@Test
	void findFileByIdReturnsTheFile() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/files/" + ID))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(FILE_JSON, MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findFileById(ID)).isEqualTo(FILE);
	}

	@Test
	void deleteFileSendsADeleteRequest() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/files/" + ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withNoContent());

		regulatoryApiClient.deleteFile(ID);
	}

	@Test
	void deleteFileWithRequirementsKeepsTheConflict() {
		String problem = "{\"status\":409,\"detail\":\"File " + ID + " has requirements. Delete them first.\"}";
		server.expect(requestTo("http://regulatory-api.test/api/v1/files/" + ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> regulatoryApiClient.deleteFile(ID))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

}
