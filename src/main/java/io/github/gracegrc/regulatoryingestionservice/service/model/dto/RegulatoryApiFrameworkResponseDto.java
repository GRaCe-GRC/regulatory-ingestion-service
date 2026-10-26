package io.github.gracegrc.regulatoryingestionservice.service.model.dto;

import java.time.Instant;
import java.util.UUID;

public record RegulatoryApiFrameworkResponseDto(
		UUID id,
		UUID authorityId,
		String name,
		String shortName,
		String citation,
		boolean enabled,
		Instant createdAt,
		String createdBy) {
}
