package org.syaku.spring.xss.support;

import com.nhncorp.lucy.security.xss.XssFilter;
import com.nhncorp.lucy.security.xss.XssPreventer;
import com.nhncorp.lucy.security.xss.XssSaxFilter;
import org.syaku.spring.xss.support.reflection.ObjectRefConverter;

import java.lang.annotation.Annotation;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 4. 24.
 */
public class XssFilterConverter implements ObjectRefConverter {
	private XssFilter xssFilter;
	private XssSaxFilter xssSaxFilter;

	public XssFilterConverter(XssFilter xssFilter, XssSaxFilter xssSaxFilter) {
		this.xssFilter = xssFilter;
		this.xssSaxFilter = xssSaxFilter;
	}

	@Override
	public Object value(Object object, Annotation annotation) {
		if (annotation == null || object == null || object.getClass() != String.class) {
			return object;
		}

		XssType xssType = ((Defence) annotation).value();

		if (xssType.equals(XssType.SAX)) {
			return xssSaxFilter.doFilter(String.valueOf(object));
		} else if (xssType.equals(XssType.DOM)) {
			return xssFilter.doFilter(String.valueOf(object));
		} else {
			return XssPreventer.escape(String.valueOf(object));
		}
	}

	@Override
	public Class<? extends Annotation> getAnnotation() {
		return Defence.class;
	}
}
