package io.github.gracegrc.regulatoryingestionservice.service.model.dto;

import java.time.LocalDate;
import java.util.UUID;

public record RegulatoryApiFileRequestDto(
		UUID frameworkId,
		String title,
		String sourceUrl,
		String versionLabel,
		LocalDate publicationDate,
		String createdBy) {
}
