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
 * @author Ángel Jurado Herruzo
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
                        usuari.getNomComplet()
                ))
                .toList();

        return ResponseEntity.ok(usuaris);
    }


    /**
     * IDENTIFICACIÓ D'USUARI MÒBIL.
     *
     * Cercat l'usuari mitjançant l'identificador rebut i retornades
     * únicament les dades necessàries quan la identificació és vàlida.
     *
     * @param request dades rebudes per identificar l'usuari
     * @return resposta amb l'usuari identificat o l'error corresponent
     */
    @PostMapping("/identificar")
    public ResponseEntity<?> identificar(@RequestBody IdentificarRequest request) {

        try {
            if (request == null || request.getId() == null) {
                throw new RuntimeException("L'identificador de l'usuari és obligatori");
            }

            Usuari usuari = usuariService.getUsuariById(request.getId());

            if (usuari == null) {
                throw new RuntimeException("No existeix cap usuari amb aquest identificador");
            }

            UsuariIdentificatResponse response = new UsuariIdentificatResponse(
                    usuari.getId(),
                    usuari.getNomComplet()
            );

            return ResponseEntity.ok(response);
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /**
     * DTO intern per rebre l'identificador de l'usuari.
     */
    public static class IdentificarRequest {

        private Long id;

        public IdentificarRequest() {
        }

        public IdentificarRequest(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }


    /**
     * DTO intern per retornar les dades bàsiques de l'usuari identificat.
     */
    public static class UsuariIdentificatResponse {

        private Long id;
        private String nomComplet;

        public UsuariIdentificatResponse() {
        }

        public UsuariIdentificatResponse(Long id, String nomComplet) {
            this.id = id;
            this.nomComplet = nomComplet;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNomComplet() {
            return nomComplet;
        }

        public void setNomComplet(String nomComplet) {
            this.nomComplet = nomComplet;
        }

    }
}