package io.github.gracegrc.regulatoryingestionservice.service.regulatory;

import io.github.gracegrc.regulatoryingestionservice.boot.client.RegulatoryApiClient;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
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
		return regulatoryApiClient.createAuthority(request);
	}

	@Override
	public List<RegulatoryAuthorityResponseDto> findAllAuthorities() {
		return regulatoryApiClient.findAllAuthorities();
	}

	@Override
	public RegulatoryAuthorityResponseDto findAuthorityById(UUID id) {
		return regulatoryApiClient.findAuthorityById(id);
	}

}
