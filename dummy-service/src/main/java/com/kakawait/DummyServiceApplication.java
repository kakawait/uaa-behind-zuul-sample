package com.kakawait;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * @author Thibaud Leprêtre
 */
@SpringBootApplication
@EnableResourceServer
@EnableDiscoveryClient
public class DummyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DummyServiceApplication.class, args);
    }

    @Controller
    @RequestMapping("/")
    public static class DummyController {

        @Autowired
        private ResourceServerTokenServices tokenService;

        @RequestMapping(method = RequestMethod.GET)
        @ResponseBody
        public String helloWorld(Principal principal) {
            return principal == null ? "Hello anonymous" : "Hello " + principal.getName();
        }

        @RequestMapping(path = "/jwt", method = RequestMethod.GET)
        @ResponseBody
        public OAuth2AccessToken jwt(OAuth2Authentication authentication) {
            OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
            return tokenService.readAccessToken(details.getTokenValue());
        }
    }

}
