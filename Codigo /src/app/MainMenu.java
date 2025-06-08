package app;

import rank.RankPanel;
import people.PeoplePanel;

import javax.swing.*;
import java.awt.*;

public class MainMenu {

    public static void mostrarMenu(String username) {
        JFrame frame = new JFrame("Panel Principal - Bienvenido " + username);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1, 10, 10));

        JButton fleetsButton = new JButton("Flotas");
        JButton peopleButton = new JButton("Tripulación");
        JButton ranksButton = new JButton("Rangos");
        JButton shipsButton = new JButton("Barcos");
        JButton stationsButton = new JButton("Estaciones");

        panel.add(fleetsButton);
        panel.add(peopleButton);
        panel.add(ranksButton);
        panel.add(shipsButton);
        panel.add(stationsButton);

        frame.add(panel);
        frame.setVisible(true);

        // Mensajes temporales para botones no implementados
        fleetsButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Entrando a Flotas..."));
        shipsButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Entrando a Barcos..."));
        stationsButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Entrando a Estaciones..."));

        // Abrir panel de personas al presionar Tripulación
        peopleButton.addActionListener(e -> {
            JFrame peopleFrame = new JFrame("Gestión de Tripulación");
            peopleFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            peopleFrame.add(new PeoplePanel());
            peopleFrame.setSize(700, 400);
            peopleFrame.setLocationRelativeTo(frame);
            peopleFrame.setVisible(true);
        });

        // Abrir panel de rangos al presionar Rangos
        ranksButton.addActionListener(e -> {
            JFrame rankFrame = new JFrame("Gestión de Rangos");
            rankFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            rankFrame.add(new RankPanel());
            rankFrame.setSize(500, 300);
            rankFrame.setLocationRelativeTo(frame);
            rankFrame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> mostrarMenu("UsuarioPrueba"));
    }
}
