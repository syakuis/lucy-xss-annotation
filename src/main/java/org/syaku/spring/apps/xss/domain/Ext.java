package org.syaku.spring.apps.xss.domain;

import lombok.Data;
import lombok.ToString;
import org.syaku.spring.xss.support.Defence;
import org.syaku.spring.xss.support.XssType;

import java.util.List;
import java.util.Map;

/**
 * 상속된 자식은 항목에 @{@link Defence} 를 사용해야 한다.
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 1.
 */
@ToString
@Data
public class Ext {
	@Defence(XssType.ESCAPE)
	private String escape;
	@Defence
	private List<Doo> doos;
	@Defence(XssType.SAX)
	private Map<String, String> map;
	@Defence(XssType.SAX)
	private String[] arrays;
}
