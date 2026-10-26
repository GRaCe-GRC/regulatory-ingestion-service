package io.github.gracegrc.regulatoryingestionservice.service.model.dto;

import java.util.UUID;

public record RegulatoryApiFrameworkRequestDto(
		UUID authorityId,
		String name,
		String shortName,
		String citation,
		String createdBy) {
}
