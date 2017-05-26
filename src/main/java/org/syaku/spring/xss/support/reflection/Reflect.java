package org.syaku.spring.xss.support.reflection;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syaku.spring.xss.support.Defence;
import org.syaku.spring.xss.support.DefenceIgnore;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 25.
 */
public class Reflect<T> {
	private static final Logger logger = LoggerFactory.getLogger(Reflect.class);

	private static final Class<? extends Annotation> ANNOTATION = Defence.class;
	private static final Class<? extends Annotation> IGNORE = DefenceIgnore.class;
	private final T object;
	private final ReflectConverter converter;

	public Reflect(T object, ReflectConverter converter) {
		if (object == null || converter == null) {
			throw new IllegalArgumentException("this argument is required; it must not be null");
		}

		this.object = object;
		this.converter = converter;
	}

	/**
	 * 무시될 타입
	 * @param clazz
	 * @return
	 */
	private boolean isTypeIgnore(Class<?> clazz) {
		return clazz.equals(Boolean.class) ||
				clazz.equals(Integer.class) ||
				clazz.equals(Character.class) ||
				clazz.equals(Byte.class) ||
				clazz.equals(Short.class) ||
				clazz.equals(Double.class) ||
				clazz.equals(Long.class) ||
				clazz.equals(Float.class) ||
				clazz.equals(Date.class) ||
				// array primitive type
				(clazz.isArray() && clazz.getComponentType().isPrimitive());
	}

	private void array(Object object, Annotation annotation) throws IllegalAccessException, NoSuchFieldException {
		Object[] result = (Object[]) object;

		int count = result.length;

		for (int i = 0; i < count; i++) {
			Object value = result[i];

			if (value == null || isTypeIgnore(value.getClass())) {
				continue;
			}

			typeSwitch(result[i], annotation);
		}
	}

	/**
	 * 같은 값을 가진 문자은 같은 공간에 저장되므로 하나가 변경되더라도 모두가 변경된다.
	 * @param object
	 * @param annotation
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	private void collection(Object object, Annotation annotation) throws IllegalAccessException, NoSuchFieldException {
		Collection<?> result = (Collection) object;
		for(Object value : result) {

			if (value == null || isTypeIgnore(value.getClass())) {
				continue;
			}

			typeSwitch(value, annotation);
		}
	}

	private void map(Object object, Annotation annotation) throws IllegalAccessException, NoSuchFieldException {
		Map result = (Map) object;
		Iterator<Map.Entry> iterator = result.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry entry = iterator.next();
			Object value = result.get(entry.getKey());

			if (value == null || isTypeIgnore(value.getClass())) {
				continue;
			}
			typeSwitch(value, annotation);
		}
	}

	private void typeSwitch(Object object, Annotation annotation) throws IllegalAccessException, NoSuchFieldException {
		Class<?> clazz = object.getClass();
		if (clazz == String.class) {
			setStringValue(object, annotation);
		} else if (clazz.isArray()) {
			array(object, annotation);
		} else if (Collection.class.isAssignableFrom(clazz)) {
			collection(object, annotation);
		} else if (Map.class.isAssignableFrom(clazz)) {
			map(object, annotation);
		} else {
			findByObjectField(object, annotation);
		}
	}

	/**
	 * {@link DefenceIgnore} 인 경우 작업이 무시한다.
	 *
	 * @param object
	 * @param anno
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	private void findByObjectField(Object object, Annotation anno) throws IllegalAccessException, NoSuchFieldException {
		Class<?> clazz = object.getClass();

		// extends
		final List<Field> fields = FieldUtils.getAllFieldsList(clazz);
		for(Field field : fields) {
			Annotation annotation = getAnnotation(field.getDeclaredAnnotations());
			annotation = annotation == null ? anno : annotation;

			if (annotation == null || annotation instanceof DefenceIgnore) {
				continue;
			}

			field.setAccessible(true);
			Object value = field.get(object);

			logger.debug(">< >< catch @{} {} {} = {}", annotation.annotationType(), field.getType(), field.getName(), value);

			if (value == null || isTypeIgnore(value.getClass())) {
				continue;
			}

			if (value.getClass() == String.class) {
				String aValue = converter.update(new String(value.toString()), annotation);

				if (aValue != null) {
					if (!Modifier.isFinal(field.getModifiers())) {
						logger.debug(">< >< update field value {} = {} -> {}", field.getName(), value, aValue);
						field.set(object, aValue);
					} else {
						logger.debug(">< >< update final value {} = {} -> {}", field.getName(), value, aValue);
						setFinalValue(object, field, aValue);
					}
				}
			} else {
				typeSwitch(value, annotation);
			}
		}
	}

	/**
	 * 문자 타입인 경우 값을 수정한다.
	 */
	private void setStringValue(Object object, Annotation annotation) throws IllegalAccessException, NoSuchFieldException {
		String aValue = converter.update(new String(object.toString()), annotation);

		Class<?> clazz = object.getClass();

		Field field = clazz.getDeclaredField("value");
		field.setAccessible(true);

		logger.debug(">< >< update string value {} = {} -> {}", field.getName(), field.get(object), aValue);

		field.set(object, aValue.toCharArray());
	}

	private void setFinalValue(Object object, Field field, String value) throws NoSuchFieldException, IllegalAccessException {
		field.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(object, value);
	}

	/**
	 * {@link DefenceIgnore} 을 찾은 경우 해당 어노테이션만 반환하고 종료한다.
	 * @param annotations
	 * @return
	 */
	private Annotation getAnnotation(Annotation[] annotations) {
		Annotation result = null;
		for (Annotation annotation : annotations) {
			if (IGNORE.equals(annotation.annotationType())) {
				return annotation;
			}
			if (ANNOTATION.equals(annotation.annotationType())) {
				result = annotation;
			}
		}

		return result;
	}

	public void go() throws ReflectException {
		Class<?> clazz = this.object.getClass();

		try {
			findByObjectField(this.object, getAnnotation(clazz.getAnnotations()));
		} catch (IllegalAccessException iae) {
			throw new ReflectException(iae.getMessage(), iae);
		} catch (NoSuchFieldException nsfe) {
			throw new ReflectException(nsfe.getMessage(), nsfe);
		}
	}
}
