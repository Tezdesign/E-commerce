package com.example.demo.livraison.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.livraison.Domain.StatutTournee;
import com.example.demo.livraison.Domain.Tournee;
import com.example.demo.livraison.service.InterfaceTourneeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tournees")
@RequiredArgsConstructor
public class TourneeController {

    private final InterfaceTourneeService tourneeService;

    @GetMapping
    public ResponseEntity<List<Tournee>> findAll(
            @RequestParam(required = false) Long livreurId,
            @RequestParam(required = false) StatutTournee statut) {

        return ResponseEntity.ok(
                tourneeService.findAll(livreurId, statut)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tournee> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tourneeService.findById(id)
        );
    }

    @PostMapping("/livreur/{livreurId}")
    public ResponseEntity<Tournee> create(
            @PathVariable Long livreurId,
            @Valid @RequestBody Tournee tournee) {

        Tournee created = tourneeService.create(livreurId, tournee);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/livreur/{livreurId}")
    public ResponseEntity<Tournee> update(
            @PathVariable Long id,
            @PathVariable Long livreurId,
            @Valid @RequestBody Tournee tournee) {

        return ResponseEntity.ok(
            tourneeService.update(id, livreurId, tournee)
        );
    }


    @PostMapping("/{id}/demarrer")
    public ResponseEntity<Tournee> demarrer(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tourneeService.demarrer(id)
        );
    }

   
    @PostMapping("/{id}/terminer")
    public ResponseEntity<Tournee> terminer(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tourneeService.terminer(id)
        );
    }

 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        tourneeService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
