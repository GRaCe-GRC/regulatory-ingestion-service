package io.github.gracegrc.regulatoryingestionservice.service.regulatory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.github.gracegrc.regulatoryingestionservice.boot.client.RegulatoryApiClient;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
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

	private static final RegulatoryAuthorityResponseDto AUTHORITY = new RegulatoryAuthorityResponseDto(ID, "EBA", "EU",
			Instant.parse("2026-10-01T10:15:30Z"), true, "admin-console");

	@Mock
	private RegulatoryApiClient regulatoryApiClient;

	@InjectMocks
	private RegulatoryApiServiceImpl regulatoryApiService;

	@Test
	void createAuthorityReturnsTheIdFromTheClient() {
		RegulatoryAuthorityRequestDto request = new RegulatoryAuthorityRequestDto("EBA", "EU", "admin-console");
		given(regulatoryApiClient.createAuthority(request)).willReturn(ID);

		assertThat(regulatoryApiService.createAuthority(request)).isEqualTo(ID);
	}

	@Test
	void findAllAuthoritiesReturnsTheListFromTheClient() {
		given(regulatoryApiClient.findAllAuthorities()).willReturn(List.of(AUTHORITY));

		assertThat(regulatoryApiService.findAllAuthorities()).containsExactly(AUTHORITY);
	}

	@Test
	void findAuthorityByIdReturnsTheAuthorityFromTheClient() {
		given(regulatoryApiClient.findAuthorityById(ID)).willReturn(AUTHORITY);

		assertThat(regulatoryApiService.findAuthorityById(ID)).isEqualTo(AUTHORITY);
	}

}
