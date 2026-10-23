package io.github.gracegrc.regulatoryingestionservice.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.regulatory.RegulatoryApiService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@WebMvcTest(RegulatoryIngestionServiceController.class)
class RegulatoryIngestionServiceControllerTest {

	private static final UUID ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final RegulatoryAuthorityResponseDto AUTHORITY = new RegulatoryAuthorityResponseDto(ID, "EBA", "EU",
			Instant.parse("2026-10-01T10:15:30Z"), true, "admin-console");

	private static final String CREATE_BODY = "{\"name\":\"EBA\",\"jurisdiction\":\"EU\",\"createdBy\":\"admin-console\"}";

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private RegulatoryApiService regulatoryApiService;

	@Test
	void postCreatesTheAuthorityAndReturnsAnEmptyBody() {
		given(regulatoryApiService.createAuthority(new RegulatoryAuthorityRequestDto("EBA", "EU", "admin-console")))
			.willReturn(ID);

		assertThat(mvc.post().uri("/api/v1/authorities").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/authorities/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postWithInvalidFieldsReturnsBadRequestWithoutCallingTheRegulatoryApi() {
		assertThat(mvc.post()
			.uri("/api/v1/authorities")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"\",\"jurisdiction\":\"EU\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"name\":\"must not be blank\","
					+ "\"createdBy\":\"must not be blank\"}}");
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void getReturnsTheList() {
		given(regulatoryApiService.findAllAuthorities()).willReturn(List.of(AUTHORITY));

		assertThat(mvc.get().uri("/api/v1/authorities")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"name\":\"EBA\"}]");
	}

	@Test
	void getByIdReturnsTheAuthority() {
		given(regulatoryApiService.findAuthorityById(ID)).willReturn(AUTHORITY);

		assertThat(mvc.get().uri("/api/v1/authorities/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"name\":\"EBA\"}");
	}

	@Test
	void getByIdWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.get().uri("/api/v1/authorities/not-a-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void aClientErrorFromTheRegulatoryApiIsReturnedUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"An authority named 'EBA' already exists\"}";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
		given(regulatoryApiService.createAuthority(any())).willThrow(HttpClientErrorException.create(HttpStatus.CONFLICT,
				"Conflict", headers, problem.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

		assertThat(mvc.post().uri("/api/v1/authorities").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void aClientErrorWithoutHeadersIsReturnedWithItsStatusAndBody() {
		given(regulatoryApiService.findAuthorityById(ID)).willThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND,
				"Not Found", null, "Not Found".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

		assertThat(mvc.get().uri("/api/v1/authorities/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyText()
			.isEqualTo("Not Found");
	}

	@Test
	void aServerErrorFromTheRegulatoryApiReturnsBadGateway() {
		given(regulatoryApiService.findAllAuthorities())
			.willThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
					HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

		assertThat(mvc.get().uri("/api/v1/authorities")).hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The Regulatory API returned 500\"}");
	}

	@Test
	void anInvalidResponseFromTheRegulatoryApiReturnsBadGateway() {
		given(regulatoryApiService.createAuthority(any()))
			.willThrow(new RestClientException("The Regulatory API did not return the location of the new authority"));

		assertThat(mvc.post().uri("/api/v1/authorities").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,"
					+ "\"detail\":\"The Regulatory API did not return the location of the new authority\"}");
	}

	@Test
	void anUnreachableRegulatoryApiReturnsBadGateway() {
		given(regulatoryApiService.findAllAuthorities()).willThrow(new ResourceAccessException("Connection refused"));

		assertThat(mvc.get().uri("/api/v1/authorities")).hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The Regulatory API is unavailable\"}");
	}

}
