package com.tiendaropa.config;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * Internacionalizacion (ES/EN) para las vistas JSP.
 *
 * El idioma se guarda en la sesion HTTP (SessionLocaleResolver), no en
 * cookie, asi que se mantiene mientras el usuario navega pero se resetea
 * al cerrar sesion o abrir el sitio en otro navegador.
 *
 * Para cambiar de idioma desde cualquier pantalla basta con agregar
 * ?lang=en o ?lang=es a la URL actual (ver LocaleChangeInterceptor abajo).
 */
@Configuration
public class LocalizacionConfig implements WebMvcConfigurer {

    /** Idioma por defecto: espanol, si el usuario no eligio ninguno. */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(new Locale("es"));
        return resolver;
    }

    /** Intercepta el parametro ?lang=xx en cualquier request y cambia el idioma. */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Value("${app.imagenes.carpeta}")
    private String carpetaImagenes;

    /**
     * Las imagenes de producto viven en una carpeta del disco, no dentro del
     * war: si estuvieran dentro, cada despliegue borraria las que subio el
     * administrador. Se publican bajo /imagenes/**.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registro) {
        String ruta = java.nio.file.Paths.get(carpetaImagenes).toAbsolutePath().normalize().toString();
        registro.addResourceHandler("/imagenes/**")
                .addResourceLocations("file:" + ruta + java.io.File.separator);
    }

    /** Busca los archivos classpath:messages_es.properties / messages_en.properties */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true); // si falta una clave, muestra la clave en vez de romper
        return source;
    }
}
