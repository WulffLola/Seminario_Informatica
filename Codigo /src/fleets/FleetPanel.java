package fleets;

import data.DBConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FleetPanel extends JPanel {
    private List<Fleet> fleets = new ArrayList<>();
    private FleetTableModel tableModel;
    private JTable table;
    private final boolean isConsultor;

    public FleetPanel(String role) {
        this.isConsultor = "consultor".equalsIgnoreCase(role);
        setLayout(new BorderLayout());

        tableModel = new FleetTableModel();
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
            JButton btnCreate = new JButton("Crear Flota");
            btnCreate.addActionListener(e -> crearFlota());
            topPanel.add(btnCreate);
        }

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        cargarFlotasDesdeDB();
    }

    private void cargarFlotasDesdeDB() {
        fleets.clear();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT f.id, f.name, f.size, p.name AS admiral_name, p.last_name " +
                     "FROM Fleet f JOIN Person p ON f.admiral_id = p.id")) {

            while (rs.next()) {
                String admiral = rs.getString("admiral_name") + " " + rs.getString("last_name");
                fleets.add(new Fleet(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("size"),
                        admiral
                ));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar flotas: " + e.getMessage());
        }
        tableModel.fireTableDataChanged();
    }

    private void crearFlota() {
        FleetDialog dialog = new FleetDialog(null);
        dialog.setVisible(true);
        Fleet nueva = dialog.getFleet();
        if (nueva != null) {
            try (Connection conn = DBConnection.connect()) {
                int admiralId = getAdmiralIdByName(nueva.getAdmiralName(), conn);
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Fleet (name, size, admiral_id) VALUES (?, ?, ?)");
                stmt.setString(1, nueva.getName());
                stmt.setInt(2, nueva.getSize());
                stmt.setInt(3, admiralId);
                stmt.executeUpdate();
                cargarFlotasDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al insertar flota: " + e.getMessage());
            }
        }
    }

    private void editarFlota(int row) {
        Fleet original = fleets.get(row);
        FleetDialog dialog = new FleetDialog(null, original);
        dialog.setVisible(true);
        Fleet editada = dialog.getFleet();
        if (editada != null) {
            try (Connection conn = DBConnection.connect()) {
                int admiralId = getAdmiralIdByName(editada.getAdmiralName(), conn);
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE Fleet SET name = ?, size = ?, admiral_id = ? WHERE id = ?");
                stmt.setString(1, editada.getName());
                stmt.setInt(2, editada.getSize());
                stmt.setInt(3, admiralId);
                stmt.setInt(4, original.getId());
                stmt.executeUpdate();
                cargarFlotasDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al actualizar flota: " + e.getMessage());
            }
        }
    }

    private void eliminarFlota(int row) {
        Fleet f = fleets.get(row);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar esta flota?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect()) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM Fleet WHERE id = ?");
                stmt.setInt(1, f.getId());
                stmt.executeUpdate();
                cargarFlotasDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar flota: " + e.getMessage());
            }
        }
    }

    private int getAdmiralIdByName(String fullName, Connection conn) throws SQLException {
        String[] parts = fullName.split(" ");
        String sql = "SELECT id FROM Person WHERE name = ? AND last_name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, parts[0]);
        stmt.setString(2, parts[1]);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getInt("id");
        throw new SQLException("No se encontró el almirante: " + fullName);
    }

    private class FleetTableModel extends AbstractTableModel {
        private final String[] columns = isConsultor ?
                new String[]{"Nombre", "Tamaño", "Almirante"} :
                new String[]{"Nombre", "Tamaño", "Almirante", "Editar", "Eliminar"};

        public int getRowCount() { return fleets.size(); }
        public int getColumnCount() { return columns.length; }
        public String getColumnName(int col) { return columns[col]; }

        public Object getValueAt(int row, int col) {
            Fleet f = fleets.get(row);
            switch (col) {
                case 0: return f.getName();
                case 1: return f.getSize();
                case 2: return f.getAdmiralName();
                case 3: return isConsultor ? null : "Editar";
                case 4: return isConsultor ? null : "Eliminar";
                default: return null;
            }
        }

        public boolean isCellEditable(int row, int col) {
            return !isConsultor && col >= 3;
        }
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }

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

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int col) {
            this.row = row;
            button.setText(label);
            clicked = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {
                if (label.equals("Editar")) editarFlota(row);
                else if (label.equals("Eliminar")) eliminarFlota(row);
            }
            clicked = false;
            return label;
        }

        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Flotas");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new FleetPanel("admin")); // o "consultor"
            frame.setSize(700, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
