package com.example.demo.livraison.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.livraison.Domain.Livraison;
import com.example.demo.livraison.Domain.StatutLivraison;

public interface LivraisonRepository extends JpaRepository<Livraison, Long> {

    boolean existsByCommandeId(Long commandeId);

    Optional<Livraison> findByCodeSuivi(String codeSuivi);

    boolean existsByTourneeId(Long tourneeId);

    boolean existsByTourneeIdAndStatutIn(Long tourneeId, Collection<StatutLivraison> statuts);

    @Query("""
    	       SELECT l FROM Livraison l LEFT JOIN l.tournee t
    	       WHERE (:statut IS NULL OR l.statut = :statut)
    	         AND (:tourneeId IS NULL OR l.tourneeId = :tourneeId)
    	         AND (:livreurId IS NULL OR t.livreur.id = :livreurId)
    	       ORDER BY l.dateCreation DESC
    	       """)
    	List<Livraison> rechercher(@Param("statut") StatutLivraison statut,
    	                           @Param("tourneeId") Long tourneeId,
    	                           @Param("livreurId") Long livreurId);
}
