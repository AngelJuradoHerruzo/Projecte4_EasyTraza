package cat.copernic.easytraza.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.service.UsuariService;

@RestController
@RequestMapping("/usuaris")
public class UsuariController {

    // ---------------------------- SERVEI I CONSTRUCTOR ----------------------------
    private final UsuariService usuariService;

    public UsuariController(UsuariService usuariService) {
        this.usuariService = usuariService;
    }


    // OBTENIR TOTS ELS USUARIS
    @GetMapping
    public List<Usuari> getAllUsuaris() {
        return usuariService.getAllUsuaris();
    }


    // OBTENIR USUARI PER ID
    @GetMapping("/{id}")
    public Usuari getUsuariById(@PathVariable Long id) {
        return usuariService.getUsuariById(id);
    }


    // CREAR USUARI
    @PostMapping
    public Usuari createUsuari(@RequestBody Usuari usuari) {
        return usuariService.createUsuari(usuari);
    }


    // ACTUALITZAR USUARI
    @PutMapping("/{id}")
    public Usuari updateUsuari(@PathVariable Long id, @RequestBody Usuari usuari) {
        return usuariService.updateUsuari(id, usuari);
    }


    // ELIMINAR USUARI
    @DeleteMapping("/{id}")
    public void deleteUsuari(@PathVariable Long id) {
        usuariService.deleteUsuari(id);
    }
}