package org.syaku.spring.boot.servlet;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.WebContentInterceptor;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.nio.charset.StandardCharsets;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 4. 13.
 */
@Configuration
@EnableWebMvc
@ComponentScan(
		basePackages = "org.syaku.spring.apps",
		useDefaultFilters = false,
		includeFilters = @ComponentScan.Filter(
				type = FilterType.ANNOTATION,
				classes = {
						Controller.class,
						ControllerAdvice.class,
						RestController.class,
						RestControllerAdvice.class
				}
		)
)
public class ServletConfiguration extends WebMvcConfigurerAdapter implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources").setCachePeriod(0);
	}

	// 확장자에 따른 미디어타입을 설정한다.
	// 확장자를 무시하지 말고 알수 없는 확장자 인 경우 기본은 text/html 미디어타입을 사용한다.
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		//configurer.ignoreUnknownPathExtensions(false).defaultContentType(MediaType.TEXT_HTML);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		WebContentInterceptor wci = new WebContentInterceptor();
		wci.setCacheSeconds(0);
		registry.addInterceptor(wci);
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		FreeMarkerViewResolver viewResolver = new FreeMarkerViewResolver();
		viewResolver.setExposeSpringMacroHelpers(true);

		viewResolver.setContentType("text/html; charset=" + StandardCharsets.UTF_8.name() +";");
		viewResolver.setCache(true);
		viewResolver.setPrefix("");
		viewResolver.setSuffix(".ftl");

		registry.viewResolver(viewResolver);
	}
}
