package stations;

import data.DBConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StationPanel extends JPanel {
    private List<Station> stations = new ArrayList<>();
    private StationTableModel tableModel;
    private JTable table;
    private final boolean isConsultor;

    public StationPanel(String role) {
        this.isConsultor = "consultor".equalsIgnoreCase(role);
        setLayout(new BorderLayout());

        tableModel = new StationTableModel();
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
            JButton btnCreate = new JButton("Nueva Estación");
            btnCreate.addActionListener(e -> crearEstacion());
            topPanel.add(btnCreate);
        }

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        cargarEstaciones();
    }

    private void cargarEstaciones() {
        stations.clear();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT s.id, CONCAT(p.name, ' ', p.last_name) as persona, sh.name as barco, r.name as rango, s.start_date, s.end_date " +
                     "FROM Station s " +
                     "JOIN Person p ON s.person_id = p.id " +
                     "JOIN Ship sh ON s.ship_id = sh.id " +
                     "JOIN Ranks r ON s.rank_id = r.id")) {
            while (rs.next()) {
                stations.add(new Station(
                        rs.getInt("id"),
                        rs.getString("persona"),
                        rs.getString("barco"),
                        rs.getString("rango"),
                        rs.getString("start_date"),
                        rs.getString("end_date")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar estaciones: " + e.getMessage());
        }
        tableModel.fireTableDataChanged();
    }

    private void crearEstacion() {
        StationDialog dialog = new StationDialog(null);
        dialog.setVisible(true);
        Station nueva = dialog.getStation();
        if (nueva != null) {
            try (Connection conn = DBConnection.connect()) {
                int personId = getIdByName(conn, "Person", "CONCAT(name, ' ', last_name)", nueva.getPersonName());
                int shipId = getIdByName(conn, "Ship", "name", nueva.getShipName());
                int rankId = getIdByName(conn, "Ranks", "name", nueva.getRankName());

                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Station (person_id, ship_id, rank_id, start_date, end_date) VALUES (?, ?, ?, ?, ?)");
                stmt.setInt(1, personId);
                stmt.setInt(2, shipId);
                stmt.setInt(3, rankId);
                stmt.setString(4, nueva.getStartDate());
                stmt.setString(5, nueva.getEndDate());
                stmt.executeUpdate();

                cargarEstaciones();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al guardar estación: " + e.getMessage());
            }
        }
    }

    private void editarEstacion(int row) {
        Station original = stations.get(row);
        StationDialog dialog = new StationDialog(null, original);
        dialog.setVisible(true);
        Station updated = dialog.getStation();

        if (updated != null) {
            try (Connection conn = DBConnection.connect()) {
                int personId = getIdByName(conn, "Person", "CONCAT(name, ' ', last_name)", updated.getPersonName());
                int shipId = getIdByName(conn, "Ship", "name", updated.getShipName());
                int rankId = getIdByName(conn, "Ranks", "name", updated.getRankName());

                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE Station SET person_id=?, ship_id=?, rank_id=?, start_date=?, end_date=? WHERE id=?"
                );
                stmt.setInt(1, personId);
                stmt.setInt(2, shipId);
                stmt.setInt(3, rankId);
                stmt.setString(4, updated.getStartDate());
                stmt.setString(5, updated.getEndDate());
                stmt.setInt(6, original.getId());
                stmt.executeUpdate();

                cargarEstaciones();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error al actualizar estación: " + ex.getMessage());
            }
        }
    }

    private void eliminarEstacion(int row) {
        Station s = stations.get(row);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar esta estación?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect()) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM Station WHERE id = ?");
                stmt.setInt(1, s.getId());
                stmt.executeUpdate();
                cargarEstaciones();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar estación: " + e.getMessage());
            }
        }
    }

    private int getIdByName(Connection conn, String table, String columnExpr, String value) throws SQLException {
        String sql = "SELECT id FROM " + table + " WHERE " + columnExpr + " = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, value);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getInt("id");
        throw new SQLException("No se encontró en " + table + ": " + value);
    }

    private class StationTableModel extends AbstractTableModel {
        private final String[] cols = isConsultor ?
                new String[]{"Tripulante", "Barco", "Rango", "Inicio", "Fin"} :
                new String[]{"Tripulante", "Barco", "Rango", "Inicio", "Fin", "Editar", "Eliminar"};

        public int getRowCount() { return stations.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int col) { return cols[col]; }

        public Object getValueAt(int row, int col) {
            Station s = stations.get(row);
            switch (col) {
                case 0: return s.getPersonName();
                case 1: return s.getShipName();
                case 2: return s.getRankName();
                case 3: return s.getStartDate();
                case 4: return s.getEndDate();
                case 5: return isConsultor ? null : "Editar";
                case 6: return isConsultor ? null : "Eliminar";
                default: return null;
            }
        }

        public boolean isCellEditable(int row, int col) {
            return !isConsultor && col >= 5;
        }
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
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
                if (label.equals("Editar")) {
                    editarEstacion(row);
                } else if (label.equals("Eliminar")) {
                    eliminarEstacion(row);
                }
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
            JFrame frame = new JFrame("Estaciones");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new StationPanel("admin")); // o "consultor"
            frame.setSize(800, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
