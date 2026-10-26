package io.github.gracegrc.regulatoryingestionservice.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryRequirementRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryRequirementResponseDto;
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
import org.springframework.web.client.RestClientException;

@WebMvcTest(RegulatoryIngestionServiceController.class)
class RegulatoryIngestionServiceControllerRequirementsTest {

	private static final UUID ID = UUID.fromString("e1f2a3b4-c5d6-4e7f-8a9b-0c1d2e3f4a50");

	private static final UUID FILE_ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID PARENT_ID = UUID.fromString("a9b8c7d6-e5f4-4a3b-9c2d-1e0f9a8b7c60");

	private static final String CREATE_BODY = "{\"fileId\":\"" + FILE_ID + "\",\"parentRequirementId\":\"" + PARENT_ID
			+ "\",\"provisionRef\":\"164.308(a)(4)(i)\",\"createdBy\":\"admin-console\"}";

	private static final RegulatoryRequirementResponseDto REQUIREMENT = new RegulatoryRequirementResponseDto(ID,
			FILE_ID, PARENT_ID, "164.308(a)(4)(i)", null, null, Instant.parse("2026-10-01T10:15:30Z"),
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
	void postCreatesTheRequirementAndReturnsAnEmptyBody() {
		given(regulatoryApiService.createRequirement(new RegulatoryRequirementRequestDto(FILE_ID, PARENT_ID,
				"164.308(a)(4)(i)", null, null, "admin-console")))
			.willReturn(ID);

		assertThat(mvc.post().uri("/api/v1/requirements").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/requirements/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postWithInvalidFieldsReturnsBadRequestWithoutCallingTheRegulatoryApi() {
		assertThat(mvc.post()
			.uri("/api/v1/requirements")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"provisionRef\":\" \",\"heading\":\"" + "x".repeat(256) + "\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"fileId\":\"must not be null\","
					+ "\"provisionRef\":\"must not be blank\",\"heading\":\"size must be between 0 and 255\"}}");
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void postWithADuplicateProvisionRefReturnsTheConflictUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"A requirement with provision reference "
				+ "'164.308(a)(4)(i)' already exists in file " + FILE_ID + "\"}";
		given(regulatoryApiService.createRequirement(any())).willThrow(clientError(HttpStatus.CONFLICT, problem));

		assertThat(mvc.post().uri("/api/v1/requirements").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void postWithAParentInAnotherFileReturnsTheBadRequestUnchanged() {
		String problem = "{\"title\":\"Bad Request\",\"status\":400,\"detail\":\"Parent requirement " + PARENT_ID
				+ " is not in file " + FILE_ID + "\"}";
		given(regulatoryApiService.createRequirement(any())).willThrow(clientError(HttpStatus.BAD_REQUEST, problem));

		assertThat(mvc.post().uri("/api/v1/requirements").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void postWhenTheRegulatoryApiReturnsNoLocationReturnsBadGateway() {
		given(regulatoryApiService.createRequirement(any())).willThrow(
				new RestClientException("The Regulatory API did not return the location of the new requirement"));

		assertThat(mvc.post().uri("/api/v1/requirements").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,"
					+ "\"detail\":\"The Regulatory API did not return the location of the new requirement\"}");
	}

	@Test
	void getReturnsTheList() {
		given(regulatoryApiService.findAllRequirements(null, null)).willReturn(List.of(REQUIREMENT));

		assertThat(mvc.get().uri("/api/v1/requirements")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"provisionRef\":\"164.308(a)(4)(i)\"}]");
	}

	@Test
	void getWithAFileIdAndAParentIdPassesBothFilters() {
		given(regulatoryApiService.findAllRequirements(FILE_ID, PARENT_ID)).willReturn(List.of(REQUIREMENT));

		assertThat(mvc.get()
			.uri("/api/v1/requirements")
			.param("fileId", FILE_ID.toString())
			.param("parentId", PARENT_ID.toString())).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"parentRequirementId\":\"" + PARENT_ID + "\"}]");
	}

	@Test
	void getWithAMalformedParentIdReturnsBadRequest() {
		assertThat(mvc.get().uri("/api/v1/requirements").param("parentId", "not-a-uuid"))
			.hasStatus(HttpStatus.BAD_REQUEST);
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void getByIdReturnsTheRequirement() {
		given(regulatoryApiService.findRequirementById(ID)).willReturn(REQUIREMENT);

		assertThat(mvc.get().uri("/api/v1/requirements/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"fileId\":\"" + FILE_ID
					+ "\",\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"admin-console\"}");
	}

	@Test
	void deleteReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/v1/requirements/{id}", ID)).hasStatus(HttpStatus.NO_CONTENT)
			.body()
			.isEmpty();
		then(regulatoryApiService).should().deleteRequirement(ID);
	}

	@Test
	void deleteOfARequirementWithChildrenReturnsTheConflictUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"Requirement " + ID
				+ " has child requirements. Delete them first.\"}";
		willThrow(clientError(HttpStatus.CONFLICT, problem)).given(regulatoryApiService).deleteRequirement(ID);

		assertThat(mvc.delete().uri("/api/v1/requirements/{id}", ID)).hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

}
