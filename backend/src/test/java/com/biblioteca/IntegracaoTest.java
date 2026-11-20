package com.biblioteca;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TESTES DE INTEGRAÇÃO
 * @SpringBootTest carrega a aplicação inteira: Controller -> Repository -> Banco (H2, perfil "test").
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long criarAutor(String json) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/autores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void deveVincularLivroAAutor() throws Exception {
        // 1. Criar autor
        Long autorId = criarAutor("""
            { "nome": "Machado de Assis", "nacionalidade": "Brasileira", "dataNascimento": "1839-06-21" }
            """);

        // 2. Criar livro vinculado ao autor (só com o id)
        String livroJson = String.format("""
            { "titulo": "Dom Casmurro", "isbn": "978-8535910667", "anoPublicacao": 1899,
              "genero": "Romance", "autor": { "id": %d } }
            """, autorId);

        MvcResult result = mockMvc.perform(post("/api/livros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(livroJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.autor.id").value(autorId))
                .andExpect(jsonPath("$.autor.nome").value("Machado de Assis"))
                .andReturn();

        Long livroId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // 3. Buscar o livro de novo e conferir o vínculo gravado no banco
        mockMvc.perform(get("/api/livros/" + livroId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autor.nome").value("Machado de Assis"));

        // 4. (extra) Listar os livros do autor
        mockMvc.perform(get("/api/livros/autor/" + autorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"));

        // 5. (extra) Autor com livros não pode ser excluído -> 409
        mockMvc.perform(delete("/api/autores/" + autorId))
                .andExpect(status().isConflict());

        // 6. Excluindo o livro antes, o autor pode ser excluído
        mockMvc.perform(delete("/api/livros/" + livroId)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/autores/" + autorId)).andExpect(status().isOk());
    }

    @Test
    void deveExecutarFluxoCompletoAutor() throws Exception {
        // 1. CRIAR
        Long id = criarAutor("""
            { "nome": "Clarice Lispector", "nacionalidade": "Ucraniana" }
            """);

        // 2. BUSCAR
        mockMvc.perform(get("/api/autores/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Clarice Lispector"));

        // 3. ATUALIZAR
        mockMvc.perform(put("/api/autores/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "nome": "Clarice Lispector", "nacionalidade": "Brasileira", "dataNascimento": "1920-12-10" }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nacionalidade").value("Brasileira"))
                .andExpect(jsonPath("$.dataNascimento").value("1920-12-10"));

        // 4. DELETAR
        mockMvc.perform(delete("/api/autores/" + id))
                .andExpect(status().isOk());

        // 5. Depois de deletado -> 404
        mockMvc.perform(get("/api/autores/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400AoCriarLivroComAutorInexistente() throws Exception {
        mockMvc.perform(post("/api/livros")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"titulo\": \"Livro órfão\", \"autor\": { \"id\": 99999 } }"))
                .andExpect(status().isBadRequest());
    }
}
