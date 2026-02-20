package com.interview.app.service.impl;

import com.interview.app.dto.LoginRequestDTO;
import com.interview.app.dto.UtenteDTO;
import com.interview.app.entity.Utente;
import com.interview.app.exception.DuplicateUsernameException;
import com.interview.app.exception.InvalidCredentialsException;
import com.interview.app.exception.ResourceNotFoundException;
import com.interview.app.mapper.UtenteMapper;
import com.interview.app.repository.UtenteRepository;
import com.interview.app.service.UtenteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UtenteServiceImpl implements UtenteService {

    private final UtenteRepository utenteRepository;
    private final UtenteMapper utenteMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UtenteDTO login(LoginRequestDTO dto) {
        log.debug("Tentativo di login per: {}", dto.getUsername());

        Utente utente = utenteRepository.findByUsername(dto.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(dto.getPassword(), utente.getPassword())) {
            log.warn("Password errata per l'utente: {}", dto.getUsername());
            throw new InvalidCredentialsException();
        }

        log.info("Login riuscito per: {}", dto.getUsername());
        return utenteMapper.toDto(utente);
    }

    @Override
    @Transactional
    public UtenteDTO register(LoginRequestDTO dto) {
        log.info("Registrazione nuovo utente: {}", dto.getUsername());

        if (utenteRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateUsernameException(dto.getUsername());
        }

        Utente utente = Utente.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        Utente saved = utenteRepository.save(utente);
        log.info("Utente registrato con id: {}", saved.getId());
        return utenteMapper.toDto(saved);
    }

    @Override
    public UtenteDTO findByUsername(String username) {
        return utenteRepository.findByUsername(username)
                .map(utenteMapper::toDto)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Utente con username '" + username + "' non trovato"));
    }
}
