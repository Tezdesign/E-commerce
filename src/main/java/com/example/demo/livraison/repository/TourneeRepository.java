package com.example.demo.livraison.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.livraison.Domain.StatutTournee;
import com.example.demo.livraison.Domain.Tournee;

public interface TourneeRepository extends JpaRepository<Tournee, Long> {
	List<Tournee> findByLivreurId(Long livreurId);

    List<Tournee> findByStatut(StatutTournee statut);

    List<Tournee> findByLivreurIdAndStatut(Long livreurId, StatutTournee statut);

    boolean existsByLivreurIdAndStatut(Long livreurId, StatutTournee statut);

}
