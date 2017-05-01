package org.syaku.spring.apps.xss.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhncorp.lucy.security.xss.XssFilter;
import com.nhncorp.lucy.security.xss.XssSaxFilter;
import org.junit.Before;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.syaku.spring.apps.xss.domain.Foo;
import org.syaku.spring.apps.xss.domain.Too;
import org.syaku.spring.boot.Bootstrap;
import org.syaku.spring.boot.servlet.ServletConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
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
	String saxFilter = "<TABLE class=\"NHN_Layout_Main\" style=\"TABLE-LAYOUT: fixed\" cellSpacing=\"0\" cellPadding=\"0\" width=\"743\">" + "</TABLE>" + "<SPAN style=\"COLOR: #66cc99\"></SPAN>";


	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	public void getXssFilter() throws Exception {
		mockMvc.perform(get("/xss?html=" + escape))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	public void xssFilter() throws Exception {
		Foo foo = new Foo();

		foo.setName(escape);
		foo.setUse(true);
		foo.setExtName(filter);

		List<Too> toos = new ArrayList<>();

		for (int i = 0; i < 100; i++) {
			toos.add(new Too(saxFilter, saxFilter));
		}

		foo.setToos(toos);
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(foo);

 		mockMvc.perform(
				post("/xss/10000?html=" + escape)
						.content(json)
						.contentType(MediaType.APPLICATION_JSON_UTF8)
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print());

	}
}
