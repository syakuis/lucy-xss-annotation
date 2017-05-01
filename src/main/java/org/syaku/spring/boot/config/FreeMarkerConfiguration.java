package org.syaku.spring.boot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 4. 13.
 */
@Configuration
public class FreeMarkerConfiguration {
	@Bean(name ="freemarkerConfig")
	public FreeMarkerConfigurer freeMarkerConfigurer() {
		FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
		configurer.setTemplateLoaderPaths(
				"classpath:/META-INF/resources/WEB-INF/views/",
				"/WEB-INF/views/"
		);
		configurer.setDefaultEncoding(StandardCharsets.UTF_8.name());

		Properties properties = new Properties();
		properties.setProperty("cache_storage", "freemarker.cache.NullCacheStorage");
		properties.setProperty("auto_import", "spring.ftl as spring");
		properties.setProperty("number_format", "0.####");
		configurer.setFreemarkerSettings(properties);

		return configurer;
	}
}
