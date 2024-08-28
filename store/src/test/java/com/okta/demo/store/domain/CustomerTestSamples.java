package com.okta.demo.store.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CustomerTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Customer getCustomerSample1() {
        return new Customer()
            .id(1L)
            .firstName("firstName1")
            .lastName("lastName1")
            .email("email1")
            .phone("phone1")
            .addressLine1("addressLine11")
            .addressLine2("addressLine21")
            .city("city1")
            .country("country1");
    }

    public static Customer getCustomerSample2() {
        return new Customer()
            .id(2L)
            .firstName("firstName2")
            .lastName("lastName2")
            .email("email2")
            .phone("phone2")
            .addressLine1("addressLine12")
            .addressLine2("addressLine22")
            .city("city2")
            .country("country2");
    }

    public static Customer getCustomerRandomSampleGenerator() {
        return new Customer()
            .id(longCount.incrementAndGet())
            .firstName(UUID.randomUUID().toString())
            .lastName(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString())
            .addressLine1(UUID.randomUUID().toString())
            .addressLine2(UUID.randomUUID().toString())
            .city(UUID.randomUUID().toString())
            .country(UUID.randomUUID().toString());
    }
}
