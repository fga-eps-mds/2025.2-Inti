package br.mds.inti.service;

import br.mds.inti.model.dto.EditProductDTO;
import br.mds.inti.model.dto.ProductResponseDTO;
import br.mds.inti.model.dto.ProductSummaryDTO;
import br.mds.inti.model.entity.ArtistProducts;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private BlobService blobService;

    @Mock
    private PostService postService;

    @InjectMocks
    private ProductService productService;

    @Test
    void updateProduct_withNullFields_shouldOnlyUpdateProvidedValues() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Profile profile = new Profile();
        profile.setId(profileId);

        ArtistProducts product = buildProduct(productId, profile);
        product.setTitle("Original title");
        product.setDescription("Original desc");
        product.setPrice(BigDecimal.TEN);
        product.setBlobName("blob-old.png");

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(ArtistProducts.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postService.generateImageUrl("blob-old.png")).thenReturn("/images/blob-old.png");

        EditProductDTO dto = new EditProductDTO(null, "Updated desc", null, null);

        ProductResponseDTO response = productService.updateProduct(productId, dto, profileId);

        assertThat(product.getTitle()).isEqualTo("Original title");
        assertThat(product.getDescription()).isEqualTo("Updated desc");
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(response.getDescription()).isEqualTo("Updated desc");
        verify(blobService, never()).uploadImage(any(UUID.class), any(MultipartFile.class));
    }

    @Test
    void updateProduct_withImageProvided_shouldUploadAndReplaceBlob() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Profile profile = new Profile();
        profile.setId(profileId);

        ArtistProducts product = buildProduct(productId, profile);
        product.setBlobName("blob-old.png");
        product.setTitle("Old");
        product.setPrice(BigDecimal.valueOf(15));

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(ArtistProducts.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postService.generateImageUrl("blob-new.png")).thenReturn("/images/blob-new.png");

        MockMultipartFile newImage = new MockMultipartFile("image", "new.png", "image/png", "bytes".getBytes());
        EditProductDTO dto = new EditProductDTO("New title", null, BigDecimal.valueOf(30), newImage);

        when(blobService.uploadImage(profileId, newImage)).thenReturn("blob-new.png");

        ProductResponseDTO response = productService.updateProduct(productId, dto, profileId);

        assertThat(product.getTitle()).isEqualTo("New title");
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(30));
        assertThat(product.getBlobName()).isEqualTo("blob-new.png");
        assertThat(response.getImgLink()).isEqualTo("/images/blob-new.png");
        verify(blobService).uploadImage(profileId, newImage);
    }

    @Test
    void updateProduct_whenNegativePrice_shouldThrowBadRequest() {
        UUID productId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Profile profile = new Profile();
        profile.setId(profileId);

        ArtistProducts product = buildProduct(productId, profile);
        product.setPrice(BigDecimal.ONE);

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));

        EditProductDTO dto = new EditProductDTO(null, null, BigDecimal.valueOf(-10), null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productService.updateProduct(productId, dto, profileId));

        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        verify(productRepository, never()).save(any());
    }

    @Test
    void getPublicProducts_shouldMapEntitiesToDtos() {
        UUID productId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Profile profile = new Profile();
        profile.setId(profileId);

        ArtistProducts product = buildProduct(productId, profile);
        product.setBlobName("blob.png");

        Pageable pageable = PageRequest.of(0, 5);
        when(productRepository.findByDeletedAtIsNull(pageable)).thenReturn(new PageImpl<>(List.of(product)));
        when(postService.generateImageUrl("blob.png")).thenReturn("/images/blob.png");

        Page<ProductResponseDTO> result = productService.getPublicProducts(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        ProductResponseDTO dto = result.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(productId);
        assertThat(dto.getProfileId()).isEqualTo(profileId);
        assertThat(dto.getImgLink()).isEqualTo("/images/blob.png");
    }

    private ArtistProducts buildProduct(UUID productId, Profile profile) {
        ArtistProducts product = new ArtistProducts();
        product.setId(productId);
        product.setProfile(profile);
        product.setCreatedAt(Instant.now());
        product.setDescription("desc");
        return product;
    }

        @Test
    void getProfileProducts_withValidProducts_shouldReturnPaginatedList() {
        UUID profileId = UUID.randomUUID();
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();

        Profile profile = new Profile();
        profile.setId(profileId);

        ArtistProducts product1 = buildProduct(productId1, profile);
        product1.setTitle("Product 1");
        product1.setDescription("Description of product 1 with detailed information about the item.");
        product1.setPrice(BigDecimal.valueOf(50.00));
        product1.setBlobName("blob1.png");

        ArtistProducts product2 = buildProduct(productId2, profile);
        product2.setTitle("Product 2");
        product2.setDescription("Description of product 2 with more details.");
        product2.setPrice(BigDecimal.valueOf(75.50));
        product2.setBlobName("blob2.png");

        Pageable pageable = PageRequest.of(0, 10);
        Page<ArtistProducts> productPage = new PageImpl<>(List.of(product1, product2), pageable, 2);

        when(profileService.getProfileById(profileId)).thenReturn(profile);
        when(productRepository.findByProfileIdAndDeletedAtIsNull(profileId, pageable))
                .thenReturn(productPage);
        when(postService.generateImageUrl("blob1.png")).thenReturn("/images/blob1.png");
        when(postService.generateImageUrl("blob2.png")).thenReturn("/images/blob2.png");

        Page<ProductSummaryDTO> result = productService.getProfileProducts(profileId, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        
        ProductSummaryDTO dto1 = result.getContent().get(0);
        assertThat(dto1.id()).isEqualTo(productId1);
        assertThat(dto1.title()).isEqualTo("Product 1");
        assertThat(dto1.price()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(dto1.imgLink()).isEqualTo("/images/blob1.png");
        assertThat(dto1.shortDescription()).isEqualTo("Description of product 1 with detailed information about the item.");

        ProductSummaryDTO dto2 = result.getContent().get(1);
        assertThat(dto2.id()).isEqualTo(productId2);
        assertThat(dto2.title()).isEqualTo("Product 2");
        assertThat(dto2.price()).isEqualByComparingTo(BigDecimal.valueOf(75.50));
        assertThat(dto2.imgLink()).isEqualTo("/images/blob2.png");

        verify(profileService).getProfileById(profileId);
        verify(productRepository).findByProfileIdAndDeletedAtIsNull(profileId, pageable);
    }

    @Test
    void getProfileProducts_withNoProducts_shouldReturnEmptyPage() {
        UUID profileId = UUID.randomUUID();

        Profile profile = new Profile();
        profile.setId(profileId);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ArtistProducts> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(profileService.getProfileById(profileId)).thenReturn(profile);
        when(productRepository.findByProfileIdAndDeletedAtIsNull(profileId, pageable))
                .thenReturn(emptyPage);

        Page<ProductSummaryDTO> result = productService.getProfileProducts(profileId, pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.hasContent()).isFalse();

        verify(profileService).getProfileById(profileId);
        verify(productRepository).findByProfileIdAndDeletedAtIsNull(profileId, pageable);
    }

    @Test
    void getProfileProducts_whenProfileNotFound_shouldThrowNotFoundException() {
        UUID profileId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(profileService.getProfileById(profileId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil não encontrado."));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productService.getProfileProducts(profileId, pageable));

        assertThat(exception.getStatusCode().value()).isEqualTo(404);
        verify(profileService).getProfileById(profileId);
        verify(productRepository, never()).findByProfileIdAndDeletedAtIsNull(any(), any());
    }

    @Test
    void getProfileProducts_withLongDescription_shouldTruncateShortDescription() {
        UUID profileId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Profile profile = new Profile();
        profile.setId(profileId);

        ArtistProducts product = buildProduct(productId, profile);
        String longDescription = "A".repeat(150); // 150 caracteres
        product.setDescription(longDescription);
        product.setTitle("Product with Long Description");
        product.setPrice(BigDecimal.valueOf(100.00));
        product.setBlobName("blob.png");

        Pageable pageable = PageRequest.of(0, 10);
        Page<ArtistProducts> productPage = new PageImpl<>(List.of(product), pageable, 1);

        when(profileService.getProfileById(profileId)).thenReturn(profile);
        when(productRepository.findByProfileIdAndDeletedAtIsNull(profileId, pageable))
                .thenReturn(productPage);
        when(postService.generateImageUrl("blob.png")).thenReturn("/images/blob.png");

        Page<ProductSummaryDTO> result = productService.getProfileProducts(profileId, pageable);

        ProductSummaryDTO dto = result.getContent().get(0);
        assertThat(dto.shortDescription()).hasSize(103); // 100 caracteres + "..."
        assertThat(dto.shortDescription()).endsWith("...");
        assertThat(dto.shortDescription()).startsWith("A");
    }
}
