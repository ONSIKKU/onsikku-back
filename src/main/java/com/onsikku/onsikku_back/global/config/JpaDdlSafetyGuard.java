package com.onsikku.onsikku_back.global.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class JpaDdlSafetyGuard implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Set<String> DANGEROUS_DDL_AUTO_VALUES = Set.of("create", "create-drop");
    private static final Set<String> SAFE_PROFILES = Set.of("local", "test");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String ddlAuto = normalize(environment.getProperty("spring.jpa.hibernate.ddl-auto"));

        if (!DANGEROUS_DDL_AUTO_VALUES.contains(ddlAuto) || isDestructiveDdlAllowed(environment)) {
            return;
        }

        String activeProfiles = Arrays.stream(environment.getActiveProfiles())
            .map(this::normalize)
            .filter(profile -> !profile.isBlank())
            .collect(Collectors.joining(", "));

        throw new IllegalStateException(
            "Refusing to start with spring.jpa.hibernate.ddl-auto=" + ddlAuto
                + ". Active profiles=" + (activeProfiles.isBlank() ? "[default]" : activeProfiles)
                + ". Destructive DDL is only allowed for local/test profiles or when APP_ALLOW_DESTRUCTIVE_DDL=true."
        );
    }

    private boolean isDestructiveDdlAllowed(ConfigurableEnvironment environment) {
        Boolean explicitAllow = environment.getProperty("app.safety.allow-destructive-ddl", Boolean.class, false);
        if (Boolean.TRUE.equals(explicitAllow)) {
            return true;
        }

        return Arrays.stream(environment.getActiveProfiles())
            .map(this::normalize)
            .anyMatch(SAFE_PROFILES::contains);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
