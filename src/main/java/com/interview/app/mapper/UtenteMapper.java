package com.interview.app.mapper;

import com.interview.app.dto.UtenteDTO;
import com.interview.app.entity.Utente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UtenteMapper {

    /**
     * Mappa Utente → UtenteDTO.
     * Il campo 'password' è escluso dalla mappatura per sicurezza.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    UtenteDTO toDto(Utente utente);
}
