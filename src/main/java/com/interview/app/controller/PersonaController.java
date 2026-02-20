package com.interview.app.controller;

import com.interview.app.dto.PagedResponseDTO;
import com.interview.app.dto.PersonaCreateDTO;
import com.interview.app.dto.PersonaDTO;
import com.interview.app.dto.PersonaFilterDTO;
import com.interview.app.dto.PersonaUpdateDTO;
import com.interview.app.service.PersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/persone")
@RequiredArgsConstructor
@Tag(name = "Persona", description = "API CRUD per la gestione della rubrica telefonica")
public class PersonaController {

    private final PersonaService personaService;

    @GetMapping
    @Operation(
        summary = "Elenca persone con filtri e paginazione",
        description = "Restituisce una lista paginata di persone. " +
                      "Tutti i parametri di filtro sono opzionali. " +
                      "I filtri su nome/cognome/telefono sono case-insensitive e usano LIKE '%value%'."
    )
    @ApiResponse(responseCode = "200", description = "Lista persone restituita con successo")
    public ResponseEntity<PagedResponseDTO<PersonaDTO>> getAll(
            @Parameter(description = "Filtro per nome (contains)") @RequestParam(required = false) String nome,
            @Parameter(description = "Filtro per cognome (contains)") @RequestParam(required = false) String cognome,
            @Parameter(description = "Filtro per telefono (contains)") @RequestParam(required = false) String telefono,
            @Parameter(description = "Filtro età minima") @RequestParam(required = false) Integer etaMin,
            @Parameter(description = "Filtro età massima") @RequestParam(required = false) Integer etaMax,
            @Parameter(description = "Numero pagina (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Dimensione pagina") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo di ordinamento") @RequestParam(defaultValue = "cognome") String sortBy,
            @Parameter(description = "Direzione ordinamento: asc | desc") @RequestParam(defaultValue = "asc") String sortDir) {

        PersonaFilterDTO filter = PersonaFilterDTO.builder()
                .nome(nome)
                .cognome(cognome)
                .telefono(telefono)
                .etaMin(etaMin)
                .etaMax(etaMax)
                .build();

        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(personaService.findAll(filter, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera persona per ID")
    @ApiResponse(responseCode = "200", description = "Persona trovata")
    @ApiResponse(responseCode = "404", description = "Persona non trovata")
    public ResponseEntity<PersonaDTO> getById(
            @Parameter(description = "ID della persona") @PathVariable Long id) {
        return ResponseEntity.ok(personaService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crea nuova persona")
    @ApiResponse(responseCode = "201", description = "Persona creata con successo")
    @ApiResponse(responseCode = "400", description = "Dati non validi")
    public ResponseEntity<PersonaDTO> create(@Valid @RequestBody PersonaCreateDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(personaService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Aggiorna persona",
        description = "Aggiornamento parziale: i campi null nel body vengono ignorati."
    )
    @ApiResponse(responseCode = "200", description = "Persona aggiornata con successo")
    @ApiResponse(responseCode = "404", description = "Persona non trovata")
    public ResponseEntity<PersonaDTO> update(
            @Parameter(description = "ID della persona") @PathVariable Long id,
            @Valid @RequestBody PersonaUpdateDTO dto) {
        return ResponseEntity.ok(personaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina persona")
    @ApiResponse(responseCode = "204", description = "Persona eliminata con successo")
    @ApiResponse(responseCode = "404", description = "Persona non trovata")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID della persona") @PathVariable Long id) {
        personaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/export")
    @Operation(
        summary = "Esporta persona in file TXT (EXTRA #1)",
        description = "Crea/sovrascrive il file: informazioni/NOME-COGNOME-{id}.txt"
    )
    @ApiResponse(responseCode = "200", description = "Esportazione completata")
    @ApiResponse(responseCode = "404", description = "Persona non trovata")
    public ResponseEntity<Void> exportToFile(
            @Parameter(description = "ID della persona") @PathVariable Long id) {
        personaService.exportToFile(id);
        return ResponseEntity.ok().build();
    }
}
