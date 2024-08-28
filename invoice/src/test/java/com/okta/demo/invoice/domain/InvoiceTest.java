package com.okta.demo.invoice.domain;

import static com.okta.demo.invoice.domain.InvoiceTestSamples.*;
import static com.okta.demo.invoice.domain.ShipmentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.okta.demo.invoice.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Invoice.class);
        Invoice invoice1 = getInvoiceSample1();
        Invoice invoice2 = new Invoice();
        assertThat(invoice1).isNotEqualTo(invoice2);

        invoice2.setId(invoice1.getId());
        assertThat(invoice1).isEqualTo(invoice2);

        invoice2 = getInvoiceSample2();
        assertThat(invoice1).isNotEqualTo(invoice2);
    }

    @Test
    void shipmentTest() {
        Invoice invoice = getInvoiceRandomSampleGenerator();
        Shipment shipmentBack = getShipmentRandomSampleGenerator();

        invoice.addShipment(shipmentBack);
        assertThat(invoice.getShipments()).containsOnly(shipmentBack);
        assertThat(shipmentBack.getInvoice()).isEqualTo(invoice);

        invoice.removeShipment(shipmentBack);
        assertThat(invoice.getShipments()).doesNotContain(shipmentBack);
        assertThat(shipmentBack.getInvoice()).isNull();

        invoice.shipments(new HashSet<>(Set.of(shipmentBack)));
        assertThat(invoice.getShipments()).containsOnly(shipmentBack);
        assertThat(shipmentBack.getInvoice()).isEqualTo(invoice);

        invoice.setShipments(new HashSet<>());
        assertThat(invoice.getShipments()).doesNotContain(shipmentBack);
        assertThat(shipmentBack.getInvoice()).isNull();
    }
}
