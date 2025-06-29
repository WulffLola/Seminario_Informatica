package users;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginApp {

    public static void mostrarLogin() {
        JFrame frame = new JFrame("Login");
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);

        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 200, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 200, 25);
        panel.add(passwordText);

        JButton loginButton = new JButton("Iniciar sesión");
        loginButton.setBounds(100, 90, 150, 25);
        panel.add(loginButton);

        JLabel messageLabel = new JLabel("");
        messageLabel.setBounds(10, 120, 300, 25);
        panel.add(messageLabel);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());

                boolean autenticado = AuthService.login(username, password);
                if (autenticado) {
                    String role = AuthService.getRole(username);
                    JOptionPane.showMessageDialog(panel, "Bienvenido, " + username + " (" + role + ")");
                    SwingUtilities.getWindowAncestor(panel).dispose(); // cerrar login
                    app.MainMenu.mostrarMenu(username, role); // pasar usuario y rol al menú
                } else {
                    messageLabel.setText("Usuario o contraseña incorrectos");
                }
            }
        });
    }
}
