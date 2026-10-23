package io.github.gracegrc.regulatoryingestionservice.service.model.dto;

import java.time.Instant;
import java.util.UUID;

public record RegulatoryApiAuthorityResponseDto(
		UUID id,
		String name,
		String jurisdiction,
		Instant createdAt,
		boolean enabled,
		String createdBy) {
}
