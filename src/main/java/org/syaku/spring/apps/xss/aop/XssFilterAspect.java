package org.syaku.spring.apps.xss.aop;

import com.nhncorp.lucy.security.xss.XssFilter;
import com.nhncorp.lucy.security.xss.XssSaxFilter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.syaku.spring.xss.support.reflection.ObjectRef;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 4. 13.
 */
@Aspect
@Component
public class XssFilterAspect {
	private static final Logger logger = LoggerFactory.getLogger(XssFilterAspect.class);

	@Autowired
	private XssSaxFilter xssSaxFilter;

	@Autowired
	private XssFilter xssFilter;

	@Pointcut("within(org.syaku.spring.apps.*.web.*) && @target(org.springframework.stereotype.Controller)")
	public void porintTarget() {
		if (logger.isDebugEnabled()) {
			logger.debug(">< >< invoke aspectj");
		}
	}

	@Before("porintTarget() && (execution(public * *(.., @org.syaku.spring.xss.support.Defence (*))) || execution(public * *(@org.syaku.spring.xss.support.Defence (*), ..)) || execution(public * *(.., @org.syaku.spring.xss.support.Defence (*), ..)))")
	public void xssFilter(JoinPoint point) throws InstantiationException, IllegalAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug(">< >< invoke aspectj");
		}
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();

		Object[] args =  point.getArgs();
		XssFilterConverter converter = new XssFilterConverter(xssFilter, xssSaxFilter);
		ObjectRef objectRef = new ObjectRef(converter);
		objectRef.getMethodParameter(method, args);
		logger.debug("{}", Arrays.asList(args).toString());
	}
}