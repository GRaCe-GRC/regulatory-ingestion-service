package io.github.gracegrc.regulatoryingestionservice.domain.mapper;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiAuthorityResponseDto;

public final class RegulatoryAuthorityMapper {

	private RegulatoryAuthorityMapper() {
	}

	public static RegulatoryApiAuthorityRequestDto toRegulatoryApiRequest(RegulatoryAuthorityRequestDto request) {
		return new RegulatoryApiAuthorityRequestDto(request.name(), request.jurisdiction(), request.createdBy());
	}

	public static RegulatoryAuthorityResponseDto toResponseDto(RegulatoryApiAuthorityResponseDto authority) {
		return new RegulatoryAuthorityResponseDto(authority.id(), authority.name(), authority.jurisdiction(),
				authority.createdAt(), authority.enabled(), authority.createdBy());
	}

}
