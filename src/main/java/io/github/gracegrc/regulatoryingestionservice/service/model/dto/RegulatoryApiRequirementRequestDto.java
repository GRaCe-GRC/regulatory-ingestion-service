package io.github.gracegrc.regulatoryingestionservice.service.model.dto;

import java.util.UUID;

public record RegulatoryApiRequirementRequestDto(
		UUID fileId,
		UUID parentRequirementId,
		String provisionRef,
		String heading,
		String text,
		String createdBy) {
}
