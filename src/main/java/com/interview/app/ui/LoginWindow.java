package com.interview.app.ui;

import com.interview.app.dto.LoginRequestDTO;
import com.interview.app.dto.UtenteDTO;
import com.interview.app.service.UtenteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutionException;

/**
 * Finestra di login dell'applicazione (EXTRA #2).
 *
 * Mostrata come prima finestra all'avvio.
 * Se le credenziali sono corrette, si chiude e apre MainWindow.
 *
 * Bean Spring @Component singleton.
 * La chiamata al service avviene tramite SwingWorker per non bloccare l'EDT.
 */
@Component
@Lazy  // non istanziare all'avvio del context - solo quando richiesto esplicitamente
@Slf4j
public class LoginWindow extends JFrame {

    private final UtenteService utenteService;
    private final ApplicationContext applicationContext;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;

    public LoginWindow(UtenteService utenteService, ApplicationContext applicationContext) {
        this.utenteService = utenteService;
        this.applicationContext = applicationContext;
        initUI();
    }

    private void initUI() {
        setTitle("Rubrica Telefonica - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 240);
        setMinimumSize(new Dimension(320, 220));
        setLocationRelativeTo(null);
        setResizable(false);

        // === JToolBar (EXTRA #3) - intestazione ===
        JToolBar titleBar = new JToolBar();
        titleBar.setFloatable(false);
        titleBar.setBackground(new Color(41, 128, 185));
        JLabel titleLabel = new JLabel("  Rubrica Telefonica");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleBar.add(titleLabel);

        // === Form panel ===
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Username row
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);

        // Password row
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);

        // Login button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginButton = new JButton("Accedi");
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(loginButton.getFont().deriveFont(Font.BOLD));
        formPanel.add(loginButton, gbc);

        // Message label (per errori)
        gbc.gridy = 3;
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(new Color(192, 57, 43));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(messageLabel, gbc);

        // Hint
        gbc.gridy = 4;
        JLabel hintLabel = new JLabel("Default: admin / admin");
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setFont(hintLabel.getFont().deriveFont(10f));
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(hintLabel, gbc);

        // === Layout ===
        setLayout(new BorderLayout());
        add(titleBar, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);

        // === Azioni ===
        loginButton.addActionListener(e -> doLogin());

        // Enter su password → login
        passwordField.addActionListener(e -> doLogin());
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });

        getRootPane().setDefaultButton(loginButton);

        // Focus su username all'apertura
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                usernameField.requestFocus();
            }
        });
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Inserire username e password");
            return;
        }

        loginButton.setEnabled(false);
        messageLabel.setText("Verifica in corso...");
        messageLabel.setForeground(Color.GRAY);

        SwingWorker<UtenteDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected UtenteDTO doInBackground() {
                return utenteService.login(new LoginRequestDTO(username, password));
            }

            @Override
            protected void done() {
                try {
                    get(); // se ha lanciato eccezione, la rilancia qui

                    // Login riuscito
                    log.info("Login riuscito per: {}", username);
                    LoginWindow.this.setVisible(false);
                    LoginWindow.this.dispose();

                    MainWindow mainWindow = applicationContext.getBean(MainWindow.class);
                    mainWindow.setVisible(true);

                } catch (ExecutionException ex) {
                    log.warn("Login fallito per: {} - {}", username, ex.getCause().getMessage());
                    loginButton.setEnabled(true);
                    messageLabel.setForeground(new Color(192, 57, 43));
                    messageLabel.setText("Credenziali non valide");
                    passwordField.setText("");
                    passwordField.requestFocus();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    loginButton.setEnabled(true);
                    messageLabel.setText("");
                }
            }
        };
        worker.execute();
    }
}
