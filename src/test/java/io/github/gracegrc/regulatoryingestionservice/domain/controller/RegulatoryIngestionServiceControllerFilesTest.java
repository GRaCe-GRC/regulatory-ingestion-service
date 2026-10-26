package io.github.gracegrc.regulatoryingestionservice.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.regulatory.RegulatoryApiService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
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

@WebMvcTest(RegulatoryIngestionServiceController.class)
class RegulatoryIngestionServiceControllerFilesTest {

	private static final UUID ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID FRAMEWORK_ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final String CREATE_BODY = "{\"frameworkId\":\"" + FRAMEWORK_ID
			+ "\",\"title\":\"CRR consolidated text\",\"publicationDate\":\"2024-07-09\",\"createdBy\":\"admin-console\"}";

	private static final RegulatoryFileResponseDto FILE = new RegulatoryFileResponseDto(ID, FRAMEWORK_ID,
			"CRR consolidated text", null, null, LocalDate.parse("2024-07-09"), Instant.parse("2026-10-01T10:15:30Z"),
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
	void postCreatesTheFileAndReturnsAnEmptyBody() {
		given(regulatoryApiService.createFile(new RegulatoryFileRequestDto(FRAMEWORK_ID, "CRR consolidated text", null,
				null, LocalDate.parse("2024-07-09"), "admin-console")))
			.willReturn(ID);

		assertThat(mvc.post().uri("/api/v1/files").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/files/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postWithInvalidFieldsReturnsBadRequestWithoutCallingTheRegulatoryApi() {
		assertThat(mvc.post()
			.uri("/api/v1/files")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"title\":\"\",\"versionLabel\":\"" + "x".repeat(256) + "\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"frameworkId\":\"must not be null\","
					+ "\"title\":\"must not be blank\",\"versionLabel\":\"size must be between 0 and 255\","
					+ "\"createdBy\":\"must not be blank\"}}");
		then(regulatoryApiService).shouldHaveNoInteractions();
	}

	@Test
	void postWithAnUnknownFrameworkReturnsTheNotFoundUnchanged() {
		String problem = "{\"title\":\"Not Found\",\"status\":404,\"detail\":\"Framework " + FRAMEWORK_ID
				+ " not found\"}";
		given(regulatoryApiService.createFile(any())).willThrow(clientError(HttpStatus.NOT_FOUND, problem));

		assertThat(mvc.post().uri("/api/v1/files").contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
			.hasStatus(HttpStatus.NOT_FOUND)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

	@Test
	void getReturnsTheList() {
		given(regulatoryApiService.findAllFiles(null)).willReturn(List.of(FILE));

		assertThat(mvc.get().uri("/api/v1/files")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"title\":\"CRR consolidated text\"}]");
	}

	@Test
	void getWithAFrameworkIdPassesTheFilter() {
		given(regulatoryApiService.findAllFiles(FRAMEWORK_ID)).willReturn(List.of(FILE));

		assertThat(mvc.get().uri("/api/v1/files").param("frameworkId", FRAMEWORK_ID.toString())).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"frameworkId\":\"" + FRAMEWORK_ID + "\"}]");
	}

	@Test
	void getByIdReturnsTheFile() {
		given(regulatoryApiService.findFileById(ID)).willReturn(FILE);

		assertThat(mvc.get().uri("/api/v1/files/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"publicationDate\":\"2024-07-09\","
					+ "\"createdAt\":\"2026-10-01T10:15:30Z\",\"createdBy\":\"admin-console\"}");
	}

	@Test
	void getByIdWhenTheRegulatoryApiFailsReturnsBadGateway() {
		given(regulatoryApiService.findFileById(ID))
			.willThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
					HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

		assertThat(mvc.get().uri("/api/v1/files/{id}", ID)).hasStatus(HttpStatus.BAD_GATEWAY)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":502,\"detail\":\"The Regulatory API returned 503\"}");
	}

	@Test
	void deleteReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/v1/files/{id}", ID)).hasStatus(HttpStatus.NO_CONTENT).body().isEmpty();
		then(regulatoryApiService).should().deleteFile(ID);
	}

	@Test
	void deleteOfAFileWithRequirementsReturnsTheConflictUnchanged() {
		String problem = "{\"title\":\"Conflict\",\"status\":409,\"detail\":\"File " + ID
				+ " has requirements. Delete them first.\"}";
		willThrow(clientError(HttpStatus.CONFLICT, problem)).given(regulatoryApiService).deleteFile(ID);

		assertThat(mvc.delete().uri("/api/v1/files/{id}", ID)).hasStatus(HttpStatus.CONFLICT)
			.hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
			.bodyText()
			.isEqualTo(problem);
	}

}
