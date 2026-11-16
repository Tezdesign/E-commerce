
package com.example.demo.livraison.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.livraison.Domain.Livraison;
import com.example.demo.livraison.Domain.StatutLivraison;
import com.example.demo.livraison.service.interfaceLivreson;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/livraisons")
@RequiredArgsConstructor
public class LivraisonController {

    private final interfaceLivreson service;

    @GetMapping
    public List<Livraison> list(
            @RequestParam(required = false) StatutLivraison statut,
            @RequestParam(required = false) Long tourneeId,
            @RequestParam(required = false) Long livreurId) {

        return service.findAll(statut, tourneeId, livreurId);
    }

    @GetMapping("/{id}")
    public Livraison get(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/suivi/{codeSuivi}")
    public Livraison suivi(@PathVariable String codeSuivi) {
        return service.findByCodeSuivi(codeSuivi);
    }

    @PostMapping("/{tourneeId}/livraisons")
    public ResponseEntity<Livraison> creerLivraison(
            @PathVariable Long tourneeId,
            @Valid @RequestBody Livraison livraison) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(tourneeId, livraison));
    }

    @PostMapping("/{id}/affecter")
    public Livraison affecter(
            @PathVariable Long id,
            @RequestParam Long tourneeId) {

        return service.affecter(id, tourneeId);
    }

    @PostMapping("/{id}/collecter")
    public Livraison collecter(@PathVariable Long id) {
        return service.collecter(id);
    }

    @PostMapping("/{id}/demarrer")
    public Livraison demarrer(@PathVariable Long id) {
        return service.demarrer(id);
    }

    @PostMapping("/{id}/confirmer")
    public Livraison confirmer(
            @PathVariable Long id,
            @RequestParam String code) {

        return service.confirmer(id, code);
    }

    @PostMapping("/{id}/echec")
    public Livraison echec(@PathVariable Long id) {
        return service.signalerEchec(id);
    }

    @PostMapping("/{id}/annuler")
    public Livraison annuler(@PathVariable Long id) {
        return service.annuler(id);
    }
    @DeleteMapping("/supprimeLivraison/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/modiferLivraison/{id}")
    public ResponseEntity<Livraison> update(
            @PathVariable Long id,
            @Valid @RequestBody Livraison livraison) {

        return ResponseEntity.ok(service.update(id, livraison));
    }
}
