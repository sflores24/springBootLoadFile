package mx.com.personal.springboot.app;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class MVCConfig implements WebMvcConfigurer {
	/*public void addResourceHandlers(ResourceHandlerRegistration registry) {
		registry.addResourceLocations("/pictures/**")
				.addResourceLocations("file:///home/workspaces/spring/spring-boot-data-jpa4/uploads/");
	}*/
}
