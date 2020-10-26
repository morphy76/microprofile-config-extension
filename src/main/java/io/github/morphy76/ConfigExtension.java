package io.github.morphy76;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.Converter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import io.smallrye.config.SmallRyeConfig;

/**
 * JUnit 5 extension to resolve microprofile @ConfigProperty in unit tests.
 *
 * @since 0.0.1
 */
public class ConfigExtension implements TestInstancePostProcessor {

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {

		final Config cfg = ConfigProvider.getConfig();

		Stream.of(testInstance.getClass().getDeclaredFields())
			  .filter(this::isConfigAnnotated)
			  .forEach(field -> set(field, testInstance, cfg));
			  
	}

	private boolean isConfigAnnotated(Field field) {
		return field.isAnnotationPresent(ConfigProperty.class);
	}

	private void set(Field field, Object testInstance, Config cfg) {

		final ConfigProperty  cfgProperty = field.getAnnotation(ConfigProperty.class);
		final Optional<?>     cfgValue    = cfg.getOptionalValue(cfgProperty.name(), field.getType());

		setFieldValue(field, testInstance, cfg, cfgProperty, cfgValue);
	}

	private void setFieldValue(Field field, Object testInstance,
	                           Config cfg, ConfigProperty cfgProperty, final Optional<?> cfgValue) {

		final Object  useValue   = extractValue(field.getType(), cfg, cfgProperty, cfgValue);

		synchronized(testInstance.getClass()) {
			final boolean accessible = field.isAccessible();
			field.setAccessible(true);
			try {
				field.set(testInstance, useValue);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO
			} finally {
				field.setAccessible(accessible);
			}
		}
	}

	private Object extractValue(Class<?> fieldType, Config cfg, ConfigProperty cfgProperty, final Optional<?> cfgValue) {

		Object useValue = null;
		if(cfgValue.isPresent()) {
			useValue = cfgValue.get();
		} else {
			useValue = fetchDefault(fieldType, cfg, cfgProperty.defaultValue());
		}
		return useValue;
	}

	private Object fetchDefault(Class<?> clazz, Config cfg, String defaultValue) {
		
		if(defaultValue != null) {
			Converter<?> converter = ((SmallRyeConfig) cfg).getConverter(clazz);
			return converter.convert(defaultValue);
		} else {
			throw new NoSuchElementException();
		}
	}
}
