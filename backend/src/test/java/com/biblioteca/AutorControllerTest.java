package com.biblioteca;

import com.biblioteca.controller.AutorController;
import com.biblioteca.model.Autor;
import com.biblioteca.repository.AutorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TESTES UNITÁRIOS - Autores (DEV 1)
 * @WebMvcTest testa só o controller; o repository é mockado com @MockBean.
 */
@WebMvcTest(AutorController.class)
class AutorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AutorRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private Autor novoAutor(Long id, String nome) {
        Autor autor = new Autor();
        autor.setId(id);
        autor.setNome(nome);
        autor.setNacionalidade("Brasileira");
        autor.setDataNascimento(LocalDate.of(1839, 6, 21));
        return autor;
    }

    @Test
    void deveCriarAutorComSucesso() throws Exception {
        Autor autor = novoAutor(1L, "Machado de Assis");
        when(repository.save(any(Autor.class))).thenReturn(autor);

        mockMvc.perform(post("/api/autores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(autor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Machado de Assis"))
                .andExpect(jsonPath("$.dataNascimento").value("1839-06-21"));
    }

    @Test
    void deveRetornar400QuandoNomeVazio() throws Exception {
        mockMvc.perform(post("/api/autores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarAutoresOrdenadosPorNome() throws Exception {
        when(repository.findAllByOrderByNomeAsc()).thenReturn(Arrays.asList(
                novoAutor(2L, "Clarice Lispector"),
                novoAutor(1L, "Machado de Assis")));

        mockMvc.perform(get("/api/autores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Clarice Lispector"))
                .andExpect(jsonPath("$[1].nome").value("Machado de Assis"));
    }

    @Test
    void deveBuscarAutorPorId() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(novoAutor(1L, "Machado de Assis")));

        mockMvc.perform(get("/api/autores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Machado de Assis"));
    }

    @Test
    void deveRetornar404ParaAutorInexistente() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/autores/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarAutor() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(novoAutor(1L, "Machado")));
        when(repository.save(any(Autor.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/autores/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Machado de Assis\", \"nacionalidade\": \"Brasileira\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Machado de Assis"));
    }

    @Test
    void deveRetornar404AoAtualizarAutorInexistente() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/autores/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Fulano\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveDeletarAutorComSucesso() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(novoAutor(1L, "Machado de Assis")));

        mockMvc.perform(delete("/api/autores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Autor removido com sucesso"));
    }

    @Test
    void deveRetornar404AoDeletarAutorInexistente() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/autores/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409AoDeletarAutorComLivros() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(novoAutor(1L, "Machado de Assis")));
        doThrow(new DataIntegrityViolationException("FK")).when(repository).flush();

        mockMvc.perform(delete("/api/autores/1"))
                .andExpect(status().isConflict());
    }
}
