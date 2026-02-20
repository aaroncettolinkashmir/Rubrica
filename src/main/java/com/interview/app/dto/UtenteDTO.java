package com.interview.app.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtenteDTO {

    private Long id;
    private String username;
    // password è SEMPRE esclusa dalle risposte
}
