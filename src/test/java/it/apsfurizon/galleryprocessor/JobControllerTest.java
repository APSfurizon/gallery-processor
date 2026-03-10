package it.apsfurizon.galleryprocessor;

import it.apsfurizon.galleryprocessor.dto.JobResponse;
import it.apsfurizon.galleryprocessor.entity.Job;
import it.apsfurizon.galleryprocessor.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobRepository jobRepository;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
    }

    @Test
    void submitJob_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": 1, \"name\": \"test-job\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void submitJob_withAuth_returnsCreated() throws Exception {
        mockMvc.perform(post("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": 1, \"name\": \"test-job\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test-job"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getJobStatus_notFound_returns404() throws Exception {
        mockMvc.perform(get("/jobs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(JobResponse.JobStatus.NOT_FOUND.name()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getJobStatus_queued_returnsQueued() throws Exception {
        jobRepository.save(new Job(42L, "my-job", Instant.now()));

        mockMvc.perform(get("/jobs/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(JobResponse.JobStatus.QUEUED.name()))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getJobStatus_done_returnsDone() throws Exception {
        Job job = new Job(7L, "done-job", Instant.now());
        job.setResult("{\"output\":\"ok\"}");
        jobRepository.save(job);

        mockMvc.perform(get("/jobs/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(JobResponse.JobStatus.DONE.name()))
                .andExpect(jsonPath("$.result").value("{\"output\":\"ok\"}"));
    }

    @Test
    void getJobStatus_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/jobs/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void submitJob_duplicateId_returns409() throws Exception {
        mockMvc.perform(post("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": 5, \"name\": \"first\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": 5, \"name\": \"duplicate\"}"))
                .andExpect(status().isConflict());
    }
}
