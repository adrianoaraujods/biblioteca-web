package com.biblioteca;

import com.biblioteca.controller.LivroController;
import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import com.biblioteca.repository.AutorRepository;
import com.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TESTES UNITÁRIOS - Livros (DEV 2)
 * @WebMvcTest testa só o controller; os repositories são mockados com @MockBean.
 */
@WebMvcTest(LivroController.class)
class LivroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LivroRepository repository;

    @MockBean
    private AutorRepository autorRepository;

    private static final String LIVRO_JSON = """
        { "titulo": "Dom Casmurro", "isbn": "978-8535910667", "anoPublicacao": 1899,
          "genero": "Romance", "autor": { "id": 1 } }
        """;

    private Autor machado() {
        Autor autor = new Autor();
        autor.setId(1L);
        autor.setNome("Machado de Assis");
        return autor;
    }

    private Livro novoLivro(Long id, String titulo) {
        Livro livro = new Livro();
        livro.setId(id);
        livro.setTitulo(titulo);
        livro.setAutor(machado());
        return livro;
    }

    @Test
    void deveCriarLivroComSucesso() throws Exception {
        when(autorRepository.findById(1L)).thenReturn(Optional.of(machado()));
        when(repository.save(any(Livro.class))).thenAnswer(inv -> {
            Livro l = inv.getArgument(0);
            l.setId(10L);
            return l;
        });

        mockMvc.perform(post("/api/livros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LIVRO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.anoPublicacao").value(1899))
                .andExpect(jsonPath("$.autor.nome").value("Machado de Assis"));
    }

    @Test
    void deveRetornar400QuandoLivroSemAutor() throws Exception {
        mockMvc.perform(post("/api/livros")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\": \"Sem autor\"}"))
                .andExpect(status().isBadRequest());
        verify(repository, never()).save(any());
    }

    @Test
    void deveRetornar400QuandoAutorInexistente() throws Exception {
        when(autorRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/livros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LIVRO_JSON))
                .andExpect(status().isBadRequest());
        verify(repository, never()).save(any());
    }

    @Test
    void deveListarLivrosOrdenadosPorTitulo() throws Exception {
        when(repository.findAllByOrderByTituloAsc()).thenReturn(Arrays.asList(
                novoLivro(2L, "Dom Casmurro"),
                novoLivro(1L, "Memórias Póstumas de Brás Cubas")));

        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$[1].titulo").value("Memórias Póstumas de Brás Cubas"))
                .andExpect(jsonPath("$[0].autor.nome").value("Machado de Assis"));
    }

    @Test
    void deveListarLivrosDeUmAutor() throws Exception {
        when(repository.findByAutorIdOrderByTituloAsc(1L))
                .thenReturn(Arrays.asList(novoLivro(1L, "Dom Casmurro")));

        mockMvc.perform(get("/api/livros/autor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"));
    }

    @Test
    void deveRetornar404ParaLivroInexistente() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/livros/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarLivroInclusiveOAutor() throws Exception {
        Autor clarice = new Autor();
        clarice.setId(2L);
        clarice.setNome("Clarice Lispector");

        when(repository.findById(1L)).thenReturn(Optional.of(novoLivro(1L, "Titulo antigo")));
        when(autorRepository.findById(2L)).thenReturn(Optional.of(clarice));
        when(repository.save(any(Livro.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/livros/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\": \"A Hora da Estrela\", \"anoPublicacao\": 1977, \"autor\": {\"id\": 2}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("A Hora da Estrela"))
                .andExpect(jsonPath("$.autor.id").value(2))
                .andExpect(jsonPath("$.autor.nome").value("Clarice Lispector"));
    }

    @Test
    void deveRetornar404AoAtualizarLivroInexistente() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/livros/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LIVRO_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveDeletarLivroComSucesso() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(novoLivro(1L, "Dom Casmurro")));

        mockMvc.perform(delete("/api/livros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Livro removido com sucesso"));
    }

    @Test
    void deveRetornar404AoDeletarLivroInexistente() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/livros/999"))
                .andExpect(status().isNotFound());
    }
}
