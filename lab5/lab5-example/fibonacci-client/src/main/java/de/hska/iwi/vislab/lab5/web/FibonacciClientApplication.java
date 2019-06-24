package de.hska.iwi.vislab.lab5.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
@EnableOAuth2Client
@Controller
public class FibonacciClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(FibonacciClientApplication.class, args);
	}

	@Autowired
	private OAuth2RestOperations restTemplate;

	@RequestMapping("/")
	public String home() {
		return "fibonacci";
	}

	@RequestMapping("/fibonacci")
	public String next(Model model) {
		String fibonacci = restTemplate.getForObject("http://localhost:8080/fibonacci", String.class);
		model.addAttribute("fibonacci", fibonacci);
		return "fibonacci";
	}

	@Bean
	public OAuth2RestOperations restTemplate(OAuth2ClientContext oauth2ClientContext) {
		return new OAuth2RestTemplate(resource(), oauth2ClientContext);
	}

	@Bean
	protected OAuth2ProtectedResourceDetails resource() {
		ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
		resource.setId("oauth2-resource");
		resource.setAccessTokenUri("http://localhost:8080/oauth/token");
		resource.setClientId("fibonacci-client");
		resource.setClientSecret("secret");
		resource.setGrantType("client_credentials");
		return resource;
	}

}
