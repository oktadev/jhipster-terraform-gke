package com.okta.demo.product.domain;

import static com.okta.demo.product.domain.ProductCategoryTestSamples.*;
import static com.okta.demo.product.domain.ProductTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.okta.demo.product.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProductCategoryTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProductCategory.class);
        ProductCategory productCategory1 = getProductCategorySample1();
        ProductCategory productCategory2 = new ProductCategory();
        assertThat(productCategory1).isNotEqualTo(productCategory2);

        productCategory2.setId(productCategory1.getId());
        assertThat(productCategory1).isEqualTo(productCategory2);

        productCategory2 = getProductCategorySample2();
        assertThat(productCategory1).isNotEqualTo(productCategory2);
    }

    @Test
    void productTest() {
        ProductCategory productCategory = getProductCategoryRandomSampleGenerator();
        Product productBack = getProductRandomSampleGenerator();

        productCategory.addProduct(productBack);
        assertThat(productCategory.getProducts()).containsOnly(productBack);
        assertThat(productBack.getProductCategory()).isEqualTo(productCategory);

        productCategory.removeProduct(productBack);
        assertThat(productCategory.getProducts()).doesNotContain(productBack);
        assertThat(productBack.getProductCategory()).isNull();

        productCategory.products(new HashSet<>(Set.of(productBack)));
        assertThat(productCategory.getProducts()).containsOnly(productBack);
        assertThat(productBack.getProductCategory()).isEqualTo(productCategory);

        productCategory.setProducts(new HashSet<>());
        assertThat(productCategory.getProducts()).doesNotContain(productBack);
        assertThat(productBack.getProductCategory()).isNull();
    }
}
