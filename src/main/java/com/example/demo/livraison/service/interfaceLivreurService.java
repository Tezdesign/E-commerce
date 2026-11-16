package com.example.demo.livraison.service;

import java.util.List;

import com.example.demo.livraison.Domain.Livreur;
import com.example.demo.livraison.Domain.StatutLivreur;

public interface interfaceLivreurService {
	   public List<Livreur> findAll(StatutLivreur statut);
	   public Livreur create(Livreur livreur);
	   public Livreur findById(Long id);
	   public Livreur update(Long id, Livreur data);
	   public Livreur changerStatut(Long id, StatutLivreur statut);
	   public void delete(Long id);
}
