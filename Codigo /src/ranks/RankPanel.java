package rank;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;


public class RankPanel extends JPanel {
    private List<Rank> ranks = new ArrayList<>();
    private RankTableModel tableModel;
    private JTable table;
    private int nextId = 1;

    public RankPanel() {
        setLayout(new BorderLayout());

        // Datos ficticios
        ranks.add(new Rank(nextId++, "Capitán", 1));
        ranks.add(new Rank(nextId++, "Teniente", 2));
        ranks.add(new Rank(nextId++, "Almirante", 0));

        tableModel = new RankTableModel();
        table = new JTable(tableModel);

        // Ajustar tamaño de columnas y agregar botones
        table.getColumn("Editar").setCellRenderer(new ButtonRenderer());
        table.getColumn("Editar").setCellEditor(new ButtonEditor(new JCheckBox(), "Editar"));

        table.getColumn("Eliminar").setCellRenderer(new ButtonRenderer());
        table.getColumn("Eliminar").setCellEditor(new ButtonEditor(new JCheckBox(), "Eliminar"));

        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnCreate = new JButton("Crear rango");
        btnCreate.addActionListener(e -> crearRango());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(btnCreate);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void crearRango() {
        RankDialog dialog = new RankDialog(null);
        dialog.setVisible(true);
        Rank nuevo = dialog.getRank();
        if (nuevo != null) {
            nuevo.setId(nextId++);
            ranks.add(nuevo);
            ordenarRanks();
            tableModel.fireTableDataChanged();
        }
    }

    private void editarRango(int row) {
        Rank toEdit = ranks.get(row);
        RankDialog dialog = new RankDialog(null, toEdit);
        dialog.setVisible(true);
        Rank updated = dialog.getRank();
        if (updated != null) {
            toEdit.setName(updated.getName());
            toEdit.setLevel(updated.getLevel());
            ordenarRanks();
            tableModel.fireTableDataChanged();
        }
    }

    private void eliminarRango(int row) {
        int option = JOptionPane.showConfirmDialog(this, "¿Eliminar este rango?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            ranks.remove(row);
            tableModel.fireTableDataChanged();
        }
    }

    private void ordenarRanks() {
        ranks.sort((r1, r2) -> Integer.compare(r1.getLevel(), r2.getLevel()));
    }

    private class RankTableModel extends AbstractTableModel {
        private String[] columns = {"Nombre", "Nivel", "Editar", "Eliminar"};

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
                case 2: return "Editar";
                case 3: return "Eliminar";
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col >= 2;  // Solo Editar y Eliminar son editables (para botones)
        }
    }

    // Renderiza un botón en la celda
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

    // Edita la celda como botón y maneja el evento click
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
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

    // Diálogo para crear o editar rango (con constructor sobrecargado)
    private class RankDialog extends JDialog {
        private JTextField tfName;
        private JTextField tfLevel;
        private Rank rank;

        public RankDialog(Frame owner) {
            this(owner, null);
        }

        public RankDialog(Frame owner, Rank rankToEdit) {
            super(owner, rankToEdit == null ? "Crear Rango" : "Editar Rango", true);
            setLayout(new GridLayout(3, 2, 10, 10));

            add(new JLabel("Nombre:"));
            tfName = new JTextField();
            add(tfName);

            add(new JLabel("Nivel:"));
            tfLevel = new JTextField();
            add(tfLevel);

            JButton btnOk = new JButton("OK");
            btnOk.addActionListener(e -> onOk());
            add(btnOk);

            JButton btnCancel = new JButton("Cancelar");
            btnCancel.addActionListener(e -> {
                rank = null;
                dispose();
            });
            add(btnCancel);

            if (rankToEdit != null) {
                tfName.setText(rankToEdit.getName());
                tfLevel.setText(String.valueOf(rankToEdit.getLevel()));
            }

            pack();
            setLocationRelativeTo(owner);
        }

        private void onOk() {
            String name = tfName.getText().trim();
            String levelStr = tfLevel.getText().trim();

            if (name.isEmpty() || levelStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int level;
            try {
                level = Integer.parseInt(levelStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Nivel debe ser un número", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            rank = new Rank(0, name, level);
            dispose();
        }

        public Rank getRank() {
            return rank;
        }
    }

    // Método main para testear
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Rangos");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new RankPanel());
            frame.setSize(500, 300);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
