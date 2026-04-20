package com.mipt.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.mipt.portal")
@SpringBootApplication(exclude = ElasticsearchRepositoriesAutoConfiguration.class)
public class PortalApplication {

  public static void main(String[] args) {
    SpringApplication.run(PortalApplication.class, args);
  }
}