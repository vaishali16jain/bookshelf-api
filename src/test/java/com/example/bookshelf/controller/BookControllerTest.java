package com.example.bookshelf.controller;

import com.example.bookshelf.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    void createBookReturns201() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", "Dune", "author", "Frank Herbert"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Dune"))
                .andExpect(jsonPath("$.author").value("Frank Herbert"));
    }

    @Test
    void createDuplicateBookReturns400() throws Exception {
        mockMvc.perform(post("/books")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("title", "Dune", "author", "Frank Herbert"))));

        mockMvc.perform(post("/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", "dune", "author", "frank herbert"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("record already exist"));
    }

    @Test
    void createInvalidBookReturns400() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", "", "author", "Frank Herbert"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("please check the request payload"));
    }

    @Test
    void createWithMalformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType("application/json")
                        .content("{ not valid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("please check the request payload"));
    }

    @Test
    void filterBooksByTitleAndAuthor() throws Exception {
        mockMvc.perform(post("/books").contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("title", "Dune", "author", "Frank Herbert"))));
        mockMvc.perform(post("/books").contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("title", "1984", "author", "George Orwell"))));

        mockMvc.perform(get("/books").param("title", "dun"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Dune"));
    }

    @Test
    void filterBooksWithNoMatchReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/books").param("title", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updateBookAuthorReturns200() throws Exception {
        String response = mockMvc.perform(post("/books").contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", "Dune", "author", "Frank Herbert"))))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(put("/books/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("author", "New Author"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("New Author"))
                .andExpect(jsonPath("$.title").value("Dune"));
    }

    @Test
    void updateMissingBookReturns404() throws Exception {
        mockMvc.perform(put("/books/{id}", "missing-id")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("author", "New Author"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("no book present"));
    }

    @Test
    void deleteBookReturns204() throws Exception {
        String response = mockMvc.perform(post("/books").contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", "Dune", "author", "Frank Herbert"))))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/books/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMissingBookReturns404() throws Exception {
        mockMvc.perform(delete("/books/{id}", "missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("no book present"));
    }
}
