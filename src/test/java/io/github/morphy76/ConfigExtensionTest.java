package io.github.morphy76;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ConfigExtension.class)
class ConfigExtensionTest {
    
    @ConfigProperty(name = "annotated.property") String annotatedProperty;
    @ConfigProperty(name = "use.default.string.property", defaultValue = "string.is.ok") String useStringDefault;
    @ConfigProperty(name = "use.default.long.property", defaultValue = "1000") long useLongDefault;

    @Test
    @DisplayName("Assert value for annotated property without default")
    void testAnnotatedPropertyWithoutDefault() throws Exception {
        assertEquals("annotated.property", annotatedProperty);
    }

    @Test
    @DisplayName("Assert value for annotated string property with default")
    void testAnnotatedStringPropertyWithDefault() throws Exception {
        assertEquals("string.is.ok", useStringDefault);
    }

    @Test
    @DisplayName("Assert value for annotated long property with default")
    void testAnnotatedLongPropertyWithDefault() throws Exception {
        assertEquals(1_000L, useLongDefault);
    }

    @Test
    @DisplayName("Access microprofile programmatically")
    void testProvidedProperty() throws Exception {

        // Fake test cause actually provided by smallrye config
        Config cfg = ConfigProvider.getConfig();
        assertEquals("api.property", cfg.getValue("api.property", String.class));
    }
}
