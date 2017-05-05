package org.syaku.spring.apps.xss.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhncorp.lucy.security.xss.XssFilter;
import com.nhncorp.lucy.security.xss.XssPreventer;
import com.nhncorp.lucy.security.xss.XssSaxFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.syaku.spring.xss.support.reflection.ObjectRef;
import org.syaku.spring.xss.support.reflection.ObjectRefConverter;

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

	@Test
	public void xssFilterValue() {
		Set<String> other = new HashSet<>();
		other.add(otherFilter);

		Foo foo = new Foo();
		foo.setOtherFilter(other);

		ObjectRefConverter converter = new BasicXssFilterConverter(xssFilter, xssSaxFilter);
		ObjectRef ref = new ObjectRef(converter);

		Foo result = ref.getValue(foo, Foo.class);
		Set<String> other2 = result.getOtherFilter();
		String otherFilter2 = other2.iterator().next();

		Assert.assertEquals(otherFilter2, xssSaxFilter.doFilter(otherFilter));
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
	public void postXssFilter() throws Exception {
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

		List<Doo> doos = new LinkedList<>();

		for (int i = 0; i < 10; i++) {
			doos.add(new Doo(saxFilter, saxFilter));
		}
		foo.setDoos(doos);

		Map<String, String> map = new HashMap<>();
		map.put("a", otherFilter);
		map.put("b", otherFilter);
		map.put("c", otherFilter);
		map.put("d", otherFilter);
		map.put("e", otherFilter);

		foo.setMap(map);

		String[] arrays = { otherFilter, otherFilter, otherFilter};
		foo.setArrays(arrays);

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
				.andExpect(jsonPath("$.otherFilter[0]", is(xssSaxFilter.doFilter(otherFilter))))
				.andExpect(jsonPath("$.toos[0].saxFilter", is(xssSaxFilter.doFilter(saxFilter))))
				.andExpect(jsonPath("$.toos[0].noFilter", is(saxFilter)))
				.andExpect(jsonPath("$.doos[0].saxFilter", is(xssSaxFilter.doFilter(saxFilter))))
				.andExpect(jsonPath("$.doos[0].noFilter", is(saxFilter)))
				.andExpect(jsonPath("$.map.e", is(xssSaxFilter.doFilter(otherFilter))))
				.andExpect(jsonPath("$.arrays[0]", is(xssSaxFilter.doFilter(otherFilter))))
				.andDo(print());
	}
}
