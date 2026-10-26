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
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiRequirementRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiRequirementResponseDto;
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
class RegulatoryApiClientRequirementsTest {

	private static final UUID ID = UUID.fromString("e1f2a3b4-c5d6-4e7f-8a9b-0c1d2e3f4a50");

	private static final UUID FILE_ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID PARENT_ID = UUID.fromString("a9b8c7d6-e5f4-4a3b-9c2d-1e0f9a8b7c60");

	private static final String REQUIREMENT_JSON = "{\"id\":\"" + ID + "\",\"fileId\":\"" + FILE_ID
			+ "\",\"parentRequirementId\":\"" + PARENT_ID + "\",\"provisionRef\":\"164.308(a)(4)(i)\","
			+ "\"heading\":null,\"text\":\"Implement policies and procedures.\","
			+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"admin-console\"}";

	private static final RegulatoryApiRequirementResponseDto REQUIREMENT = new RegulatoryApiRequirementResponseDto(ID,
			FILE_ID, PARENT_ID, "164.308(a)(4)(i)", null, "Implement policies and procedures.",
			Instant.parse("2026-10-01T10:15:30Z"), "admin-console");

	private static final RegulatoryApiRequirementRequestDto REQUEST = new RegulatoryApiRequirementRequestDto(FILE_ID,
			PARENT_ID, "164.308(a)(4)(i)", null, "Implement policies and procedures.", "admin-console");

	@Autowired
	private RegulatoryApiClient regulatoryApiClient;

	@Autowired
	private MockRestServiceServer server;

	@AfterEach
	void verifyAllRequestsWereMade() {
		server.verify();
	}

	@Test
	void createRequirementPostsTheRequestAndReturnsTheIdFromTheLocation() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/requirements"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("{\"fileId\":\"" + FILE_ID + "\",\"parentRequirementId\":\"" + PARENT_ID
					+ "\",\"provisionRef\":\"164.308(a)(4)(i)\",\"heading\":null,"
					+ "\"text\":\"Implement policies and procedures.\",\"createdBy\":\"admin-console\"}"))
			.andRespond(withStatus(HttpStatus.CREATED)
				.location(URI.create("http://regulatory-api.test/api/v1/requirements/" + ID)));

		assertThat(regulatoryApiClient.createRequirement(REQUEST)).isEqualTo(ID);
	}

	@Test
	void createRequirementWithoutALocationThrows() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/requirements"))
			.andRespond(withStatus(HttpStatus.CREATED));

		assertThatThrownBy(() -> regulatoryApiClient.createRequirement(REQUEST))
			.isInstanceOf(RestClientException.class)
			.hasMessage("The Regulatory API did not return the location of the new requirement");
	}

	@Test
	void findAllRequirementsWithoutFiltersReturnsTheList() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/requirements"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[" + REQUIREMENT_JSON + "]", MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findAllRequirements(null, null)).containsExactly(REQUIREMENT);
	}

	@Test
	void findAllRequirementsWithAFileSendsOnlyThatFilter() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/requirements?fileId=" + FILE_ID))
			.andRespond(withSuccess("[" + REQUIREMENT_JSON + "]", MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findAllRequirements(FILE_ID, null)).containsExactly(REQUIREMENT);
	}

	@Test
	void findAllRequirementsWithAParentSendsOnlyThatFilter() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/requirements?parentId=" + PARENT_ID))
			.andRespond(withSuccess("[" + REQUIREMENT_JSON + "]", MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findAllRequirements(null, PARENT_ID)).containsExactly(REQUIREMENT);
	}

	@Test
	void findAllRequirementsWithBothFiltersSendsBoth() {
		server.expect(requestTo(
				"http://regulatory-api.test/api/v1/requirements?fileId=" + FILE_ID + "&parentId=" + PARENT_ID))
			.andRespond(withSuccess("[" + REQUIREMENT_JSON + "]", MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findAllRequirements(FILE_ID, PARENT_ID)).containsExactly(REQUIREMENT);
	}

	@Test
	void findRequirementByIdReturnsTheRequirement() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/requirements/" + ID))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(REQUIREMENT_JSON, MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findRequirementById(ID)).isEqualTo(REQUIREMENT);
	}

	@Test
	void deleteRequirementSendsADeleteRequest() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/requirements/" + ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withNoContent());

		regulatoryApiClient.deleteRequirement(ID);
	}

	@Test
	void deleteRequirementWithChildrenKeepsTheConflict() {
		String problem = "{\"status\":409,\"detail\":\"Requirement " + ID
				+ " has child requirements. Delete them first.\"}";
		server.expect(requestTo("http://regulatory-api.test/api/v1/requirements/" + ID))
			.andExpect(method(HttpMethod.DELETE))
			.andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> regulatoryApiClient.deleteRequirement(ID))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

}
