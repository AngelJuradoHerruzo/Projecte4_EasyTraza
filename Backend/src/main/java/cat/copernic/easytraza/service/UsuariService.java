package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.repository.UsuariRepository;

@Service
@Transactional
public class UsuariService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final UsuariRepository usuariRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuariService(UsuariRepository usuariRepository, PasswordEncoder passwordEncoder) {
        this.usuariRepository = usuariRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // OBTENIR TOTS ELS USUARIS
    public List<Usuari> getAllUsuaris() {
        return usuariRepository.findAll();
    }


    // OBTENIR USUARI PER ID
    public Usuari getUsuariById(Long id) {
        Optional<Usuari> usuariOpt = usuariRepository.findById(id);
        return usuariOpt.orElse(null);
    }


    // CREAR USUARI
    public Usuari createUsuari(Usuari usuari) {
        validarDadesUsuari(usuari, null);

        usuari.setNomComplet(usuari.getNomComplet().trim());
        usuari.setEmail(usuari.getEmail().trim());
        usuari.setPassword(passwordEncoder.encode(usuari.getPassword().trim()));

        return usuariRepository.save(usuari);
    }


    // ACTUALITZAR USUARI
    public Usuari updateUsuari(Long id, Usuari usuari) {

        Optional<Usuari> usuariOpt = usuariRepository.findById(id);

        if (usuariOpt.isEmpty()) {
            throw new RuntimeException("Usuari no trobat");
        }

        validarDadesUsuari(usuari, id);

        Usuari usuariActual = usuariOpt.get();

        usuariActual.setNomComplet(usuari.getNomComplet().trim());
        usuariActual.setRolUsuari(usuari.getRolUsuari());
        usuariActual.setEmail(usuari.getEmail().trim());

        // Només actualitza la contrasenya si s'ha introduït
        if (usuari.getPassword() != null && !usuari.getPassword().trim().isEmpty()) {
            usuariActual.setPassword(passwordEncoder.encode(usuari.getPassword().trim()));
        }

        return usuariRepository.save(usuariActual);
    }


    // ELIMINAR USUARI
    public void deleteUsuari(Long id) {
        usuariRepository.deleteById(id);
    }


    // VALIDAR DADES D'USUARI
    private void validarDadesUsuari(Usuari usuari, Long id) {

        if (usuari.getNomComplet() == null || usuari.getNomComplet().trim().isEmpty()) {
            throw new RuntimeException("El nom complet és obligatori");
        }

        if (usuari.getEmail() == null || usuari.getEmail().trim().isEmpty()) {
            throw new RuntimeException("El correu electrònic és obligatori");
        }

        if (usuari.getRolUsuari() == null) {
            throw new RuntimeException("El rol és obligatori");
        }

        // Si és creació, la contrasenya és obligatòria
        if (id == null && (usuari.getPassword() == null || usuari.getPassword().trim().isEmpty())) {
            throw new RuntimeException("La contrasenya és obligatòria");
        }

        // Validar email duplicat
        Optional<Usuari> usuariAmbMateixEmail = usuariRepository.findByEmail(usuari.getEmail().trim());

        if (usuariAmbMateixEmail.isPresent()) {
            // Creació: si ja existeix, error
            if (id == null) {
                throw new RuntimeException("Aquest correu electrònic ja està en ús");
            }

            // Edició: si l'email és d'un altre usuari, error
            if (!usuariAmbMateixEmail.get().getId().equals(id)) {
                throw new RuntimeException("Aquest correu electrònic ja està en ús");
            }
        }
    }
}