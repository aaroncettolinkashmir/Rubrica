package com.interview.app.ui;

import com.interview.app.dto.PersonaDTO;
import com.interview.app.service.PersonaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Finestra principale della Rubrica Telefonica.
 *
 * Mostra una JTable con tutte le persone.
 * JToolBar (EXTRA #3) con bottoni: Nuovo, Modifica, Elimina, Esporta, Aggiorna.
 *
 * Bean Spring @Component singleton: i service sono iniettati via costruttore.
 * Tutte le chiamate ai service avvengono tramite SwingWorker (mai sull'EDT).
 */
@Component
@Lazy  // non istanziare all'avvio del context - solo quando richiesto esplicitamente
@Slf4j
public class MainWindow extends JFrame {

    private final PersonaService personaService;

    private PersonaTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;

    // Toolbar buttons
    private JButton btnNew;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnExport;
    private JButton btnRefresh;

    public MainWindow(PersonaService personaService) {
        this.personaService = personaService;
        initUI();
    }

    private void initUI() {
        setTitle("Rubrica Telefonica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 400));
        setLocationRelativeTo(null);

        // === JToolBar (EXTRA #3) ===
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(true);

        btnNew    = createToolbarButton("icons/add.png",     "Nuova Persona (Ins)",  "Nuovo");
        btnEdit   = createToolbarButton("icons/edit.png",    "Modifica Persona",     "Modifica");
        btnDelete = createToolbarButton("icons/delete.png",  "Elimina Persona",      "Elimina");
        btnExport = createToolbarButton("icons/export.png",  "Esporta in File",      "Esporta");
        btnRefresh= createToolbarButton("icons/refresh.png", "Aggiorna Tabella",     "Aggiorna");

        toolBar.add(btnNew);
        toolBar.add(btnEdit);
        toolBar.addSeparator();
        toolBar.add(btnDelete);
        toolBar.addSeparator();
        toolBar.add(btnExport);
        toolBar.add(btnRefresh);

        // === JTable ===
        tableModel = new PersonaTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);   // click su colonna → ordinamento
        table.setRowHeight(22);
        table.setFillsViewportHeight(true);

        // Colonna ID stretta
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(70);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // === Status bar ===
        statusLabel = new JLabel(" Pronto");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());

        // === Layout principale ===
        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // === Azioni bottoni ===
        btnNew.addActionListener(e -> openEditor(null));

        btnEdit.addActionListener(e -> {
            PersonaDTO selected = getSelectedPersona();
            if (selected == null) {
                showNoSelectionError("modificare");
                return;
            }
            openEditor(selected);
        });

        btnDelete.addActionListener(e -> deleteSelected());
        btnExport.addActionListener(e -> exportSelected());
        btnRefresh.addActionListener(e -> loadData());

        // Double-click su riga → modifica
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    PersonaDTO selected = getSelectedPersona();
                    if (selected != null) {
                        openEditor(selected);
                    }
                }
            }
        });

        // Keyboard shortcuts
        btnNew.setMnemonic('N');
        btnEdit.setMnemonic('M');
        btnDelete.setMnemonic('E');

        // Carica dati all'apertura della finestra
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                loadData();
            }
        });
    }

    /**
     * Carica tutte le persone dal service in background e aggiorna la tabella.
     */
    public void loadData() {
        setStatus("Caricamento...");
        new SwingWorker<List<PersonaDTO>, Void>() {
            @Override
            protected List<PersonaDTO> doInBackground() {
                return personaService.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<PersonaDTO> list = get();
                    tableModel.setData(list);
                    setStatus("Persone caricate: " + list.size());
                } catch (ExecutionException ex) {
                    log.error("Errore caricamento dati", ex.getCause());
                    JOptionPane.showMessageDialog(MainWindow.this,
                        "Errore nel caricamento dei dati:\n" + ex.getCause().getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
                    setStatus("Errore caricamento");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void openEditor(PersonaDTO existing) {
        EditorPersonaDialog dialog = new EditorPersonaDialog(this, personaService, existing);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData(); // aggiorna tabella dopo salvataggio
        }
    }

    private void deleteSelected() {
        PersonaDTO selected = getSelectedPersona();
        if (selected == null) {
            showNoSelectionError("eliminare");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Eliminare la persona " + selected.getNome() + " " + selected.getCognome() + "?",
            "Conferma eliminazione",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setStatus("Eliminazione in corso...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                personaService.delete(selected.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadData();
                } catch (ExecutionException ex) {
                    log.error("Errore eliminazione", ex.getCause());
                    JOptionPane.showMessageDialog(MainWindow.this,
                        "Errore nell'eliminazione:\n" + ex.getCause().getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
                    setStatus("Errore eliminazione");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void exportSelected() {
        PersonaDTO selected = getSelectedPersona();
        if (selected == null) {
            showNoSelectionError("esportare");
            return;
        }

        setStatus("Esportazione in corso...");
        final PersonaDTO toExport = selected;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                personaService.exportToFile(toExport.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    String filename = "informazioni/" +
                        toExport.getNome().replaceAll("[^a-zA-Z0-9]", "_") + "-" +
                        toExport.getCognome().replaceAll("[^a-zA-Z0-9]", "_") + "-" +
                        toExport.getId() + ".txt";
                    JOptionPane.showMessageDialog(MainWindow.this,
                        "Persona esportata in:\n" + filename,
                        "Esportazione completata",
                        JOptionPane.INFORMATION_MESSAGE);
                    setStatus("Esportazione completata");
                } catch (ExecutionException ex) {
                    log.error("Errore esportazione", ex.getCause());
                    JOptionPane.showMessageDialog(MainWindow.this,
                        "Errore nell'esportazione:\n" + ex.getCause().getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
                    setStatus("Errore esportazione");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /**
     * Restituisce la PersonaDTO selezionata nella tabella, o null se nessuna riga è selezionata.
     * Converte l'indice dalla view all'indice del model (necessario quando il sorter è attivo).
     */
    private PersonaDTO getSelectedPersona() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getPersonaAt(modelRow);
    }

    private void showNoSelectionError(String action) {
        JOptionPane.showMessageDialog(this,
            "Per " + action + " è necessario selezionare prima una persona dalla tabella.",
            "Nessuna selezione",
            JOptionPane.WARNING_MESSAGE);
    }

    private void setStatus(String message) {
        statusLabel.setText(" " + message);
    }

    /**
     * Crea un JButton per la toolbar con icona da classpath.
     * Fallback al testo se l'icona non è trovata.
     */
    private JButton createToolbarButton(String iconPath, String tooltip, String fallbackText) {
        JButton btn = new JButton();
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);

        try {
            URL iconUrl = getClass().getClassLoader().getResource(iconPath);
            if (iconUrl != null) {
                ImageIcon icon = new ImageIcon(iconUrl);
                Image scaled = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaled));
            } else {
                btn.setText(fallbackText);
                log.debug("Icona non trovata: {} - uso testo fallback: {}", iconPath, fallbackText);
            }
        } catch (Exception e) {
            btn.setText(fallbackText);
            log.debug("Errore caricamento icona {}: {}", iconPath, e.getMessage());
        }

        return btn;
    }
}
