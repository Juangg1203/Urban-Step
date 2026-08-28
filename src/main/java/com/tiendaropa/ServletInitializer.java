package com.tiendaropa;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/** Permite desplegar el WAR en un Tomcat externo ademas del embebido. */
public class ServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TiendaRopaApplication.class);
    }
}
