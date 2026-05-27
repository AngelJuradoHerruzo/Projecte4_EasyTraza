package cat.copernic.easytraza.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import cat.copernic.easytraza.enums.EstatAlbaraClient;
import jakarta.persistence.*;

/**
 * ENTITAT ALBARÀ DE CLIENT.
 *
 * Representat el lliurament d'un o més productes a un client,
 * juntament amb les línies de producció i els lots associats.
 *
 * @author Ángel Jurado Herruz
 */
@Entity
@Table(name = "albarans_client")
public class AlbaraClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataAlbara;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstatAlbaraClient estat = EstatAlbaraClient.PENDENT_LLIURAR;

    @OneToMany(mappedBy = "albaraClient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LiniaProduccio> liniesProduccio = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "albarans_client_lots",
            joinColumns = @JoinColumn(name = "albara_client_id"),
            inverseJoinColumns = @JoinColumn(name = "lot_proveidor_id")
    )
    private List<LotProveidor> lotsAssociats = new ArrayList<>();


    /*********************       .CONSTRUCTORS.       *********************/
    public AlbaraClient() { }

    public AlbaraClient(Long id, LocalDateTime dataAlbara, Client client,
                        EstatAlbaraClient estat, List<LiniaProduccio> liniesProduccio,
                        List<LotProveidor> lotsAssociats) {
        this.id = id;
        this.dataAlbara = dataAlbara;
        this.client = client;
        this.estat = estat;
        this.liniesProduccio = liniesProduccio;
        this.lotsAssociats = lotsAssociats;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ───── DATA I HORA ALBARÀ ─────
    public LocalDateTime getDataAlbara() { return dataAlbara; }
    public void setDataAlbara(LocalDateTime dataAlbara) { this.dataAlbara = dataAlbara; }

    // ────────── CLIENT ──────────
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    // ─────────── ESTAT ───────────
    public EstatAlbaraClient getEstat() { return estat; }
    public void setEstat(EstatAlbaraClient estat) { this.estat = estat; }

    // ──── LÍNIES PRODUCCIÓ ────
    public List<LiniaProduccio> getLiniesProduccio() { return liniesProduccio; }
    public void setLiniesProduccio(List<LiniaProduccio> liniesProduccio) { this.liniesProduccio = liniesProduccio; }

    // ────── LOTS ASSOCIATS ──────
    public List<LotProveidor> getLotsAssociats() { return lotsAssociats; }
    public void setLotsAssociats(List<LotProveidor> lotsAssociats) { this.lotsAssociats = lotsAssociats; }
}
