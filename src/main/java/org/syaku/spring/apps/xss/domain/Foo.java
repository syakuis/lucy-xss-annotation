package org.syaku.spring.apps.xss.domain;

import lombok.Data;
import lombok.ToString;
import org.syaku.spring.xss.support.Defence;

import java.util.List;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 1.
 */
@ToString
@Data
@Defence
public class Foo extends Ext {
	private String name;
	private List<Too> toos;
	private boolean use;
}
