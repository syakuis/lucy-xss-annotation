package org.syaku.spring.apps.xss.domain;

import lombok.Data;
import lombok.ToString;
import org.syaku.spring.xss.support.Defence;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 1.
 */
@ToString
@Data
public class Too {
	@Defence
	private String html;
	private String noHtml;

	public Too() {
	}

	public Too(String html, String noHtml) {
		this.html = html;
		this.noHtml = noHtml;
	}
}
