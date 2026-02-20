package com.interview.app.service;

import com.interview.app.dto.PagedResponseDTO;
import com.interview.app.dto.PersonaCreateDTO;
import com.interview.app.dto.PersonaDTO;
import com.interview.app.dto.PersonaFilterDTO;
import com.interview.app.dto.PersonaUpdateDTO;
import com.interview.app.entity.Persona;
import com.interview.app.exception.ResourceNotFoundException;
import com.interview.app.mapper.PersonaMapper;
import com.interview.app.repository.PersonaRepository;
import com.interview.app.service.impl.PersonaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonaService - Unit Tests")
class PersonaServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private PersonaMapper personaMapper;

    @Mock
    private FileExportService fileExportService;

    @InjectMocks
    private PersonaServiceImpl personaService;

    private Persona persona;
    private PersonaDTO personaDTO;

    @BeforeEach
    void setUp() {
        persona = Persona.builder()
                .id(1L)
                .nome("Mario")
                .cognome("Rossi")
                .indirizzo("Via Roma 1")
                .telefono("3331234567")
                .eta(30)
                .build();

        personaDTO = PersonaDTO.builder()
                .id(1L)
                .nome("Mario")
                .cognome("Rossi")
                .indirizzo("Via Roma 1")
                .telefono("3331234567")
                .eta(30)
                .build();
    }

    // =================== findById ===================

    @Test
    @DisplayName("findById: restituisce DTO quando la persona esiste")
    void findById_whenExists_returnsDto() {
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(personaMapper.toDto(persona)).thenReturn(personaDTO);

        PersonaDTO result = personaService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNome()).isEqualTo("Mario");
        assertThat(result.getCognome()).isEqualTo("Rossi");
        verify(personaRepository).findById(1L);
        verify(personaMapper).toDto(persona);
    }

    @Test
    @DisplayName("findById: lancia ResourceNotFoundException quando non trovata")
    void findById_whenNotFound_throwsResourceNotFoundException() {
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(personaMapper, never()).toDto(any());
    }

    // =================== findAll (lista) ===================

    @Test
    @DisplayName("findAll: restituisce lista vuota quando non ci sono persone")
    void findAll_whenEmpty_returnsEmptyList() {
        when(personaRepository.findAll()).thenReturn(Collections.emptyList());
        when(personaMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<PersonaDTO> result = personaService.findAll();

        assertThat(result).isEmpty();
        verify(personaRepository).findAll();
    }

    @Test
    @DisplayName("findAll: restituisce lista di DTO mappati")
    void findAll_whenPersonasExist_returnsMappedDtoList() {
        List<Persona> personas = List.of(persona);
        List<PersonaDTO> dtos = List.of(personaDTO);

        when(personaRepository.findAll()).thenReturn(personas);
        when(personaMapper.toDtoList(personas)).thenReturn(dtos);

        List<PersonaDTO> result = personaService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("Mario");
        verify(personaMapper).toDtoList(personas);
    }

    // =================== findAll (paginato) ===================

    @Test
    @DisplayName("findAll paginato: restituisce PagedResponseDTO con metadati corretti")
    void findAllPaged_returnsCorrectPageMetadata() {
        PersonaFilterDTO filter = new PersonaFilterDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Persona> page = new PageImpl<>(List.of(persona), pageable, 1);

        when(personaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(personaMapper.toDtoList(List.of(persona))).thenReturn(List.of(personaDTO));

        PagedResponseDTO<PersonaDTO> result = personaService.findAll(filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("findAll paginato: filter null non genera NullPointerException")
    void findAllPaged_withNullFilter_doesNotThrow() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Persona> page = new PageImpl<>(List.of(persona), pageable, 1);

        when(personaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(personaMapper.toDtoList(any())).thenReturn(List.of(personaDTO));

        assertThatCode(() -> personaService.findAll(null, pageable)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("findAll paginato: pagina vuota restituisce lista content vuota")
    void findAllPaged_emptyPage_returnsEmptyContent() {
        PersonaFilterDTO filter = new PersonaFilterDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Persona> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(personaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);
        when(personaMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        PagedResponseDTO<PersonaDTO> result = personaService.findAll(filter, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.isLast()).isTrue();
    }

    // =================== create ===================

    @Test
    @DisplayName("create: salva l'entità e restituisce il DTO")
    void create_validDto_savesAndReturnsDto() {
        PersonaCreateDTO createDTO = PersonaCreateDTO.builder()
                .nome("Mario")
                .cognome("Rossi")
                .telefono("3331234567")
                .eta(30)
                .build();

        when(personaMapper.toEntity(createDTO)).thenReturn(persona);
        when(personaRepository.save(persona)).thenReturn(persona);
        when(personaMapper.toDto(persona)).thenReturn(personaDTO);

        PersonaDTO result = personaService.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getNome()).isEqualTo("Mario");
        verify(personaRepository).save(persona);
        verify(personaMapper).toDto(persona);
    }

    @Test
    @DisplayName("create: chiama repository.save esattamente una volta")
    void create_callsRepositorySaveExactlyOnce() {
        PersonaCreateDTO dto = PersonaCreateDTO.builder()
                .nome("Test").cognome("User").telefono("123456").build();

        when(personaMapper.toEntity(dto)).thenReturn(persona);
        when(personaRepository.save(any())).thenReturn(persona);
        when(personaMapper.toDto(any())).thenReturn(personaDTO);

        personaService.create(dto);

        verify(personaRepository, times(1)).save(any(Persona.class));
    }

    @Test
    @DisplayName("create: restituisce DTO con l'ID assegnato dal database")
    void create_returnsDtoWithGeneratedId() {
        PersonaCreateDTO dto = PersonaCreateDTO.builder()
                .nome("Nuovo").cognome("Utente").telefono("999").build();

        PersonaDTO expectedDto = PersonaDTO.builder().id(42L).nome("Nuovo").build();

        when(personaMapper.toEntity(dto)).thenReturn(persona);
        when(personaRepository.save(persona)).thenReturn(persona);
        when(personaMapper.toDto(persona)).thenReturn(expectedDto);

        PersonaDTO result = personaService.create(dto);

        assertThat(result.getId()).isEqualTo(42L);
    }

    // =================== update ===================

    @Test
    @DisplayName("update: aggiorna l'entità quando trovata e restituisce DTO aggiornato")
    void update_whenFound_updatesAndReturnsDto() {
        PersonaUpdateDTO updateDTO = PersonaUpdateDTO.builder()
                .nome("Luigi")
                .build();

        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(personaRepository.save(persona)).thenReturn(persona);
        when(personaMapper.toDto(persona)).thenReturn(personaDTO);

        PersonaDTO result = personaService.update(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(personaMapper).updateEntityFromDto(updateDTO, persona);
        verify(personaRepository).save(persona);
    }

    @Test
    @DisplayName("update: lancia ResourceNotFoundException quando l'ID non esiste")
    void update_whenNotFound_throwsResourceNotFoundException() {
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaService.update(99L, new PersonaUpdateDTO()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(personaMapper, never()).updateEntityFromDto(any(), any());
        verify(personaRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: chiama mapper.updateEntityFromDto per applicare le modifiche parziali")
    void update_callsMapperUpdateEntityFromDto() {
        PersonaUpdateDTO dto = PersonaUpdateDTO.builder().telefono("0000").build();

        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(personaRepository.save(persona)).thenReturn(persona);
        when(personaMapper.toDto(persona)).thenReturn(personaDTO);

        personaService.update(1L, dto);

        verify(personaMapper, times(1)).updateEntityFromDto(dto, persona);
    }

    // =================== delete ===================

    @Test
    @DisplayName("delete: elimina con successo quando la persona esiste")
    void delete_whenExists_deletesSuccessfully() {
        when(personaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(personaRepository).deleteById(1L);

        assertThatCode(() -> personaService.delete(1L)).doesNotThrowAnyException();

        verify(personaRepository).existsById(1L);
        verify(personaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: lancia ResourceNotFoundException quando non trovata")
    void delete_whenNotFound_throwsResourceNotFoundException() {
        when(personaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> personaService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(personaRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete: non chiama deleteById se la persona non esiste")
    void delete_whenNotFound_neverCallsDeleteById() {
        when(personaRepository.existsById(anyLong())).thenReturn(false);

        try {
            personaService.delete(100L);
        } catch (ResourceNotFoundException e) {
            // atteso
        }

        verify(personaRepository, never()).deleteById(anyLong());
    }

    // =================== exportToFile ===================

    @Test
    @DisplayName("exportToFile: delega a fileExportService con il DTO corretto")
    void exportToFile_whenFound_callsFileExportService() {
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(personaMapper.toDto(persona)).thenReturn(personaDTO);
        doNothing().when(fileExportService).export(personaDTO);

        personaService.exportToFile(1L);

        verify(fileExportService).export(personaDTO);
    }

    @Test
    @DisplayName("exportToFile: lancia ResourceNotFoundException quando la persona non esiste")
    void exportToFile_whenNotFound_throwsResourceNotFoundException() {
        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaService.exportToFile(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(fileExportService, never()).export(any());
    }

    @Test
    @DisplayName("exportToFile: il DTO passato a fileExportService ha l'ID corretto")
    void exportToFile_passesCorrectDtoToExportService() {
        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(personaMapper.toDto(persona)).thenReturn(personaDTO);
        doNothing().when(fileExportService).export(any());

        personaService.exportToFile(1L);

        ArgumentCaptor<PersonaDTO> captor = ArgumentCaptor.forClass(PersonaDTO.class);
        verify(fileExportService).export(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getNome()).isEqualTo("Mario");
    }
}
