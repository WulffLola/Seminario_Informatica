package ranks;

import data.DBConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RankPanel extends JPanel {
    private List<Rank> ranks = new ArrayList<>();
    private RankTableModel tableModel;
    private JTable table;
    private final boolean isConsultor;

    public RankPanel(String role) {
        this.isConsultor = "consultor".equalsIgnoreCase(role);
        setLayout(new BorderLayout());

        tableModel = new RankTableModel();
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
            JButton btnCreate = new JButton("Crear rango");
            btnCreate.addActionListener(e -> crearRango());
            topPanel.add(btnCreate);
        }

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        cargarRanksDesdeDB();
    }

    private void cargarRanksDesdeDB() {
        ranks.clear();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Ranks ORDER BY level ASC")) {

            while (rs.next()) {
                ranks.add(new Rank(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("level")
                ));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar rangos: " + e.getMessage());
        }
        tableModel.fireTableDataChanged();
    }

    private void crearRango() {
        RankDialog dialog = new RankDialog(null);
        dialog.setVisible(true);
        Rank nuevo = dialog.getRank();
        if (nuevo != null) {
            try (Connection conn = DBConnection.connect()) {
                String sql = "INSERT INTO Ranks (name, level) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nuevo.getName());
                stmt.setInt(2, nuevo.getLevel());
                stmt.executeUpdate();
                cargarRanksDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al insertar rango: " + e.getMessage());
            }
        }
    }

    private void editarRango(int row) {
        Rank original = ranks.get(row);
        RankDialog dialog = new RankDialog(null, original);
        dialog.setVisible(true);
        Rank actualizado = dialog.getRank();
        if (actualizado != null) {
            try (Connection conn = DBConnection.connect()) {
                String sql = "UPDATE Ranks SET name = ?, level = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, actualizado.getName());
                stmt.setInt(2, actualizado.getLevel());
                stmt.setInt(3, original.getId());
                stmt.executeUpdate();
                cargarRanksDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al actualizar rango: " + e.getMessage());
            }
        }
    }

    private void eliminarRango(int row) {
        Rank r = ranks.get(row);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar este rango?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect()) {
                String sql = "DELETE FROM Ranks WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, r.getId());
                stmt.executeUpdate();
                cargarRanksDesdeDB();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar rango: " + e.getMessage());
            }
        }
    }

    private class RankTableModel extends AbstractTableModel {
        private final String[] columns = isConsultor ?
                new String[]{"Nombre", "Nivel"} :
                new String[]{"Nombre", "Nivel", "Editar", "Eliminar"};

        @Override
        public int getRowCount() {
            return ranks.size();
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
            Rank rank = ranks.get(row);
            switch (col) {
                case 0: return rank.getName();
                case 1: return rank.getLevel();
                case 2: return isConsultor ? null : "Editar";
                case 3: return isConsultor ? null : "Eliminar";
                default: return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return !isConsultor && col >= 2;
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
            button = new JButton();
            button.setOpaque(true);
            this.label = label;
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
                    editarRango(row);
                } else if (label.equals("Eliminar")) {
                    eliminarRango(row);
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
            JFrame frame = new JFrame("Rangos");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new RankPanel("admin")); // o "consultor"
            frame.setSize(500, 300);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
