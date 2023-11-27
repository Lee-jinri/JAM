package com.jam.config;


import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer{
	// 스프링 시큐리티가 제공하는 필터들을 사용할 수 있도록 활성화 해준다.
}

/*
 * Spring Security 를 웹 에서 사용하기 위해서는 앞선 포스팅에서도 언급했듯이 Spring Security Filter 를 사용할 수 있게 스프링 프로젝트에 등록해줘야한다.

따라서 이를 가능하게 해줄, AbstractSecurityWebApplicationInitializer 라는 클래스를 상속 받게 해야한다.


 
 AbstractSecurityWebApplicationInitializer를 상속받은 클래스를 생성하면 Spring Security 필터 체인을 활성화할 수 있습니다. 이 클래스는 Servlet 3.0+ 환경에서 Spring Security를 구성하고 초기화하는 데 사용됩니다. 이 클래스를 상속받는 것은 Spring Security 필터 체인을 등록하고 구성하는 한 가지 방법입니다.

AbstractSecurityWebApplicationInitializer를 상속받는 클래스를 생성하면 Spring Security 필터 체인이 자동으로 활성화됩니다. 이 클래스는 다음과 같은 Spring Security 필터를 활성화합니다:

DelegatingFilterProxy: 이 필터는 Servlet 컨테이너의 필터 체인에 등록되어 Spring Security 필터 체인을 활성화합니다. 이 필터는 springSecurityFilterChain 빈에 연결되어 있는 필터 체인을 실행합니다.
AbstractSecurityWebApplicationInitializer 클래스를 사용하면 Spring Security를 구성하고 시작하는 데 필요한 기본적인 작업을 간소화할 수 있으며, 보안 설정을 초기화하는 데 도움을 줍니다. 이 클래스를 사용하면 web.xml 파일을 편집하지 않고도 Spring Security를 구성할 수 있습니다.
 
 다음은 DelegatingFilterProxy에 대한 더 자세한 설명입니다:

DelegatingFilterProxy
Spring Bean으로 필터 인스턴스화: DelegatingFilterProxy는 web.xml 또는 Java 기반 설정에서 정의한 Spring Bean 이름을 기반으로 필터를 Spring Bean으로 인스턴스화합니다. 이렇게 함으로써 Spring의 IoC 컨테이너에서 필터의 생명주기를 관리하고 Spring ApplicationContext에서 필터를 구성할 수 있습니다.

Spring Security와 함께 사용: Spring Security는 DelegatingFilterProxy를 통해 Spring Security 필터 체인을 서블릿 컨테이너의 필터 체인에 등록합니다. 이렇게 하면 Spring Security 필터들이 요청을 처리하고 사용자 인증 및 권한 부여를 수행할 수 있습니다.

설정 및 구성: Spring Security 필터 체인은 일반적으로 복잡하며 다양한 옵션을 가질 수 있습니다. DelegatingFilterProxy를 사용하면 필터 체인을 구성하는 데 필요한 설정을 Spring Bean으로 정의하고, Spring의 강력한 구성 기능을 활용하여 필터 체인을 구성할 수 있습니다.

서블릿 컨테이너와의 통합: DelegatingFilterProxy는 서블릿 컨테이너의 필터 체인에 필터를 등록하는 역할을 합니다. 따라서 서블릿 컨테이너는 이 필터를 요청 처리에 참여시킵니다.

보안 및 인증: 주로 Spring Security와 관련이 있지만, 다른 커스텀 필터도 Spring Bean으로 관리하고 구성할 수 있습니다. 이를 통해 인증, 권한 부여, 로깅, 압축 및 기타 웹 애플리케이션과 관련된 작업을 수행하는 서블릿 필터를 구현할 수 있습니다.
 
 
 \
 아니면 WebConfig에 이거 추가
 	@Bean
    public FilterRegistrationBean<DelegatingFilterProxy> securityFilterChain() {
        FilterRegistrationBean<DelegatingFilterProxy> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new DelegatingFilterProxy(AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME));
        filterRegistrationBean.addUrlPatterns("/*"); // 필터를 모든 URL에 적용하려면 수정하세요.

        return filterRegistrationBean;
    }
    
    이건 web.xml
    <filter>
    <filter-name>springSecurityFilterChain</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
</filter>

<filter-mapping>
    <filter-name>springSecurityFilterChain</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
  
  <filter-name>은 springSecurityFilterChain으로 설정되어 있어야 합니다. 이 이름은 Spring Security 필터 체인을 찾아서 활성화하는 데 사용됩니다.
  
  <filter-class>는 org.springframework.web.filter.DelegatingFilterProxy로 설정되어야 합니다.
  
  <filter-mapping>을 사용하여 어떤 URL 패턴에 대해 Spring Security 필터 체인을 활성화할 것인지 정의합니다. 위의 예제에서는 모든 URL에 대해 활성화하도록 설정되어 있습니다.
 
 
 * 
 * 
 * */
