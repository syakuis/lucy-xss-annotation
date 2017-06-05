package org.syaku.spring.apps.xss;

import lombok.Data;
import lombok.ToString;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syaku.spring.xss.support.Defence;
import org.syaku.spring.xss.support.DefenceIgnore;
import org.syaku.spring.xss.support.XssType;
import org.syaku.spring.xss.support.reflection.Reflect;
import org.syaku.spring.xss.support.reflection.ReflectConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 25.
 */
public class ReflectTest {
	private static final Logger logger = LoggerFactory.getLogger(ReflectTest.class);

	private void reflectString(Object object, boolean upper) throws IllegalAccessException, NoSuchFieldException {
		Class<?> clazz = object.getClass();

		Field field = clazz.getDeclaredField("value");
		field.setAccessible(true);

		String aValue = new String(String.valueOf(object));

		if (upper) {
			aValue = aValue.toUpperCase();
		} else {
			aValue = aValue.toLowerCase();
		}

		field.set(object, aValue.toCharArray());
	}

	private void reflectField(Object object, boolean upper) throws IllegalAccessException, NoSuchFieldException {
		Class<?> clazz = object.getClass();

		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			Object value = field.get(object);

			if (value == null) {
				continue;
			}

			String aValue = new String(value.toString());

			if (upper) {
				aValue = aValue.toUpperCase();
			} else {
				aValue = aValue.toLowerCase();
			}

			field.set(object, aValue);
		}
	}

	private void reflectCollection(Object object) throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		//Object[] objects = (Object[]) object;

		//logger.debug("{} {}", objects.hashCode(), object.hashCode());

		if (!Collection.class.isAssignableFrom(object.getClass())) {
			return;
		}

		Collection<String> collection = (Collection) object;

		Collection result;

		Class<?> arraysType = Class.forName("java.util.Arrays$ArrayList");
		if (arraysType.isAssignableFrom(object.getClass())) {
			result = new ArrayList();
		} else {
			result = (Collection) object.getClass().newInstance();
		}

		for (String text : collection) {
			result.add(text.toUpperCase());
		}

		Class<? extends Collection> clazz = collection.getClass();
//		Constructor[] constructors = clazz.getDeclaredConstructors();
//
//		if (constructors.length > 0) {
//			constructors[0].setAccessible(true);
//			constructors[0].newInstance(result);
//		}

