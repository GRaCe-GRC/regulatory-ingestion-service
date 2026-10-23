package io.github.gracegrc.regulatoryingestionservice.boot.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.gracegrc.regulatoryingestionservice.boot.RegulatoryApiConfig;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiAuthorityResponseDto;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@RestClientTest(RegulatoryApiClient.class)
@Import(RegulatoryApiConfig.class)
@TestPropertySource(properties = "regulatory-api.base-url=http://regulatory-api.test")
class RegulatoryApiClientTest {

	private static final UUID ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final String AUTHORITY_JSON = "{\"id\":\"" + ID + "\",\"name\":\"EBA\",\"jurisdiction\":\"EU\","
			+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"enabled\":true,\"createdBy\":\"admin-console\"}";

	private static final RegulatoryApiAuthorityResponseDto AUTHORITY = new RegulatoryApiAuthorityResponseDto(ID, "EBA", "EU",
			Instant.parse("2026-10-01T10:15:30Z"), true, "admin-console");

	@Autowired
	private RegulatoryApiClient regulatoryApiClient;

	@Autowired
	private MockRestServiceServer server;

	@AfterEach
	void verifyAllRequestsWereMade() {
		server.verify();
	}

	@Test
	void createAuthorityPostsTheRequestAndReturnsTheIdFromTheLocation() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/authorities"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("{\"name\":\"EBA\",\"jurisdiction\":\"EU\",\"createdBy\":\"admin-console\"}"))
			.andRespond(withStatus(HttpStatus.CREATED)
				.location(URI.create("http://regulatory-api.test/api/v1/authorities/" + ID)));

		UUID id = regulatoryApiClient.createAuthority(new RegulatoryApiAuthorityRequestDto("EBA", "EU", "admin-console"));

		assertThat(id).isEqualTo(ID);
	}

	@Test
	void createAuthorityWithoutALocationThrows() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/authorities"))
			.andRespond(withStatus(HttpStatus.CREATED));

		assertThatThrownBy(
				() -> regulatoryApiClient.createAuthority(new RegulatoryApiAuthorityRequestDto("EBA", "EU", "admin-console")))
			.isInstanceOf(RestClientException.class)
			.hasMessage("The Regulatory API did not return the location of the new authority");
	}

	@Test
	void createAuthorityWithAnInvalidLocationThrows() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/authorities"))
			.andRespond(withStatus(HttpStatus.CREATED).location(URI.create("http://regulatory-api.test/api/v1/authorities/x")));

		assertThatThrownBy(
				() -> regulatoryApiClient.createAuthority(new RegulatoryApiAuthorityRequestDto("EBA", "EU", "admin-console")))
			.isInstanceOf(RestClientException.class)
			.hasMessageStartingWith("The Regulatory API returned an invalid location");
	}

	@Test
	void findAllAuthoritiesReturnsTheList() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/authorities"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("[" + AUTHORITY_JSON + "]", MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findAllAuthorities()).containsExactly(AUTHORITY);
	}

	@Test
	void findAuthorityByIdReturnsTheAuthority() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/authorities/" + ID))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(AUTHORITY_JSON, MediaType.APPLICATION_JSON));

		assertThat(regulatoryApiClient.findAuthorityById(ID)).isEqualTo(AUTHORITY);
	}

	@Test
	void aClientErrorFromTheRegulatoryApiKeepsItsStatusAndBody() {
		String problem = "{\"status\":404,\"detail\":\"Authority " + ID + " not found\"}";
		server.expect(requestTo("http://regulatory-api.test/api/v1/authorities/" + ID))
			.andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem));

		assertThatThrownBy(() -> regulatoryApiClient.findAuthorityById(ID))
			.isInstanceOfSatisfying(HttpClientErrorException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(ex.getResponseBodyAsString()).isEqualTo(problem);
			});
	}

	@Test
	void aServerErrorFromTheRegulatoryApiIsThrown() {
		server.expect(requestTo("http://regulatory-api.test/api/v1/authorities")).andRespond(withServerError());

		assertThatThrownBy(() -> regulatoryApiClient.findAllAuthorities())
			.isInstanceOf(HttpServerErrorException.class);
	}

}
