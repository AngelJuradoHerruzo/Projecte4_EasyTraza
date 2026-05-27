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

/**
 * SERVEI D'USUARIS.
 *
 * Gestionades les operacions de consulta, creació, modificació i eliminació dels usuaris.
 * També controlades les validacions de dades i la gestió de l'avatar associat.
 *
 * @author Ángel Jurado Herruz
 */
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


    /**
     * OBTENCIÓ DEL LLISTAT.
     *
     * Obtingut el conjunt de dades sol·licitat pel servei, aplicant
     * els filtres o l'ordenació corresponents quan és necessari.
     *
     * @return llista de resultats obtinguda
     */
    public List<Usuari> getAllUsuaris() {
        return usuariRepository.findAll();
    }


    /**
     * OBTENCIÓ DE DADES.
     *
     * Obtinguda la informació sol·licitada a partir de les dades disponibles
     * o dels paràmetres rebuts pel mètode.
     *
     * @param id identificador utilitzat en l'operació
     * @return resultat obtingut pel mètode
     */
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


    /**
     * CREACIÓ DEL REGISTRE.
     *
     * Creat un nou registre o objecte a partir de les dades rebudes,
     * aplicant prèviament les comprovacions necessàries.
     *
     * @param usuari valor de usuari utilitzat pel mètode
     * @param avatarFile fitxer rebut per al procés
     * @return registre resultant de l'operació
     */
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


    /**
     * ACTUALITZACIÓ DEL REGISTRE.
     *
     * Actualitzat el registre indicat amb les dades rebudes, mantenint
     * les validacions pròpies del servei.
     *
     * @param id identificador utilitzat en l'operació
     * @param usuari valor de usuari utilitzat pel mètode
     * @param avatarFile fitxer rebut per al procés
     * @return registre resultant de l'operació
     */
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


    /**
     * ACTUALITZACIÓ DEL REGISTRE.
     *
     * Actualitzat el registre indicat amb les dades rebudes, mantenint
     * les validacions pròpies del servei.
     *
     * @param id identificador utilitzat en l'operació
     * @param usuari valor de usuari utilitzat pel mètode
     * @param avatarFile fitxer rebut per al procés
     * @return registre resultant de l'operació
     */
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


    /**
     * ELIMINACIÓ DEL REGISTRE.
     *
     * Eliminat el registre identificat quan el servei permet completar
     * l'operació sol·licitada.
     *
     * @param id identificador utilitzat en l'operació
     */
    public void deleteUsuari(Long id) {
        usuariRepository.deleteById(id);
    }


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param usuari valor de usuari utilitzat pel mètode
     * @param id identificador utilitzat en l'operació
     */
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


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param avatarFile fitxer rebut per al procés
     * @param obligatori valor de obligatori utilitzat pel mètode
     */
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


    /**
     * GUARDAT DE DADES.
     *
     * Gestionat el fitxer o la dada associada al registre actual
     * segons l'operació requerida.
     *
     * @param usuari valor de usuari utilitzat pel mètode
     * @param avatarFile fitxer rebut per al procés
     */
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


    /**
     * ORDENACIÓ DE DADES.
     *
     * Ordenada la llista rebuda segons el criteri indicat o segons
     * l'ordre propi del servei.
     *
     * @param usuaris valor de usuaris utilitzat pel mètode
     * @param ordre camp utilitzat per ordenar les dades
     * @param direccio direcció de l'ordenació
     */
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


    /**
     * NORMALITZACIÓ DEL VALOR.
     *
     * Executada l'operació pròpia del servei utilitzant les dades rebudes
     * i retornant el resultat corresponent quan aplica.
     *
     * @param valor valor que s'ha de processar
     * @return text obtingut pel mètode
     */
    private String valorText(String valor) {
        return valor != null ? valor : "";
    }


    /**
     * COMPROVACIÓ DE CONTINGUT.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param valor valor que s'ha de processar
     * @param filtre valor utilitzat per filtrar les dades
     * @return cert si es compleix la condició indicada
     */
    private boolean conteText(String valor, String filtre) {

        if (filtre == null || filtre.isBlank()) {
            return true;
        }

        if (valor == null) {
            return false;
        }

        return valor.toLowerCase().contains(filtre.trim().toLowerCase());
    }


    /**
     * OBTENCIÓ DEL MISSATGE.
     *
     * Obtingut el text internacionalitzat corresponent al codi rebut
     * i als arguments indicats.
     *
     * @param codi codi del missatge que s'ha d'obtenir
     * @param arguments arguments aplicats al missatge
     * @return text obtingut pel mètode
     */
    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}