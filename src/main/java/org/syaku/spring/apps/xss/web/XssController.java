package org.syaku.spring.apps.xss.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.syaku.spring.apps.xss.domain.Foo;
import org.syaku.spring.xss.support.Defence;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 1.
 */
@Controller
@RequestMapping("/xss")
public class XssController {
	private static final Logger logger = LoggerFactory.getLogger(XssController.class);

	@GetMapping("")
	public String getView(
			@Defence @RequestParam(value = "html", required = false) String html) {
		logger.debug("{}", html);
		return "xss/xss";
	}

	@PostMapping("/{idx}")
	@ResponseBody
	public Foo getPost(
			@Defence @RequestBody Foo foo,
			@PathVariable("idx") String idx,
			@RequestParam(value = "html", required = false) String html) {
		logger.debug("{}", html);
		logger.debug("{}", foo);
		return foo;
	}
}
