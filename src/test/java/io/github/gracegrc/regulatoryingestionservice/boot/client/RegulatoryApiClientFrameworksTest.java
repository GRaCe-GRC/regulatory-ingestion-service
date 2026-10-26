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
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFrameworkResponseDto;
import java.net.URI;
import java.time.Instant;
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
class RegulatoryApiClientFrameworksTest {

	private static final UUID ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final UUID AUTHORITY_ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final String FRAMEWORK_JSON = "{\"id\":\"" + ID + "\",\"authorityId\":\"" + AUTHORITY_ID
			+ "\",\"name\":\"Capital Requirements Regulation\",\"shortName\":\"CRR\",\"citation\":null,"
			+ "\"enabled\":true,\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"admin-console\"}";

	private static final RegulatoryApiFrameworkResponseDto FRAMEWORK = new RegulatoryApiFrameworkResponseDto(ID,
			AUTHORITY_ID, "Capital Requirements Regulation", "CRR", null, true, Instant.parse("2026-10-01T10:15:30Z"),
			"admin-console");

	private static final RegulatoryApiFrameworkRequestDto REQUEST = new RegulatoryApiFrameworkRequestDto(AUTHORITY_ID,
			"Capital Requirements Regulation", "CRR", null, "admin-console");

	@Autowired
	private RegulatoryApiClient regulatoryApiClient;

	@Autowired
	private MockRestServiceServer server;

	@AfterEach
	void verifyAllRequestsWereMade() {
		server.verify();
	}

	@Test
	void createFrameworkPostsTheRequestAndReturnsTheIdFromTheLocation() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/frameworks"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("{\"authorityId\":\"" + AUTHORITY_ID + "\",\"name\":\"Capital Requirements "
					+ "Regulation\",\"shortName\":\"CRR\",\"citation\":null,\"createdBy\":\"admin-console\"}"))
			.andRespond(withStatus(HttpStatus.CREATED)
				.location(URI.create("http://regulatory-api.test/api/v1/frameworks/" + ID)));

		assertThat(regulatoryApiClient.createFramework(REQUEST)).isEqualTo(ID);
	}

	@Test
	void createFrameworkWithoutALocationThrows() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/frameworks"))
			.andRespond(withStatus(HttpStatus.CREATED));

		assertThatThrownBy(() -> regulatoryApiClient.createFramework(REQUEST)).isInstanceOf(RestClientException.class)
			.hasMessage("The Regulatory API did not return the location of the new framework");
	}

	@Test
	void findAllFrameworksWithoutAnAuthorityReturnsTheList() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/frameworks"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[" + FRAMEWORK_JSON + "]", MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findAllFrameworks(null)).containsExactly(FRAMEWORK);
	}

	@Test
	void findAllFrameworksWithAnAuthoritySendsTheFilter() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/frameworks?authorityId=" + AUTHORITY_ID))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[" + FRAMEWORK_JSON + "]", MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findAllFrameworks(AUTHORITY_ID)).containsExactly(FRAMEWORK);
	}

	@Test
	void findFrameworkByIdReturnsTheFramework() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/frameworks/" + ID))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(FRAMEWORK_JSON, MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findFrameworkById(ID)).isEqualTo(FRAMEWORK);
	}

	@Test
	void deleteFrameworkSendsADeleteRequest() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/frameworks/" + ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withNoContent());

		regulatoryApiClient.deleteFramework(ID);
	}

	@Test
	void deleteFrameworkWithFilesKeepsTheConflict() {
		String problem = "{\"status\":409,\"detail\":\"Framework " + ID + " has files. Delete them first.\"}";
		server.expect(requestTo("http://regulatory-api.test/api/v1/frameworks/" + ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> regulatoryApiClient.deleteFramework(ID))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

}
