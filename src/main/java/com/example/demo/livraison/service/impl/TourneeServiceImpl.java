
package com.example.demo.livraison.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.livraison.Domain.Livreur;
import com.example.demo.livraison.Domain.StatutLivreur;
import com.example.demo.livraison.Domain.StatutTournee;
import com.example.demo.livraison.Domain.Tournee;
import com.example.demo.livraison.common.exception.ConflictException;
import com.example.demo.livraison.common.exception.ResourceNotFoundException;
import com.example.demo.livraison.repository.LivreurRepository;
import com.example.demo.livraison.repository.TourneeRepository;
import com.example.demo.livraison.service.InterfaceTourneeService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TourneeServiceImpl implements InterfaceTourneeService {

    private final TourneeRepository repository;
    private final LivreurRepository livreurRepository;

    @Override
    public List<Tournee> findAll(Long livreurId, StatutTournee statut) {

        if (livreurId != null && statut != null) {
            return repository.findByLivreurIdAndStatut(livreurId, statut);
        }

        if (livreurId != null) {
            return repository.findByLivreurId(livreurId);
        }

        if (statut != null) {
            return repository.findByStatut(statut);
        }

        return repository.findAll();
    }

    @Override
    public Tournee findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Tournée introuvable : id=" + id
                    )
                );
    }

    @Override
    public Tournee create(Long livreurId, Tournee tournee) {

        verifierDate(tournee.getDate());

        Livreur livreur = chargerLivreurUtilisable(livreurId);

        tournee.setId(null);
        tournee.setStatut(StatutTournee.PLANIFIEE);
        tournee.setLivreur(livreur);

        return repository.save(tournee);
    }

    @Override
    public Tournee update(Long id, Long livreurId, Tournee data) {

        Tournee tournee = findById(id);

        if (tournee.getStatut() != StatutTournee.PLANIFIEE) {
            throw new ConflictException(
                "Seule une tournée planifiée peut être modifiée"
            );
        }

        verifierDate(data.getDate());

        // Vérifier que le livreur existe et qu'il est utilisable
        Livreur livreur = chargerLivreurUtilisable(livreurId);

        tournee.setDate(data.getDate());
        tournee.setZone(data.getZone());
        tournee.setLivreur(livreur);

        return repository.save(tournee);
    }

    @Override
    public Tournee demarrer(Long id) {

        Tournee tournee = findById(id);

        if (tournee.getStatut() != StatutTournee.PLANIFIEE) {
            throw new ConflictException(
                "Seule une tournée planifiée peut être démarrée"
            );
        }

        Livreur livreur = tournee.getLivreur();

        if (livreur.getStatut() != StatutLivreur.DISPONIBLE) {
            throw new ConflictException(
                "Le livreur n'est pas disponible (statut : "
                + livreur.getStatut() + ")"
            );
        }

        livreur.setStatut(StatutLivreur.EN_TOURNEE);

        tournee.setStatut(StatutTournee.EN_COURS);

        return repository.save(tournee);
    }

    @Override
    public Tournee terminer(Long id) {

        Tournee tournee = findById(id);

        if (tournee.getStatut() != StatutTournee.EN_COURS) {
            throw new ConflictException(
                "Seule une tournée en cours peut être terminée"
            );
        }

        Livreur livreur = tournee.getLivreur();

        livreur.setStatut(StatutLivreur.DISPONIBLE);

        tournee.setStatut(StatutTournee.TERMINEE);

        return repository.save(tournee);
    }

    @Override
    public void delete(Long id) {

        Tournee tournee = findById(id);

        if (tournee.getStatut() != StatutTournee.PLANIFIEE) {
            throw new ConflictException(
                "Seule une tournée planifiée peut être supprimée"
            );
        }

        repository.delete(tournee);
    }

    private void verifierDate(LocalDate date) {

        if (date == null) {
            throw new ConflictException("La date est obligatoire");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new ConflictException(
                "La date de la tournée ne peut pas être dans le passé"
            );
        }
    }

    private Livreur chargerLivreurUtilisable(Long livreurId) {

        Livreur livreur = livreurRepository.findById(livreurId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Livreur introuvable : id=" + livreurId
                    )
                );

        if (livreur.getStatut() == StatutLivreur.HORS_SERVICE) {
            throw new ConflictException("Le livreur est hors service");
        }

        return livreur;
    }
}
