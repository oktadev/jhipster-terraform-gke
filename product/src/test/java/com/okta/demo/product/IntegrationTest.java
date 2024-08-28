package com.okta.demo.product;

import com.okta.demo.product.config.AsyncSyncConfiguration;
import com.okta.demo.product.config.EmbeddedSQL;
import com.okta.demo.product.config.JacksonConfiguration;
import com.okta.demo.product.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { ProductApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, TestSecurityConfiguration.class })
@EmbeddedSQL
public @interface IntegrationTest {
}
