package fleets;

import data.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class FleetDialog extends JDialog {
    private JTextField tfName, tfSize;
    private JComboBox<String> cbAdmiral;
    private Fleet fleet;

    public FleetDialog(Frame owner) {
        this(owner, null);
    }

    public FleetDialog(Frame owner, Fleet f) {
        super(owner, f == null ? "Crear Flota" : "Editar Flota", true);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Nombre:"));
        tfName = new JTextField();
        add(tfName);

        add(new JLabel("Tamaño:"));
        tfSize = new JTextField();
        add(tfSize);

        add(new JLabel("Almirante:"));
        cbAdmiral = new JComboBox<>(getAdmiralesDesdeDB());
        add(cbAdmiral);

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> onOk());
        add(btnOk);

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> {
            fleet = null;
            dispose();
        });
        add(btnCancel);

        if (f != null) {
            tfName.setText(f.getName());
            tfSize.setText(String.valueOf(f.getSize()));
            cbAdmiral.setSelectedItem(f.getAdmiralName());
            fleet = f;
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private String[] getAdmiralesDesdeDB() {
        ArrayList<String> lista = new ArrayList<>();
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT name, last_name FROM Person WHERE rank_id = 1")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("name") + " " + rs.getString("last_name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar almirantes: " + e.getMessage());
        }
        return lista.toArray(new String[0]);
    }

    private void onOk() {
        String name = tfName.getText().trim();
        String sizeStr = tfSize.getText().trim();
        String admiral = (String) cbAdmiral.getSelectedItem();

        if (name.isEmpty() || sizeStr.isEmpty() || admiral == null) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int size;
        try {
            size = Integer.parseInt(sizeStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tamaño debe ser un número", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        fleet = new Fleet(0, name, size, admiral);
        dispose();
    }

    public Fleet getFleet() {
        return fleet;
    }
}
