package com.interview.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test di integrazione base: verifica che il context Spring si carichi correttamente.
 *
 * - spring.main.headless=true impedisce a SwingAppRunner di aprire finestre durante i test
 * - @ActiveProfiles("dev") usa H2 in-memory invece di MySQL
 */
@SpringBootTest(properties = {
        "spring.main.headless=true"
})
@ActiveProfiles("dev")
class ApplicationTests {

    @Test
    void contextLoads() {
        // Il test passa se il context Spring si inizializza senza errori
    }
}
