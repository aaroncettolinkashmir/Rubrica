package com.interview.app.service;

import com.interview.app.dto.LoginRequestDTO;
import com.interview.app.dto.UtenteDTO;

public interface UtenteService {

    /**
     * Autentica un utente.
     *
     * @param dto username e password in chiaro
     * @return UtenteDTO se le credenziali sono corrette
     * @throws com.interview.app.exception.InvalidCredentialsException se le credenziali sono errate
     */
    UtenteDTO login(LoginRequestDTO dto);

    /**
     * Registra un nuovo utente.
     *
     * @param dto username e password in chiaro (verrà hashata con BCrypt)
     * @return UtenteDTO dell'utente creato
     * @throws com.interview.app.exception.DuplicateUsernameException se l'username è già in uso
     */
    UtenteDTO register(LoginRequestDTO dto);

    /**
     * Cerca un utente per username.
     *
     * @param username l'username da cercare
     * @return UtenteDTO
     * @throws com.interview.app.exception.ResourceNotFoundException se non trovato
     */
    UtenteDTO findByUsername(String username);
}
