package io.github.gracegrc.regulatoryingestionservice.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFrameworkResponseDto;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegulatoryFrameworkMapperTest {

	private static final UUID ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final UUID AUTHORITY_ID = UUID.fromString("3f2c1a9e-7d4b-4e8a-9c1f-2b6d5e8a7c40");

	@Test
	void toRegulatoryApiRequestCopiesEveryField() {
		RegulatoryApiFrameworkRequestDto request = RegulatoryFrameworkMapper
			.toRegulatoryApiRequest(new RegulatoryFrameworkRequestDto(AUTHORITY_ID, "Capital Requirements Regulation",
					"CRR", "Regulation (EU) No 575/2013", "admin-console"));

		assertThat(request).isEqualTo(new RegulatoryApiFrameworkRequestDto(AUTHORITY_ID,
				"Capital Requirements Regulation", "CRR", "Regulation (EU) No 575/2013", "admin-console"));
	}

	@Test
	void toResponseDtoCopiesEveryField() {
		Instant createdAt = Instant.parse("2026-10-01T10:15:30Z");

		RegulatoryFrameworkResponseDto response = RegulatoryFrameworkMapper
			.toResponseDto(new RegulatoryApiFrameworkResponseDto(ID, AUTHORITY_ID, "Capital Requirements Regulation",
					"CRR", "Regulation (EU) No 575/2013", false, createdAt, "admin-console"));

		assertThat(response).isEqualTo(new RegulatoryFrameworkResponseDto(ID, AUTHORITY_ID,
				"Capital Requirements Regulation", "CRR", "Regulation (EU) No 575/2013", false, createdAt,
				"admin-console"));
	}

}
