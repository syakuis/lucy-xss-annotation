package org.syaku.spring.apps.xss.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.syaku.spring.apps.xss.domain.Foo;
import org.syaku.spring.xss.support.Defence;
import org.syaku.spring.xss.support.XssType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 1.
 */
@Controller
@RequestMapping("/xss")
public class XssController {

	@GetMapping("")
	@ResponseBody
	public Map<String, String> dispView(
			@RequestParam(value = "html", required = false) @Defence(XssType.ESCAPE) String html) {
		Map<String, String> result = new HashMap<>();
		result.put("html", html);
		return result;
	}

	@PostMapping("/{idx}")
	@ResponseBody
	public Foo procPost(
			@Defence @RequestBody Foo foo,
			@Defence @PathVariable("idx") String idx) {
		foo.setIdx(idx);
		return foo;
	}
}
