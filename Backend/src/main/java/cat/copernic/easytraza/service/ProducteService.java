package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.Producte;
import cat.copernic.easytraza.repository.ProducteRepository;

@Service
@Transactional
public class ProducteService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final ProducteRepository producteRepository;

    public ProducteService(ProducteRepository producteRepository) {
        this.producteRepository = producteRepository;
    }


    // OBTENIR TOTS ELS PRODUCTES
    public List<Producte> getAllProductes() {
        return producteRepository.findAll();
    }


    // OBTENIR PRODUCTE PER ID
    public Producte getProducteById(Long id) {
        Optional<Producte> producteOpt = producteRepository.findById(id);
        return producteOpt.orElse(null);
    }


    // CREAR PRODUCTE
    public Producte createProducte(Producte producte) {
        return producteRepository.save(producte);
    }


    // ACTUALITZAR PRODUCTE
    public Producte updateProducte(Long id, Producte producte) {

        Optional<Producte> producteOpt = producteRepository.findById(id);

        if (producteOpt.isPresent()) {
            Producte producteActual = producteOpt.get();

            producteActual.setDescripcio(producte.getDescripcio());

            return producteRepository.save(producteActual);
        }

        return null;
    }


    // ELIMINAR PRODUCTE
    public void deleteProducte(Long id) {
        producteRepository.deleteById(id);
    }
}