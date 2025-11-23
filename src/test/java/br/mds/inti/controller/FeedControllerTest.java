package br.mds.inti.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

    @InjectMocks
    private FeedController feedController;

    @Test
    void getOrganizationDashboard_ShouldReturnWelcomeMessage() {
        // Act
        ResponseEntity<String> response = feedController.getOrganizationDashboard();

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals("Bem-vindo à área exclusiva de organizações!", response.getBody());
    }

    @Test
    void getOrganizationDashboard_ShouldReturnStringType() {
        // Act
        ResponseEntity<String> response = feedController.getOrganizationDashboard();

        // Assert
        assertNotNull(response.getBody());
        assertInstanceOf(String.class, response.getBody());
    }

    @Test
    void getOrganizationDashboard_ShouldNotReturnEmptyString() {
        // Act
        ResponseEntity<String> response = feedController.getOrganizationDashboard();

        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }
}
