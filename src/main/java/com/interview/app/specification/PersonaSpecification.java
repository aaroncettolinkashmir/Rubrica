package com.interview.app.specification;

import com.interview.app.dto.PersonaFilterDTO;
import com.interview.app.entity.Persona;
import org.springframework.data.jpa.domain.Specification;

public class PersonaSpecification {

    private PersonaSpecification() {
        // utility class - non istanziabile
    }

    /**
     * Filtra per nome ILIKE '%value%'
     */
    public static Specification<Persona> nomeContains(String value) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("nome")), "%" + value.toLowerCase() + "%");
    }

    /**
     * Filtra per cognome ILIKE '%value%'
     */
    public static Specification<Persona> cognomeContains(String value) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("cognome")), "%" + value.toLowerCase() + "%");
    }

    /**
     * Filtra per telefono LIKE '%value%'
     */
    public static Specification<Persona> telefonoContains(String value) {
        return (root, query, cb) ->
                cb.like(root.get("telefono"), "%" + value + "%");
    }

    /**
     * Filtra per eta >= min
     */
    public static Specification<Persona> etaGreaterThanOrEqual(Integer min) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("eta"), min);
    }

    /**
     * Filtra per eta <= max
     */
    public static Specification<Persona> etaLessThanOrEqual(Integer max) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("eta"), max);
    }

    /**
     * Costruisce uno Specification combinato AND da PersonaFilterDTO.
     * I campi null/blank vengono ignorati automaticamente.
     * Se filter è null o tutti i campi sono vuoti, restituisce Specification.where(null)
     * che equivale a nessun filtro (seleziona tutto).
     */
    public static Specification<Persona> buildFilter(PersonaFilterDTO filter) {
        Specification<Persona> spec = Specification.where(null);

        if (filter == null) {
            return spec;
        }

        if (filter.getNome() != null && !filter.getNome().isBlank()) {
            spec = spec.and(nomeContains(filter.getNome()));
        }
        if (filter.getCognome() != null && !filter.getCognome().isBlank()) {
            spec = spec.and(cognomeContains(filter.getCognome()));
        }
        if (filter.getTelefono() != null && !filter.getTelefono().isBlank()) {
            spec = spec.and(telefonoContains(filter.getTelefono()));
        }
        if (filter.getEtaMin() != null) {
            spec = spec.and(etaGreaterThanOrEqual(filter.getEtaMin()));
        }
        if (filter.getEtaMax() != null) {
            spec = spec.and(etaLessThanOrEqual(filter.getEtaMax()));
        }

        return spec;
    }
}
