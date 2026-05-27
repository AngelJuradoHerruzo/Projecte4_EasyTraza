package cat.copernic.easytraza.entities;

import jakarta.persistence.*;

/**
 * ENTITAT UNITAT DE MESURA.
 *
 * Representada una unitat de mesura utilitzada en les línies
 * dels albarans i desada de forma normalitzada.
 *
 * @author Ángel Jurado Herruz
 */
@Entity
@Table(name = "unitats_mesura")
public class UnitatMesura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_unitat", nullable = false, unique = true, length = 4)
    private String nom;


    /*********************       .CONSTRUCTORS.       *********************/
    public UnitatMesura() { }

    public UnitatMesura(Long id, String nom) { 
        this.id = id;
        this.nom = nom;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ─────────── NOM ───────────
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
