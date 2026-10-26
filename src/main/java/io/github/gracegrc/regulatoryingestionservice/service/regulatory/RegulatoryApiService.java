package io.github.gracegrc.regulatoryingestionservice.service.regulatory;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkResponseDto;
import java.util.List;
import java.util.UUID;

public interface RegulatoryApiService {

	UUID createAuthority(RegulatoryAuthorityRequestDto request);

	List<RegulatoryAuthorityResponseDto> findAllAuthorities();

	RegulatoryAuthorityResponseDto findAuthorityById(UUID id);

	void deleteAuthority(UUID id);

	UUID createFramework(RegulatoryFrameworkRequestDto request);

	List<RegulatoryFrameworkResponseDto> findAllFrameworks(UUID authorityId);

	RegulatoryFrameworkResponseDto findFrameworkById(UUID id);

	void deleteFramework(UUID id);

	UUID createFile(RegulatoryFileRequestDto request);

	List<RegulatoryFileResponseDto> findAllFiles(UUID frameworkId);

	RegulatoryFileResponseDto findFileById(UUID id);

	void deleteFile(UUID id);

}
