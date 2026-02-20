package com.interview.app.ui;

import com.interview.app.dto.PersonaDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel per la JTable della rubrica.
 * Mostra le colonne: ID, Nome, Cognome, Indirizzo, Telefono, Età.
 */
public class PersonaTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {
        "ID", "Nome", "Cognome", "Indirizzo", "Telefono", "Età"
    };

    private List<PersonaDTO> data = new ArrayList<>();

    /**
     * Sostituisce i dati della tabella e notifica la JTable di aggiornarsi.
     */
    public void setData(List<PersonaDTO> data) {
        this.data = new ArrayList<>(data);
        fireTableDataChanged();
    }

    /**
     * Restituisce la PersonaDTO alla riga specificata (indice nel model, non nella view).
     */
    public PersonaDTO getPersonaAt(int rowIndex) {
        return data.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PersonaDTO p = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> p.getId();
            case 1 -> p.getNome();
            case 2 -> p.getCognome();
            case 3 -> p.getIndirizzo() != null ? p.getIndirizzo() : "";
            case 4 -> p.getTelefono();
            case 5 -> p.getEta();
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Long.class;
            case 5 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // tabella non editabile direttamente
    }
}
