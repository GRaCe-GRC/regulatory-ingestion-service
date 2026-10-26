package io.github.gracegrc.regulatoryingestionservice.boot.client;

import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiAuthorityResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFileRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFileResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiFrameworkResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiRequirementRequestDto;
import io.github.gracegrc.regulatoryingestionservice.service.model.dto.RegulatoryApiRequirementResponseDto;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RegulatoryApiClient {

	private static final String AUTHORITIES_PATH = "/api/v1/authorities";

	private static final String FRAMEWORKS_PATH = "/api/v1/frameworks";

	private static final String FILES_PATH = "/api/v1/files";

	private static final String REQUIREMENTS_PATH = "/api/v1/requirements";

	private final RestClient regulatoryApiRestClient;

	public RegulatoryApiClient(RestClient regulatoryApiRestClient) {
		this.regulatoryApiRestClient = regulatoryApiRestClient;
	}

	public UUID createAuthority(RegulatoryApiAuthorityRequestDto request) {
		return create(AUTHORITIES_PATH, request, "authority");
	}

	public List<RegulatoryApiAuthorityResponseDto> findAllAuthorities() {
		return regulatoryApiRestClient.get()
			.uri(AUTHORITIES_PATH)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}

	public RegulatoryApiAuthorityResponseDto findAuthorityById(UUID id) {
		return regulatoryApiRestClient.get()
			.uri(AUTHORITIES_PATH + "/{id}", id)
			.retrieve()
			.body(RegulatoryApiAuthorityResponseDto.class);
	}

	public void deleteAuthority(UUID id) {
		delete(AUTHORITIES_PATH, id);
	}

	public UUID createFramework(RegulatoryApiFrameworkRequestDto request) {
		return create(FRAMEWORKS_PATH, request, "framework");
	}

	public List<RegulatoryApiFrameworkResponseDto> findAllFrameworks(UUID authorityId) {
		return regulatoryApiRestClient.get()
			.uri(builder -> builder.path(FRAMEWORKS_PATH)
				.queryParamIfPresent("authorityId", Optional.ofNullable(authorityId))
				.build())
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}

	public RegulatoryApiFrameworkResponseDto findFrameworkById(UUID id) {
		return regulatoryApiRestClient.get()
			.uri(FRAMEWORKS_PATH + "/{id}", id)
			.retrieve()
			.body(RegulatoryApiFrameworkResponseDto.class);
	}

	public void deleteFramework(UUID id) {
		delete(FRAMEWORKS_PATH, id);
	}

	public UUID createFile(RegulatoryApiFileRequestDto request) {
		return create(FILES_PATH, request, "file");
	}

	public List<RegulatoryApiFileResponseDto> findAllFiles(UUID frameworkId) {
		return regulatoryApiRestClient.get()
			.uri(builder -> builder.path(FILES_PATH)
				.queryParamIfPresent("frameworkId", Optional.ofNullable(frameworkId))
				.build())
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}

	public RegulatoryApiFileResponseDto findFileById(UUID id) {
		return regulatoryApiRestClient.get()
			.uri(FILES_PATH + "/{id}", id)
			.retrieve()
			.body(RegulatoryApiFileResponseDto.class);
	}

	public void deleteFile(UUID id) {
		delete(FILES_PATH, id);
	}

	public UUID createRequirement(RegulatoryApiRequirementRequestDto request) {
		return create(REQUIREMENTS_PATH, request, "requirement");
	}

	public List<RegulatoryApiRequirementResponseDto> findAllRequirements(UUID fileId, UUID parentId) {
		return regulatoryApiRestClient.get()
			.uri(builder -> builder.path(REQUIREMENTS_PATH)
				.queryParamIfPresent("fileId", Optional.ofNullable(fileId))
				.queryParamIfPresent("parentId", Optional.ofNullable(parentId))
				.build())
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}

	public RegulatoryApiRequirementResponseDto findRequirementById(UUID id) {
		return regulatoryApiRestClient.get()
			.uri(REQUIREMENTS_PATH + "/{id}", id)
			.retrieve()
			.body(RegulatoryApiRequirementResponseDto.class);
	}

	public void deleteRequirement(UUID id) {
		delete(REQUIREMENTS_PATH, id);
	}

	private UUID create(String path, Object request, String resource) {
		URI location = regulatoryApiRestClient.post()
			.uri(path)
			.body(request)
			.retrieve()
			.toBodilessEntity()
			.getHeaders()
			.getLocation();
		return idFrom(location, resource);
	}

	private void delete(String path, UUID id) {
		regulatoryApiRestClient.delete().uri(path + "/{id}", id).retrieve().toBodilessEntity();
	}

	private static UUID idFrom(URI location, String resource) {
		if (location == null) {
			throw new RestClientException("The Regulatory API did not return the location of the new " + resource);
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
