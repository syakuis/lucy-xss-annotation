package org.syaku.spring.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.syaku.spring.boot.servlet.ServletConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
		Bootstrap.class,
		ServletConfiguration.class
})
public class BootstrapTest {
	@Test
	public void test() {
	}
}
