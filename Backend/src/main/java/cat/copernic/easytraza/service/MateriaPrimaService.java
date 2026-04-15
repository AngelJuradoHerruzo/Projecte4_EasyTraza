package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.MateriaPrima;
import cat.copernic.easytraza.repository.MateriaPrimaRepository;

@Service
@Transactional
public class MateriaPrimaService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final MateriaPrimaRepository materiaPrimaRepository;

    public MateriaPrimaService(MateriaPrimaRepository materiaPrimaRepository) {
        this.materiaPrimaRepository = materiaPrimaRepository;
    }


    // OBTENIR TOTES LES MATÈRIES PRIMERES
    public List<MateriaPrima> getAllMateriesPrimeres() {
        return materiaPrimaRepository.findAll();
    }


    // OBTENIR MATÈRIA PRIMERA PER ID
    public MateriaPrima getMateriaPrimaById(Long id) {
        Optional<MateriaPrima> materiaPrima = materiaPrimaRepository.findById(id);
        return materiaPrima.orElse(null);
    }


    // CREAR MATÈRIA PRIMERA
    public MateriaPrima createMateriaPrima(MateriaPrima materiaPrima) {
        return materiaPrimaRepository.save(materiaPrima);
    }


    // ACTUALITZAR MATÈRIA PRIMERA
    public MateriaPrima updateMateriaPrima(Long id, MateriaPrima materiaPrima) {

        Optional<MateriaPrima> materiaPrimaOpt = materiaPrimaRepository.findById(id);

        if (materiaPrimaOpt.isPresent()) {
            MateriaPrima materiaPrimaActual = materiaPrimaOpt.get();

            materiaPrimaActual.setNomMateria(materiaPrima.getNomMateria());
            materiaPrimaActual.setDescripcio(materiaPrima.getDescripcio());

            return materiaPrimaRepository.save(materiaPrimaActual);
        }

        return null;
    }


    // ELIMINAR MATÈRIA PRIMERA
    public void deleteMateriaPrima(Long id) {
        materiaPrimaRepository.deleteById(id);
    }
}