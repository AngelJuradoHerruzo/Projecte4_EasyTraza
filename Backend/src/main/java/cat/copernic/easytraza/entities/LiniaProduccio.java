package cat.copernic.easytraza.entities;

import jakarta.persistence.*;

/**
 * ENTITAT LÍNIA DE PRODUCCIÓ.
 *
 * Representada una quantitat de producte manufacturat dins d'un albarà
 * de client, indicant el producte i l'operari responsables.
 *
 * @author Ángel Jurado Herruzo
 */
@Entity
@Table(name = "linies_produccio")
public class LiniaProduccio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantitat;

    @ManyToOne
    @JoinColumn(name = "producte_id", nullable = false)
    private Producte producte;

    @ManyToOne
    @JoinColumn(name = "operari_id", nullable = false)
    private Usuari operari;

    @ManyToOne
    @JoinColumn(name = "albara_client_id", nullable = false)
    private AlbaraClient albaraClient;


    /*********************       .CONSTRUCTORS.       *********************/
    public LiniaProduccio() { }

    public LiniaProduccio(Long id, Integer quantitat, Producte producte,
                          Usuari operari, AlbaraClient albaraClient) {
        this.id = id;
        this.quantitat = quantitat;
        this.producte = producte;
        this.operari = operari;
        this.albaraClient = albaraClient;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ───────── QUANTITAT ─────────
    public Integer getQuantitat() { return quantitat; }
    public void setQuantitat(Integer quantitat) { this.quantitat = quantitat; }

    // ───────── PRODUCTE ─────────
    public Producte getProducte() { return producte; }
    public void setProducte(Producte producte) { this.producte = producte; }

    // ───────── OPERARI ─────────
    public Usuari getOperari() { return operari; }
    public void setOperari(Usuari operari) { this.operari = operari; }

    // ───── ALBARÀ DE CLIENT ─────
    public AlbaraClient getAlbaraClient() { return albaraClient; }
    public void setAlbaraClient(AlbaraClient albaraClient) { this.albaraClient = albaraClient; }
}
