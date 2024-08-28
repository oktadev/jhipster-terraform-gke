package com.okta.demo.product.web.rest;

import static com.okta.demo.product.domain.ProductCategoryAsserts.*;
import static com.okta.demo.product.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.demo.product.IntegrationTest;
import com.okta.demo.product.domain.ProductCategory;
import com.okta.demo.product.repository.ProductCategoryRepository;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProductCategoryResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProductCategoryResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/product-categories";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProductCategoryMockMvc;

    private ProductCategory productCategory;

    private ProductCategory insertedProductCategory;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductCategory createEntity(EntityManager em) {
        ProductCategory productCategory = new ProductCategory().name(DEFAULT_NAME).description(DEFAULT_DESCRIPTION);
        return productCategory;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductCategory createUpdatedEntity(EntityManager em) {
        ProductCategory productCategory = new ProductCategory().name(UPDATED_NAME).description(UPDATED_DESCRIPTION);
        return productCategory;
    }

    @BeforeEach
    public void initTest() {
        productCategory = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedProductCategory != null) {
            productCategoryRepository.delete(insertedProductCategory);
            insertedProductCategory = null;
        }
    }

    @Test
    @Transactional
    void createProductCategory() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ProductCategory
        var returnedProductCategory = om.readValue(
            restProductCategoryMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(productCategory))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProductCategory.class
        );

        // Validate the ProductCategory in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertProductCategoryUpdatableFieldsEquals(returnedProductCategory, getPersistedProductCategory(returnedProductCategory));

        insertedProductCategory = returnedProductCategory;
    }

    @Test
    @Transactional
    void createProductCategoryWithExistingId() throws Exception {
        // Create the ProductCategory with an existing ID
        productCategory.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductCategoryMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(productCategory))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        productCategory.setName(null);

        // Create the ProductCategory, which fails.

        restProductCategoryMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(productCategory))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProductCategories() throws Exception {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.saveAndFlush(productCategory);

        // Get all the productCategoryList
        restProductCategoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(productCategory.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getProductCategory() throws Exception {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.saveAndFlush(productCategory);

        // Get the productCategory
        restProductCategoryMockMvc
            .perform(get(ENTITY_API_URL_ID, productCategory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(productCategory.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingProductCategory() throws Exception {
        // Get the productCategory
        restProductCategoryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProductCategory() throws Exception {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.saveAndFlush(productCategory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the productCategory
        ProductCategory updatedProductCategory = productCategoryRepository.findById(productCategory.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProductCategory are not directly saved in db
        em.detach(updatedProductCategory);
        updatedProductCategory.name(UPDATED_NAME).description(UPDATED_DESCRIPTION);

        restProductCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedProductCategory.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedProductCategory))
            )
            .andExpect(status().isOk());

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProductCategoryToMatchAllProperties(updatedProductCategory);
    }

    @Test
    @Transactional
    void putNonExistingProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, productCategory.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(productCategory))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductCategoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(productCategory))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductCategoryMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(productCategory))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProductCategoryWithPatch() throws Exception {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.saveAndFlush(productCategory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the productCategory using partial update
        ProductCategory partialUpdatedProductCategory = new ProductCategory();
        partialUpdatedProductCategory.setId(productCategory.getId());

        partialUpdatedProductCategory.name(UPDATED_NAME);

        restProductCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProductCategory.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProductCategory))
            )
            .andExpect(status().isOk());

        // Validate the ProductCategory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProductCategoryUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedProductCategory, productCategory),
            getPersistedProductCategory(productCategory)
        );
    }

    @Test
    @Transactional
    void fullUpdateProductCategoryWithPatch() throws Exception {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.saveAndFlush(productCategory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the productCategory using partial update
        ProductCategory partialUpdatedProductCategory = new ProductCategory();
        partialUpdatedProductCategory.setId(productCategory.getId());

        partialUpdatedProductCategory.name(UPDATED_NAME).description(UPDATED_DESCRIPTION);

        restProductCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProductCategory.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProductCategory))
            )
            .andExpect(status().isOk());

        // Validate the ProductCategory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProductCategoryUpdatableFieldsEquals(
            partialUpdatedProductCategory,
            getPersistedProductCategory(partialUpdatedProductCategory)
        );
    }

    @Test
    @Transactional
    void patchNonExistingProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, productCategory.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(productCategory))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(productCategory))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductCategoryMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(productCategory))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProductCategory() throws Exception {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.saveAndFlush(productCategory);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the productCategory
        restProductCategoryMockMvc
            .perform(delete(ENTITY_API_URL_ID, productCategory.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return productCategoryRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ProductCategory getPersistedProductCategory(ProductCategory productCategory) {
        return productCategoryRepository.findById(productCategory.getId()).orElseThrow();
    }

    protected void assertPersistedProductCategoryToMatchAllProperties(ProductCategory expectedProductCategory) {
        assertProductCategoryAllPropertiesEquals(expectedProductCategory, getPersistedProductCategory(expectedProductCategory));
    }

    protected void assertPersistedProductCategoryToMatchUpdatableProperties(ProductCategory expectedProductCategory) {
        assertProductCategoryAllUpdatablePropertiesEquals(expectedProductCategory, getPersistedProductCategory(expectedProductCategory));
    }
}
