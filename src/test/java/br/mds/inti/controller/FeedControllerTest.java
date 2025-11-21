package br.mds.inti.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

    @InjectMocks
    private FeedController feedController;

    @Test
    void getOrganizationDashboard_ShouldReturnWelcomeMessage() {
        // Act
        String result = feedController.getOrganizationDashboard();

        // Assert
        assertNotNull(result);
        assertEquals("Bem-vindo à área exclusiva de organizações!", result);
    }

    @Test
    void getOrganizationDashboard_ShouldReturnStringType() {
        // Act
        String result = feedController.getOrganizationDashboard();

        // Assert
        assertInstanceOf(String.class, result);
    }

    @Test
    void getOrganizationDashboard_ShouldNotReturnEmptyString() {
        // Act
        String result = feedController.getOrganizationDashboard();

        // Assert
        assertFalse(result.isEmpty());
    }
}
