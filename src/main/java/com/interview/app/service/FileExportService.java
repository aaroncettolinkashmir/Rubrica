package com.interview.app.service;

import com.interview.app.dto.PersonaDTO;

public interface FileExportService {

    /**
     * Esporta i dati di una persona in un file txt.
     * Il file viene salvato in: informazioni/NOME-COGNOME-{id}.txt
     * La cartella viene creata automaticamente se non esiste.
     *
     * @param persona il DTO con i dati della persona da esportare
     */
    void export(PersonaDTO persona);
}
