package com.example.demo.livraison.Domain;


import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "livraison")
@Getter
@Setter
@NoArgsConstructor
public class Livraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Référence vers le microservice Commande : pas de clé étrangère
    @NotNull(message = "Le commandeId est obligatoire")
    @Column(name = "commande_id", nullable = false, unique = true)
    private Long commandeId;

    @NotBlank(message = "L'adresse de collecte est obligatoire")
    @Size(max = 255)
    @Column(name = "adresse_collecte", nullable = false)
    private String adresseCollecte;

    @NotBlank(message = "L'adresse de livraison est obligatoire")
    @Size(max = 255)
    @Column(name = "adresse_livraison", nullable = false)
    private String adresseLivraison;

    @NotBlank(message = "Le nom du destinataire est obligatoire")
    @Size(max = 150)
    @Column(name = "destinataire_nom", nullable = false, length = 150)
    private String destinataireNom;

    @NotBlank(message = "Le téléphone du destinataire est obligatoire")
    @Size(max = 20)
    @Column(name = "destinataire_telephone", nullable = false, length = 20)
    private String destinataireTelephone;

    @Column(name = "code_suivi", nullable = false, unique = true, length = 20)
    private String codeSuivi;

    @JsonIgnore
    @Column(name = "code_confirmation", nullable = false, length = 10)
    private String codeConfirmation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutLivraison statut;

    @Column(name = "nombre_tentatives", nullable = false)
    private int nombreTentatives = 0;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_collecte")
    private LocalDateTime dateCollecte;

    @Column(name = "date_livraison")
    private LocalDateTime dateLivraison;

    @Column(name = "tournee_id")
    private Long tourneeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tournee_id", insertable = false, updatable = false)
    private Tournee tournee;
}