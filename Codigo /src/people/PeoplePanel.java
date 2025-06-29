package people;

import data.DBConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeoplePanel extends JPanel {

    private List<Person> people = new ArrayList<>();
    private PersonTableModel tableModel;
    private JTable table;
    private final boolean isConsultor;

    public PeoplePanel(String role) {
        this.isConsultor = "consultor".equalsIgnoreCase(role);
        setLayout(new BorderLayout());

        tableModel = new PersonTableModel();
        table = new JTable(tableModel);

        if (!isConsultor) {
            table.getColumn("Editar").setCellRenderer(new ButtonRenderer());
            table.getColumn("Editar").setCellEditor(new ButtonEditor(new JCheckBox(), "Editar"));
            table.getColumn("Eliminar").setCellRenderer(new ButtonRenderer());
            table.getColumn("Eliminar").setCellEditor(new ButtonEditor(new JCheckBox(), "Eliminar"));
        }

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (!isConsultor) {
            JButton btnCreate = new JButton("Crear persona");
            btnCreate.addActionListener(e -> crearPersona());
            topPanel.add(btnCreate);
        }

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        cargarPersonasDesdeDB();
    }

    private void cargarPersonasDesdeDB() {
        people.clear();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.id, p.name, p.last_name, p.dni, p.birthdate, r.name AS rank_name " +
                     "FROM Person p JOIN Ranks r ON p.rank_id = r.id")) {

            while (rs.next()) {
                people.add(new Person(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("last_name"),
                        rs.getString("dni"),
                        rs.getString("birthdate"),
                        rs.getString("rank_name")
                ));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar personas: " + e.getMessage());
        }
        tableModel.fireTableDataChanged();
    }

    private void crearPersona() {
        PersonDialog dialog = new PersonDialog(null);
        dialog.setVisible(true);
        Person nueva = dialog.getPerson();
        if (nueva != null) {
            try (Connection conn = DBConnection.connect()) {
                int rankId = getRankIdByName(nueva.getRankName(), conn);
                String sql = "INSERT INTO Person (name, last_name, dni, birthdate, rank_id) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nueva.getName());
                stmt.setString(2, nueva.getLastName());
                stmt.setString(3, nueva.getDni());
                stmt.setString(4, nueva.getBirthdate());
                stmt.setInt(5, rankId);
                stmt.executeUpdate();
                cargarPersonasDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al insertar persona: " + e.getMessage());
            }
        }
    }

    private void editarPersona(int row) {
        Person personaOriginal = people.get(row);
        PersonDialog dialog = new PersonDialog(null, personaOriginal);
        dialog.setVisible(true);
        Person editada = dialog.getPerson();
        if (editada != null) {
            try (Connection conn = DBConnection.connect()) {
                int rankId = getRankIdByName(editada.getRankName(), conn);
                String sql = "UPDATE Person SET name = ?, last_name = ?, dni = ?, birthdate = ?, rank_id = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, editada.getName());
                stmt.setString(2, editada.getLastName());
                stmt.setString(3, editada.getDni());
                stmt.setString(4, editada.getBirthdate());
                stmt.setInt(5, rankId);
                stmt.setInt(6, personaOriginal.getId());
                stmt.executeUpdate();
                cargarPersonasDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al actualizar persona: " + e.getMessage());
            }
        }
    }

    private void eliminarPersona(int row) {
        Person p = people.get(row);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar esta persona?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect()) {
                String sql = "DELETE FROM Person WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, p.getId());
                stmt.executeUpdate();
                cargarPersonasDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar persona: " + e.getMessage());
            }
        }
    }

    private int getRankIdByName(String rankName, Connection conn) throws SQLException {
        String sql = "SELECT id FROM Ranks WHERE name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, rankName);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        } else {
            throw new SQLException("Rango no encontrado: " + rankName);
        }
    }

    private class PersonTableModel extends AbstractTableModel {
        private final String[] columns = isConsultor ?
                new String[]{"ID", "Nombre", "Apellido", "DNI", "Fecha Nac.", "Rango"} :
                new String[]{"ID", "Nombre", "Apellido", "DNI", "Fecha Nac.", "Rango", "Editar", "Eliminar"};

        @Override
        public int getRowCount() {
            return people.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            Person p = people.get(row);
            switch (col) {
                case 0: return p.getId();
                case 1: return p.getName();
                case 2: return p.getLastName();
                case 3: return p.getDni();
                case 4: return p.getBirthdate();
                case 5: return p.getRankName();
                case 6: return isConsultor ? null : "Editar";
                case 7: return isConsultor ? null : "Eliminar";
                default: return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return !isConsultor && (col == 6 || col == 7);
        }
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final String label;
        private boolean clicked;
        private int row;

        public ButtonEditor(JCheckBox checkBox, String label) {
            super(checkBox);
            this.button = new JButton();
            this.label = label;
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;
            button.setText(label);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                if (label.equals("Editar")) {
                    editarPersona(row);
                } else if (label.equals("Eliminar")) {
                    eliminarPersona(row);
                }
            }
            clicked = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Personas");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new PeoplePanel("admin")); // O "consultor"
            frame.setSize(700, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
