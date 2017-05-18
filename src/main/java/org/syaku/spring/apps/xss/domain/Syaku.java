package org.syaku.spring.apps.xss.domain;

import lombok.Data;
import org.syaku.spring.xss.support.Defence;

import java.io.Serializable;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 18.
 */
@Data
@Defence
public class Syaku implements Serializable {
	private static final long serialVersionUID = -788258402617404494L;

	private String name;
	private int num;
}
