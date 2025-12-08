package br.mds.inti.controller;

import br.mds.inti.model.dto.CreateProductDTO;
import br.mds.inti.model.dto.EditProductDTO;
import br.mds.inti.model.dto.ProductResponseDTO;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProductController productController;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile();
        profile.setId(UUID.randomUUID());
    }

    @Test
    void createProduct_shouldReturnCreatedResponse() {
        CreateProductDTO dto = new CreateProductDTO("Title", "Desc", BigDecimal.TEN, null);
        ProductResponseDTO expected = buildProductResponse();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(profile);
        when(productService.createProduct(dto, profile.getId())).thenReturn(expected);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            ResponseEntity<ProductResponseDTO> response = productController.createProduct(dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isSameAs(expected);
            verify(productService).createProduct(dto, profile.getId());
        }
    }

    @Test
    void getProducts_shouldReturnPage() {
        Pageable pageable = PageRequest.of(1, 5);
        ProductResponseDTO dto = buildProductResponse();
        Page<ProductResponseDTO> page = new PageImpl<>(List.of(dto));
        when(productService.getPublicProducts(pageable)).thenReturn(page);

        ResponseEntity<Page<ProductResponseDTO>> response = productController.getProducts(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(page);
        verify(productService).getPublicProducts(pageable);
    }

    @Test
    void getProductById_shouldReturnBody() {
        UUID productId = UUID.randomUUID();
        ProductResponseDTO dto = buildProductResponse();
        when(productService.getProductById(productId)).thenReturn(dto);

        ResponseEntity<ProductResponseDTO> response = productController.getProductById(productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
        verify(productService).getProductById(productId);
    }

    @Test
    void updateProduct_shouldUseAuthenticatedProfile() {
        UUID productId = UUID.randomUUID();
        EditProductDTO dto = new EditProductDTO("Updated", null, null, null);
        ProductResponseDTO expected = buildProductResponse();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(profile);
        when(productService.updateProduct(productId, dto, profile.getId())).thenReturn(expected);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            ResponseEntity<ProductResponseDTO> response = productController.updateProduct(productId, dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(expected);
            verify(productService).updateProduct(productId, dto, profile.getId());
        }
    }

    @Test
    void deleteProduct_shouldReturnNoContent() {
        UUID productId = UUID.randomUUID();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(profile);
        doNothing().when(productService).deleteProduct(productId, profile.getId());

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            ResponseEntity<Void> response = productController.deleteProduct(productId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(productService).deleteProduct(productId, profile.getId());
        }
    }

    private ProductResponseDTO buildProductResponse() {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(UUID.randomUUID());
        dto.setProfileId(UUID.randomUUID());
        dto.setProfileUsername("artist.username");
        dto.setProfileName("Artist Name");
        dto.setProfilePictureUrl("https://cdn.example.com/avatar.png");
        dto.setTitle("Product");
        dto.setDescription("Description");
        dto.setPrice(BigDecimal.ONE);
        dto.setImgLink("/images/img.png");
        return dto;
    }
}
