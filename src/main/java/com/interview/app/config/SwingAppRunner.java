package com.interview.app.config;

import com.interview.app.ui.LoginWindow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

@Component
@RequiredArgsConstructor
@Slf4j
public class SwingAppRunner implements ApplicationRunner {

    private final ApplicationContext applicationContext;

    /**
     * Eseguito DOPO che il context Spring è completamente inizializzato.
     * Lancia la UI Swing sull'Event Dispatch Thread.
     *
     * Se l'ambiente è headless (server, CI), skippa l'avvio Swing.
     */
    @Override
    public void run(ApplicationArguments args) {
        if (GraphicsEnvironment.isHeadless()) {
            log.info("Ambiente headless rilevato - avvio Swing UI saltato");
            return;
        }

        log.info("Avvio interfaccia Swing...");
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = applicationContext.getBean(LoginWindow.class);
            loginWindow.setVisible(true);
        });
    }
}
