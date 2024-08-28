package com.okta.demo.invoice;

import com.okta.demo.invoice.config.AsyncSyncConfiguration;
import com.okta.demo.invoice.config.EmbeddedSQL;
import com.okta.demo.invoice.config.JacksonConfiguration;
import com.okta.demo.invoice.config.TestSecurityConfiguration;
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
@SpringBootTest(classes = { InvoiceApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, TestSecurityConfiguration.class })
@EmbeddedSQL
public @interface IntegrationTest {
}
