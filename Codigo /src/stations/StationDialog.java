package stations;

import data.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class StationDialog extends JDialog {
    private JComboBox<String> cbPerson, cbShip, cbRank;
    private JTextField tfStartDate, tfEndDate;
    private Station station;

    public StationDialog(Frame owner) {
        this(owner, null);
    }

    public StationDialog(Frame owner, Station s) {
        super(owner, s == null ? "Asignar Estación" : "Editar Estación", true);
        setLayout(new GridLayout(6, 2, 10, 10));

        cbPerson = new JComboBox<>(getFromDB("SELECT CONCAT(name, ' ', last_name) FROM Person"));
        cbShip = new JComboBox<>(getFromDB("SELECT name FROM Ship"));
        cbRank = new JComboBox<>(getFromDB("SELECT name FROM Ranks"));

        tfStartDate = new JTextField();
        tfEndDate = new JTextField();

        add(new JLabel("Tripulante:"));
        add(cbPerson);
        add(new JLabel("Barco:"));
        add(cbShip);
        add(new JLabel("Rango:"));
        add(cbRank);
        add(new JLabel("Fecha Inicio (YYYY-MM-DD):"));
        add(tfStartDate);
        add(new JLabel("Fecha Fin (opcional):"));
        add(tfEndDate);

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> onOk());
        add(btnOk);

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> {
            station = null;
            dispose();
        });
        add(btnCancel);

        if (s != null) {
            cbPerson.setSelectedItem(s.getPersonName());
            cbShip.setSelectedItem(s.getShipName());
            cbRank.setSelectedItem(s.getRankName());
            tfStartDate.setText(s.getStartDate());
            tfEndDate.setText(s.getEndDate());
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private String[] getFromDB(String query) {
        ArrayList<String> list = new ArrayList<>();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage());
        }
        return list.toArray(new String[0]);
    }

    private void onOk() {
        String person = (String) cbPerson.getSelectedItem();
        String ship = (String) cbShip.getSelectedItem();
        String rank = (String) cbRank.getSelectedItem();
        String start = tfStartDate.getText().trim();
        String end = tfEndDate.getText().trim();

        if (person == null || ship == null || rank == null || start.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        station = new Station(0, person, ship, rank, start, end.isEmpty() ? null : end);
        dispose();
    }

    public Station getStation() {
        return station;
    }
}
