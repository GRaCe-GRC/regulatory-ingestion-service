package io.github.gracegrc.regulatoryingestionservice.boot.client;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RegulatoryApiClient {

	private static final String AUTHORITIES_PATH = "/api/v1/authorities";

	private final RestClient regulatoryApiRestClient;

	public RegulatoryApiClient(RestClient regulatoryApiRestClient) {
		this.regulatoryApiRestClient = regulatoryApiRestClient;
	}

	public UUID createAuthority(RegulatoryAuthorityRequestDto request) {
		URI location = regulatoryApiRestClient.post()
			.uri(AUTHORITIES_PATH)
			.body(request)
			.retrieve()
			.toBodilessEntity()
			.getHeaders()
			.getLocation();
		return idFrom(location);
	}

	public List<RegulatoryAuthorityResponseDto> findAllAuthorities() {
		return regulatoryApiRestClient.get()
			.uri(AUTHORITIES_PATH)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}

	public RegulatoryAuthorityResponseDto findAuthorityById(UUID id) {
		return regulatoryApiRestClient.get()
			.uri(AUTHORITIES_PATH + "/{id}", id)
			.retrieve()
			.body(RegulatoryAuthorityResponseDto.class);
	}

	private static UUID idFrom(URI location) {
		if (location == null) {
			throw new RestClientException("The Regulatory API did not return the location of the new authority");
		}
		String path = location.getPath();
		try {
			return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
		}
		catch (IllegalArgumentException ex) {
			throw new RestClientException("The Regulatory API returned an invalid location: " + location, ex);
		}
	}

}
