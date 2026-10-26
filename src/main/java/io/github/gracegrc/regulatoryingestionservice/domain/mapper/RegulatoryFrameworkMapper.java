package io.github.gracegrc.regulatoryingestionservice.domain.mapper;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFrameworkResponseDto;

public final class RegulatoryFrameworkMapper {

	private RegulatoryFrameworkMapper() {
	}

	public static RegulatoryApiFrameworkRequestDto toRegulatoryApiRequest(RegulatoryFrameworkRequestDto request) {
		return new RegulatoryApiFrameworkRequestDto(request.authorityId(), request.name(), request.shortName(),
				request.citation(), request.createdBy());
	}

	public static RegulatoryFrameworkResponseDto toResponseDto(RegulatoryApiFrameworkResponseDto framework) {
		return new RegulatoryFrameworkResponseDto(framework.id(), framework.authorityId(), framework.name(),
				framework.shortName(), framework.citation(), framework.enabled(), framework.createdAt(),
				framework.createdBy());
	}

}
