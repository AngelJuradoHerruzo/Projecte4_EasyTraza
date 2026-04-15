package cat.copernic.easytraza.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.Proveidor;
import cat.copernic.easytraza.service.ProveidorService;

@RestController
@RequestMapping("/proveidors")
public class ProveidorController {

    // ---------------------------- SERVEI I CONSTRUCTOR ----------------------------   
    private final ProveidorService proveidorService;

    public ProveidorController(ProveidorService proveidorService) {
        this.proveidorService = proveidorService;
    }


    // OBTENIR TOTS ELS PROVEÏDORS
    @GetMapping
    public List<Proveidor> getAllProveidors() {
        return proveidorService.getAllProveidors();
    }


    // OBTENIR PROVEÏDOR PER ID
    @GetMapping("/{id}")
    public Proveidor getProveidorById(@PathVariable Long id) {
        return proveidorService.getProveidorById(id);
    }


    // CREAR PROVEÏDOR
    @PostMapping
    public Proveidor createProveidor(@RequestBody Proveidor proveidor) {
        return proveidorService.createProveidor(proveidor);
    }


    // ACTUALITZAR PROVEÏDOR
    @PutMapping("/{id}")
    public Proveidor updateProveidor(@PathVariable Long id, @RequestBody Proveidor proveidor) {
        return proveidorService.updateProveidor(id, proveidor);
    }


    // ELIMINAR PROVEÏDOR
    @DeleteMapping("/{id}")
    public void deleteProveidor(@PathVariable Long id) {
        proveidorService.deleteProveidor(id);
    }
}