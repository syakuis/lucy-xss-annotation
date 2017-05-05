package org.syaku.spring.xss.support.reflection;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 5. 6.
 */
public class ObjectRefException extends ReflectiveOperationException {
	public ObjectRefException() {
		super();
	}

	public ObjectRefException(String message) {
		super(message);
	}

	public ObjectRefException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectRefException(Throwable cause) {
		super(cause);
	}
}
