package ships;

import data.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class ShipDialog extends JDialog {
    private JTextField tfName, tfSize;
    private JComboBox<String> cbFleet;
    private Ship ship;

    public ShipDialog(Frame owner) {
        this(owner, null);
    }

    public ShipDialog(Frame owner, Ship s) {
        super(owner, s == null ? "Crear Barco" : "Editar Barco", true);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Nombre:"));
        tfName = new JTextField();
        add(tfName);

        add(new JLabel("Tamaño:"));
        tfSize = new JTextField();
        add(tfSize);

        add(new JLabel("Flota:"));
        cbFleet = new JComboBox<>(getFleetsFromDB());
        add(cbFleet);

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> onOk());
        add(btnOk);

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> {
            ship = null;
            dispose();
        });
        add(btnCancel);

        if (s != null) {
            tfName.setText(s.getName());
            tfSize.setText(String.valueOf(s.getSize()));
            cbFleet.setSelectedItem(s.getFleetName());
            ship = s;
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private String[] getFleetsFromDB() {
        ArrayList<String> list = new ArrayList<>();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM Fleet")) {
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar flotas: " + e.getMessage());
        }
        return list.toArray(new String[0]);
    }

    private void onOk() {
        String name = tfName.getText().trim();
        String sizeStr = tfSize.getText().trim();
        String fleet = (String) cbFleet.getSelectedItem();

        if (name.isEmpty() || sizeStr.isEmpty() || fleet == null) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int size;
        try {
            size = Integer.parseInt(sizeStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tamaño inválido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ship = new Ship(0, name, size, fleet);
        dispose();
    }

    public Ship getShip() {
        return ship;
    }
}
