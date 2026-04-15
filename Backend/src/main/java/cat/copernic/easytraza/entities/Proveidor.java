package cat.copernic.easytraza.entities;

import jakarta.persistence.*;

/**
 * ENTITAT PROVEÏDOR
 *
 * Representa un proveïdor de matèries primeres del sistema.
 *
 * @author Ángel Jurado
 */
@Entity
@Table(name = "proveidors")
public class Proveidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cif;

    @Column(nullable = false)
    private String nomProveidor;

    private String adreca;

    @Column(length = 200)
    private String descripcio;


    /*********************       .CONSTRUCTORS.       *********************/
    public Proveidor() { }

    public Proveidor(Long id, String cif, String nomProveidor, String adreca, String descripcio) {
        this.id = id;
        this.cif = cif;
        this.nomProveidor = nomProveidor;
        this.adreca = adreca;
        this.descripcio = descripcio;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ─────────── CIF ───────────
    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }

    // ─────────── NOM ───────────
    public String getNomProveidor() { return nomProveidor; }
    public void setNomProveidor(String nomProveidor) { this.nomProveidor = nomProveidor; }

    // ───────── ADREÇA ─────────
    public String getAdreca() { return adreca; }
    public void setAdreca(String adreca) { this.adreca = adreca; }

    // ─────── DESCRIPCIÓ ───────
    public String getDescripcio() { return descripcio; }
    public void setDescripcio(String descripcio) { this.descripcio = descripcio; }
}