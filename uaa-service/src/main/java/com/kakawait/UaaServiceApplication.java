package com.kakawait;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

/**
 * @author Thibaud Leprêtre
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableWebSecurity
public class UaaServiceApplication extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(UaaServiceApplication.class, args);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/oauth/confirm_access").setViewName("authorize");
    }

    @Order(ManagementServerProperties.ACCESS_OVERRIDE_ORDER)
    @Configuration
    protected static class LoginConfiguration extends WebSecurityConfigurerAdapter {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.formLogin().loginPage("/login").permitAll().and().authorizeRequests()
                .anyRequest().authenticated();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.parentAuthenticationManager(authenticationManager);
        }
    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Bean
        public TokenEnhancer tokenEnhancer() {
            return new CustomTokenEnhancer();
        }

        @Bean
        public TokenStore tokenStore() {
            return new JwtTokenStore(jwtAccessTokenConverter());
        }

        @Bean
        public JwtAccessTokenConverter jwtAccessTokenConverter() {
            JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
            KeyPair keyPair = new KeyStoreKeyFactory(new ClassPathResource("keystore.jks"), "foobar".toCharArray())
                    .getKeyPair("test");
            converter.setKeyPair(keyPair);
            return converter;
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory()
                   .withClient("acme")
                   .secret("acmesecret")
                   .authorizedGrantTypes("authorization_code", "refresh_token", "password")
                   .scopes("openid");
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
            tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter()));

            endpoints.authenticationManager(authenticationManager)
                     .tokenEnhancer(tokenEnhancerChain)
                     .tokenStore(tokenStore());
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
        }

    }

    private static class CustomTokenEnhancer implements TokenEnhancer {
        @Override
        public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
            Map<String, Object> additionalInfo = new HashMap<>();
            additionalInfo.put("additional", "some-information");
            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
            return accessToken;
        }
    }
}
