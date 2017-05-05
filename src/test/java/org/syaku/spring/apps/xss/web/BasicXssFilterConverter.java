package org.syaku.spring.apps.xss.web;

import com.nhncorp.lucy.security.xss.XssFilter;
import com.nhncorp.lucy.security.xss.XssSaxFilter;
import org.syaku.spring.xss.support.reflection.ObjectRefConverter;

import java.lang.annotation.Annotation;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 4.
 */
public class BasicXssFilterConverter implements ObjectRefConverter {
	private XssFilter xssFilter;
	private XssSaxFilter xssSaxFilter;

	public BasicXssFilterConverter(XssFilter xssFilter, XssSaxFilter xssSaxFilter) {
		this.xssFilter = xssFilter;
		this.xssSaxFilter = xssSaxFilter;
	}

	@Override
	public Object value(Object object, Annotation annotation) {
		if (object == null || object.getClass() != String.class) {
			return object;
		}

		String value = (String) object;

		return xssSaxFilter.doFilter(value);
	}

	@Override
	public Class<? extends Annotation> getAnnotation() {
		return null;
	}
}
