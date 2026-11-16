package com.example.demo.livraison.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.demo.livraison.Domain.Livraison;
import com.example.demo.livraison.Domain.StatutLivraison;
import com.example.demo.livraison.Domain.StatutTournee;
import com.example.demo.livraison.Domain.Tournee;
import com.example.demo.livraison.common.exception.ConflictException;
import com.example.demo.livraison.common.exception.ResourceNotFoundException;
import com.example.demo.livraison.repository.LivraisonRepository;
import com.example.demo.livraison.repository.TourneeRepository;
import com.example.demo.livraison.service.interfaceLivreson;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
@Service
public class LivraisonServiceImpl implements interfaceLivreson{

	private static final int MAX_TENTATIVES = 3;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final LivraisonRepository repository;
    private final TourneeRepository tourneeRepository;

    @Override
    @Transactional
    public List<Livraison> findAll(StatutLivraison statut, Long tourneeId, Long livreurId) {
        return repository.rechercher(statut, tourneeId, livreurId);
    }

    @Override
    @Transactional
    public Livraison findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable : id=" + id));
    }

    @Override
    @Transactional
    public Livraison findByCodeSuivi(String codeSuivi) {
        return repository.findByCodeSuivi(codeSuivi)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune livraison pour le code : " + codeSuivi));
    }

    @Override
    public Livraison create(Long tourneeId, Livraison livraison) {
        Tournee tournee = tourneeRepository.findById(tourneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournée introuvable : id=" + tourneeId));
        if (tournee.getStatut() != StatutTournee.PLANIFIEE) {
            throw new ConflictException("La tournée doit être planifiée pour recevoir une livraison");
        }
        if (repository.existsByCommandeId(livraison.getCommandeId())) {
            throw new ConflictException("Une livraison existe déjà pour la commande " + livraison.getCommandeId());
        }

        livraison.setId(null);                                     // force la création
        livraison.setTourneeId(tourneeId);                         // vient du path, pas du JSON
        livraison.setTournee(tournee);                             // pour que la réponse contienne la tournée
        livraison.setStatut(StatutLivraison.AFFECTEE);
        livraison.setNombreTentatives(0);
        livraison.setDateCollecte(null);
        livraison.setDateLivraison(null);
        livraison.setDateCreation(LocalDateTime.now());
        livraison.setCodeSuivi("LIV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        livraison.setCodeConfirmation(String.format("%06d", RANDOM.nextInt(1_000_000)));
        return repository.save(livraison);
    }
    @Override
    public Livraison affecter(Long id, Long tourneeId) {
        Livraison livraison = findById(id);
        verifierStatut(livraison, StatutLivraison.A_AFFECTER, "affectée à une tournée");

        Tournee tournee = tourneeRepository.findById(tourneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournée introuvable : id=" + tourneeId));
        if (tournee.getStatut() != StatutTournee.PLANIFIEE) {
            throw new ConflictException("La tournée doit être planifiée pour recevoir une livraison");
        }

        livraison.setTourneeId(tourneeId);
        livraison.setTournee(tournee);                             // pour la réponse
        livraison.setStatut(StatutLivraison.AFFECTEE);
        return repository.save(livraison);
    }

    @Override
    public Livraison collecter(Long id) {
        Livraison livraison = findById(id);
        verifierStatut(livraison, StatutLivraison.AFFECTEE, "collectée");

        livraison.setStatut(StatutLivraison.COLLECTEE);
        livraison.setDateCollecte(LocalDateTime.now());
        return repository.save(livraison);
    }

    @Override
    public Livraison demarrer(Long id) {
        Livraison livraison = findById(id);
        verifierStatut(livraison, StatutLivraison.COLLECTEE, "démarrée");

        if (livraison.getTournee().getStatut() != StatutTournee.EN_COURS) {
            throw new ConflictException("La tournée doit être démarrée avant de démarrer une livraison");
        }
        livraison.setStatut(StatutLivraison.EN_COURS);
        return repository.save(livraison);
    }

    @Override
    public Livraison confirmer(Long id, String code) {
        Livraison livraison = findById(id);
        verifierStatut(livraison, StatutLivraison.EN_COURS, "confirmée");

        if (!livraison.getCodeConfirmation().equals(code)) {
            throw new ConflictException("Code de confirmation incorrect");
        }
        livraison.setStatut(StatutLivraison.LIVREE);
        livraison.setDateLivraison(LocalDateTime.now());
        return repository.save(livraison);
    }

    @Override
    public Livraison signalerEchec(Long id) {
        Livraison livraison = findById(id);
        verifierStatut(livraison, StatutLivraison.EN_COURS, "déclarée en échec");

        livraison.setNombreTentatives(livraison.getNombreTentatives() + 1);
        if (livraison.getNombreTentatives() >= MAX_TENTATIVES) {
            livraison.setStatut(StatutLivraison.ECHEC);            
        } else {
            livraison.setStatut(StatutLivraison.COLLECTEE);        
        }
        return repository.save(livraison);
    }

    @Override
    public Livraison annuler(Long id) {
        Livraison livraison = findById(id);
        if (livraison.getStatut() != StatutLivraison.A_AFFECTER
                && livraison.getStatut() != StatutLivraison.AFFECTEE) {
            throw new ConflictException("Annulation impossible : le colis est déjà collecté ou la livraison terminée");
        }
        livraison.setStatut(StatutLivraison.ANNULEE);
        return repository.save(livraison);
    }

    // ---------- helper ----------
    private void verifierStatut(Livraison livraison, StatutLivraison attendu, String action) {
        if (livraison.getStatut() != attendu) {
            throw new ConflictException("La livraison ne peut pas être " + action
                    + " (statut actuel : " + livraison.getStatut() + ", attendu : " + attendu + ")");
        }
    }

    @Override
    public void delete(Long id) {

        Livraison livraison = findById(id);

        if (livraison.getStatut() != StatutLivraison.A_AFFECTER
                && livraison.getStatut() != StatutLivraison.AFFECTEE) {

            throw new ConflictException(
                "Suppression impossible : la livraison est déjà collectée ou terminée"
            );
        }

        repository.delete(livraison);
    }
    @Override
    public Livraison update(Long id, Livraison livraison) {

        Livraison livraisonExistante = findById(id);

        if (livraisonExistante.getStatut() != StatutLivraison.A_AFFECTER
                && livraisonExistante.getStatut() != StatutLivraison.AFFECTEE) {
            throw new ConflictException(
                "Modification impossible : la livraison est déjà en cours ou terminée"
            );
        }

        livraisonExistante.setCommandeId(livraison.getCommandeId());
        livraisonExistante.setAdresseCollecte(livraison.getAdresseCollecte());
        livraisonExistante.setAdresseLivraison(livraison.getAdresseLivraison());
        livraisonExistante.setDestinataireNom(livraison.getDestinataireNom());
        livraisonExistante.setDestinataireTelephone(livraison.getDestinataireTelephone());

        return repository.save(livraisonExistante);
    }
}