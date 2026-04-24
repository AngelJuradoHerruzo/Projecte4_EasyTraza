package cat.copernic.easytraza.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

import cat.copernic.easytraza.enums.EstatLot;

@Entity
@Table(name = "lot_proveidor")
public class LotProveidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String identificadorLot;

    private Integer quantitat;

    @Enumerated(EnumType.STRING)
    private EstatLot estatLot;

    private LocalDate dataCaducitat;

    private LocalDate dataObertura;

    private LocalDate dataAcabament;

    @ManyToOne
    @JoinColumn(name = "materia_prima_id", nullable = false)
    private MateriaPrima materiaPrima;

    @ManyToOne
    @JoinColumn(name = "albara_proveidor_id", nullable = false)
    private AlbaraProveidor albaraProveidor;


    /*********************       .CONSTRUCTORS.       *********************/
    public LotProveidor() {}

    public LotProveidor(Long id, String identificadorLot, Integer quantitat, EstatLot estatLot,
                        LocalDate dataCaducitat, LocalDate dataObertura, LocalDate dataAcabament,
                        MateriaPrima materiaPrima, AlbaraProveidor albaraProveidor) {
        this.id = id;
        this.identificadorLot = identificadorLot;
        this.quantitat = quantitat;
        this.estatLot = estatLot;
        this.dataCaducitat = dataCaducitat;
        this.dataObertura = dataObertura;
        this.dataAcabament = dataAcabament;
        this.materiaPrima = materiaPrima;
        this.albaraProveidor = albaraProveidor;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ──── IDENTIFICADOR LOT ────
    public String getIdentificadorLot() { return identificadorLot; }
    public void setIdentificadorLot(String identificadorLot) { this.identificadorLot = identificadorLot; }

    // ──────── QUANTITAT ────────
    public Integer getQuantitat() { return quantitat; }
    public void setQuantitat(Integer quantitat) { this.quantitat = quantitat; }

    // ────────── ESTAT ──────────
    public EstatLot getEstat() { return estatLot; }
    public void setEstat(EstatLot estatLot) { this.estatLot = estatLot; }

    // ───── DATA CADUCITAT ─────
    public LocalDate getDataCaducitat() { return dataCaducitat; }
    public void setDataCaducitat(LocalDate dataCaducitat) { this.dataCaducitat = dataCaducitat; }

    // ─────     DATA OBERTURA ─────
    public LocalDate getDataObertura() { return dataObertura; }
    public void setDataObertura(LocalDate dataObertura) { this.dataObertura = dataObertura; }

    // ───── DATA ACABAMENT ─────
    public LocalDate getDataAcabament() { return dataAcabament; }
    public void setDataAcabament(LocalDate dataAcabament) { this.dataAcabament = dataAcabament; }

    // ───── MATERIA PRIMA ─────
    public MateriaPrima getMateriaPrima() { return materiaPrima; }
    public void setMateriaPrima(MateriaPrima materiaPrima) { this.materiaPrima = materiaPrima; }

    // ───── ALBARÀ PROVEÏDOR ─────
    public AlbaraProveidor getAlbaraProveidor() { return albaraProveidor; }
    public void setAlbaraProveidor(AlbaraProveidor albaraProveidor) { this.albaraProveidor = albaraProveidor; }
}