package org.syaku.spring.xss.support.reflection;

import java.lang.annotation.Annotation;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http ://syaku.tistory.com
 * @since 2017. 4. 24.
 */
public interface ObjectRefConverter {
	/**
	 * 원래 객체를 임의적으로 조작하여 반환한다. 변경하지 않을 경우 원래 객체 그래도를 반환한다.
	 *
	 * @param object     기존 객체
	 * @param annotation  해당 객체에 할당된 어노테이션
	 * @return the object
	 */
	Object value(Object object, Annotation annotation);

	/**
	 * 어노테이션 클래스와 일치하는 어노테이션만 value 메서드 파라메터로 얻는 다.
	 *
	 * @return the annotation
	 */
	Class<? extends Annotation> getAnnotation();
}