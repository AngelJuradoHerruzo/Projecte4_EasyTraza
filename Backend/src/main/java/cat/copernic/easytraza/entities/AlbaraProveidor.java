package cat.copernic.easytraza.entities;

import java.time.LocalDateTime;
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
    private LocalDateTime dataRecepcio;

    @ManyToOne
    @JoinColumn(name = "proveidor_id", nullable = false)
    private Proveidor proveidor;

    @ManyToOne
    @JoinColumn(name = "usuari_receptor_id", nullable = false)
    private Usuari usuariReceptor;

    @OneToMany(mappedBy = "albaraProveidor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LotProveidor> lots;


    /*********************       .CONSTRUCTORS.       *********************/
    public AlbaraProveidor() { }

    public AlbaraProveidor(Long id, LocalDateTime dataRecepcio, Proveidor proveidor, Usuari usuariReceptor, List<LotProveidor> lots) {
        this.id = id;
        this.dataRecepcio = dataRecepcio;
        this.proveidor = proveidor;
        this.usuariReceptor = usuariReceptor;
        this.lots = lots;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ────── DATA RECEPCIÓ ──────
    public LocalDateTime getDataRecepcio() { return dataRecepcio; }
    public void setDataRecepcio(LocalDateTime dataRecepcio) { this.dataRecepcio = dataRecepcio; }

    // ───────── PROVEÏDOR ─────────
    public Proveidor getProveidor() { return proveidor; }
    public void setProveidor(Proveidor proveidor) { this.proveidor = proveidor; }

    // ─────── USUARI RECEPTOR ───────
    public Usuari getUsuariReceptor() { return usuariReceptor; }
    public void setUsuariReceptor(Usuari usuariReceptor) { this.usuariReceptor = usuariReceptor; }

    // ─────────── LOTS ───────────
    public List<LotProveidor> getLots() { return lots; }
    public void setLots(List<LotProveidor> lots) { this.lots = lots; }
}
