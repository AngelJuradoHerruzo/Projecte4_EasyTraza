package cat.copernic.easytraza.entities;

import jakarta.persistence.*;

/**
 * ENTITAT PRODUCTE
 *
 * Representa un producte que el negoci produeix o ven.
 *
 * @author Ángel Jurado
 */
@Entity
@Table(name = "productes")
public class Producte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    

    @Column(nullable = false, unique = true)
    private String nomProducte;

    @Column(length = 200)
    private String descripcio;


    /*********************       .CONSTRUCTORS.       *********************/
    public Producte() { }

    public Producte(Long id, String nomProducte, String descripcio) {
        this.id = id;
        this.nomProducte = nomProducte;
        this.descripcio = descripcio;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ─────────── NOM ───────────
    public String getNomProducte() { return nomProducte; }
    public void setNomProducte(String nomProducte) { this.nomProducte = nomProducte; }

    // ─────── DESCRIPCIÓ ───────
    public String getDescripcio() { return descripcio; }
    public void setDescripcio(String descripcio) { this.descripcio = descripcio; }
}