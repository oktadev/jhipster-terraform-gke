package com.okta.demo.invoice.domain;

import static com.okta.demo.invoice.domain.InvoiceTestSamples.*;
import static com.okta.demo.invoice.domain.ShipmentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.okta.demo.invoice.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ShipmentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Shipment.class);
        Shipment shipment1 = getShipmentSample1();
        Shipment shipment2 = new Shipment();
        assertThat(shipment1).isNotEqualTo(shipment2);

        shipment2.setId(shipment1.getId());
        assertThat(shipment1).isEqualTo(shipment2);

        shipment2 = getShipmentSample2();
        assertThat(shipment1).isNotEqualTo(shipment2);
    }

    @Test
    void invoiceTest() {
        Shipment shipment = getShipmentRandomSampleGenerator();
        Invoice invoiceBack = getInvoiceRandomSampleGenerator();

        shipment.setInvoice(invoiceBack);
        assertThat(shipment.getInvoice()).isEqualTo(invoiceBack);

        shipment.invoice(null);
        assertThat(shipment.getInvoice()).isNull();
    }
}
