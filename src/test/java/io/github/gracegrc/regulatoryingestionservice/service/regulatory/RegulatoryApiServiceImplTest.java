package io.github.gracegrc.regulatoryingestionservice.service.regulatory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.gracegrc.regulatoryingestionservice.boot.client.RegulatoryApiClient;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiAuthorityResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFrameworkResponseDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegulatoryApiServiceImplTest {

	private static final UUID ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	private static final RegulatoryApiAuthorityResponseDto API_AUTHORITY = new RegulatoryApiAuthorityResponseDto(ID,
			"EBA", "EU", CREATED_AT, true, "admin-console");

	private static final RegulatoryAuthorityResponseDto AUTHORITY = new RegulatoryAuthorityResponseDto(ID, "EBA", "EU",
			CREATED_AT, true, "admin-console");

	private static final UUID FRAMEWORK_ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final RegulatoryApiFrameworkResponseDto API_FRAMEWORK = new RegulatoryApiFrameworkResponseDto(
			FRAMEWORK_ID, ID, "Capital Requirements Regulation", "CRR", null, true, CREATED_AT, "admin-console");

	private static final RegulatoryFrameworkResponseDto FRAMEWORK = new RegulatoryFrameworkResponseDto(FRAMEWORK_ID,
			ID, "Capital Requirements Regulation", "CRR", null, true, CREATED_AT, "admin-console");

	@Mock
	private RegulatoryApiClient regulatoryApiClient;

	@InjectMocks
	private RegulatoryApiServiceImpl regulatoryApiService;

	@Test
	void createAuthoritySendsTheMappedRequestAndReturnsTheId() {
		given(regulatoryApiClient.createAuthority(new RegulatoryApiAuthorityRequestDto("EBA", "EU", "admin-console")))
			.willReturn(ID);

		assertThat(regulatoryApiService.createAuthority(new RegulatoryAuthorityRequestDto("EBA", "EU", "admin-console")))
			.isEqualTo(ID);
	}

	@Test
	void findAllAuthoritiesMapsTheListFromTheClient() {
		given(regulatoryApiClient.findAllAuthorities()).willReturn(List.of(API_AUTHORITY));

		assertThat(regulatoryApiService.findAllAuthorities()).containsExactly(AUTHORITY);
	}

	@Test
	void findAuthorityByIdMapsTheAuthorityFromTheClient() {
		given(regulatoryApiClient.findAuthorityById(ID)).willReturn(API_AUTHORITY);

		assertThat(regulatoryApiService.findAuthorityById(ID)).isEqualTo(AUTHORITY);
	}

	@Test
	void deleteAuthorityAsksTheClientToDeleteIt() {
		regulatoryApiService.deleteAuthority(ID);

		then(regulatoryApiClient).should().deleteAuthority(ID);
	}

	@Test
	void createFrameworkSendsTheMappedRequestAndReturnsTheId() {
		given(regulatoryApiClient.createFramework(new RegulatoryApiFrameworkRequestDto(ID,
				"Capital Requirements Regulation", "CRR", null, "admin-console")))
			.willReturn(FRAMEWORK_ID);

		assertThat(regulatoryApiService.createFramework(new RegulatoryFrameworkRequestDto(ID,
				"Capital Requirements Regulation", "CRR", null, "admin-console")))
			.isEqualTo(FRAMEWORK_ID);
	}

	@Test
	void findAllFrameworksPassesTheAuthorityAndMapsTheList() {
		given(regulatoryApiClient.findAllFrameworks(ID)).willReturn(List.of(API_FRAMEWORK));

		assertThat(regulatoryApiService.findAllFrameworks(ID)).containsExactly(FRAMEWORK);
	}

	@Test
	void findFrameworkByIdMapsTheFrameworkFromTheClient() {
		given(regulatoryApiClient.findFrameworkById(FRAMEWORK_ID)).willReturn(API_FRAMEWORK);

		assertThat(regulatoryApiService.findFrameworkById(FRAMEWORK_ID)).isEqualTo(FRAMEWORK);
	}

	@Test
	void deleteFrameworkAsksTheClientToDeleteIt() {
		regulatoryApiService.deleteFramework(FRAMEWORK_ID);

		then(regulatoryApiClient).should().deleteFramework(FRAMEWORK_ID);
	}

}
