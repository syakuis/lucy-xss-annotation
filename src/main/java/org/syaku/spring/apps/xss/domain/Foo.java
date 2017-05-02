package org.syaku.spring.apps.xss.domain;

import lombok.Data;
import lombok.ToString;
import org.syaku.spring.xss.support.Defence;
import org.syaku.spring.xss.support.XssType;

import java.util.List;
import java.util.Set;

/**
 * 컬랙션이나 제너릭 타입인 경우 대상이 되는 클래스타입에 @{@link Defence} 사용해야 한다.
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 1.
 */
@ToString
@Data
public class Foo extends Ext {
	private String idx;
	@Defence
	private String filter;
	@Defence
	private List<Too> toos;
	private boolean use;
	@Defence(XssType.SAX)
	private Set<String> otherFilter;
}
