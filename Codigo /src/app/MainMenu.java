package app;

import fleets.FleetPanel;
import people.PeoplePanel;
import ranks.RankPanel;
import ships.ShipPanel;
import stations.StationPanel;

import javax.swing.*;
import java.awt.*;

public class MainMenu {

    public static void mostrarMenu(String username, String role) {
        JFrame frame = new JFrame("Panel Principal - Bienvenido " + username + " (" + role + ")");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new CardLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(6, 1, 10, 10));

        JButton fleetsButton = new JButton("Flotas");
        JButton peopleButton = new JButton("Tripulación");
        JButton ranksButton = new JButton("Rangos");
        JButton shipsButton = new JButton("Barcos");
        JButton stationsButton = new JButton("Estaciones");
        JButton exitButton = new JButton("Cerrar sesión");

        if (!"consultor".equalsIgnoreCase(role)) {
            menuPanel.add(fleetsButton);
            menuPanel.add(peopleButton);
            menuPanel.add(ranksButton);
            menuPanel.add(shipsButton);
            menuPanel.add(stationsButton);
        } else {
            menuPanel.add(fleetsButton);
            menuPanel.add(peopleButton);
            menuPanel.add(ranksButton);
            menuPanel.add(shipsButton);
            menuPanel.add(stationsButton);
        }
        menuPanel.add(exitButton);

        mainPanel.add(menuPanel, "menu");

        // Paneles individuales
        FleetPanel fleetPanel = new FleetPanel(role);
        PeoplePanel peoplePanel = new PeoplePanel(role);
        RankPanel rankPanel = new RankPanel(role);
        ShipPanel shipPanel = new ShipPanel(role);
        StationPanel stationPanel = new StationPanel(role);

        // Agregamos botón para volver en cada panel
        agregarBotonVolver(fleetPanel, mainPanel);
        agregarBotonVolver(peoplePanel, mainPanel);
        agregarBotonVolver(rankPanel, mainPanel);
        agregarBotonVolver(shipPanel, mainPanel);
        agregarBotonVolver(stationPanel, mainPanel);

        mainPanel.add(fleetPanel, "flotas");
        mainPanel.add(peoplePanel, "tripulacion");
        mainPanel.add(rankPanel, "rangos");
        mainPanel.add(shipPanel, "barcos");
        mainPanel.add(stationPanel, "estaciones");

        // Listeners
        fleetsButton.addActionListener(e -> mostrarPantalla(mainPanel, "flotas"));
        peopleButton.addActionListener(e -> mostrarPantalla(mainPanel, "tripulacion"));
        ranksButton.addActionListener(e -> mostrarPantalla(mainPanel, "rangos"));
        shipsButton.addActionListener(e -> mostrarPantalla(mainPanel, "barcos"));
        stationsButton.addActionListener(e -> mostrarPantalla(mainPanel, "estaciones"));
        exitButton.addActionListener(e -> {
            frame.dispose();
            users.LoginApp.mostrarLogin();
        });

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static void mostrarPantalla(JPanel contenedor, String nombre) {
        CardLayout cl = (CardLayout) contenedor.getLayout();
        cl.show(contenedor, nombre);
    }

    private static void agregarBotonVolver(JPanel panel, JPanel contenedor) {
        JButton volverBtn = new JButton("⬅ Volver al menú");
        volverBtn.addActionListener(e -> mostrarPantalla(contenedor, "menu"));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(volverBtn);
        panel.add(top, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> mostrarMenu("UsuarioPrueba", "admin")); // o "consultor"
    }
}
