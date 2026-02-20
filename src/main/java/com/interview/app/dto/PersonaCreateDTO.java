package com.interview.app.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaCreateDTO {

    @NotBlank(message = "Il nome non può essere vuoto")
    @Size(max = 100)
    private String nome;

    @NotBlank(message = "Il cognome non può essere vuoto")
    @Size(max = 100)
    private String cognome;

    @Size(max = 255)
    private String indirizzo;

    @NotBlank(message = "Il telefono non può essere vuoto")
    private String telefono;

    @Min(value = 0, message = "L'età non può essere negativa")
    @Max(value = 150, message = "L'età non è plausibile")
    private Integer eta;
}
