package cat.copernic.easytraza.entities;

import java.time.LocalDate;

import cat.copernic.easytraza.enums.EstatLot;
import jakarta.persistence.*;

/**
 * ENTITAT LOT DE PROVEÏDOR
 *
 * Representa un lot de matèria primera recepcionat mitjançant un albarà de proveïdor.
 *
 * @author Ángel Jurado
 */
@Entity
@Table(name = "lots_proveidor")
public class LotProveidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String identificadorLot;

    @Column(nullable = false)
    private Integer quantitat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstatLot estat = EstatLot.EN_ESTOC;

    private LocalDate dataCaducitat;

    private LocalDate dataObertura;

    private LocalDate dataAcabament;

    @ManyToOne
    @JoinColumn(name = "materia_prima_id", nullable = false)
    private MateriaPrimera materiaPrimera;

    @ManyToOne
    @JoinColumn(name = "albara_proveidor_id", nullable = false)
    private AlbaraProveidor albaraProveidor;


    /*********************       .CONSTRUCTORS.       *********************/
    public LotProveidor() { }

    public LotProveidor(Long id, String identificadorLot, Integer quantitat, EstatLot estat, LocalDate dataCaducitat,
                        LocalDate dataObertura, LocalDate dataAcabament, MateriaPrimera materiaPrimera, AlbaraProveidor albaraProveidor) {
        this.id = id;
        this.identificadorLot = identificadorLot;
        this.quantitat = quantitat;
        this.estat = estat;
        this.dataCaducitat = dataCaducitat;
        this.dataObertura = dataObertura;
        this.dataAcabament = dataAcabament;
        this.materiaPrimera = materiaPrimera;
        this.albaraProveidor = albaraProveidor;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ───── IDENTIFICADOR LOT ─────
    public String getIdentificadorLot() { return identificadorLot; }
    public void setIdentificadorLot(String identificadorLot) { this.identificadorLot = identificadorLot; }

    // ───────── QUANTITAT ─────────
    public Integer getQuantitat() { return quantitat; }
    public void setQuantitat(Integer quantitat) { this.quantitat = quantitat; }

    // ─────────── ESTAT ───────────
    public EstatLot getEstat() { return estat; }
    public void setEstat(EstatLot estat) { this.estat = estat; }

    // ────── DATA CADUCITAT ──────
    public LocalDate getDataCaducitat() { return dataCaducitat; }
    public void setDataCaducitat(LocalDate dataCaducitat) { this.dataCaducitat = dataCaducitat; }

    // ────── DATA OBERTURA ──────
    public LocalDate getDataObertura() { return dataObertura; }
    public void setDataObertura(LocalDate dataObertura) { this.dataObertura = dataObertura; }

    // ───── DATA ACABAMENT ─────
    public LocalDate getDataAcabament() { return dataAcabament; }
    public void setDataAcabament(LocalDate dataAcabament) { this.dataAcabament = dataAcabament; }

    // ───── MATÈRIA PRIMERA ─────
    public MateriaPrimera getMateriaPrimera() { return materiaPrimera; }
    public void setMateriaPrimera(MateriaPrimera materiaPrimera) { this.materiaPrimera = materiaPrimera; }

    // ──── ALBARÀ DE PROVEÏDOR ────
    public AlbaraProveidor getAlbaraProveidor() { return albaraProveidor; }
    public void setAlbaraProveidor(AlbaraProveidor albaraProveidor) { this.albaraProveidor = albaraProveidor; }
}
