package io.github.gracegrc.regulatoryingestionservice.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkResponseDto;
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
import org.springframework.web.client.ResourceAccessException;

@WebMvcTest(RegulatoryIngestionServiceController.class)
class RegulatoryIngestionServiceControllerFrameworksTest {

	private static final UUID ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final UUID AUTHORITY_ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final String CREATE_BODY = "{\"authorityId\":\"" + AUTHORITY_ID
			+ "\",\"name\":\"Capital Requirements Regulation\",\"shortName\":\"CRR\",\"createdBy\":\"admin-console\"}";

	private static final RegulatoryFrameworkResponseDto FRAMEWORK = new RegulatoryFrameworkResponseDto(ID,
			AUTHORITY_ID, "Capital Requirements Regulation", "CRR", null, true, Instant.parse("2026-10-01T10:15:30Z"),
			"admin-console");

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private RegulatoryApiService regulatoryApiService;

	private static HttpClientErrorException clientError(HttpStatus status, String problem) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
		return HttpClientErrorException.create(status, status.getReasonPhrase(), headers,
				problem.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
	}

	@Test
	void postCreatesTheFrameworkAndReturnsAnEmptyBody() {
		given(regulatoryApiService.createFramework(new RegulatoryFrameworkRequestDto(AUTHORITY_ID,
				"Capital Requirements Regulation", "CRR", null, "admin-console")))
			.willReturn(ID);

		assertThat(mvc.post().uri("/api/v1/frameworks").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/frameworks/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postWithInvalidFieldsReturnsBadRequestWithoutCallingTheRegulatoryApi() {
		assertThat(mvc.post()
			.uri("/api/v1/frameworks")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"\",\"citation\":\"" + "x".repeat(256) + "\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"authorityId\":\"must not be null\","
					+ "\"name\":\"must not be blank\",\"citation\":\"size must be between 0 and 255\"}}");
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void postWithADuplicateNameReturnsTheConflictUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"A framework named "
				+ "'Capital Requirements Regulation' already exists for authority " + AUTHORITY_ID + "\"}";
		given(regulatoryApiService.createFramework(any())).willThrow(clientError(HttpStatus.CONFLICT, problem));

		assertThat(mvc.post().uri("/api/v1/frameworks").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void getReturnsTheList() {
		given(regulatoryApiService.findAllFrameworks(null)).willReturn(List.of(FRAMEWORK));

		assertThat(mvc.get().uri("/api/v1/frameworks")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"shortName\":\"CRR\"}]");
	}

	@Test
	void getWithAnAuthorityIdPassesTheFilter() {
		given(regulatoryApiService.findAllFrameworks(AUTHORITY_ID)).willReturn(List.of(FRAMEWORK));

		assertThat(mvc.get().uri("/api/v1/frameworks").param("authorityId", AUTHORITY_ID.toString())).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"authorityId\":\"" + AUTHORITY_ID + "\"}]");
	}

	@Test
	void getWithAMalformedAuthorityIdReturnsBadRequest() {
		assertThat(mvc.get().uri("/api/v1/frameworks").param("authorityId", "not-a-uuid"))
			.hasStatus(HttpStatus.BAD_REQUEST);
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void getByIdReturnsTheFramework() {
		given(regulatoryApiService.findFrameworkById(ID)).willReturn(FRAMEWORK);

		assertThat(mvc.get().uri("/api/v1/frameworks/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"name\":\"Capital Requirements Regulation\",\"enabled\":true,"
					+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"admin-console\"}");
	}

	@Test
	void getByIdWhenTheRegulatoryApiIsUnreachableReturnsBadGateway() {
		given(regulatoryApiService.findFrameworkById(ID)).willThrow(new ResourceAccessException("Connection refused"));

		assertThat(mvc.get().uri("/api/v1/frameworks/{id}", ID)).hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The Regulatory API is unavailable\"}");
	}

	@Test
	void deleteReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/v1/frameworks/{id}", ID)).hasStatus(HttpStatus.NO_CONTENT).body().isEmpty();
		then(regulatoryApiService).should().deleteFramework(ID);
	}

	@Test
	void deleteOfAFrameworkWithFilesReturnsTheConflictUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"Framework " + ID
				+ " has files. Delete them first.\"}";
		willThrow(clientError(HttpStatus.CONFLICT, problem)).given(regulatoryApiService).deleteFramework(ID);

		assertThat(mvc.delete().uri("/api/v1/frameworks/{id}", ID)).hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

}
