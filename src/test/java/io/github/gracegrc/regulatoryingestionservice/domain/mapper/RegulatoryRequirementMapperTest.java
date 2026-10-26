package io.github.gracegrc.regulatoryingestionservice.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryRequirementRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryRequirementResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiRequirementRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiRequirementResponseDto;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegulatoryRequirementMapperTest {

	private static final UUID ID = UUID.fromString("e1f2a3b4-c5d6-4e7f-8a9b-0c1d2e3f4a50");

	private static final UUID FILE_ID = UUID.fromString("c4d5e6f7-1a2b-4c3d-8e9f-0a1b2c3d4e50");

	private static final UUID PARENT_ID = UUID.fromString("a9b8c7d6-e5f4-4a3b-9c2d-1e0f9a8b7c60");

	@Test
	void toRegulatoryApiRequestCopiesEveryField() {
		RegulatoryApiRequirementRequestDto request = RegulatoryRequirementMapper.toRegulatoryApiRequest(
				new RegulatoryRequirementRequestDto(FILE_ID, PARENT_ID, "164.308(a)(4)(i)", "Heading", "Text",
						"admin-console"));

		assertThat(request).isEqualTo(new RegulatoryApiRequirementRequestDto(FILE_ID, PARENT_ID, "164.308(a)(4)(i)",
				"Heading", "Text", "admin-console"));
	}

	@Test
	void toResponseDtoCopiesEveryField() {
		Instant createdAt = Instant.parse("2026-10-01T10:15:30Z");

		RegulatoryRequirementResponseDto response = RegulatoryRequirementMapper.toResponseDto(
				new RegulatoryApiRequirementResponseDto(ID, FILE_ID, PARENT_ID, "164.308(a)(4)(i)", "Heading", "Text",
						createdAt, "admin-console"));

		assertThat(response).isEqualTo(new RegulatoryRequirementResponseDto(ID, FILE_ID, PARENT_ID, "164.308(a)(4)(i)",
				"Heading", "Text", createdAt, "admin-console"));
	}

}
