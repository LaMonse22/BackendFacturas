package com.facturacion.facturacion.config;

import org.springframework.context.annotation.Configuration; // Importa para marcar la clase como una fuente de definiciones de beans.
import org.springframework.web.servlet.config.annotation.CorsRegistry; // Importa para configurar las reglas CORS.
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // Interfaz para configurar Spring MVC.

@Configuration // Le dice a Spring que esta clase contiene métodos de configuración y es una fuente de definiciones de beans.
public class CorsConfig implements WebMvcConfigurer { // Implementa WebMvcConfigurer para personalizar la configuración de Spring MVC, incluyendo CORS.

    @Override // Sobreescribe el método de la interfaz WebMvcConfigurer.
    public void addCorsMappings(CorsRegistry registry) { // Este método es donde defines tus reglas CORS.
        registry.addMapping("/api/**") // Aplica las reglas CORS a cualquier URL que comience con "/api/" (ej. /api/facturacion, /api/users, etc.). Esto cubre todos tus endpoints bajo /api.
                .allowedOrigins("http://localhost:5173") // ¡IMPORTANTE! Especifica los orígenes (dominios/puertos) que están permitidos para acceder a tu API. Si tu frontend React se ejecuta en http://localhost:3000, debe estar aquí. Puedes añadir más si tienes otros frontends (ej. "https://tu-dominio.com").
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Define los métodos HTTP permitidos desde los orígenes especificados. OPTIONS es importante para las solicitudes "preflight" de CORS.
                .allowedHeaders("*") // Permite todas las cabeceras HTTP en las solicitudes. Podrías especificar cabeceras si lo necesitaras por seguridad.
                .allowCredentials(true); // Permite el envío de credenciales (como cookies, encabezados de autorización) en las solicitudes CORS. Necesario si usas autenticación basada en sesión o tokens en cabeceras.
    }
}