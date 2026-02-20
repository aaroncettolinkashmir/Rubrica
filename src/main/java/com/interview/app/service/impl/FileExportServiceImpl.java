package com.interview.app.service.impl;

import com.interview.app.dto.PersonaDTO;
import com.interview.app.service.FileExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
public class FileExportServiceImpl implements FileExportService {

    private static final String EXPORT_DIR = "informazioni";

    @Override
    public void export(PersonaDTO persona) {
        try {
            Path dir = Paths.get(EXPORT_DIR);
            Files.createDirectories(dir); // no-op se esiste già

            String filename = buildFilename(persona);
            Path filePath = dir.resolve(filename);

            String content = buildFileContent(persona);
            Files.writeString(filePath, content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Persona esportata in: {}", filePath.toAbsolutePath());

        } catch (IOException e) {
            log.error("Errore durante l'esportazione della persona id={}: {}",
                    persona.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante l'esportazione del file: " + e.getMessage(), e);
        }
    }

    private String buildFilename(PersonaDTO persona) {
        String nome = sanitize(persona.getNome());
        String cognome = sanitize(persona.getCognome());
        return nome + "-" + cognome + "-" + persona.getId() + ".txt";
    }

    private String buildFileContent(PersonaDTO p) {
        return "=== RUBRICA TELEFONICA ===" + System.lineSeparator() +
               "ID:         " + p.getId() + System.lineSeparator() +
               "Nome:       " + p.getNome() + System.lineSeparator() +
               "Cognome:    " + p.getCognome() + System.lineSeparator() +
               "Indirizzo:  " + (p.getIndirizzo() != null ? p.getIndirizzo() : "N/D") + System.lineSeparator() +
               "Telefono:   " + p.getTelefono() + System.lineSeparator() +
               "Età:        " + (p.getEta() != null ? p.getEta().toString() : "N/D") + System.lineSeparator();
    }

    /**
     * Rimuove caratteri non sicuri per i nomi file.
     */
    private String sanitize(String s) {
        if (s == null) return "UNKNOWN";
        return s.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
