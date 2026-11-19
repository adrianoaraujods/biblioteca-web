package com.biblioteca.controller;

import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import com.biblioteca.repository.AutorRepository;
import com.biblioteca.repository.LivroRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/livros")
@CrossOrigin(origins = "*")
public class LivroController {

    private final LivroRepository repository;
    private final AutorRepository autorRepository;

    public LivroController(LivroRepository repository, AutorRepository autorRepository) {
        this.repository = repository;
        this.autorRepository = autorRepository;
    }

    // O JSON traz só "autor": {"id": 1}; aqui buscamos o autor completo no banco.
    private Optional<Autor> carregarAutor(Livro livro) {
        if (livro.getAutor() == null || livro.getAutor().getId() == null) {
            return Optional.empty();
        }
        return autorRepository.findById(livro.getAutor().getId());
    }

    private ResponseEntity<Map<String, String>> autorInvalido() {
        return ResponseEntity.badRequest()
                .body(Map.of("mensagem", "Autor não encontrado. Informe \"autor\": {\"id\": ...} de um autor existente."));
    }

    // CREATE - Criar novo livro ligado a um autor
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody Livro livro) {
        Optional<Autor> autor = carregarAutor(livro);
        if (autor.isEmpty()) {
            return autorInvalido();
        }
        livro.setAutor(autor.get());
        Livro salvo = repository.save(livro);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // READ - Listar todos os livros (ordem: título)
    @GetMapping
    public ResponseEntity<List<Livro>> listar() {
        return ResponseEntity.ok(repository.findAllByOrderByTituloAsc());
    }

    // READ - Buscar livro por ID
    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // READ (extra) - Listar os livros de um autor
    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<Livro>> listarPorAutor(@PathVariable Long autorId) {
        return ResponseEntity.ok(repository.findByAutorIdOrderByTituloAsc(autorId));
    }

    // UPDATE - Atualizar livro (inclusive o autor)
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody Livro dados) {
        Optional<Livro> existente = repository.findById(id);
        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<Autor> autor = carregarAutor(dados);
        if (autor.isEmpty()) {
            return autorInvalido();
        }
        Livro livro = existente.get();
        livro.setTitulo(dados.getTitulo());
        livro.setIsbn(dados.getIsbn());
        livro.setAnoPublicacao(dados.getAnoPublicacao());
        livro.setGenero(dados.getGenero());
        livro.setAutor(autor.get());
        return ResponseEntity.ok(repository.save(livro));
    }

    // DELETE - Remover livro
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletar(@PathVariable Long id) {
        return repository.findById(id)
                .map(livro -> {
                    repository.delete(livro);
                    return ResponseEntity.ok(Map.of("mensagem", "Livro removido com sucesso"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
