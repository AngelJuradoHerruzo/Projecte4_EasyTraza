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

    
    // PREPARAR LLISTAT WEB D'USUARIS AMB FILTRES OPCIONALS
    public List<Usuari> getUsuarisLlistat(String dni, String nomComplet, String email, Long usuariId) {

        List<Usuari> usuaris = new java.util.ArrayList<>(usuariRepository.findAll());

        usuaris.removeIf(usuari -> usuariId != null && usuari.getId().equals(usuariId));

        if (dni != null && !dni.isBlank()) {
            usuaris.removeIf(usuari -> !conteText(usuari.getDni(), dni));
        }

        if (nomComplet != null && !nomComplet.isBlank()) {
            usuaris.removeIf(usuari -> !conteText(usuari.getNomComplet(), nomComplet));
        }

        if (email != null && !email.isBlank()) {
            usuaris.removeIf(usuari -> !conteText(usuari.getEmail(), email));
        }

        return usuaris;
    }


    // CREAR USUARI
    public Usuari createUsuari(Usuari usuari) {
        validarDadesUsuari(usuari, null);

        usuari.setDni(usuari.getDni().trim().toUpperCase());
        usuari.setNomComplet(usuari.getNomComplet().trim());
        usuari.setEmail(usuari.getEmail().trim().toLowerCase());
        usuari.setPassword(passwordEncoder.encode(usuari.getPassword().trim()));

        return usuariRepository.save(usuari);
    }


    // ACTUALITZAR USUARI
    public Usuari updateUsuari(Long id, Usuari usuari) {

        Optional<Usuari> usuariOpt = usuariRepository.findById(id);

        if (usuariOpt.isEmpty()) {
            throw new RuntimeException("Usuari no trobat");
        }

        Usuari usuariActual = usuariOpt.get();

        usuari.setId(id);

        // El DNI no es pot modificar un cop creat l'usuari
        usuari.setDni(usuariActual.getDni());

        validarDadesUsuari(usuari, id);

        usuariActual.setNomComplet(usuari.getNomComplet().trim());
        usuariActual.setRolUsuari(usuari.getRolUsuari());
        usuariActual.setEmail(usuari.getEmail().trim().toLowerCase());

        // Només actualitza la contrasenya si s'ha introduït
        if (usuari.getPassword() != null && !usuari.getPassword().trim().isEmpty()) {
            usuariActual.setPassword(passwordEncoder.encode(usuari.getPassword().trim()));
        }

        return usuariRepository.save(usuariActual);
    }


    // ACTUALITZAR PERFIL D'USUARI
    public Usuari updatePerfilUsuari(Long id, Usuari usuari) {

        Optional<Usuari> usuariOpt = usuariRepository.findById(id);

        if (usuariOpt.isEmpty()) {
            throw new RuntimeException("Usuari no trobat");
        }

        Usuari usuariActual = usuariOpt.get();

        usuari.setId(id);

        // El DNI i el rol no es poden modificar des del perfil
        usuari.setDni(usuariActual.getDni());
        usuari.setRolUsuari(usuariActual.getRolUsuari());

        validarDadesUsuari(usuari, id);

        usuariActual.setNomComplet(usuari.getNomComplet().trim());
        usuariActual.setEmail(usuari.getEmail().trim().toLowerCase());

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

        if (usuari.getDni() != null) {
            usuari.setDni(usuari.getDni().trim().toUpperCase());
        }

        if (usuari.getNomComplet() != null) {
            usuari.setNomComplet(usuari.getNomComplet().trim());
        }

        if (usuari.getEmail() != null) {
            usuari.setEmail(usuari.getEmail().trim().toLowerCase());
        }

        if (usuari.getDni() == null || usuari.getDni().isBlank()) {
            throw new RuntimeException("El DNI és obligatori");
        }

        if (!usuari.getDni().matches("\\d{8}[A-Z]")) {
            throw new RuntimeException("El DNI ha de tenir el format 12345678A");
        }

        Optional<Usuari> usuariAmbMateixDni = usuariRepository.findByDni(usuari.getDni());

        if (usuariAmbMateixDni.isPresent()
                && (id == null || !usuariAmbMateixDni.get().getId().equals(id))) {
            throw new RuntimeException("Aquest DNI ja està en ús");
        }

        if (usuari.getNomComplet() == null || usuari.getNomComplet().isBlank()) {
            throw new RuntimeException("El nom complet és obligatori");
        }

        if (!usuari.getNomComplet().matches("^[A-Za-zÀ-ÿ]+(?:\\s+[A-Za-zÀ-ÿ]+)+$")) {
            throw new RuntimeException("El nom complet ha d'incloure nom i almenys un cognom");
        }

        if (usuari.getEmail() == null || usuari.getEmail().isBlank()) {
            throw new RuntimeException("El correu electrònic és obligatori");
        }

        if (!usuari.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException("El format del correu incorrecte");
        }

        if (usuari.getRolUsuari() == null) {
            throw new RuntimeException("El rol és obligatori");
        }

        if (id == null && (usuari.getPassword() == null || usuari.getPassword().trim().isEmpty())) {
            throw new RuntimeException("La contrasenya és obligatòria");
        }

        Optional<Usuari> usuariAmbMateixEmail = usuariRepository.findByEmail(usuari.getEmail());

        if (usuariAmbMateixEmail.isPresent()
                && (id == null || !usuariAmbMateixEmail.get().getId().equals(id))) {
            throw new RuntimeException("Correu electrònic ja està en ús");
        }
    }


    // COMPROVAR SI UN TEXT CONTÉ UN FILTRE IGNORANT MAJÚSCULES I MINÚSCULES
    private boolean conteText(String valor, String filtre) {

        if (filtre == null || filtre.isBlank()) {
            return true;
        }

        if (valor == null) {
            return false;
        }

        return valor.toLowerCase().contains(filtre.trim().toLowerCase());
    }
}