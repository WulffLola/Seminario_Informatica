package ships;

import data.DBConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShipPanel extends JPanel {
    private List<Ship> ships = new ArrayList<>();
    private ShipTableModel tableModel;
    private JTable table;
    private final boolean isConsultor;

    public ShipPanel(String role) {
        this.isConsultor = "consultor".equalsIgnoreCase(role);
        setLayout(new BorderLayout());

        tableModel = new ShipTableModel();
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
            JButton btnCreate = new JButton("Crear Barco");
            btnCreate.addActionListener(e -> crearBarco());
            topPanel.add(btnCreate);
        }

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        cargarBarcosDesdeDB();
    }

    private void cargarBarcosDesdeDB() {
        ships.clear();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.id, s.name, s.size, f.name AS fleet_name " +
                     "FROM Ship s JOIN Fleet f ON s.fleet_id = f.id")) {

            while (rs.next()) {
                ships.add(new Ship(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("size"),
                        rs.getString("fleet_name")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar barcos: " + e.getMessage());
        }
        tableModel.fireTableDataChanged();
    }

    private void crearBarco() {
        ShipDialog dialog = new ShipDialog(null);
        dialog.setVisible(true);
        Ship nuevo = dialog.getShip();
        if (nuevo != null) {
            try (Connection conn = DBConnection.connect()) {
                int fleetId = getFleetIdByName(nuevo.getFleetName(), conn);
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Ship (name, size, fleet_id) VALUES (?, ?, ?)");
                stmt.setString(1, nuevo.getName());
                stmt.setInt(2, nuevo.getSize());
                stmt.setInt(3, fleetId);
                stmt.executeUpdate();
                cargarBarcosDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al insertar barco: " + e.getMessage());
            }
        }
    }

    private void editarBarco(int row) {
        Ship original = ships.get(row);
        ShipDialog dialog = new ShipDialog(null, original);
        dialog.setVisible(true);
        Ship editado = dialog.getShip();
        if (editado != null) {
            try (Connection conn = DBConnection.connect()) {
                int fleetId = getFleetIdByName(editado.getFleetName(), conn);
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE Ship SET name = ?, size = ?, fleet_id = ? WHERE id = ?");
                stmt.setString(1, editado.getName());
                stmt.setInt(2, editado.getSize());
                stmt.setInt(3, fleetId);
                stmt.setInt(4, original.getId());
                stmt.executeUpdate();
                cargarBarcosDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al actualizar barco: " + e.getMessage());
            }
        }
    }

    private void eliminarBarco(int row) {
        Ship s = ships.get(row);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar este barco?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect()) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM Ship WHERE id = ?");
                stmt.setInt(1, s.getId());
                stmt.executeUpdate();
                cargarBarcosDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar barco: " + e.getMessage());
            }
        }
    }

    private int getFleetIdByName(String fleetName, Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM Fleet WHERE name = ?");
        stmt.setString(1, fleetName);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getInt("id");
        throw new SQLException("No se encontró la flota: " + fleetName);
    }

    private class ShipTableModel extends AbstractTableModel {
        private final String[] columns = isConsultor ?
                new String[]{"Nombre", "Tamaño", "Flota"} :
                new String[]{"Nombre", "Tamaño", "Flota", "Editar", "Eliminar"};

        public int getRowCount() { return ships.size(); }
        public int getColumnCount() { return columns.length; }
        public String getColumnName(int col) { return columns[col]; }

        public Object getValueAt(int row, int col) {
            Ship s = ships.get(row);
            switch (col) {
                case 0: return s.getName();
                case 1: return s.getSize();
                case 2: return s.getFleetName();
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
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

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            this.row = row;
            button.setText(label);
            clicked = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {
                if (label.equals("Editar")) editarBarco(row);
                else if (label.equals("Eliminar")) eliminarBarco(row);
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
            JFrame frame = new JFrame("Barcos");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new ShipPanel("admin")); // o "consultor"
            frame.setSize(700, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
