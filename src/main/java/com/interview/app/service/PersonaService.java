package com.interview.app.service;

import com.interview.app.dto.PagedResponseDTO;
import com.interview.app.dto.PersonaCreateDTO;
import com.interview.app.dto.PersonaDTO;
import com.interview.app.dto.PersonaFilterDTO;
import com.interview.app.dto.PersonaUpdateDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PersonaService {

    /**
     * Cerca una persona per ID.
     *
     * @param id l'ID della persona
     * @return PersonaDTO
     * @throws com.interview.app.exception.ResourceNotFoundException se non trovata
     */
    PersonaDTO findById(Long id);

    /**
     * Restituisce tutte le persone senza filtro e senza paginazione.
     * Usato dalla Swing UI per caricare la JTable.
     *
     * @return lista di PersonaDTO
     */
    List<PersonaDTO> findAll();

    /**
     * Restituisce le persone con filtri dinamici e paginazione.
     * Usato dalla REST API.
     *
     * @param filter parametri di filtro (tutti opzionali)
     * @param pageable parametri di paginazione e ordinamento
     * @return pagina di PersonaDTO
     */
    PagedResponseDTO<PersonaDTO> findAll(PersonaFilterDTO filter, Pageable pageable);

    /**
     * Crea una nuova persona.
     *
     * @param dto dati della nuova persona
     * @return PersonaDTO salvata con ID generato
     */
    PersonaDTO create(PersonaCreateDTO dto);

    /**
     * Aggiorna una persona esistente.
     * I campi null nel DTO non sovrascrivono i dati esistenti.
     *
     * @param id  l'ID della persona da aggiornare
     * @param dto dati aggiornati (campi null ignorati)
     * @return PersonaDTO aggiornata
     * @throws com.interview.app.exception.ResourceNotFoundException se non trovata
     */
    PersonaDTO update(Long id, PersonaUpdateDTO dto);

    /**
     * Elimina una persona per ID.
     *
     * @param id l'ID della persona da eliminare
     * @throws com.interview.app.exception.ResourceNotFoundException se non trovata
     */
    void delete(Long id);

    /**
     * Esporta i dati di una persona in un file txt (EXTRA #1).
     * Salva in: informazioni/NOME-COGNOME-{id}.txt
     *
     * @param id l'ID della persona da esportare
     * @throws com.interview.app.exception.ResourceNotFoundException se non trovata
     */
    void exportToFile(Long id);
}
