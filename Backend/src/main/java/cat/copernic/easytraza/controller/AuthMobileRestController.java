package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.service.UsuariService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLADOR REST D'AUTENTICACIÓ MÒBIL.
 *
 * Gestionada la identificació d'usuaris de l'aplicació mòbil sense contrasenya
 * i la consulta dels usuaris disponibles per iniciar la sessió.
 *
 * @author Ángel Jurado Herruz
 */
@RestController
@RequestMapping("/api/mobile/auth")
public class AuthMobileRestController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final UsuariService usuariService;

    public AuthMobileRestController(UsuariService usuariService) {
        this.usuariService = usuariService;
    }


    /**
     * LLISTAT D'USUARIS MÒBIL.
     *
     * Recuperats els usuaris disponibles i preparades les dades mínimes
     * necessàries perquè l'aplicació mòbil permeti identificar-los.
     *
     * @return resposta amb la llista d'usuaris disponibles
     */
    @GetMapping("/usuaris")
    public ResponseEntity<List<UsuariIdentificatResponse>> llistarUsuaris() {

        List<UsuariIdentificatResponse> usuaris = usuariService.getAllUsuaris()
                .stream()
                .map(usuari -> new UsuariIdentificatResponse(
                        usuari.getId(),
                        usuari.getDni(),
                        usuari.getNomComplet(),
                        usuari.getEmail(),
                        usuari.getRolUsuari().name()
                ))
                .toList();

        return ResponseEntity.ok(usuaris);
    }


    /**
     * IDENTIFICACIÓ D'USUARI MÒBIL.
     *
     * Cercat l'usuari mitjançant el correu rebut i retornades les dades
     * necessàries quan la identificació és vàlida.
     *
     * @param request dades rebudes per identificar l'usuari
     * @return resposta amb l'usuari identificat o l'error corresponent
     */
    @PostMapping("/identificar")
    public ResponseEntity<?> identificar(@RequestBody IdentificarRequest request) {

        try {
            if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new RuntimeException("El correu electrònic és obligatori");
            }

            String email = request.getEmail().trim().toLowerCase();

            Usuari usuari = usuariService.getAllUsuaris()
                    .stream()
                    .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No existeix cap usuari amb aquest correu electrònic"));

            UsuariIdentificatResponse response = new UsuariIdentificatResponse(
                    usuari.getId(),
                    usuari.getDni(),
                    usuari.getNomComplet(),
                    usuari.getEmail(),
                    usuari.getRolUsuari().name()
            );

            return ResponseEntity.ok(response);
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /**
     * DTO intern per rebre el correu electrònic de l'usuari.
     */
    public static class IdentificarRequest {

        private String email;

        public IdentificarRequest() {
        }

        public IdentificarRequest(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }


    /**
     * DTO intern per retornar les dades bàsiques de l'usuari identificat.
     */
    public static class UsuariIdentificatResponse {

        private Long id;
        private String dni;
        private String nomComplet;
        private String email;
        private String rolUsuari;

        public UsuariIdentificatResponse() {
        }

        public UsuariIdentificatResponse(Long id, String dni, String nomComplet, String email, String rolUsuari) {
            this.id = id;
            this.dni = dni;
            this.nomComplet = nomComplet;
            this.email = email;
            this.rolUsuari = rolUsuari;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDni() {
            return dni;
        }

        public void setDni(String dni) {
            this.dni = dni;
        }

        public String getNomComplet() {
            return nomComplet;
        }

        public void setNomComplet(String nomComplet) {
            this.nomComplet = nomComplet;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRolUsuari() {
            return rolUsuari;
        }

        public void setRolUsuari(String rolUsuari) {
            this.rolUsuari = rolUsuari;
        }
    }
}