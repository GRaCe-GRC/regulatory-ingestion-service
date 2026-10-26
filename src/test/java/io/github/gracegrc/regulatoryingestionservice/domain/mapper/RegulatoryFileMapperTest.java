package io.github.gracegrc.regulatoryingestionservice.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFileRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFileResponseDto;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegulatoryFileMapperTest {

	private static final UUID ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID FRAMEWORK_ID = UUID.fromString("8b1e4c2a-5f3d-4a7e-b9c6-1d2e3f4a5b60");

	private static final LocalDate PUBLISHED = LocalDate.parse("2024-07-09");

	@Test
	void toRegulatoryApiRequestCopiesEveryField() {
		RegulatoryApiFileRequestDto request = RegulatoryFileMapper.toRegulatoryApiRequest(new RegulatoryFileRequestDto(
				FRAMEWORK_ID, "CRR consolidated text", "https://example.org/crr.pdf", "2024-07", PUBLISHED,
				"admin-console"));

		assertThat(request).isEqualTo(new RegulatoryApiFileRequestDto(FRAMEWORK_ID, "CRR consolidated text",
				"https://example.org/crr.pdf", "2024-07", PUBLISHED, "admin-console"));
	}

	@Test
	void toResponseDtoCopiesEveryField() {
		Instant createdAt = Instant.parse("2026-10-01T10:15:30Z");

		RegulatoryFileResponseDto response = RegulatoryFileMapper.toResponseDto(new RegulatoryApiFileResponseDto(ID,
				FRAMEWORK_ID, "CRR consolidated text", "https://example.org/crr.pdf", "2024-07", PUBLISHED, createdAt,
				"admin-console"));

		assertThat(response).isEqualTo(new RegulatoryFileResponseDto(ID, FRAMEWORK_ID, "CRR consolidated text",
				"https://example.org/crr.pdf", "2024-07", PUBLISHED, createdAt, "admin-console"));
	}

}
