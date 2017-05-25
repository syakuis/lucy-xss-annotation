package org.syaku.spring.apps.xss.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 25.
 */
public class Foo {
	private static final Logger logger = LoggerFactory.getLogger(Foo.class);

	private final String title;
	private String name;

	public Foo(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
