package com.example.demo.livraison.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.livraison.Domain.Livreur;
import com.example.demo.livraison.Domain.StatutLivreur;

public interface LivreurRepository extends JpaRepository<Livreur, Long>{
   
    boolean existsByTelephone(String telephone);

  
    boolean existsByTelephoneAndIdNot(String telephone, Long id);


    List<Livreur> findByStatut(StatutLivreur statut);
}
