package org.syaku.spring.xss.support.reflection;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 리플랙션을 이용하여 객체를 조작한다.
 *
 * 1. 모든 타입의 객체를 변경한다.
 * 2. 원하는 어노테이션의 객체를 변경한다.
 *
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http ://syaku.tistory.com
 * @since 2017. 4. 24.
 */
public class ObjectRef {
	private static final Logger logger = LoggerFactory.getLogger(ObjectRef.class);

	private final ObjectRefConverter converter;
	private Class<? extends Annotation> annotation;

	public ObjectRef(ObjectRefConverter converter) {
		this.converter = converter;
		this.annotation = converter.getAnnotation();
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(Object value, Class<T> clazz) {
		return getValue(value, null, clazz);
	}

	/**
	 * 객체의 값을 수정한다.
	 *
	 * @param <T>        the type parameter
	 * @param value      the value
	 * @param annotation the annotation
	 * @param clazz      the clazz
	 * @return the value
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(Object value, Annotation annotation, Class<T> clazz) {
		return clazz.cast(getType(value, annotation));
	}

	/**
	 * 메서드의 파라메터값을 수정한다.
	 *
	 * @param method the method
	 * @param args   the args
	 */
	public Object[] getMethodParameter(Method method, Object[] args) {
		if (logger.isDebugEnabled()) {
			logger.debug("MethodName {}, Parameters {}, Annotation", method.getName(), Arrays.asList(args).toString(), this.annotation);
		}
		// v1.7
		int i = 0;
		Annotation[][] methodAnnotations = method.getParameterAnnotations();
		for (Annotation[] annotations : methodAnnotations) {
			for (Annotation annotation : annotations) {
				if (annotation != null && this.annotation.equals(annotation.annotationType())) {
					// 배열은 reflection array 사용해야 데이터를 변경할 수 있다.
					// args[i] = getType(args[i], annotation);
					Array.set(args, i, getType(args[i], annotation));
				}
			}
			i++;
		}

		// 메서드 파라메터 모든 어노에티션을 가져온다.
		// v1.8
		/*
		Parameter[] parameters = method.getParameters();

		int size = args.length;

		if (size != parameters.length) {
			throw new IndexOutOfBoundsException();
		}

		for (int i = 0; i < size; i++) {
			Annotation annotation = parameters[i].getAnnotation(this.annotation);
			if (annotation != null) {
				logger.debug("Method parameter before value {}", args[i]);
				// 배열은 reflection array 사용해야 데이터를 변경할 수 있다.
				// args[i] = getType(args[i], annotation);
				Array.set(args, i, getType(args[i], annotation));
				logger.debug("Method parameter after value {}", args[i]);
			}
		}
		*/

		return args;
	}

	private boolean isWrapperType(Class<?> clazz) {
		return clazz.equals(Boolean.class) ||
				clazz.equals(Integer.class) ||
				clazz.equals(Character.class) ||
				clazz.equals(Byte.class) ||
				clazz.equals(Short.class) ||
				clazz.equals(Double.class) ||
				clazz.equals(Long.class) ||
				clazz.equals(Float.class);
	}

	private Object getType(Object value, Annotation annotation) {
		if (value == null) {
			return null;
		}
		Class clz = value.getClass();

		try {
			if (isWrapperType(clz) || clz == String.class) {
				logger.debug(">< >< === Reference Type {} {} {}", annotation, value, clz);
				return converter.value(value, annotation);
			} else if (clz.isArray()) {
				return getArray(value, annotation);
			} else if (Collection.class.isAssignableFrom(clz)) {
				return getCollection(value, annotation);
			} else if (Map.class.isAssignableFrom(clz)) {
				return getMap(value, annotation);
			} else {
				return getObject(value);
			}
		} catch (IllegalAccessException iae) {
			logger.error(iae.getMessage(), iae);
		} catch (InstantiationException ie) {
			logger.error(ie.getMessage(), ie);
		}

		return value;
	}

	private Object getObject(Object object) throws IllegalAccessException, InstantiationException {
		Class clz = object.getClass();

		boolean isAnnoTypeClz = true;
		Annotation annotationClz = null;

		if (this.annotation != null) {
			// class에 어노테이션이 있는 경우
			annotationClz = clz.getAnnotation(this.annotation);
			if (annotationClz != null) {
				isAnnoTypeClz = this.annotation.equals(annotationClz.annotationType());
			}
		}

		logger.debug(">< >< >>> in Object @{} ({}) {}", object.hashCode(), clz, object);

		final List<Field> fields = FieldUtils.getAllFieldsList(clz);
		//Field[] fields = clz.getDeclaredFields();
		for(Field field : fields) {
			field.setAccessible(true);
			Object value = field.get(object);
			Annotation annotation = annotationClz;
			boolean isAnnoType = isAnnoTypeClz;

			if (this.annotation != null && annotationClz == null) {
				annotation = field.getAnnotation(this.annotation);
				if (annotation != null) {
					isAnnoType = this.annotation.equals(annotation.annotationType());
				}
			}

			// annotation 조건이 있는 경우
			if (value != null && isAnnoType) {
				Object result = getType(value, annotation);
				logger.debug(">< >< === changing Object field @{} {} isAnnoType: {}, value: {}, result: {}", object.hashCode(), field.getName(), isAnnoType, value, result);

				// primitive type 은 null 넣을 수 없다.
				if (result == null) {
					result = value;
				}

				field.set(object, result);
			}
		}

		logger.debug(">< >< <<< out Object @{} ({}) {}", object.hashCode(), clz, object);
		return object;
	}

	private Object[] getArray(Object object, Annotation annotation) throws IllegalAccessException, InstantiationException {
		Object[] objects = (Object[]) object;
		int count = objects.length;

		Object[] result = (Object[]) Array.newInstance(object.getClass().getComponentType(), count);

		for (int i = 0; i < count; i++) {
			Array.set(result, i, getType(objects[i], annotation));
		}

		return result;
	}

	private Map getMap(Object object, Annotation annotation) throws IllegalAccessException, InstantiationException {
		Map result = (Map) object.getClass().newInstance();
		Map map = (Map) object;

		Iterator<Map.Entry> iterator = map.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry entry = iterator.next();
			Object key = entry.getKey();
			result.put(key, getType(map.get(key), annotation));
		}


		return result;
	}

	private Collection getCollection(Object object, Annotation annotation) throws InstantiationException, IllegalAccessException {
		Class clz = object.getClass();
		Collection result = Collections.emptyList();

		try {
			// Arrays.asTo(...) ArrayList 와 다른 타입이므로 변경한다.
			Class<?> arraysType = Class.forName("java.util.Arrays$ArrayList");
			if (arraysType.isAssignableFrom(clz)) {
				result = new ArrayList();
			} else {
				result = (Collection) object.getClass().newInstance();
			}

			for(Object value : (Collection) object) {
				result.add(getType(value, annotation));
			}
		} catch (ClassNotFoundException e) {
			logger.debug(e.getMessage());
		}

		return result;
	}
}