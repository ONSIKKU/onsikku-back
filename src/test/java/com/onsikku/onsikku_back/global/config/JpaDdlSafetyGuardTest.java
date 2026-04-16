package com.onsikku.onsikku_back.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JpaDdlSafetyGuardTest {

    private final JpaDdlSafetyGuard guard = new JpaDdlSafetyGuard();

    @Test
    void blocksDestructiveDdlOutsideSafeProfiles() {
        GenericApplicationContext context = contextWith("prod", "create", false);

        assertThatThrownBy(() -> guard.initialize(context))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ddl-auto=create");
    }

    @Test
    void allowsDestructiveDdlForLocalProfile() {
        GenericApplicationContext context = contextWith("local", "create", false);

        assertThatNoException().isThrownBy(() -> guard.initialize(context));
    }

    @Test
    void allowsSafeDdlForProductionProfile() {
        GenericApplicationContext context = contextWith("prod", "validate", false);

        assertThatNoException().isThrownBy(() -> guard.initialize(context));
    }

    @Test
    void allowsExplicitOverride() {
        GenericApplicationContext context = contextWith("prod", "create", true);

        assertThatNoException().isThrownBy(() -> guard.initialize(context));
    }

    private GenericApplicationContext contextWith(String profile, String ddlAuto, boolean allowDestructiveDdl) {
        GenericApplicationContext context = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();

        if (profile != null && !profile.isBlank()) {
            environment.setActiveProfiles(profile);
        }

        environment.setProperty("spring.jpa.hibernate.ddl-auto", ddlAuto);
        environment.setProperty("app.safety.allow-destructive-ddl", Boolean.toString(allowDestructiveDdl));
        context.setEnvironment(environment);
        return context;
    }
}
