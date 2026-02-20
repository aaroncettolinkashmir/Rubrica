package com.interview.app.mapper;

import com.interview.app.dto.PersonaCreateDTO;
import com.interview.app.dto.PersonaDTO;
import com.interview.app.dto.PersonaUpdateDTO;
import com.interview.app.entity.Persona;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper
// componentModel = "spring" è impostato globalmente via compiler arg -Amapstruct.defaultComponentModel=spring
public interface PersonaMapper {

    PersonaDTO toDto(Persona persona);

    /**
     * PersonaCreateDTO non ha id: ignorato esplicitamente.
     * L'id viene assegnato dal database (IDENTITY).
     */
    @Mapping(target = "id", ignore = true)
    Persona toEntity(PersonaCreateDTO dto);

    /**
     * Aggiornamento parziale: i campi null nel DTO NON sovrascrivono l'entità.
     * L'id dell'entità non viene mai modificato.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(PersonaUpdateDTO dto, @MappingTarget Persona persona);

    List<PersonaDTO> toDtoList(List<Persona> personas);
}
