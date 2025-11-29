package br.mds.inti.service;

import br.mds.inti.model.dto.ProductSummaryDTO;
import br.mds.inti.model.entity.ArtistProducts;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ArtistProductsRepository;
import br.mds.inti.repositories.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ArtistProductsRepository artistProductsRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProductService productService;

    private Profile profile;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        profileId = UUID.randomUUID();
        profile = new Profile(); // assumindo que tem construtor sem argumentos
    }

    @Test
    void getProductsByProfile_shouldReturnMappedPage_whenProfileExists() {
        // given
        int page = 0;
        int size = 10;
        UUID viewerProfileId = UUID.randomUUID(); // ainda não usado no service

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));

        ArtistProducts product1 = new ArtistProducts();
        product1.setId(UUID.randomUUID());
        product1.setProfile(profile);
        product1.setTitle("Produto 1");
        product1.setBlobName("blob-1.png");
        product1.setPrice(new BigDecimal("19.90"));
        product1.setDescription("Descrição 1");
        product1.setCreatedAt(Instant.now());

        ArtistProducts product2 = new ArtistProducts();
        product2.setId(UUID.randomUUID());
        product2.setProfile(profile);
        product2.setTitle("Produto 2");
        product2.setBlobName(null); // sem imagem
        product2.setPrice(null);    // sem preço
        product2.setDescription("Descrição 2");
        product2.setCreatedAt(Instant.now());

        Page<ArtistProducts> productsPage =
                new PageImpl<>(List.of(product1, product2), PageRequest.of(page, size), 2);

        when(artistProductsRepository
                .findByProfileAndDeletedAtIsNullOrderByCreatedAtDesc(eq(profile), any(Pageable.class)))
                .thenReturn(productsPage);

        // when
        Page<ProductSummaryDTO> result =
                productService.getProductsByProfile(profileId, viewerProfileId, page, size);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        ProductSummaryDTO dto1 = result.getContent().get(0);
        assertEquals(product1.getId(), dto1.id());
        assertEquals("Produto 1", dto1.title());
        assertEquals("blob-1.png", dto1.imgLink());
        assertEquals(19.90, dto1.price());
        assertEquals("Descrição 1", dto1.description());

        ProductSummaryDTO dto2 = result.getContent().get(1);
        assertEquals(product2.getId(), dto2.id());
        assertEquals("Produto 2", dto2.title());
        assertNull(dto2.imgLink());
        assertNull(dto2.price());
        assertEquals("Descrição 2", dto2.description());

        // garante que o pageable foi montado certo
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(artistProductsRepository)
                .findByProfileAndDeletedAtIsNullOrderByCreatedAtDesc(eq(profile), pageableCaptor.capture());

        Pageable usedPageable = pageableCaptor.getValue();
        assertEquals(page, usedPageable.getPageNumber());
        assertEquals(size, usedPageable.getPageSize());
    }

    @Test
    void getProductsByProfile_shouldThrowNotFound_whenProfileDoesNotExist() {
        when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> productService.getProductsByProfile(profileId, UUID.randomUUID(), 0, 10)
        );

        assertEquals("404 NOT_FOUND \"Profile not found\"", ex.getMessage());
        verifyNoInteractions(artistProductsRepository);
    }

    @Test
    void getProductsByProfileUsername_shouldReturnMappedPage_whenProfileExists() {
        // given
        String username = "artist_user";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.of(profile));

        ArtistProducts product = new ArtistProducts();
        product.setId(UUID.randomUUID());
        product.setProfile(profile);
        product.setTitle("Produto Username");
        product.setBlobName("blob-user.png");
        product.setPrice(new BigDecimal("50.00"));
        product.setDescription("Desc username");
        product.setCreatedAt(Instant.now());

        Page<ArtistProducts> page =
                new PageImpl<>(List.of(product), PageRequest.of(0, 5), 1);

        when(artistProductsRepository
                .findByProfileAndDeletedAtIsNullOrderByCreatedAtDesc(eq(profile), any(Pageable.class)))
                .thenReturn(page);

        // when
        Page<ProductSummaryDTO> result =
                productService.getProductsByProfileUsername(username, 0, 5);

        // then
        assertEquals(1, result.getTotalElements());
        ProductSummaryDTO dto = result.getContent().get(0);

        assertEquals(product.getId(), dto.id());
        assertEquals("Produto Username", dto.title());
        assertEquals("blob-user.png", dto.imgLink());
        assertEquals(50.00, dto.price());
        assertEquals("Desc username", dto.description());
    }

    @Test
    void getProductsByProfileUsername_shouldThrowNotFound_whenProfileDoesNotExist() {
        when(profileRepository.findByUsername("X")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> productService.getProductsByProfileUsername("X", 0, 10)
        );

        assertEquals("404 NOT_FOUND \"Profile not found\"", ex.getMessage());
        verifyNoInteractions(artistProductsRepository);
    }
}
