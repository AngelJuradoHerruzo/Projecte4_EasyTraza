package cat.copernic.easytraza.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;

/**
 * TOKEN DE RECUPERACIÓ DE CONTRASENYA.
 *
 * Representades les dades temporals necessàries per validar un enllaç
 * de recuperació de contrasenya associat a un usuari del sistema.
 *
 * @author Ángel Jurado Herruz
 */
@Entity
@Table(name = "tokens_recuperacio_contrasenya")
public class TokenRecuperacioContrasenya {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuari_id", nullable = false)
    private Usuari usuari;

    @Column(nullable = false)
    private LocalDateTime dataCaducitat;

    @Column(nullable = false)
    private boolean utilitzat = false;


    /*********************       .CONSTRUCTORS.       *********************/
    public TokenRecuperacioContrasenya() { }

    public TokenRecuperacioContrasenya(String tokenHash, Usuari usuari, LocalDateTime dataCaducitat) {
        this.tokenHash = tokenHash;
        this.usuari = usuari;
        this.dataCaducitat = dataCaducitat;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public Usuari getUsuari() { return usuari; }
    public void setUsuari(Usuari usuari) { this.usuari = usuari; }

    public LocalDateTime getDataCaducitat() { return dataCaducitat; }
    public void setDataCaducitat(LocalDateTime dataCaducitat) { this.dataCaducitat = dataCaducitat; }

    public boolean isUtilitzat() { return utilitzat; }
    public void setUtilitzat(boolean utilitzat) { this.utilitzat = utilitzat; }
}
