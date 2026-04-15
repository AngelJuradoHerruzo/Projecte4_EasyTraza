package cat.copernic.easytraza.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.MateriaPrima;
import cat.copernic.easytraza.service.MateriaPrimaService;

@RestController
@RequestMapping("/materies-primeres")
public class MateriaPrimaController {

    // ---------------------------- SERVEI I CONSTRUCTOR ----------------------------
    private final MateriaPrimaService materiaPrimaService;

    public MateriaPrimaController(MateriaPrimaService materiaPrimaService) {
        this.materiaPrimaService = materiaPrimaService;
    }


    // OBTENIR TOTES LES MATÈRIES PRIMERES
    @GetMapping
    public List<MateriaPrima> getAllMateriesPrimeres() {
        return materiaPrimaService.getAllMateriesPrimeres();
    }


    // OBTENIR MATÈRIA PRIMERA PER ID
    @GetMapping("/{id}")
    public MateriaPrima getMateriaPrimaById(@PathVariable Long id) {
        return materiaPrimaService.getMateriaPrimaById(id);
    }


    // CREAR MATÈRIA PRIMERA
    @PostMapping
    public MateriaPrima createMateriaPrima(@RequestBody MateriaPrima materiaPrima) {
        return materiaPrimaService.createMateriaPrima(materiaPrima);
    }


    // ACTUALITZAR MATÈRIA PRIMERA
    @PutMapping("/{id}")
    public MateriaPrima updateMateriaPrima(@PathVariable Long id, @RequestBody MateriaPrima materiaPrima) {
        return materiaPrimaService.updateMateriaPrima(id, materiaPrima);
    }


    // ELIMINAR MATÈRIA PRIMERA
    @DeleteMapping("/{id}")
    public void deleteMateriaPrima(@PathVariable Long id) {
        materiaPrimaService.deleteMateriaPrima(id);
    }
}