//		for (Constructor constructor : constructors) {
//			constructor.setAccessible(true);
//			logger.debug("{} <---", constructor.getName());
//
//			//constructor.newInstance(new Object[]{});
//		}

		logger.debug("{}", clazz);
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			Object value = field.get(object);
			logger.debug("{} {}", field.getType().getComponentType(), field.getName());

			if (field.getName().equals("a")) {
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

				field.set(object, new Object[]{});
			} else if (field.getName().equals("elementData")) {
				logger.debug("{} {} {}", field.getType(), field.getName(), value);
			}
		}
	}

	/**
	 * good 이라는 값을 변경하면 모든 값들이 변경된다.
	 * 하지만 Foo 클래스의 good 을 리플랙션으로 변경하면 해당 클래스만 변경된다.
	 * 이건 문자 타입을 직접 제어했기 때문에 모두가 변경되는 것이고
	 * 한번 클래스 타입으로 랩핑하면 클래스 타입을 참조하기 때문에 단일적으로만 변경된다.
	 * @throws Exception
	 */
	@Test
	public void reflection_string_test() throws Exception {
		String name = "good";

		Foo foo = new Foo();
		foo.setName("good");
		foo.setName2("good");

		reflectString(name, true);

		logger.debug("{}, {}", name, foo);

		reflectField(foo, false);

		logger.debug("{}, {}", name, foo);

		List<String> list = new ArrayList<>();
		list.add("aa");
		list.add("aa");
		list.add("aa");

		for (String string : list) {
			reflectString(string, true);
			logger.debug("{}, {}", list, string);
		}
	}

	/**
	 * collection 의 같은 값의 문자를 하나라도 변경하면 같은 값 모두가 변경된다.
	 * Arrays.asList() 는 또 다른 리스트 타입이고 이는 불편이여서 직접 변경할 수 없다. 해당 클래스를 열어보면 알 수 있다.
	 *
	 * @throws Exception
	 */
	@Test
	public void reflection_collection_test() throws Exception {
		List<String> arrayListBak = new ArrayList<>();
		arrayListBak.add("a");
		arrayListBak.add("b");
		arrayListBak.add("a");

		List<String> arrayList = new ArrayList<>();
		arrayList.add("a");
		arrayList.add("b");
		arrayList.add("a");

		List<String> arraysList = Arrays.asList("b", "b", "b");
		// UnsupportedOperationException 발생
		// arraysList.add("good");

		Assert.assertNotSame(arrayList, arraysList);

		Collection collection = arrayList.getClass().newInstance();

		for (String text : arrayList) {
			collection.add(text.toUpperCase());
		}

		arrayList.clear();
		arrayList.addAll(collection);

		Assert.assertEquals(arrayList, collection);
		Assert.assertNotSame(arrayList, collection);

		logger.debug("{} != {} == {}", arrayListBak, arrayList, collection);

		reflectCollection(arraysList);
		reflectCollection(arrayList);
		reflectCollection(new HashSet<>());
		reflectCollection(new HashMap<>());
		reflectCollection(new String[]{});

		//Assert.assertEquals(arraysList.size(), 0);


		logger.debug("{} != {} == {}", arraysList, arrayList, collection);
	}

	/**
	 * 컬랙션을 변경하기 위해 새로 객체를 생성하서 새로 만들 후 다시 대입해야 한다.
	 * @throws Exception
	 */
	@Test
	public void referenceType_test() throws Exception {
		String name = "syaku";
		ReferenceModel model = new ReferenceModel("syaku");
		model.setString("syaku");
		model.setString2("syaku");
		model.setNoConverter("syaku");
		model.setNullString(null);

		logger.debug(">< >< original {}", model.toString());

		Reflect<ReferenceModel> reflect = new Reflect<>(model, new ReplaceConverter());
		reflect.go();

		Assert.assertEquals(model.getFinalString(), "SYAKU");
		Assert.assertEquals(model.getString(), "SYAKU");
		Assert.assertEquals(model.getString2(), "SYAKU");
		Assert.assertEquals(model.getNoConverter(), "syaku");
		Assert.assertNull(model.getNullString());
		Assert.assertNull(model.getNoInstance());
		Assert.assertNull(model.getDate());
		Assert.assertFalse(model.isBool());
		Assert.assertEquals(model.getInteger(), 0);

		logger.debug(">< >< converter {}", model.toString());
	}

	@Test
	public void collectionType_test() throws Exception {
		Set<Date> date = new HashSet<>();
		date.add(new Date());

		CollectionModel model = new CollectionModel(
				Arrays.asList("good", "good", "good"),
				date
		);

		Map<String, String> map = new HashMap<>();
		map.put("map1", "good");
		map.put("map2", "good");
		map.put("map3", "good");
		model.setMap(map);

		List<List<String>> list = new ArrayList<>();
		list.add(Arrays.asList("list1"));
		list.add(Arrays.asList("list1"));
		list.add(Arrays.asList("list1"));
		model.setList(list);


		List<Map<String, List<String>>> listMap = new ArrayList<>();
		Map<String, List<String>> mapList = new HashMap<>();
		mapList.put("111", Arrays.asList("good"));
		listMap.add(mapList);
		model.setListMap(listMap);

		model.setArray(new String[]{ "array1", "array2", "array2" });

		model.setDefenceIgnoreString("ignore");

		Reflect<CollectionModel> reflect = new Reflect<>(model, new ReplaceConverter());
		reflect.go();

		logger.debug(">< >< converter {}", model.toString());
	}
}

class ReplaceConverter implements ReflectConverter {
	private static final Logger logger = LoggerFactory.getLogger(ReplaceConverter.class);

	@Override
	public String update(String object, Annotation annotation) {
		if (annotation == null || object == null || object.getClass() != String.class) {
			return object;
		}

		XssType xssType = ((Defence) annotation).value();

		logger.debug(">< >< replace @{} value : {}", xssType, object);

		return object.toUpperCase();
	}
}

@Data
@ToString
class Foo {
	private String name;
	private String name2;
}

@ToString
@Data
class ReferenceModel {
	private static final Logger logger = LoggerFactory.getLogger(org.syaku.spring.apps.xss.model.Foo.class);

	@Defence
	private final String finalString;
	@Defence
	private String string;
	@Defence
	private String string2;
	@DefenceIgnore
	private String noConverter;
	@Defence
	private String nullString;
	@Defence
	private String noInstance;
	@Defence
	private int integer;
	@Defence
	private boolean bool;
	@Defence
	private Date date;

	public ReferenceModel(String finalString) {
		this.finalString = finalString;
	}
}

@ToString
@Data
@Defence
class CollectionModel {
	private final List<String> finalString;
	private List<Integer> integer;
	private Map<String, Boolean> bool;
	private final Set<Date> date;
	private List<List<String>> list;
	private List<Map<String, List<String>>> listMap;
	private Map<String, String> map;

	private String[] array;

	@DefenceIgnore
	private String defenceIgnoreString;

	public CollectionModel(List<String> finalString, Set<Date> date) {
		this.finalString = finalString;
		this.date = date;
	}
}