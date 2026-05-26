package cat.copernic.easytraza.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.repository.UsuariRepository;

@Service
@Transactional
public class UsuariService {    

    private static final Logger LOGGER = LoggerFactory.getLogger(UsuariService.class);

    // ---------------------------- CONSTANTS ----------------------------
    private static final long MIDA_MAXIMA_AVATAR = 5 * 1024 * 1024;

    private static final Set<String> TIPUS_AVATAR_PERMESOS = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );


    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final UsuariRepository usuariRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    public UsuariService(UsuariRepository usuariRepository, PasswordEncoder passwordEncoder, MessageSource messageSource) {
        this.usuariRepository = usuariRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
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


    // PREPARAR LLISTAT WEB D'USUARIS AMB FILTRES I ORDENACIÓ OPCIONALS
    public List<Usuari> getUsuarisLlistat(String dni,
                                           String nomComplet,
                                           String email,
                                           String ordre,
                                           String direccio,
                                           Long usuariId) {

        List<Usuari> usuaris = new ArrayList<>(usuariRepository.findAll());

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

        ordenarUsuaris(usuaris, ordre, direccio);

        return usuaris;
    }


    // CREAR USUARI
    public Usuari createUsuari(Usuari usuari, MultipartFile avatarFile) {

        validarDadesUsuari(usuari, null);
        validarAvatar(avatarFile, true);

        usuari.setDni(usuari.getDni().trim().toUpperCase());
        usuari.setNomComplet(usuari.getNomComplet().trim());
        usuari.setEmail(usuari.getEmail().trim().toLowerCase());
        usuari.setPassword(passwordEncoder.encode(usuari.getPassword().trim()));

        guardarAvatar(usuari, avatarFile);

        return usuariRepository.save(usuari);
    }


    // ACTUALITZAR USUARI
    public Usuari updateUsuari(Long id, Usuari usuari, MultipartFile avatarFile) {

        Optional<Usuari> usuariOpt = usuariRepository.findById(id);

        if (usuariOpt.isEmpty()) {
            throw new RuntimeException(missatge("service.usuari.noTrobat"));
        }

        Usuari usuariActual = usuariOpt.get();

        usuari.setId(id);

        // El DNI no es pot modificar un cop creat l'usuari
        usuari.setDni(usuariActual.getDni());

        validarDadesUsuari(usuari, id);
        validarAvatar(avatarFile, usuariActual.getAvatar() == null || usuariActual.getAvatar().length == 0);

        usuariActual.setNomComplet(usuari.getNomComplet().trim());
        usuariActual.setRolUsuari(usuari.getRolUsuari());
        usuariActual.setEmail(usuari.getEmail().trim().toLowerCase());

        // Només actualitza la contrasenya si s'ha introduït
        if (usuari.getPassword() != null && !usuari.getPassword().trim().isEmpty()) {
            usuariActual.setPassword(passwordEncoder.encode(usuari.getPassword().trim()));
        }

        // Només actualitza l'avatar si s'ha seleccionat un fitxer nou
        if (avatarFile != null && !avatarFile.isEmpty()) {
            guardarAvatar(usuariActual, avatarFile);
        }

        return usuariRepository.save(usuariActual);
    }


    // ACTUALITZAR PERFIL D'USUARI
    public Usuari updatePerfilUsuari(Long id, Usuari usuari, MultipartFile avatarFile) {

        Optional<Usuari> usuariOpt = usuariRepository.findById(id);

        if (usuariOpt.isEmpty()) {
            throw new RuntimeException(missatge("service.usuari.noTrobat"));
        }

        Usuari usuariActual = usuariOpt.get();

        usuari.setId(id);

        // El DNI i el rol no es poden modificar des del perfil
        usuari.setDni(usuariActual.getDni());
        usuari.setRolUsuari(usuariActual.getRolUsuari());

        validarDadesUsuari(usuari, id);
        validarAvatar(avatarFile, usuariActual.getAvatar() == null || usuariActual.getAvatar().length == 0);

        usuariActual.setNomComplet(usuari.getNomComplet().trim());
        usuariActual.setEmail(usuari.getEmail().trim().toLowerCase());

        // Només actualitza la contrasenya si s'ha introduït
        if (usuari.getPassword() != null && !usuari.getPassword().trim().isEmpty()) {
            usuariActual.setPassword(passwordEncoder.encode(usuari.getPassword().trim()));
        }

        // Només actualitza l'avatar si s'ha seleccionat un fitxer nou
        if (avatarFile != null && !avatarFile.isEmpty()) {
            guardarAvatar(usuariActual, avatarFile);
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
            throw new RuntimeException(missatge("service.usuari.dniObligatori"));
        }

        if (!usuari.getDni().matches("\\d{8}[A-Z]")) {
            throw new RuntimeException(missatge("service.usuari.dniFormat"));
        }

        Optional<Usuari> usuariAmbMateixDni = usuariRepository.findByDni(usuari.getDni());

        if (usuariAmbMateixDni.isPresent()
                && (id == null || !usuariAmbMateixDni.get().getId().equals(id))) {
            throw new RuntimeException(missatge("service.usuari.dniUs"));
        }

        if (usuari.getNomComplet() == null || usuari.getNomComplet().isBlank()) {
            throw new RuntimeException(missatge("service.usuari.nomObligatori"));
        }

        if (!usuari.getNomComplet().matches("^[A-Za-zÀ-ÿ]+(?:\\s+[A-Za-zÀ-ÿ]+)+$")) {
            throw new RuntimeException(missatge("service.usuari.nomCognom"));
        }

        if (usuari.getEmail() == null || usuari.getEmail().isBlank()) {
            throw new RuntimeException(missatge("service.usuari.emailObligatori"));
        }

        if (!usuari.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException(missatge("service.usuari.emailFormat"));
        }

        if (usuari.getRolUsuari() == null) {
            throw new RuntimeException(missatge("service.usuari.rolObligatori"));
        }

        if (id == null && (usuari.getPassword() == null || usuari.getPassword().trim().isEmpty())) {
            throw new RuntimeException(missatge("service.usuari.passwordObligatoria"));
        }

        Optional<Usuari> usuariAmbMateixEmail = usuariRepository.findByEmail(usuari.getEmail());

        if (usuariAmbMateixEmail.isPresent()
                && (id == null || !usuariAmbMateixEmail.get().getId().equals(id))) {
            throw new RuntimeException(missatge("service.usuari.emailUs"));
        }
    }


    // VALIDAR FITXER D'AVATAR
    private void validarAvatar(MultipartFile avatarFile, boolean obligatori) {

        boolean avatarBuit = avatarFile == null || avatarFile.isEmpty();

        if (avatarBuit) {
            if (obligatori) {
                throw new RuntimeException(missatge("service.usuari.avatarObligatori"));
            }

            return;
        }

        if (avatarFile.getSize() > MIDA_MAXIMA_AVATAR) {
            throw new RuntimeException(missatge("service.usuari.avatarMax"));
        }

        String tipusContingut = avatarFile.getContentType();

        if (tipusContingut == null
                || !TIPUS_AVATAR_PERMESOS.contains(tipusContingut.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException(missatge("service.usuari.avatarTipus"));
        }
    }


    // GUARDAR FITXER D'AVATAR A L'USUARI
    private void guardarAvatar(Usuari usuari, MultipartFile avatarFile) {
        try {
            usuari.setAvatar(avatarFile.getBytes());
            usuari.setAvatarTipusContingut(avatarFile.getContentType());
            usuari.setAvatarNomFitxer(avatarFile.getOriginalFilename());
        }
        catch (IOException e) {
            LOGGER.error("No s'ha pogut guardar la foto o avatar.", e);
            throw new RuntimeException(missatge("service.usuari.avatarGuardar"));
        }
    }


    // ORDENAR USUARIS PEL CAMP SELECCIONAT
    private void ordenarUsuaris(List<Usuari> usuaris, String ordre, String direccio) {

        String campOrdre = ordre != null && !ordre.isBlank() ? ordre : "nomComplet";

        Comparator<Usuari> comparator;

        switch (campOrdre) {
            case "id":
                comparator = Comparator.comparing(Usuari::getId);
                break;

            case "dni":
                comparator = Comparator.comparing(
                    usuari -> valorText(usuari.getDni()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "rolUsuari":
                comparator = Comparator.comparing(
                    usuari -> usuari.getRolUsuari() != null ? usuari.getRolUsuari().name() : "",
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "email":
                comparator = Comparator.comparing(
                    usuari -> valorText(usuari.getEmail()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "nomComplet":
            default:
                comparator = Comparator.comparing(
                    usuari -> valorText(usuari.getNomComplet()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;
        }

        if ("desc".equalsIgnoreCase(direccio)) {
            comparator = comparator.reversed();
        }

        usuaris.sort(comparator.thenComparing(Usuari::getId));
    }


    // RETORNAR TEXT SEGUR PER A L'ORDENACIÓ
    private String valorText(String valor) {
        return valor != null ? valor : "";
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

    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}