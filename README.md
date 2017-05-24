# Spring XSS Filter Annotation : lucy xss filter

[![Build Status](https://semaphoreci.com/api/v1/syaku/lucy-xss-annotation/branches/master/shields_badge.svg)](https://semaphoreci.com/syaku/lucy-xss-annotation) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/44746b1e1bea4cc8ae35122a732d8bf9)](https://www.codacy.com/app/syaku/lucy-xss-annotation?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=syakuis/lucy-xss-annotation&amp;utm_campaign=Badge_Grade) 

XSS(크로스 사이트 스크립팅) 취약점을 방어하기 위한 Secured Coding 을 자동화 시켜주는 프로그램입니다. 필터링을 자동화의 처리하는 기본적인 라이브러리로 https://github.com/naver/lucy-xss-filter 를 이용하고 이것을 선언적인 방식(@annotation)으로 구현하여 쉽게 적용할 수 있게 개발하였습니다.

### 문제점에 대하여...

- 요청된 입력값을 모두 필터할 경우 



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



### 선언적인 어노테이션 사용방법

@Defence 어노테이션은 @Controller 스테레오 타입에 한해 클래스 메서드 파라메터, 클래스(자식 포함), 클래스 항목에만 사용할 수 있다. 그리고 문자열만 필터된다.

```java
public void method(
	@Defence String filter, // (1)
	@Defence Foo foo			 // (2)
) { ...

```

메서드 파라메터가 (1)참조타입(기본타입)인 경우에만 필터가 작동하고 (2)클래스타입? 인 경우 클래스 내부에 또다시 @Defence 을 선언해야 한다. 다시말해 (2)번의 경우 필터 되는 것이 아니라 필터 대상이 된다. 실제 필터가 작동되게 하려면 Foo 클래스 내부에 @Defence 을 선언해야 한다.

```java
@Defence
public class Foo { ...
```

클래스에 @Defence 을 선언하면 모두를 항목이 필터된다.

```java
@Defence // (1)
public class Foo extends Too { ...
```

상속의 경우 (1)번과 같이 클래스에 선언하면 Foo Too 모두 필터가 된다. 만약 항목에 @Defence 을 선언한다면 자식 클래스 항목에도 @Defence 을 선언해야 필터된다. 
단 자식 클래스는 클래스에 @Defence 를 선언할 수 없다. 꼭 항목에 선언해야 한다. (이부분은 추후 패치할 예정이다.)
부모 클래스에 @Defence 를 선언하고 자식 클래스 항목에 @Defence 를 선언하면 자식 클래스 항목의 @Defence 무시된다. 부모 클래스 @Defence 선언에 따라 필터된다.

```java
public class Foo {
	@Defence
	private String name;
	@Defence
	private List<String> lists; // (1)
	@Defence
	private List<Too> toos; // (2)
	...
	
```

클래스 항목에 @Defence 을 선언하면 해당 항목만 필터한다. (1)번의 경우 제너릭타입이 참조나 기본타입인 경우 필터가 되지만 (2)번 같이 클래스타입인 경우는 필터가 되는 것이 아니라 필터 대상이 되고 Too 클래스 내부에 @Defence 을 선언해야 한다.


### 적용하기

```java
@Controller
@RequestMapping("/xss")
public class XssController {

	@GetMapping("")
	@ResponseBody
	public Map<String, String> dispView(
			@RequestParam(value = "html", required = false) @Defence(XssType.ESCAPE) String html) {
			
		Map<String, String> result = new HashMap<>();
		result.put("html", html);
		return result;
	}

	@PostMapping("/{idx}")
	@ResponseBody
	public Foo procPost(
			@Defence @RequestBody Foo foo,
			@Defence @PathVariable("idx") String idx) {
			
		foo.setIdx(idx);
		return foo;
	}
}
```
