package com.kakawait;

import com.netflix.zuul.ZuulFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.ProxyRouteLocator;
import org.springframework.context.annotation.Bean;

/**
 * @author Thibaud Leprêtre
 */
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @ConditionalOnProperty(value = "zuul.add-proxy-headers", matchIfMissing = true)
    @Bean
    public ZuulFilter xForwardedFilter(ProxyRouteLocator routeLocator) {
        return new ForwardedHeaderFilter(routeLocator);
    }

}
