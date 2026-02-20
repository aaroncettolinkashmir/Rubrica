package com.interview.app.service.impl;

import com.interview.app.dto.PagedResponseDTO;
import com.interview.app.dto.PersonaCreateDTO;
import com.interview.app.dto.PersonaDTO;
import com.interview.app.dto.PersonaFilterDTO;
import com.interview.app.dto.PersonaUpdateDTO;
import com.interview.app.entity.Persona;
import com.interview.app.exception.ResourceNotFoundException;
import com.interview.app.mapper.PersonaMapper;
import com.interview.app.repository.PersonaRepository;
import com.interview.app.service.FileExportService;
import com.interview.app.service.PersonaService;
import com.interview.app.specification.PersonaSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;
    private final PersonaMapper personaMapper;
    private final FileExportService fileExportService;

    @Override
    public PersonaDTO findById(Long id) {
        log.debug("Ricerca persona con id: {}", id);
        return personaRepository.findById(id)
                .map(personaMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Persona", id));
    }

    @Override
    public List<PersonaDTO> findAll() {
        log.debug("Recupero tutte le persone");
        return personaMapper.toDtoList(personaRepository.findAll());
    }

    @Override
    public PagedResponseDTO<PersonaDTO> findAll(PersonaFilterDTO filter, Pageable pageable) {
        log.debug("Ricerca persone con filtri: {} e paginazione: {}", filter, pageable);

        Page<Persona> page = personaRepository.findAll(
                PersonaSpecification.buildFilter(filter),
                pageable);

        return PagedResponseDTO.<PersonaDTO>builder()
                .content(personaMapper.toDtoList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public PersonaDTO create(PersonaCreateDTO dto) {
        log.info("Creazione nuova persona: {} {}", dto.getNome(), dto.getCognome());
        Persona persona = personaMapper.toEntity(dto);
        Persona saved = personaRepository.save(persona);
        log.info("Persona creata con id: {}", saved.getId());
        return personaMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PersonaDTO update(Long id, PersonaUpdateDTO dto) {
        log.info("Aggiornamento persona id: {}", id);
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona", id));

        // MapStruct aggiorna solo i campi non-null del DTO
        personaMapper.updateEntityFromDto(dto, persona);
        Persona saved = personaRepository.save(persona);
        log.info("Persona id {} aggiornata", id);
        return personaMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Eliminazione persona id: {}", id);
        if (!personaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Persona", id);
        }
        personaRepository.deleteById(id);
        log.info("Persona id {} eliminata", id);
    }

    @Override
    @Transactional
    public void exportToFile(Long id) {
        log.info("Esportazione persona id: {} in file", id);
        PersonaDTO dto = findById(id); // lancia ResourceNotFoundException se non trovata
        fileExportService.export(dto);
    }
}
