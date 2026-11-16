package com.example.demo.livraison.Domain;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "livreur")
@Getter @Setter @NoArgsConstructor
public class Livreur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nom;
    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String prenom;
    @NotBlank
    @Pattern(regexp = "^[0-9+ ]{8,20}$", message = "Numéro de téléphone invalide")
    @Column(nullable = false, unique = true, length = 20)
    private String telephone;
    @Size(max = 50)
    @Column(length = 50)
    private String vehicule;
    @Size(max = 255)
    @Column(length = 255)
    private String zones;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutLivreur statut ;
    @JsonIgnore
    @OneToMany(mappedBy = "livreur")
    private List<Tournee> tournees = new ArrayList<>();
}