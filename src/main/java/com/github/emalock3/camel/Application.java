package com.github.emalock3.camel;

import java.time.LocalDateTime;
import java.util.function.Supplier;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ContextScanDefinition;
import org.apache.camel.spring.CamelContextFactoryBean;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {
    
    @Bean
    public CamelContextFactoryBean camelContextFactoryBean(
            ApplicationContext applicationContext) {
        CamelContextFactoryBean ccfb = new CamelContextFactoryBean();
        ccfb.setApplicationContext(applicationContext);
        ccfb.setId("camelContext");
        ccfb.setContextScan(new ContextScanDefinition());
        return ccfb;
    }
    
    @Bean
    public Supplier<LocalDateTime> currentTimeFactory() {
        return () -> LocalDateTime.now();
    }
    
    @Bean
    RouteBuilder route1() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start1")
                        .log("Hello, ${body}!")
                        .transform(body(String.class).prepend("-+-+-").append("-+-+-"))
                        .to("direct:start2");
            }
        };
    }
    
    @Bean
    RouteBuilder route2(Supplier<LocalDateTime> currentTimeFactory) {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start2")
                        .setHeader("curTime", method(currentTimeFactory))
                        .log("${header[curTime]} - Hello, ${body}!!");
            }
        };
    }
    
    public static void main(String ... args) {
        try (ConfigurableApplicationContext context = 
                new SpringApplicationBuilder(Application.class)
                        .showBanner(false)
                        .web(false)
                        .run(args)) {
            ProducerTemplate producer = context.getBean(SpringCamelContext.class).createProducerTemplate();
            producer.sendBody("direct:start1", "World");
            producer.sendBody("direct:start1", "Hogehoge");
        }
    }
}
