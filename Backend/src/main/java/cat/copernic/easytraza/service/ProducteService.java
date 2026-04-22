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
        Optional<Producte> producte = producteRepository.findById(id);
        return producte.orElse(null);
    }


    // CREAR PRODUCTE
    public Producte createProducte(Producte producte) {
        validarDadesProducte(producte);
        return producteRepository.save(producte);
    }


    // ACTUALITZAR PRODUCTE
    public Producte updateProducte(Long id, Producte producte) {

        Optional<Producte> producteOpt = producteRepository.findById(id);

        if (producteOpt.isPresent()) {
            validarDadesProducte(producte);

            Producte producteActual = producteOpt.get();

            producteActual.setNomProducte(producte.getNomProducte().trim());
            producteActual.setDescripcio(
                producte.getDescripcio() != null ? producte.getDescripcio().trim() : null
            );

            return producteRepository.save(producteActual);
        }

        return null;
    }


    // ELIMINAR PRODUCTE
    public void deleteProducte(Long id) {
        producteRepository.deleteById(id);
    }


    // VALIDAR DADES DEL PRODUCTE
    private void validarDadesProducte(Producte producte) {

        if (producte.getNomProducte() != null) {
            producte.setNomProducte(producte.getNomProducte().trim());
        }

        if (producte.getDescripcio() != null) {
            producte.setDescripcio(producte.getDescripcio().trim());
        }

        if (producte.getNomProducte() == null || producte.getNomProducte().isBlank()) {
            throw new RuntimeException("El nom del producte és obligatori.");
        }

        if (producte.getDescripcio() != null && producte.getDescripcio().length() > 50) {
            throw new RuntimeException("La descripció no pot superar els 50 caràcters.");
        }
    }
}