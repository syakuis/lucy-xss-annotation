# lucy xss annotation

[![Build Status](https://semaphoreci.com/api/v1/syaku/lucy-xss-annotation/branches/master/shields_badge.svg)](https://semaphoreci.com/syaku/lucy-xss-annotation) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/44746b1e1bea4cc8ae35122a732d8bf9)](https://www.codacy.com/app/syaku/lucy-xss-annotation?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=syakuis/lucy-xss-annotation&amp;utm_campaign=Badge_Grade) 

https://github.com/naver/lucy-xss-filter 를 선언적인 방식(@annotation)으로 필터할 수 있게 개발하였습니다.

테스트용 소스와 함께 포함되어 있어 **기본 클래스는 다음과 같습니다.**

- /src/main/java/org/syaku/spring/boot/config/LucyXssFilterConfiguration.java
- /src/main/java/org/syaku/spring/xss/support/**

### 설정

```java
@Configuration
public class LucyXssFilterConfiguration {

	@Bean
	public XssSaxFilter xssSaxFilter() {
		return XssSaxFilter.getInstance("lucy-xss-sax.xml", true);
	}

	@Bean
	public XssFilter xssFilter() {
		return XssFilter.getInstance("lucy-xss.xml", true);
	}

	@Bean
	public XssFilterAspect xssFilterAspect() {
		return new XssFilterAspect();
	}
}
```

### 사용법

```java

@Defence
@Defence(XssType.DOM)

```

자바빈 클래스와 자바빈 클래스 항목, 메서드 파라메터에 어노테이션을 사용할 수 있다. 메서드 파라메터는 꼭 @Cotroller 스테레오 타입의 클래스에서만 사용할 수 있다.

