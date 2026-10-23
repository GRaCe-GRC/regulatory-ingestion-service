package io.github.gracegrc.regulatoryingestionservice.domain.controller;

import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityRequestDto;
import io.github.gracegrc.regulatoryingestionservice.domain.model.dto.RegulatoryAuthorityResponseDto;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/authorities")
public class RegulatoryIngestionServiceController {

	private final RegulatoryApiService regulatoryApiService;

	public RegulatoryIngestionServiceController(RegulatoryApiService regulatoryApiService) {
		this.regulatoryApiService = regulatoryApiService;
	}

	@PostMapping
	public ResponseEntity<Void> create(@Valid @RequestBody RegulatoryAuthorityRequestDto request) {
		UUID id = regulatoryApiService.createAuthority(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).build();
	}

	@GetMapping
	public List<RegulatoryAuthorityResponseDto> findAll() {
		return regulatoryApiService.findAllAuthorities();
	}

	@GetMapping("/{id}")
	public RegulatoryAuthorityResponseDto findById(@PathVariable UUID id) {
		return regulatoryApiService.findAuthorityById(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		regulatoryApiService.deleteAuthority(id);
	}

}
