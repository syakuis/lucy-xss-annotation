package org.syaku.spring.apps.xss.domain;

import lombok.Data;
import lombok.ToString;
import org.syaku.spring.xss.support.Defence;
import org.syaku.spring.xss.support.XssType;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 2.
 */
@ToString
@Data
public class Doo {
	@Defence(XssType.SAX)
	private String saxFilter;
	private String noFilter;

	public Doo() {
	}

	public Doo(String saxFilter, String noFilter) {
		this.saxFilter = saxFilter;
		this.noFilter = noFilter;
	}
}
