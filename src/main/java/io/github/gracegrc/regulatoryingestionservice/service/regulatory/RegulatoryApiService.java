package io.github.gracegrc.regulatoryingestionservice.service.regulatory;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import java.util.List;
import java.util.UUID;

public interface RegulatoryApiService {

	UUID createAuthority(RegulatoryAuthorityRequestDto request);

	List<RegulatoryAuthorityResponseDto> findAllAuthorities();

	RegulatoryAuthorityResponseDto findAuthorityById(UUID id);

	void deleteAuthority(UUID id);

}
