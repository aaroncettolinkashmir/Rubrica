package com.interview.app.controller;

import com.interview.app.dto.LoginRequestDTO;
import com.interview.app.dto.UtenteDTO;
import com.interview.app.service.UtenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticazione", description = "API per login e registrazione utenti")
public class AuthController {

    private final UtenteService utenteService;

    @PostMapping("/login")
    @Operation(
        summary = "Login utente",
        description = "Autentica l'utente con username e password. " +
                      "Default: admin / admin"
    )
    @ApiResponse(responseCode = "200", description = "Login riuscito - restituisce UtenteDTO")
    @ApiResponse(responseCode = "401", description = "Credenziali non valide")
    public ResponseEntity<UtenteDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(utenteService.login(dto));
    }

    @PostMapping("/register")
    @Operation(
        summary = "Registra nuovo utente",
        description = "Registra un nuovo utente. La password viene hashata con BCrypt."
    )
    @ApiResponse(responseCode = "201", description = "Utente registrato con successo")
    @ApiResponse(responseCode = "409", description = "Username già in uso")
    public ResponseEntity<UtenteDTO> register(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(utenteService.register(dto));
    }
}
