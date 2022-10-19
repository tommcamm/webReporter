package edu.tomm.webreporter.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.tomm.webreporter.IntegrationTest;
import edu.tomm.webreporter.domain.Report;
import edu.tomm.webreporter.domain.User;
import edu.tomm.webreporter.repository.ReportRepository;
import edu.tomm.webreporter.service.ReportService;
import edu.tomm.webreporter.service.criteria.ReportCriteria;
import edu.tomm.webreporter.service.dto.ReportDTO;
import edu.tomm.webreporter.service.mapper.ReportMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ReportResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ReportResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_IP_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_IP_ADDRESS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/reports";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ReportRepository reportRepository;

    @Mock
    private ReportRepository reportRepositoryMock;

    @Autowired
    private ReportMapper reportMapper;

    @Mock
    private ReportService reportServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReportMockMvc;

    private Report report;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Report createEntity(EntityManager em) {
        Report report = new Report().title(DEFAULT_TITLE).description(DEFAULT_DESCRIPTION).ipAddress(DEFAULT_IP_ADDRESS);
        // Add required entity
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        report.setUser(user);
        return report;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Report createUpdatedEntity(EntityManager em) {
        Report report = new Report().title(UPDATED_TITLE).description(UPDATED_DESCRIPTION).ipAddress(UPDATED_IP_ADDRESS);
        // Add required entity
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        report.setUser(user);
        return report;
    }

    @BeforeEach
    public void initTest() {
        report = createEntity(em);
    }

    @Test
    @Transactional
    void createReport() throws Exception {
        int databaseSizeBeforeCreate = reportRepository.findAll().size();
        // Create the Report
        ReportDTO reportDTO = reportMapper.toDto(report);
        restReportMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reportDTO)))
            .andExpect(status().isCreated());

        // Validate the Report in the database
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeCreate + 1);
        Report testReport = reportList.get(reportList.size() - 1);
        assertThat(testReport.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testReport.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testReport.getIpAddress()).isEqualTo(DEFAULT_IP_ADDRESS);
    }

    @Test
    @Transactional
    void createReportWithExistingId() throws Exception {
        // Create the Report with an existing ID
        report.setId(1L);
        ReportDTO reportDTO = reportMapper.toDto(report);

        int databaseSizeBeforeCreate = reportRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restReportMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reportDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Report in the database
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = reportRepository.findAll().size();
        // set the field null
        report.setTitle(null);

        // Create the Report, which fails.
        ReportDTO reportDTO = reportMapper.toDto(report);

        restReportMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reportDTO)))
            .andExpect(status().isBadRequest());

        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIpAddressIsRequired() throws Exception {
        int databaseSizeBeforeTest = reportRepository.findAll().size();
        // set the field null
        report.setIpAddress(null);

        // Create the Report, which fails.
        ReportDTO reportDTO = reportMapper.toDto(report);

        restReportMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reportDTO)))
            .andExpect(status().isBadRequest());

        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllReports() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList
        restReportMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(report.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].ipAddress").value(hasItem(DEFAULT_IP_ADDRESS)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReportsWithEagerRelationshipsIsEnabled() throws Exception {
        when(reportServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReportMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(reportServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReportsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(reportServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReportMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(reportRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getReport() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get the report
        restReportMockMvc
            .perform(get(ENTITY_API_URL_ID, report.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(report.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.ipAddress").value(DEFAULT_IP_ADDRESS));
    }

    @Test
    @Transactional
    void getReportsByIdFiltering() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        Long id = report.getId();

        defaultReportShouldBeFound("id.equals=" + id);
        defaultReportShouldNotBeFound("id.notEquals=" + id);

        defaultReportShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultReportShouldNotBeFound("id.greaterThan=" + id);

        defaultReportShouldBeFound("id.lessThanOrEqual=" + id);
        defaultReportShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllReportsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where title equals to DEFAULT_TITLE
        defaultReportShouldBeFound("title.equals=" + DEFAULT_TITLE);

        // Get all the reportList where title equals to UPDATED_TITLE
        defaultReportShouldNotBeFound("title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllReportsByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where title in DEFAULT_TITLE or UPDATED_TITLE
        defaultReportShouldBeFound("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE);

        // Get all the reportList where title equals to UPDATED_TITLE
        defaultReportShouldNotBeFound("title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllReportsByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where title is not null
        defaultReportShouldBeFound("title.specified=true");

        // Get all the reportList where title is null
        defaultReportShouldNotBeFound("title.specified=false");
    }

    @Test
    @Transactional
    void getAllReportsByTitleContainsSomething() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where title contains DEFAULT_TITLE
        defaultReportShouldBeFound("title.contains=" + DEFAULT_TITLE);

        // Get all the reportList where title contains UPDATED_TITLE
        defaultReportShouldNotBeFound("title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllReportsByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where title does not contain DEFAULT_TITLE
        defaultReportShouldNotBeFound("title.doesNotContain=" + DEFAULT_TITLE);

        // Get all the reportList where title does not contain UPDATED_TITLE
        defaultReportShouldBeFound("title.doesNotContain=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllReportsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where description equals to DEFAULT_DESCRIPTION
        defaultReportShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the reportList where description equals to UPDATED_DESCRIPTION
        defaultReportShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllReportsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultReportShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the reportList where description equals to UPDATED_DESCRIPTION
        defaultReportShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllReportsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where description is not null
        defaultReportShouldBeFound("description.specified=true");

        // Get all the reportList where description is null
        defaultReportShouldNotBeFound("description.specified=false");
    }

    @Test
    @Transactional
    void getAllReportsByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where description contains DEFAULT_DESCRIPTION
        defaultReportShouldBeFound("description.contains=" + DEFAULT_DESCRIPTION);

        // Get all the reportList where description contains UPDATED_DESCRIPTION
        defaultReportShouldNotBeFound("description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllReportsByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where description does not contain DEFAULT_DESCRIPTION
        defaultReportShouldNotBeFound("description.doesNotContain=" + DEFAULT_DESCRIPTION);

        // Get all the reportList where description does not contain UPDATED_DESCRIPTION
        defaultReportShouldBeFound("description.doesNotContain=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllReportsByIpAddressIsEqualToSomething() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where ipAddress equals to DEFAULT_IP_ADDRESS
        defaultReportShouldBeFound("ipAddress.equals=" + DEFAULT_IP_ADDRESS);

        // Get all the reportList where ipAddress equals to UPDATED_IP_ADDRESS
        defaultReportShouldNotBeFound("ipAddress.equals=" + UPDATED_IP_ADDRESS);
    }

    @Test
    @Transactional
    void getAllReportsByIpAddressIsInShouldWork() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where ipAddress in DEFAULT_IP_ADDRESS or UPDATED_IP_ADDRESS
        defaultReportShouldBeFound("ipAddress.in=" + DEFAULT_IP_ADDRESS + "," + UPDATED_IP_ADDRESS);

        // Get all the reportList where ipAddress equals to UPDATED_IP_ADDRESS
        defaultReportShouldNotBeFound("ipAddress.in=" + UPDATED_IP_ADDRESS);
    }

    @Test
    @Transactional
    void getAllReportsByIpAddressIsNullOrNotNull() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where ipAddress is not null
        defaultReportShouldBeFound("ipAddress.specified=true");

        // Get all the reportList where ipAddress is null
        defaultReportShouldNotBeFound("ipAddress.specified=false");
    }

    @Test
    @Transactional
    void getAllReportsByIpAddressContainsSomething() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where ipAddress contains DEFAULT_IP_ADDRESS
        defaultReportShouldBeFound("ipAddress.contains=" + DEFAULT_IP_ADDRESS);

        // Get all the reportList where ipAddress contains UPDATED_IP_ADDRESS
        defaultReportShouldNotBeFound("ipAddress.contains=" + UPDATED_IP_ADDRESS);
    }

    @Test
    @Transactional
    void getAllReportsByIpAddressNotContainsSomething() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reportList where ipAddress does not contain DEFAULT_IP_ADDRESS
        defaultReportShouldNotBeFound("ipAddress.doesNotContain=" + DEFAULT_IP_ADDRESS);

        // Get all the reportList where ipAddress does not contain UPDATED_IP_ADDRESS
        defaultReportShouldBeFound("ipAddress.doesNotContain=" + UPDATED_IP_ADDRESS);
    }

    @Test
    @Transactional
    void getAllReportsByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            reportRepository.saveAndFlush(report);
            user = UserResourceIT.createEntity(em);
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        report.setUser(user);
        reportRepository.saveAndFlush(report);
        Long userId = user.getId();

        // Get all the reportList where user equals to userId
        defaultReportShouldBeFound("userId.equals=" + userId);

        // Get all the reportList where user equals to (userId + 1)
        defaultReportShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultReportShouldBeFound(String filter) throws Exception {
        restReportMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(report.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].ipAddress").value(hasItem(DEFAULT_IP_ADDRESS)));

        // Check, that the count call also returns 1
        restReportMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultReportShouldNotBeFound(String filter) throws Exception {
        restReportMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restReportMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingReport() throws Exception {
        // Get the report
        restReportMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingReport() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        int databaseSizeBeforeUpdate = reportRepository.findAll().size();

        // Update the report
        Report updatedReport = reportRepository.findById(report.getId()).get();
        // Disconnect from session so that the updates on updatedReport are not directly saved in db
        em.detach(updatedReport);
        updatedReport.title(UPDATED_TITLE).description(UPDATED_DESCRIPTION).ipAddress(UPDATED_IP_ADDRESS);
        ReportDTO reportDTO = reportMapper.toDto(updatedReport);

        restReportMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reportDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reportDTO))
            )
            .andExpect(status().isOk());

        // Validate the Report in the database
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeUpdate);
        Report testReport = reportList.get(reportList.size() - 1);
        assertThat(testReport.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testReport.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testReport.getIpAddress()).isEqualTo(UPDATED_IP_ADDRESS);
    }

    @Test
    @Transactional
    void putNonExistingReport() throws Exception {
        int databaseSizeBeforeUpdate = reportRepository.findAll().size();
        report.setId(count.incrementAndGet());

        // Create the Report
        ReportDTO reportDTO = reportMapper.toDto(report);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReportMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reportDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reportDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Report in the database
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchReport() throws Exception {
        int databaseSizeBeforeUpdate = reportRepository.findAll().size();
        report.setId(count.incrementAndGet());

        // Create the Report
        ReportDTO reportDTO = reportMapper.toDto(report);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(reportDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Report in the database
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReport() throws Exception {
        int databaseSizeBeforeUpdate = reportRepository.findAll().size();
        report.setId(count.incrementAndGet());

        // Create the Report
        ReportDTO reportDTO = reportMapper.toDto(report);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(reportDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Report in the database
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateReportWithPatch() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        int databaseSizeBeforeUpdate = reportRepository.findAll().size();

        // Update the report using partial update
        Report partialUpdatedReport = new Report();
        partialUpdatedReport.setId(report.getId());

        partialUpdatedReport.description(UPDATED_DESCRIPTION).ipAddress(UPDATED_IP_ADDRESS);

        restReportMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReport.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedReport))
            )
            .andExpect(status().isOk());

        // Validate the Report in the database
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeUpdate);
        Report testReport = reportList.get(reportList.size() - 1);
        assertThat(testReport.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testReport.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testReport.getIpAddress()).isEqualTo(UPDATED_IP_ADDRESS);
    }

    @Test
    @Transactional
    void fullUpdateReportWithPatch() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        int databaseSizeBeforeUpdate = reportRepository.findAll().size();

        // Update the report using partial update
        Report partialUpdatedReport = new Report();
        partialUpdatedReport.setId(report.getId());

        partialUpdatedReport.title(UPDATED_TITLE).description(UPDATED_DESCRIPTION).ipAddress(UPDATED_IP_ADDRESS);

        restReportMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReport.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedReport))
            )
            .andExpect(status().isOk());

        // Validate the Report in the database
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeUpdate);
        Report testReport = reportList.get(reportList.size() - 1);
        assertThat(testReport.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testReport.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testReport.getIpAddress()).isEqualTo(UPDATED_IP_ADDRESS);
    }

    @Test
    @Transactional
    void patchNonExistingReport() throws Exception {
        int databaseSizeBeforeUpdate = reportRepository.findAll().size();
        report.setId(count.incrementAndGet());

        // Create the Report
        ReportDTO reportDTO = reportMapper.toDto(report);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReportMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, reportDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(reportDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Report in the database
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReport() throws Exception {
        int databaseSizeBeforeUpdate = reportRepository.findAll().size();
        report.setId(count.incrementAndGet());

        // Create the Report
        ReportDTO reportDTO = reportMapper.toDto(report);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(reportDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Report in the database
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReport() throws Exception {
        int databaseSizeBeforeUpdate = reportRepository.findAll().size();
        report.setId(count.incrementAndGet());

        // Create the Report
        ReportDTO reportDTO = reportMapper.toDto(report);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(reportDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Report in the database
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteReport() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        int databaseSizeBeforeDelete = reportRepository.findAll().size();

        // Delete the report
        restReportMockMvc
            .perform(delete(ENTITY_API_URL_ID, report.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Report> reportList = reportRepository.findAll();
        assertThat(reportList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
