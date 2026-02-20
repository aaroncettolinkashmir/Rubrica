package com.interview.app.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaUpdateDTO {

    @Size(max = 100)
    private String nome;

    @Size(max = 100)
    private String cognome;

    @Size(max = 255)
    private String indirizzo;

    private String telefono;

    @Min(value = 0, message = "L'età non può essere negativa")
    @Max(value = 150, message = "L'età non è plausibile")
    private Integer eta;
}
