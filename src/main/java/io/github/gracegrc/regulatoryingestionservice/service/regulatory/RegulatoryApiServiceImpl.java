package io.github.gracegrc.regulatoryingestionservice.service.regulatory;

import io.github.gracegrc.regulatoryingestionservice.boot.client.RegulatoryApiClient;
import io.github.gracegrc.regulatoryingestionservice.domain.mapper.RegulatoryAuthorityMapper;
import io.github.gracegrc.regulatoryingestionservice.domain.mapper.RegulatoryFileMapper;
import io.github.gracegrc.regulatoryingestionservice.domain.mapper.RegulatoryFrameworkMapper;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFileRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFileResponseDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkResponseDto;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RegulatoryApiServiceImpl implements RegulatoryApiService {

	private final RegulatoryApiClient regulatoryApiClient;

	public RegulatoryApiServiceImpl(RegulatoryApiClient regulatoryApiClient) {
		this.regulatoryApiClient = regulatoryApiClient;
	}

	@Override
	public UUID createAuthority(RegulatoryAuthorityRequestDto request) {
		return regulatoryApiClient.createAuthority(RegulatoryAuthorityMapper.toRegulatoryApiRequest(request));
	}

	@Override
	public List<RegulatoryAuthorityResponseDto> findAllAuthorities() {
		return regulatoryApiClient.findAllAuthorities().stream().map(RegulatoryAuthorityMapper::toResponseDto).toList();
	}

	@Override
	public RegulatoryAuthorityResponseDto findAuthorityById(UUID id) {
		return RegulatoryAuthorityMapper.toResponseDto(regulatoryApiClient.findAuthorityById(id));
	}

	@Override
	public void deleteAuthority(UUID id) {
		regulatoryApiClient.deleteAuthority(id);
	}

	@Override
	public UUID createFramework(RegulatoryFrameworkRequestDto request) {
		return regulatoryApiClient.createFramework(RegulatoryFrameworkMapper.toRegulatoryApiRequest(request));
	}

	@Override
	public List<RegulatoryFrameworkResponseDto> findAllFrameworks(UUID authorityId) {
		return regulatoryApiClient.findAllFrameworks(authorityId)
			.stream()
			.map(RegulatoryFrameworkMapper::toResponseDto)
			.toList();
	}

	@Override
	public RegulatoryFrameworkResponseDto findFrameworkById(UUID id) {
		return RegulatoryFrameworkMapper.toResponseDto(regulatoryApiClient.findFrameworkById(id));
	}

	@Override
	public void deleteFramework(UUID id) {
		regulatoryApiClient.deleteFramework(id);
	}

	@Override
	public UUID createFile(RegulatoryFileRequestDto request) {
		return regulatoryApiClient.createFile(RegulatoryFileMapper.toRegulatoryApiRequest(request));
	}

	@Override
	public List<RegulatoryFileResponseDto> findAllFiles(UUID frameworkId) {
		return regulatoryApiClient.findAllFiles(frameworkId).stream().map(RegulatoryFileMapper::toResponseDto).toList();
	}

	@Override
	public RegulatoryFileResponseDto findFileById(UUID id) {
		return RegulatoryFileMapper.toResponseDto(regulatoryApiClient.findFileById(id));
	}

	@Override
	public void deleteFile(UUID id) {
		regulatoryApiClient.deleteFile(id);
	}

}
