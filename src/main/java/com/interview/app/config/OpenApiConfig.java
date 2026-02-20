package com.interview.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OpenApiConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/swagger-ui.html");
    }

    @Bean
    public OpenAPI rubricaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rubrica Telefonica API")
                        .version("1.0.0")
                        .description("""
                                API REST per la gestione della rubrica telefonica.

                                Funzionalità:
                                - CRUD completo per le Persone
                                - Filtri dinamici (nome, cognome, telefono, età)
                                - Paginazione e ordinamento
                                - Autenticazione utenti
                                - Esportazione persona in file

                                Swagger UI: /swagger-ui.html
                                API Docs JSON: /api-docs
                                """)
                        .contact(new Contact()
                                .name("Rubrica Telefonica")
                                .email("rubrica@example.com")));
    }
}
