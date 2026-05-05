package cat.copernic.easytraza.entities;

import jakarta.persistence.*;

/**
 * ENTITAT MATÈRIA PRIMERA
 *
 * Representa una matèria primera del sistema.
 * És un producte base utilitzat per a la fabricació de producte acabat.
 *
 * @author Ángel Jurado
 */
@Entity
@Table(name = "materies_primeres")
public class MateriaPrimera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nomMateria;

    @Column(length = 50)
    private String descripcio;


    /*********************       .CONSTRUCTORS.       *********************/
    public MateriaPrimera() { }

    public MateriaPrimera(Long id, String nomMateria, String descripcio) {
        this.id = id;
        this.nomMateria = nomMateria;
        this.descripcio = descripcio;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ─────────── NOM ───────────
    public String getNomMateria() { return nomMateria; }
    public void setNomMateria(String nomMateria) { this.nomMateria = nomMateria; }

    // ─────── DESCRIPCIO ───────
    public String getDescripcio() { return descripcio; }
    public void setDescripcio(String descripcio) { this.descripcio = descripcio; }
}