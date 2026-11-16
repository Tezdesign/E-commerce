package com.example.demo.livraison.service;

import java.util.List;

import com.example.demo.livraison.Domain.Livraison;
import com.example.demo.livraison.Domain.StatutLivraison;

public interface interfaceLivreson {

	    List<Livraison> findAll(StatutLivraison statut, Long tourneeId, Long livreurId);

	    Livraison findById(Long id);

	    Livraison findByCodeSuivi(String codeSuivi);

	    Livraison create(Long tourneeId, Livraison livraison);

	    Livraison affecter(Long id, Long tourneeId);

	    Livraison collecter(Long id);

	    Livraison demarrer(Long id);

	    Livraison confirmer(Long id, String code);

	    Livraison signalerEchec(Long id);

	    Livraison annuler(Long id);
	    
	    void delete(Long id);
	    
	    Livraison update(Long id, Livraison livraison);
	}
