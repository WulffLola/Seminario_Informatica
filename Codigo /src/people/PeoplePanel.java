package people;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class PeoplePanel extends JPanel {

    private List<Person> people = new ArrayList<>();
    private PersonTableModel tableModel;
    private JTable table;
    private int nextId = 4;

    public PeoplePanel() {
        setLayout(new BorderLayout());

        // Datos ficticios
        people.add(new Person(1, "Juan", "Perez", "12345678", "1980-01-15", "Capitán"));
        people.add(new Person(2, "Ana", "Gómez", "87654321", "1990-05-23", "Teniente"));
        people.add(new Person(3, "Luis", "Díaz", "11223344", "1985-11-30", "Alférez"));

        tableModel = new PersonTableModel();
        table = new JTable(tableModel);

        // Configurar botones en las columnas
        table.getColumn("Editar").setCellRenderer(new ButtonRenderer());
        table.getColumn("Editar").setCellEditor(new ButtonEditor(new JCheckBox(), "Editar"));

        table.getColumn("Eliminar").setCellRenderer(new ButtonRenderer());
        table.getColumn("Eliminar").setCellEditor(new ButtonEditor(new JCheckBox(), "Eliminar"));

        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnCreate = new JButton("Crear Persona");
        btnCreate.addActionListener(e -> crearPersona());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(btnCreate);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void crearPersona() {
        PersonDialog dialog = new PersonDialog(null);
        dialog.setVisible(true);
        Person nuevo = dialog.getPerson();
        if (nuevo != null) {
            nuevo.setId(nextId++);
            people.add(nuevo);
            tableModel.fireTableDataChanged();
        }
    }

    private void editarPersona(int row) {
        Person toEdit = people.get(row);
        PersonDialog dialog = new PersonDialog(null, toEdit);
        dialog.setVisible(true);
        Person updated = dialog.getPerson();
        if (updated != null) {
            people.set(row, updated);
            tableModel.fireTableDataChanged();
        }
    }

    private void eliminarPersona(int row) {
        int option = JOptionPane.showConfirmDialog(this, "¿Eliminar esta persona?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            people.remove(row);
            tableModel.fireTableDataChanged();
        }
    }

    private class PersonTableModel extends AbstractTableModel {
        private String[] columns = {"ID", "Nombre", "Apellido", "DNI", "Fecha Nac.", "Rango", "Editar", "Eliminar"};

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
                case 6: return "Editar";
                case 7: return "Eliminar";
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 6 || col == 7;
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

    // Diálogo para crear o editar persona
    private class PersonDialog extends JDialog {
        private JTextField tfName, tfLastName, tfDni, tfBirthdate, tfRankName;
        private Person person;

        public PersonDialog(Frame owner) {
            this(owner, null);
        }

        public PersonDialog(Frame owner, Person p) {
            super(owner, p == null ? "Crear Persona" : "Editar Persona", true);
            setLayout(new GridLayout(6, 2, 10, 10));

            add(new JLabel("Nombre:"));
            tfName = new JTextField();
            add(tfName);

            add(new JLabel("Apellido:"));
            tfLastName = new JTextField();
            add(tfLastName);

            add(new JLabel("DNI:"));
            tfDni = new JTextField();
            add(tfDni);

            add(new JLabel("Fecha Nac. (YYYY-MM-DD):"));
            tfBirthdate = new JTextField();
            add(tfBirthdate);

            add(new JLabel("Rango:"));
            tfRankName = new JTextField();
            add(tfRankName);

            JButton btnOk = new JButton("OK");
            btnOk.addActionListener(e -> onOk());
            add(btnOk);

            JButton btnCancel = new JButton("Cancelar");
            btnCancel.addActionListener(e -> {
                person = null;
                dispose();
            });
            add(btnCancel);

            if (p != null) {
                tfName.setText(p.getName());
                tfLastName.setText(p.getLastName());
                tfDni.setText(p.getDni());
                tfBirthdate.setText(p.getBirthdate());
                tfRankName.setText(p.getRankName());
                person = p;
            }

            pack();
            setLocationRelativeTo(owner);
        }

        private void onOk() {
            String name = tfName.getText().trim();
            String lastName = tfLastName.getText().trim();
            String dni = tfDni.getText().trim();
            String birthdate = tfBirthdate.getText().trim();
            String rank = tfRankName.getText().trim();

            if (name.isEmpty() || lastName.isEmpty() || dni.isEmpty() || birthdate.isEmpty() || rank.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (person == null) {
                person = new Person(0, name, lastName, dni, birthdate, rank);
            } else {
                person.setName(name);
                person.setLastName(lastName);
                person.setDni(dni);
                person.setBirthdate(birthdate);
                person.setRankName(rank);
            }
            dispose();
        }

        public Person getPerson() {
            return person;
        }
    }

    // Para testear rápido
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Personas");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new PeoplePanel());
            frame.setSize(700, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
