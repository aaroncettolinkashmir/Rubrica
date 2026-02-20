package com.interview.app.ui;

import com.interview.app.dto.PersonaCreateDTO;
import com.interview.app.dto.PersonaDTO;
import com.interview.app.dto.PersonaUpdateDTO;
import com.interview.app.service.PersonaService;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

/**
 * Dialog modale per l'inserimento e la modifica di una Persona.
 *
 * Modalità CREATE: existing == null → tutti i campi vuoti
 * Modalità UPDATE: existing != null → campi pre-popolati
 *
 * Chiamate al service avvengono tramite SwingWorker per non bloccare l'EDT.
 */
public class EditorPersonaDialog extends JDialog {

    private final PersonaService personaService;
    private final PersonaDTO existing; // null in modalità CREATE

    private JTextField nomeField;
    private JTextField cognomeField;
    private JTextField indirizzoField;
    private JTextField telefonoField;
    private JTextField etaField;

    private JButton saveButton;
    private JButton cancelButton;

    private boolean saved = false;

    public EditorPersonaDialog(Frame parent, PersonaService personaService, PersonaDTO existing) {
        super(parent, existing == null ? "Nuova Persona" : "Modifica Persona", true);
        this.personaService = personaService;
        this.existing = existing;
        initUI();
        if (existing != null) {
            populateFields();
        }
    }

    private void initUI() {
        setSize(420, 300);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        // Riga: Nome *
        nomeField = addFormRow(formPanel, gbc, 0, "Nome *:", 20);
        // Riga: Cognome *
        cognomeField = addFormRow(formPanel, gbc, 1, "Cognome *:", 20);
        // Riga: Indirizzo
        indirizzoField = addFormRow(formPanel, gbc, 2, "Indirizzo:", 20);
        // Riga: Telefono *
        telefonoField = addFormRow(formPanel, gbc, 3, "Telefono *:", 20);
        // Riga: Età
        etaField = addFormRow(formPanel, gbc, 4, "Età:", 20);

        // Nota campi obbligatori
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("* campi obbligatori"), gbc);

        // Panel bottoni
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        saveButton = new JButton("Salva");
        cancelButton = new JButton("Annulla");
        saveButton.setPreferredSize(new Dimension(90, 28));
        cancelButton.setPreferredSize(new Dimension(90, 28));
        btnPanel.add(saveButton);
        btnPanel.add(cancelButton);

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Azioni bottoni
        saveButton.addActionListener(e -> doSave());
        cancelButton.addActionListener(e -> dispose());

        // Enter in qualsiasi campo → salva
        KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
        getRootPane().setDefaultButton(saveButton);

        // Escape → annulla
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JTextField addFormRow(JPanel panel, GridBagConstraints gbc,
                                   int row, String labelText, int columns) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField field = new JTextField(columns);
        panel.add(field, gbc);
        return field;
    }

    private void populateFields() {
        nomeField.setText(existing.getNome() != null ? existing.getNome() : "");
        cognomeField.setText(existing.getCognome() != null ? existing.getCognome() : "");
        indirizzoField.setText(existing.getIndirizzo() != null ? existing.getIndirizzo() : "");
        telefonoField.setText(existing.getTelefono() != null ? existing.getTelefono() : "");
        etaField.setText(existing.getEta() != null ? existing.getEta().toString() : "");
    }

    private void doSave() {
        // Validazione client-side
        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        String indirizzo = indirizzoField.getText().trim();
        String telefono = telefonoField.getText().trim();
        String etaText = etaField.getText().trim();

        if (nome.isEmpty() || cognome.isEmpty() || telefono.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Nome, Cognome e Telefono sono obbligatori.",
                "Errore di validazione",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        Integer eta = null;
        if (!etaText.isEmpty()) {
            try {
                eta = Integer.parseInt(etaText);
                if (eta < 0 || eta > 150) {
                    throw new NumberFormatException("Fuori range");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Età non valida. Inserire un numero intero tra 0 e 150.",
                    "Errore di validazione",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);

        final Integer finalEta = eta;
        final String finalIndirizzo = indirizzo.isEmpty() ? null : indirizzo;

        SwingWorker<PersonaDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected PersonaDTO doInBackground() {
                if (existing == null) {
                    // Modalità CREATE
                    PersonaCreateDTO dto = PersonaCreateDTO.builder()
                            .nome(nome)
                            .cognome(cognome)
                            .indirizzo(finalIndirizzo)
                            .telefono(telefono)
                            .eta(finalEta)
                            .build();
                    return personaService.create(dto);
                } else {
                    // Modalità UPDATE
                    PersonaUpdateDTO dto = PersonaUpdateDTO.builder()
                            .nome(nome)
                            .cognome(cognome)
                            .indirizzo(finalIndirizzo)
                            .telefono(telefono)
                            .eta(finalEta)
                            .build();
                    return personaService.update(existing.getId(), dto);
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // lancia l'eccezione se doInBackground ha fallito
                    saved = true;
                    dispose();
                } catch (ExecutionException ex) {
                    saveButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    JOptionPane.showMessageDialog(EditorPersonaDialog.this,
                        "Errore nel salvataggio:\n" + ex.getCause().getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    saveButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    /**
     * @return true se l'utente ha premuto Salva con successo
     */
    public boolean isSaved() {
        return saved;
    }
}
