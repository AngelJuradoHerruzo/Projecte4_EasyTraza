package cat.copernic.easytraza.entities;

import cat.copernic.easytraza.enums.RolUsuari;
import jakarta.persistence.*;

/**
 * ENTITAT USUARI
 *
 * Representa la classe dels usuaris del sistema.
 * Hi ha dos tipus d'usuaris:
 * - Administradors, que accedeixen des de l'aplicació web (PC) i requereixen autenticació.
 * - Operaris, que utilitzen l'aplicació mòbil Android i només necessiten identificar-se.
 *
 * @author Ángel Jurado
 */
@Entity
@Table(name = "usuaris")
public class Usuari {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 9)
    private String dni;

    @Column(nullable = false)
    private String nomComplet;

    @Enumerated(EnumType.STRING)
    private RolUsuari rolUsuari = RolUsuari.OPERARI;

    @Column(unique = true)
    private String email;

    private String password;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] avatar;

    private String avatarTipusContingut;

    private String avatarNomFitxer;


    /*********************       .CONSTRUCTORS.       *********************/
    public Usuari() { }

    public Usuari(Long id, String dni, String nomComplet, RolUsuari rolUsuari, String email, String password) {
        this.id = id;
        this.dni = dni;
        this.nomComplet = nomComplet;
        this.rolUsuari = rolUsuari;
        this.email = email;
        this.password = password;
    }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // ─────────── DNI ───────────
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    // ─────── NOM COMPLET ───────
    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    // ─────────── ROL ───────────
    public RolUsuari getRolUsuari() { return rolUsuari; }
    public void setRolUsuari(RolUsuari rolUsuari) { this.rolUsuari = rolUsuari; }

    // ────────── EMAIL ──────────
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // ────────── PSWD ──────────
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // ───────── AVATAR ─────────
    public byte[] getAvatar() { return avatar; }
    public void setAvatar(byte[] avatar) { this.avatar = avatar; }

    // ───── TIPUS CONTINGUT AVATAR ─────
    public String getAvatarTipusContingut() { return avatarTipusContingut; }
    public void setAvatarTipusContingut(String avatarTipusContingut) {
        this.avatarTipusContingut = avatarTipusContingut;
    }

    // ───── NOM FITXER AVATAR ─────
    public String getAvatarNomFitxer() { return avatarNomFitxer; }
    public void setAvatarNomFitxer(String avatarNomFitxer) {
        this.avatarNomFitxer = avatarNomFitxer;
    }
}