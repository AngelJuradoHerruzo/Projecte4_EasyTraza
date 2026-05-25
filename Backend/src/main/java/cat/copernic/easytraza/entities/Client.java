package cat.copernic.easytraza.entities;

import jakarta.persistence.*;

/**
 * ENTITAT CLIENT
 *
 * Representa un client del sistema.
 *
 * @author Ángel Jurado
 */
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cif;

    @Column(nullable = false)
    private String nomComplet;

    @Column(nullable = false, unique = true)
    private String telefon;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String adreca;

    @Column(length = 100)
    private String observacions;

    @Transient
    private boolean teAlbarans;


    /*********************       .CONSTRUCTORS.       *********************/
    public Client() { }

    public Client(Long id, String cif, String nomComplet, String telefon, String email,
                  String adreca, String observacions) {
        this.id = id;
        this.cif = cif;
        this.nomComplet = nomComplet;
        this.telefon = telefon;
        this.email = email;
        this.adreca = adreca;
        this.observacions = observacions;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ─────────── CIF ───────────
    public String getCif() { return cif; }
    public void setCif(String cif) { this.cif = cif; }

    // ─────── NOM COMPLET ───────
    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    // ───────── TELÈFON ─────────
    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    // ────────── EMAIL ──────────
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // ───────── ADREÇA ─────────
    public String getAdreca() { return adreca; }
    public void setAdreca(String adreca) { this.adreca = adreca; }

    // ───── OBSERVACIONS ─────
    public String getObservacions() { return observacions; }
    public void setObservacions(String observacions) { this.observacions = observacions; }

    // ───── ALBARANS ASSOCIATS ─────
    public boolean isTeAlbarans() { return teAlbarans; }
    public void setTeAlbarans(boolean teAlbarans) { this.teAlbarans = teAlbarans; }
}
