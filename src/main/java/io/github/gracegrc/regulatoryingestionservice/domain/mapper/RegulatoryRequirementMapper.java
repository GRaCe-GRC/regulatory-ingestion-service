package io.github.gracegrc.regulatoryingestionservice.domain.mapper;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryRequirementRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryRequirementResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiRequirementRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiRequirementResponseDto;

public final class RegulatoryRequirementMapper {

	private RegulatoryRequirementMapper() {
	}

	public static RegulatoryApiRequirementRequestDto toRegulatoryApiRequest(RegulatoryRequirementRequestDto request) {
		return new RegulatoryApiRequirementRequestDto(request.fileId(), request.parentRequirementId(),
				request.provisionRef(), request.heading(), request.text(), request.createdBy());
	}

	public static RegulatoryRequirementResponseDto toResponseDto(RegulatoryApiRequirementResponseDto requirement) {
		return new RegulatoryRequirementResponseDto(requirement.id(), requirement.fileId(),
				requirement.parentRequirementId(), requirement.provisionRef(), requirement.heading(),
				requirement.text(), requirement.createdAt(), requirement.createdBy());
	}

}
