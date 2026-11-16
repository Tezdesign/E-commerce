package com.example.demo.livraison.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.livraison.Domain.Livreur;
import com.example.demo.livraison.Domain.StatutLivreur;
import com.example.demo.livraison.common.exception.ConflictException;
import com.example.demo.livraison.common.exception.ResourceNotFoundException;
import com.example.demo.livraison.repository.LivreurRepository;
import com.example.demo.livraison.service.interfaceLivreurService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class LivreureServiceImpl implements interfaceLivreurService{
	
	 private final LivreurRepository repository ;
	 
	@Override
	@Transactional
    public List<Livreur> findAll(StatutLivreur statut) {
        return (statut == null) ? repository.findAll() : repository.findByStatut(statut);
    }

	@Override
	 public Livreur create(Livreur livreur) {
        if (repository.existsByTelephone(livreur.getTelephone())) {
            throw new ConflictException("Un livreur avec ce téléphone existe déjà");
        }
        livreur.setId(null);                              // force la création
        livreur.setStatut(StatutLivreur.DISPONIBLE);
        return repository.save(livreur);
    }

	@Override
	@Transactional
	public Livreur findById(Long id) {
		// TODO Auto-generated method stub
		 return getOrThrow(id);
	}

	@Override
	public Livreur update(Long id, Livreur data) {
		// TODO Auto-generated method stub
		Livreur livreur = getOrThrow(id);
        if (repository.existsByTelephoneAndIdNot(data.getTelephone(), id)) {
            throw new ConflictException("Un livreur avec ce téléphone existe déjà");
        }
        livreur.setNom(data.getNom());
        livreur.setPrenom(data.getPrenom());
        livreur.setTelephone(data.getTelephone());
        livreur.setVehicule(data.getVehicule());
        livreur.setZones(data.getZones());
        return repository.save(livreur);
    }
		  


	@Override
	public Livreur changerStatut(Long id, StatutLivreur statut) {
		// TODO Auto-generated method stub
	      Livreur livreur = getOrThrow(id);
	        livreur.setStatut(statut);
	        return repository.save(livreur);
	    }

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
	       repository.delete(getOrThrow(id));
	    }
    private Livreur getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur introuvable : id=" + id));
    }
}
