package com.example.demo.livraison.service;

import java.util.List;

import com.example.demo.livraison.Domain.StatutTournee;
import com.example.demo.livraison.Domain.Tournee;

public interface InterfaceTourneeService {
	List<Tournee> findAll(Long livreurId, StatutTournee statut);

    Tournee findById(Long id);

    Tournee create(Long livreurId, Tournee tournee);

    Tournee update(Long id, Long livreurId, Tournee tournee);

    Tournee demarrer(Long id);

    Tournee terminer(Long id);

    void delete(Long id);
}
