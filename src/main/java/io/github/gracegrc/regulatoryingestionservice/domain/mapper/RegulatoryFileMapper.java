package io.github.gracegrc.regulatoryingestionservice.domain.mapper;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFileRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFileResponseDto;

public final class RegulatoryFileMapper {

	private RegulatoryFileMapper() {
	}

	public static RegulatoryApiFileRequestDto toRegulatoryApiRequest(RegulatoryFileRequestDto request) {
		return new RegulatoryApiFileRequestDto(request.frameworkId(), request.title(), request.sourceUrl(),
				request.versionLabel(), request.publicationDate(), request.createdBy());
	}

	public static RegulatoryFileResponseDto toResponseDto(RegulatoryApiFileResponseDto file) {
		return new RegulatoryFileResponseDto(file.id(), file.frameworkId(), file.title(), file.sourceUrl(),
				file.versionLabel(), file.publicationDate(), file.createdAt(), file.createdBy());
	}

}
