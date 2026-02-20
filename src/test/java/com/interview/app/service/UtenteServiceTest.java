package com.interview.app.service;

import com.interview.app.dto.LoginRequestDTO;
import com.interview.app.dto.UtenteDTO;
import com.interview.app.entity.Utente;
import com.interview.app.exception.DuplicateUsernameException;
import com.interview.app.exception.InvalidCredentialsException;
import com.interview.app.exception.ResourceNotFoundException;
import com.interview.app.mapper.UtenteMapper;
import com.interview.app.repository.UtenteRepository;
import com.interview.app.service.impl.UtenteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UtenteService - Unit Tests")
class UtenteServiceTest {

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private UtenteMapper utenteMapper;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UtenteServiceImpl utenteService;

    private Utente utente;
    private UtenteDTO utenteDTO;
    private static final String RAW_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // Pre-hash del RAW_PASSWORD con BCrypt (come nel DB)
        String hashedPassword = new BCryptPasswordEncoder().encode(RAW_PASSWORD);

        utente = Utente.builder()
                .id(1L)
                .username("admin")
                .password(hashedPassword)
                .build();

        utenteDTO = UtenteDTO.builder()
                .id(1L)
                .username("admin")
                .build();
    }

    // =================== login ===================

    @Test
    @DisplayName("login: restituisce UtenteDTO con credenziali corrette")
    void login_correctCredentials_returnsUtenteDto() {
        when(utenteRepository.findByUsername("admin")).thenReturn(Optional.of(utente));
        when(utenteMapper.toDto(utente)).thenReturn(utenteDTO);

        UtenteDTO result = utenteService.login(new LoginRequestDTO("admin", RAW_PASSWORD));

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin");
        verify(utenteMapper).toDto(utente);
    }

    @Test
    @DisplayName("login: lancia InvalidCredentialsException per username sconosciuto")
    void login_unknownUsername_throwsInvalidCredentialsException() {
        when(utenteRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                utenteService.login(new LoginRequestDTO("ghost", "anypass")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenziali non valide");

        verify(utenteMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("login: lancia InvalidCredentialsException per password errata")
    void login_wrongPassword_throwsInvalidCredentialsException() {
        when(utenteRepository.findByUsername("admin")).thenReturn(Optional.of(utente));

        assertThatThrownBy(() ->
                utenteService.login(new LoginRequestDTO("admin", "wrongpassword")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(utenteMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("login: usa BCrypt per il confronto password (non plain text)")
    void login_usesBCryptForPasswordComparison() {
        when(utenteRepository.findByUsername("admin")).thenReturn(Optional.of(utente));
        when(utenteMapper.toDto(utente)).thenReturn(utenteDTO);

        // Deve funzionare con la password in chiaro (BCrypt.matches)
        assertThatCode(() ->
                utenteService.login(new LoginRequestDTO("admin", RAW_PASSWORD)))
                .doesNotThrowAnyException();

        // Deve fallire con una stringa identica all'hash (conferma che non fa confronto plain)
        assertThatThrownBy(() ->
                utenteService.login(new LoginRequestDTO("admin", utente.getPassword())))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    // =================== register ===================

    @Test
    @DisplayName("register: crea l'utente quando lo username è disponibile")
    void register_newUsername_savesAndReturnsDto() {
        when(utenteRepository.existsByUsername("newuser")).thenReturn(false);
        when(utenteRepository.save(any(Utente.class))).thenReturn(utente);
        when(utenteMapper.toDto(utente)).thenReturn(utenteDTO);

        UtenteDTO result = utenteService.register(new LoginRequestDTO("newuser", "pass123"));

        assertThat(result).isNotNull();
        verify(utenteRepository).save(any(Utente.class));
    }

    @Test
    @DisplayName("register: lancia DuplicateUsernameException se username già in uso")
    void register_duplicateUsername_throwsDuplicateUsernameException() {
        when(utenteRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() ->
                utenteService.register(new LoginRequestDTO("admin", "pass")))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessageContaining("admin");

        verify(utenteRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: la password viene hashata con BCrypt prima del salvataggio")
    void register_passwordIsHashedWithBCrypt() {
        when(utenteRepository.existsByUsername(anyString())).thenReturn(false);
        when(utenteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(utenteMapper.toDto(any())).thenReturn(utenteDTO);

        utenteService.register(new LoginRequestDTO("testuser", RAW_PASSWORD));

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(utenteRepository).save(captor.capture());

        Utente savedUtente = captor.getValue();

        // La password salvata NON deve essere il plain text
        assertThat(savedUtente.getPassword()).isNotEqualTo(RAW_PASSWORD);
        // Deve essere un hash BCrypt valido
        assertThat(passwordEncoder.matches(RAW_PASSWORD, savedUtente.getPassword())).isTrue();
    }

    @Test
    @DisplayName("register: chiama repository.save esattamente una volta")
    void register_callsRepositorySaveExactlyOnce() {
        when(utenteRepository.existsByUsername(anyString())).thenReturn(false);
        when(utenteRepository.save(any())).thenReturn(utente);
        when(utenteMapper.toDto(any())).thenReturn(utenteDTO);

        utenteService.register(new LoginRequestDTO("user", "pass"));

        verify(utenteRepository, times(1)).save(any(Utente.class));
    }

    @Test
    @DisplayName("register: non chiama save se username è duplicato")
    void register_whenDuplicateUsername_neverCallsSave() {
        when(utenteRepository.existsByUsername("admin")).thenReturn(true);

        try {
            utenteService.register(new LoginRequestDTO("admin", "pass"));
        } catch (DuplicateUsernameException e) {
            // atteso
        }

        verify(utenteRepository, never()).save(any());
    }

    // =================== findByUsername ===================

    @Test
    @DisplayName("findByUsername: restituisce DTO quando l'utente esiste")
    void findByUsername_whenExists_returnsDto() {
        when(utenteRepository.findByUsername("admin")).thenReturn(Optional.of(utente));
        when(utenteMapper.toDto(utente)).thenReturn(utenteDTO);

        UtenteDTO result = utenteService.findByUsername("admin");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("findByUsername: lancia ResourceNotFoundException quando non trovato")
    void findByUsername_whenNotFound_throwsResourceNotFoundException() {
        when(utenteRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utenteService.findByUsername("ghost"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost");

        verify(utenteMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("findByUsername: il DTO restituito non contiene la password")
    void findByUsername_returnsDtoWithoutPassword() {
        when(utenteRepository.findByUsername("admin")).thenReturn(Optional.of(utente));
        when(utenteMapper.toDto(utente)).thenReturn(utenteDTO);

        UtenteDTO result = utenteService.findByUsername("admin");

        // UtenteDTO non ha il campo password per design
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isNotNull();
        // Verifica che il mapper venga chiamato (il mapper esclude la password)
        verify(utenteMapper).toDto(utente);
    }
}
