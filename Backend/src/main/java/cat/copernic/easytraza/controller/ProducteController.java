package cat.copernic.easytraza.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.Producte;
import cat.copernic.easytraza.service.ProducteService;

@RestController
@RequestMapping("/productes")
public class ProducteController {

    // ---------------------------- SERVEI I CONSTRUCTOR ----------------------------
    private final ProducteService producteService;

    public ProducteController(ProducteService producteService) {
        this.producteService = producteService;
    }


    // OBTENIR TOTS ELS PRODUCTES
    @GetMapping
    public List<Producte> getAllProductes() {
        return producteService.getAllProductes();
    }


    // OBTENIR PRODUCTE PER ID
    @GetMapping("/{id}")
    public Producte getProducteById(@PathVariable Long id) {
        return producteService.getProducteById(id);
    }


    // CREAR PRODUCTE
    @PostMapping
    public Producte createProducte(@RequestBody Producte producte) {
        return producteService.createProducte(producte);
    }


    // ACTUALITZAR PRODUCTE
    @PutMapping("/{id}")
    public Producte updateProducte(@PathVariable Long id, @RequestBody Producte producte) {
        return producteService.updateProducte(id, producte);
    }


    // ELIMINAR PRODUCTE
    @DeleteMapping("/{id}")
    public void deleteProducte(@PathVariable Long id) {
        producteService.deleteProducte(id);
    }
}