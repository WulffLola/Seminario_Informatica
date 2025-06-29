package people;

import data.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonDialog extends JDialog {
    private JTextField tfName, tfLastName, tfDni, tfBirthdate;
    private JComboBox<String> cbRank;
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
        cbRank = new JComboBox<>(getRangosDesdeDB());
        add(cbRank);

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
            cbRank.setSelectedItem(p.getRankName());
            person = p;
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private String[] getRangosDesdeDB() {
        List<String> rangos = new ArrayList<>();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM Ranks ORDER BY level ASC")) {
            while (rs.next()) {
                rangos.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar rangos: " + e.getMessage());
        }
        return rangos.toArray(new String[0]);
    }

    private void onOk() {
        String name = tfName.getText().trim();
        String lastName = tfLastName.getText().trim();
        String dni = tfDni.getText().trim();
        String birthdate = tfBirthdate.getText().trim();
        String rank = (String) cbRank.getSelectedItem();

        if (name.isEmpty() || lastName.isEmpty() || dni.isEmpty() || birthdate.isEmpty() || rank == null) {
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
