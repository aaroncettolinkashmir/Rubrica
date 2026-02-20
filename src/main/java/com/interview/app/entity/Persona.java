package com.interview.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "persona")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Il nome non può essere vuoto")
    @Size(max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Il cognome non può essere vuoto")
    @Size(max = 100)
    @Column(name = "cognome", nullable = false, length = 100)
    private String cognome;

    @Size(max = 255)
    @Column(name = "indirizzo")
    private String indirizzo;

    @NotBlank(message = "Il telefono non può essere vuoto")
    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Min(value = 0, message = "L'età non può essere negativa")
    @Max(value = 150, message = "L'età non è plausibile")
    @Column(name = "eta")
    private Integer eta;
}
