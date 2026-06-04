package com.example.vendeFacil.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Personaliza a página do Swagger UI (/swagger-ui.html) e o contrato
// OpenAPI (/v3/api-docs) com as informações do PratoFácil.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pratoFacilOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("PratoFácil API")
                .description("API REST para gestão de pedidos de pequenos empreendedores de comida. "
                        + "Demonstra conceitos de Sistemas Distribuídos: arquitetura cliente-servidor, "
                        + "API REST, protocolo HTTP, middleware e persistência de dados.")
                .version("v1"));
    }
}
