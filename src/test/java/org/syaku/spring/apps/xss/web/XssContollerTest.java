package org.syaku.spring.apps.xss.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhncorp.lucy.security.xss.XssFilter;
import com.nhncorp.lucy.security.xss.XssPreventer;
import com.nhncorp.lucy.security.xss.XssSaxFilter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.syaku.spring.apps.xss.domain.Doo;
import org.syaku.spring.apps.xss.domain.Foo;
import org.syaku.spring.apps.xss.domain.Too;
import org.syaku.spring.boot.Bootstrap;
import org.syaku.spring.boot.servlet.ServletConfiguration;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 4. 13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
		Bootstrap.class,
		ServletConfiguration.class
})
public class XssContollerTest {
	private static final Logger logger = LoggerFactory.getLogger(XssContollerTest.class);

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private XssSaxFilter xssSaxFilter;

	@Autowired
	private XssFilter xssFilter;

	String escape = "\"><script>alert('xss_');</script>";
	String filter = "<img src=\"<img src=1\\ onerror=alert(1234)>\" onerror=\"alert('XSS')\">";
	String saxFilter = "<TABLE class=\"NHN_Layout_Main\" style=\"TABLE-LAYOUT: fixed\" cellSpacing=\"0\" cellPadding=\"0\" width=\"743\">" + "</TABLE>" + "<SPAN style=\"COLOR: #66cc99\"><img src=\"<img src=1\\ onerror=alert(1234)>\" onerror=\"alert('XSS')\"></SPAN>";
	String otherFilter = "<IMG SRC=j&#X41vascript:alert('test2')><b onmouseover=alert('Wufff!')>click me!</b><img src=\"http://url.to.file.which/not.exist\" onerror=alert(document.cookie);>";

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
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

	private void printType(Class<?> clz, String name) {
		if (isWrapperType(clz) || clz == String.class) {
			logger.debug(">< >< {} ==>  wrapperType", name);
		} else if (clz.isArray()) {
			logger.debug(">< >< {} ==>  array", name);
		} else if (Collection.class.isAssignableFrom(clz)) {
			logger.debug(">< >< {} ==>  collection", name);
		} else if (Map.class.isAssignableFrom(clz)) {
			logger.debug(">< >< {} ==>  map", name);
		} else {
			logger.debug(">< >< {} ==> class type", name);
		}
	}

	@Test
	public void typeTest() {
		Set<String> set = new HashSet<>();
		printType(set.getClass(), "set");
		Map<String, Object> map = new LinkedHashMap<>();
		printType(map.getClass(), "map");
		List<String> list = new ArrayList<>();
		printType(list.getClass(), "list");
		Object object = new Object();
		printType(object.getClass(), "object");
		String string = new String();
		printType(string.getClass(), "string");
		Integer integer = new Integer(1);
		printType(integer.getClass(), "integer");
	}

	@Test
	public void getXssFilter() throws Exception {
		mockMvc.perform(get("/xss?html=" + escape))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.html", is(XssPreventer.escape(escape))))
				.andDo(print());
	}

	@Test
	public void xssFilter() throws Exception {
		Foo foo = new Foo();
		foo.setIdx("10000");
		foo.setFilter(filter);
		foo.setUse(true);
		foo.setEscape(escape);

		Set<String> other = new HashSet<>();
		other.add(otherFilter);
		foo.setOtherFilter(other);

		List<Too> toos = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			toos.add(new Too(saxFilter, saxFilter));
		}
		foo.setToos(toos);

		List<Doo> doos = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			doos.add(new Doo(saxFilter, saxFilter));
		}
		foo.setDoos(doos);

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(foo);

 		mockMvc.perform(
				post("/xss/10000")
						.content(json)
						.contentType(MediaType.APPLICATION_JSON_UTF8)
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.idx", is(foo.getIdx())))
				.andExpect(jsonPath("$.filter", is(xssFilter.doFilter(filter))))
				.andExpect(jsonPath("$.escape", is(XssPreventer.escape(escape))))
				.andExpect(jsonPath("$.use", is(true)))
				//.andExpect(jsonPath("$.otherFilter[0]", is(xssSaxFilter.doFilter(otherFilter))))
				.andExpect(jsonPath("$.toos[0].saxFilter", is(xssSaxFilter.doFilter(saxFilter))))
				.andExpect(jsonPath("$.toos[0].noFilter", is(saxFilter)))
				.andExpect(jsonPath("$.doos[0].saxFilter", is(xssSaxFilter.doFilter(saxFilter))))
				.andExpect(jsonPath("$.doos[0].noFilter", is(saxFilter)))
				.andDo(print());

	}
}
