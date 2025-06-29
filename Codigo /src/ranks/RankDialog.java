package ranks;

import javax.swing.*;
import java.awt.*;

public class RankDialog extends JDialog {
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
            rank = rankToEdit;
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
