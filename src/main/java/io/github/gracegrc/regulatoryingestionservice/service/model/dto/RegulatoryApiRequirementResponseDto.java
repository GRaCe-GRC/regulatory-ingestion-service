package io.github.gracegrc.regulatoryingestionservice.service.model.dto;

import java.time.Instant;
import java.util.UUID;

public record RegulatoryApiRequirementResponseDto(
		UUID id,
		UUID fileId,
		UUID parentRequirementId,
		String provisionRef,
		String heading,
		String text,
		Instant createdAt,
		String createdBy) {
}
