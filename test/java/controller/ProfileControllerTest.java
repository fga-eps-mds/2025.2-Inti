package br.mds.inti.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.service.ProfileService;
import br.mds.inti.service.exceptions.ProfileNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProfileControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ProfileService profileService;

        private ProfileResponse testProfileResponse;

        @BeforeEach
        void setUp() {
                testProfileResponse = new ProfileResponse(
                                "Test User",
                                "testuser",
                                "http://example.com/pic.jpg",
                                "Test bio",
                                10,
                                20,
                                new ArrayList<>());
        }

        @Test
        @WithMockUser
        void getMe_ReturnsProfileResponse() throws Exception {
                when(profileService.getProfile(anyInt(), anyInt())).thenReturn(testProfileResponse);

                mockMvc.perform(get("/profiles/me")
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value(testProfileResponse.name()))
                                .andExpect(jsonPath("$.username").value(testProfileResponse.username()))
                                .andExpect(jsonPath("$.profile_picture_url")
                                                .value(testProfileResponse.profile_picture_url()))
                                .andExpect(jsonPath("$.bio").value(testProfileResponse.bio()))
                                .andExpect(jsonPath("$.followersCount").value(testProfileResponse.followersCount()))
                                .andExpect(jsonPath("$.followingCount").value(testProfileResponse.followingCount()))
                                .andExpect(jsonPath("$.posts").exists());

                verify(profileService).getProfile(0, 10);
        }

        @Test
        @WithMockUser
        void getPublicProfile_WhenProfileExists_ReturnsProfileResponse() throws Exception {
                when(profileService.getProfileByUsername(anyString(), anyInt(), anyInt()))
                                .thenReturn(testProfileResponse);

                mockMvc.perform(get("/profiles/{username}", "testuser")
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value(testProfileResponse.name()))
                                .andExpect(jsonPath("$.username").value(testProfileResponse.username()))
                                .andExpect(jsonPath("$.profile_picture_url")
                                                .value(testProfileResponse.profile_picture_url()))
                                .andExpect(jsonPath("$.bio").value(testProfileResponse.bio()))
                                .andExpect(jsonPath("$.followersCount").value(testProfileResponse.followersCount()))
                                .andExpect(jsonPath("$.followingCount").value(testProfileResponse.followingCount()))
                                .andExpect(jsonPath("$.posts").exists());

                verify(profileService).getProfileByUsername("testuser", 0, 10);
        }

        @Test
        @WithMockUser
        void getPublicProfile_WhenProfileNotFound_ReturnsNotFound() throws Exception {
                String username = "nonexistent";
                when(profileService.getProfileByUsername(eq(username), anyInt(), anyInt()))
                                .thenThrow(new ProfileNotFoundException(username));

                mockMvc.perform(get("/profiles/{username}", username)
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());

                verify(profileService).getProfileByUsername(username, 0, 10);
        }

        @Test
        @WithMockUser
        void getString_ReturnsTestString() throws Exception {
                mockMvc.perform(get("/profiles/string/teste/user")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().string("teste"));
        }

        @Test
        @WithMockUser(roles = "ORGANIZATION")
        void getStringOrg_WithOrganizationRole_ReturnsTestString() throws Exception {
                mockMvc.perform(get("/profiles/string/teste/organization")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().string("teste"));
        }

        @Test
        @WithMockUser(roles = "USER")
        void getStringOrg_WithoutOrganizationRole_ReturnsForbidden() throws Exception {
                mockMvc.perform(get("/profiles/string/teste/organization")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden());
        }
}