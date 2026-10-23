package io.github.gracegrc.regulatoryingestionservice.service.regulatory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.gracegrc.regulatoryingestionservice.boot.client.RegulatoryApiClient;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiAuthorityResponseDto;
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

}
