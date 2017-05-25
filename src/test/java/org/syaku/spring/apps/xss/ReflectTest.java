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
import java.util.*;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 25.
 */
public class ReflectTest {
	private static final Logger logger = LoggerFactory.getLogger(ReflectTest.class);

	/**
	 * good 이라는 값을 변경하면 모든 값들이 변경된다.
	 * 하지만 Foo 클래스의 good 을 리플랙션으로 변경하면 해당 클래스만 변경된다.
	 * @throws Exception
	 */
	@Test
	public void pass_by_referance_test() throws Exception {
		String name = "good";

		Foo foo = new Foo();
		foo.setName("good");
		foo.setName2("good");

		Reflect.upper2("good");

		logger.debug("{}", name);
		logger.debug("{}", foo);
	}

	@Test
	public void referenceType_test() throws Exception {
		ReferenceModel model = new ReferenceModel("syaku");
		model.setString("syaku");
		model.setNoConverter("syaku");
		model.setNullString(null);

		logger.debug(">< >< original {}", model.toString());

		Reflect<ReferenceModel> reflect = new Reflect<>(model, new ReplaceConverter());
		reflect.go();

		Assert.assertEquals(model.getFinalString(), "SYAKU");
		Assert.assertEquals(model.getString(), "SYAKU");
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

		List<String> list = new ArrayList<>();
		list.add("list1");
		list.add("list2");
		list.add("list2");
		model.setList(list);

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
	private List<String> list;
	private Map<String, String> map;

	private String[] array;

	@DefenceIgnore
	private String defenceIgnoreString;

	public CollectionModel(List<String> finalString, Set<Date> date) {
		this.finalString = finalString;
		this.date = date;
	}
}