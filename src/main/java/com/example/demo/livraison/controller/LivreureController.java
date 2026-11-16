package com.example.demo.livraison.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.example.demo.livraison.Domain.Livreur;
import com.example.demo.livraison.Domain.StatutLivreur;
import com.example.demo.livraison.service.interfaceLivreurService;

import java.util.List;

@RestController
@RequestMapping("/api/livreurs")
@RequiredArgsConstructor
public class LivreureController {

    private final interfaceLivreurService service;

    @GetMapping
    public List<Livreur> list(@RequestParam(required = false) StatutLivreur statut) {
        return service.findAll(statut);
    }

    @GetMapping("/{id}")
    public Livreur get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Livreur> create(@Valid @RequestBody Livreur livreur) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(livreur));
    }

    @PutMapping("/{id}")
    public Livreur update(@PathVariable Long id, @Valid @RequestBody Livreur livreur) {
        return service.update(id, livreur);
    }

    @PatchMapping("/{id}/disponibilite")
    public Livreur changerStatut(@PathVariable Long id, @RequestParam StatutLivreur statut) {
        return service.changerStatut(id, statut);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    
    
}