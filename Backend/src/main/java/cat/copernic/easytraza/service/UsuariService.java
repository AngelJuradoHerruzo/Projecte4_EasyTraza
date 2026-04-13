package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.repository.UsuariRepository;

@Service
@Transactional
public class UsuariService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final UsuariRepository usuariRepository;

    public UsuariService(UsuariRepository usuariRepository) {
        this.usuariRepository = usuariRepository;
    }


    // OBTENIR TOTS ELS USUARIS
    public List<Usuari> getAllUsuaris() {
        return usuariRepository.findAll();
    }


    // OBTENIR USUARI PER ID
    public Usuari getUsuariById(Long id) {
        Optional<Usuari> usuari = usuariRepository.findById(id);
        return usuari.orElse(null);
    }


    // CREAR USUARI
    public Usuari createUsuari(Usuari usuari) {
        return usuariRepository.save(usuari);
    }


    // ACTUALITZAR USUARI
    public Usuari updateUsuari(Long id, Usuari usuari) {

        Optional<Usuari> usuariOpt = usuariRepository.findById(id);

        if (usuariOpt.isPresent()) {
            Usuari usuariActual = usuariOpt.get();

            usuariActual.setNomComplet(usuari.getNomComplet());
            usuariActual.setRolUsuari(usuari.getRolUsuari());
            usuariActual.setEmail(usuari.getEmail());
            usuariActual.setPassword(usuari.getPassword());

            return usuariRepository.save(usuariActual);
        }

        return null;
    }


    // ELIMINAR USUARI
    public void deleteUsuari(Long id) {
        usuariRepository.deleteById(id);
    }
}