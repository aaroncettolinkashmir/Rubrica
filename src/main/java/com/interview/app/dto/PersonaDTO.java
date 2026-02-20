package com.interview.app.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaDTO {

    private Long id;
    private String nome;
    private String cognome;
    private String indirizzo;
    private String telefono;
    private Integer eta;
}
