package com.okta.demo.invoice.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ShipmentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Shipment getShipmentSample1() {
        return new Shipment().id(1L).trackingCode("trackingCode1").details("details1");
    }

    public static Shipment getShipmentSample2() {
        return new Shipment().id(2L).trackingCode("trackingCode2").details("details2");
    }

    public static Shipment getShipmentRandomSampleGenerator() {
        return new Shipment()
            .id(longCount.incrementAndGet())
            .trackingCode(UUID.randomUUID().toString())
            .details(UUID.randomUUID().toString());
    }
}
