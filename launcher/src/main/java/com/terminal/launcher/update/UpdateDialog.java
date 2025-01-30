package com.terminal.launcher.update;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class UpdateDialog extends JDialog implements UpdateCallback {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton cancelButton;
    private boolean isCancelled = false;

    public UpdateDialog(JFrame parent) {
        super(parent, "Обновление Terminal", true);
        initComponents();
    }

    private void initComponents() {
        setSize(400, 150);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        statusLabel = new JLabel("Подготовка к обновлению...");
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> {
            isCancelled = true;
            dispose();
        });

        panel.add(statusLabel, gbc);
        panel.add(progressBar, gbc);
        panel.add(cancelButton, gbc);

        add(panel);
    }

    @Override
    public void onUpdateStart() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Начало обновления...");
            progressBar.setValue(0);
        });
    }

    @Override
    public void onUpdateProgress(float progress, String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            progressBar.setValue((int)(progress * 100));
        });
    }

    @Override
    public void onUpdateComplete() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                "Обновление успешно завершено!\nПерезапустите приложение для применения обновлений.",
                "Обновление завершено",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });
    }

    @Override
    public void onUpdateError(String error) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                "Ошибка при обновлении: " + error,
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
            dispose();
        });
    }

    public boolean isCancelled() {
        return isCancelled;
    }
} 