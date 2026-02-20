package com.interview.app.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaFilterDTO {

    private String nome;       // contains (case-insensitive)
    private String cognome;    // contains (case-insensitive)
    private String telefono;   // contains
    private Integer etaMin;    // eta >= etaMin
    private Integer etaMax;    // eta <= etaMax
}
