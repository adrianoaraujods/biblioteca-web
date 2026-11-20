package com.biblioteca.controller;

import com.biblioteca.model.Autor;
import com.biblioteca.repository.AutorRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/autores")
@CrossOrigin(origins = "*")
public class AutorController {

    private final AutorRepository repository;

    public AutorController(AutorRepository repository) {
        this.repository = repository;
    }

    // CREATE - Criar novo autor
    @PostMapping
    public ResponseEntity<Autor> criar(@Valid @RequestBody Autor autor) {
        Autor salvo = repository.save(autor);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // READ - Listar todos os autores (ordem: nome)
    @GetMapping
    public ResponseEntity<List<Autor>> listar() {
        return ResponseEntity.ok(repository.findAllByOrderByNomeAsc());
    }

    // READ - Buscar autor por ID
    @GetMapping("/{id}")
    public ResponseEntity<Autor> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE - Atualizar autor
    @PutMapping("/{id}")
    public ResponseEntity<Autor> atualizar(@PathVariable Long id,
                                           @Valid @RequestBody Autor dados) {
        return repository.findById(id)
                .map(autor -> {
                    autor.setNome(dados.getNome());
                    autor.setNacionalidade(dados.getNacionalidade());
                    autor.setDataNascimento(dados.getDataNascimento());
                    return ResponseEntity.ok(repository.save(autor));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE - Remover autor
    // (extra) se o autor tiver livros, a FK impede a exclusão -> 409 com mensagem amigável
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletar(@PathVariable Long id) {
        return repository.findById(id)
                .map(autor -> {
                    try {
                        repository.delete(autor);
                        repository.flush();
                        return ResponseEntity.ok(Map.of("mensagem", "Autor removido com sucesso"));
                    } catch (DataIntegrityViolationException e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of("mensagem",
                                        "Não é possível excluir: o autor possui livros cadastrados. Exclua os livros antes."));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
