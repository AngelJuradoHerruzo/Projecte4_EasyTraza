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

    @Column(nullable = false)
    private String nomComplet;

    @Enumerated(EnumType.STRING)
    private RolUsuari rolUsuari = RolUsuari.OPERARI;

    @Column(unique = true)
    private String email;

    private String password;


    /*********************       .CONSTRUCTORS.       *********************/
    public Usuari() { }


    /*********************       .GETTERS & SETTERS.       *********************/
    // ─────────── ID ───────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

}