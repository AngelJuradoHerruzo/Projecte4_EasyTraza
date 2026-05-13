package cat.copernic.easytraza.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

/**
 * ENTITAT ALBARÀ DE PROVEÏDOR
 *
 * Representa una recepció de lots de matèria primera d'un proveïdor.
 *
 * @author Ángel Jurado
 */
@Entity
@Table(name = "albarans_proveidor")
public class AlbaraProveidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dataRecepcio;

    @ManyToOne
    @JoinColumn(name = "proveidor_id", nullable = false)
    private Proveidor proveidor;

    @ManyToOne
    @JoinColumn(name = "usuari_receptor_id", nullable = false)
    private Usuari usuariReceptor;

    @OneToMany(mappedBy = "albaraProveidor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LotProveidor> lots;

    @ElementCollection
    @CollectionTable(name = "albara_proveidor_fitxers", joinColumns = @JoinColumn(name = "albara_id"))
    @Column(name = "fitxer_path")
    private List<String> fitxers = new ArrayList<>();


    /*********************       .CONSTRUCTORS.       *********************/
    public AlbaraProveidor() { }

    public AlbaraProveidor(Long id, LocalDate dataRecepcio, Proveidor proveidor, Usuari usuariReceptor, List<LotProveidor> lots) {
        this.id = id;
        this.dataRecepcio = dataRecepcio;
        this.proveidor = proveidor;
        this.usuariReceptor = usuariReceptor;
        this.lots = lots;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ──────────── ID ────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ─────── DATA RECEPCIÓ ───────
    public LocalDate getDataRecepcio() { return dataRecepcio; }
    public void setDataRecepcio(LocalDate dataRecepcio) { this.dataRecepcio = dataRecepcio; }

    // ───────── PROVEÏDOR ─────────
    public Proveidor getProveidor() { return proveidor; }
    public void setProveidor(Proveidor proveidor) { this.proveidor = proveidor; }

    // ────── USUARI RECEPTOR ──────
    public Usuari getUsuariReceptor() { return usuariReceptor; }
    public void setUsuariReceptor(Usuari usuariReceptor) { this.usuariReceptor = usuariReceptor; }

    // ─────────── LOTS ───────────
    public List<LotProveidor> getLots() { return lots; }
    public void setLots(List<LotProveidor> lots) { this.lots = lots; }

    // ─────── RUTA FITXERS ───────
    public List<String> getFitxers() { return fitxers; }
    public void setFitxers(List<String> fitxers) { this.fitxers = fitxers; }
}