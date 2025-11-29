package br.mds.inti.controller;

import br.mds.inti.model.dto.ProductSummaryDTO;
import br.mds.inti.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void getProductsByProfile_shouldReturn200AndPageOfProducts() throws Exception {
        UUID profileId = UUID.randomUUID();

        ProductSummaryDTO dto1 = new ProductSummaryDTO(
                UUID.randomUUID(),
                "Produto 1",
                "img1.png",
                10.0,
                "Desc 1"
        );

        ProductSummaryDTO dto2 = new ProductSummaryDTO(
                UUID.randomUUID(),
                "Produto 2",
                null,
                20.0,
                "Desc 2"
        );

        Page<ProductSummaryDTO> page =
                new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 10), 2);

        when(productService.getProductsByProfile(eq(profileId), isNull(), eq(0), eq(10)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/profiles/{profileId}/products", profileId)
                                .param("page", "0")
                                .param("size", "10")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title", is("Produto 1")))
                .andExpect(jsonPath("$.content[0].imgLink", is("img1.png")))
                .andExpect(jsonPath("$.content[0].price", is(10.0)))
                .andExpect(jsonPath("$.content[0].description", is("Desc 1")))
                .andExpect(jsonPath("$.content[1].title", is("Produto 2")))
                .andExpect(jsonPath("$.content[1].imgLink", nullValue()))
                .andExpect(jsonPath("$.content[1].price", is(20.0)))
                .andExpect(jsonPath("$.content[1].description", is("Desc 2")))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void getProductsByProfile_shouldReturn404WhenProfileNotFound() throws Exception {
        UUID profileId = UUID.randomUUID();

        when(productService.getProductsByProfile(eq(profileId), isNull(), anyInt(), anyInt()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Profile not found"));

        mockMvc.perform(
                        get("/profiles/{profileId}/products", profileId)
                                .param("page", "0")
                                .param("size", "10")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }
}
