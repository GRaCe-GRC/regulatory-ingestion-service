package io.github.gracegrc.regulatoryingestionservice.domain.controller;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryFrameworkResponseDto;
import io.github.gracegrc.regulatoryingestionservice.service.regulatory.RegulatoryApiService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1")
public class RegulatoryIngestionServiceController {

	private final RegulatoryApiService regulatoryApiService;

	public RegulatoryIngestionServiceController(RegulatoryApiService regulatoryApiService) {
		this.regulatoryApiService = regulatoryApiService;
	}

	@PostMapping("/authorities")
	public ResponseEntity<Void> createAuthority(@Valid @RequestBody RegulatoryAuthorityRequestDto request) {
		return created(regulatoryApiService.createAuthority(request));
	}

	@GetMapping("/authorities")
	public List<RegulatoryAuthorityResponseDto> findAllAuthorities() {
		return regulatoryApiService.findAllAuthorities();
	}

	@GetMapping("/authorities/{id}")
	public RegulatoryAuthorityResponseDto findAuthorityById(@PathVariable UUID id) {
		return regulatoryApiService.findAuthorityById(id);
	}

	@DeleteMapping("/authorities/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAuthority(@PathVariable UUID id) {
		regulatoryApiService.deleteAuthority(id);
	}

	@PostMapping("/frameworks")
	public ResponseEntity<Void> createFramework(@Valid @RequestBody RegulatoryFrameworkRequestDto request) {
		return created(regulatoryApiService.createFramework(request));
	}

	@GetMapping("/frameworks")
	public List<RegulatoryFrameworkResponseDto> findAllFrameworks(@RequestParam(required = false) UUID authorityId) {
		return regulatoryApiService.findAllFrameworks(authorityId);
	}

	@GetMapping("/frameworks/{id}")
	public RegulatoryFrameworkResponseDto findFrameworkById(@PathVariable UUID id) {
		return regulatoryApiService.findFrameworkById(id);
	}

	@DeleteMapping("/frameworks/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteFramework(@PathVariable UUID id) {
		regulatoryApiService.deleteFramework(id);
	}

	private static ResponseEntity<Void> created(UUID id) {
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

}
