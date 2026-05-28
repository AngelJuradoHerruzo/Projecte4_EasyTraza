package cat.copernic.easytraza.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.TokenRecuperacioContrasenya;
import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.repository.TokenRecuperacioContrasenyaRepository;
import cat.copernic.easytraza.repository.UsuariRepository;

/**
 * SERVEI DE RECUPERACIÓ DE CONTRASENYA.
 *
 * Gestionades les sol·licituds de recuperació, la generació de tokens
 * temporals, l'enviament del correu i l'actualització segura de la contrasenya.
 *
 * @author Ángel Jurado Herruz
 */
@Service
@Transactional
public class RecuperacioContrasenyaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecuperacioContrasenyaService.class);

    private final UsuariRepository usuariRepository;
    private final TokenRecuperacioContrasenyaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final MessageSource messageSource;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.username}")
    private String correuEmissor;

    @Value("${app.recuperacio.base-url}")
    private String baseUrl;

    @Value("${app.recuperacio.token-duracio-minuts}")
    private long duracioTokenMinuts;

    public RecuperacioContrasenyaService(UsuariRepository usuariRepository,
                                         TokenRecuperacioContrasenyaRepository tokenRepository,
                                         PasswordEncoder passwordEncoder,
                                         JavaMailSender javaMailSender,
                                         MessageSource messageSource) {
        this.usuariRepository = usuariRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
    }


    /**
     * SOL·LICITUD DE RECUPERACIÓ.
     *
     * Processada la petició rebuda sense revelar si el correu existeix.
     * Quan l'usuari està registrat, generat un token temporal i enviat
     * l'enllaç que permet establir una nova contrasenya.
     *
     * @param email correu electrònic introduït per l'usuari
     */
    public void solLicitarRecuperacio(String email) {

        if (email == null || email.isBlank()) {
            return;
        }

        Optional<Usuari> usuariOpt = usuariRepository.findByEmail(email.trim().toLowerCase());

        if (usuariOpt.isEmpty()) {
            return;
        }

        Usuari usuari = usuariOpt.get();
        String token = generarToken();

        tokenRepository.deleteByUsuari(usuari);

        TokenRecuperacioContrasenya registreToken = new TokenRecuperacioContrasenya(
                calcularHash(token),
                usuari,
                LocalDateTime.now().plusMinutes(duracioTokenMinuts)
        );

        tokenRepository.save(registreToken);

        try {
            enviarCorreuRecuperacio(usuari, token);
        }
        catch (MailException ex) {
            tokenRepository.delete(registreToken);
            LOGGER.error("No s'ha pogut enviar el correu de recuperació de contrasenya.", ex);
        }
    }


    /**
     * VALIDACIÓ DE L'ENLLAÇ.
     *
     * Comprovat si el token rebut existeix, no ha estat utilitzat
     * i encara es troba dins del termini de validesa configurat.
     *
     * @param token token rebut des de l'enllaç del correu
     * @return cert si el token permet continuar amb el restabliment
     */
    public boolean tokenValid(String token) {
        return obtenirTokenValid(token).isPresent();
    }


    /**
     * RESTABLIMENT DE LA CONTRASENYA.
     *
     * Validat el token i actualitzada la contrasenya de l'usuari
     * aplicant la codificació segura utilitzada per l'aplicació.
     *
     * @param token token rebut des de l'enllaç de recuperació
     * @param novaContrasenya nova contrasenya indicada per l'usuari
     * @param confirmacio repetició de la nova contrasenya
     */
    public void restablirContrasenya(String token, String novaContrasenya, String confirmacio) {

        if (novaContrasenya == null || novaContrasenya.isBlank()) {
            throw new IllegalArgumentException(missatge("recuperacio.contrasenya.obligatoria"));
        }

        if (!novaContrasenya.equals(confirmacio)) {
            throw new IllegalArgumentException(missatge("recuperacio.contrasenya.noCoincideix"));
        }

        TokenRecuperacioContrasenya registreToken = obtenirTokenValid(token)
                .orElseThrow(() -> new IllegalArgumentException(missatge("recuperacio.enllacNoValid")));

        Usuari usuari = registreToken.getUsuari();
        usuari.setPassword(passwordEncoder.encode(novaContrasenya.trim()));

        usuariRepository.save(usuari);

        registreToken.setUtilitzat(true);
        tokenRepository.save(registreToken);
    }


    /**
     * ENVIAMENT DEL CORREU.
     *
     * Preparat i enviat el missatge que conté l'enllaç temporal
     * de recuperació en l'idioma seleccionat per l'usuari.
     *
     * @param usuari usuari destinatari del correu de recuperació
     * @param token token temporal que s'ha d'incorporar a l'enllaç
     */
    private void enviarCorreuRecuperacio(Usuari usuari, String token) {

        Locale locale = LocaleContextHolder.getLocale();
        String idioma = locale.getLanguage();
        String enllac = baseUrl + "/restablir-contrasenya?token=" + token + "&lang=" + idioma;

        SimpleMailMessage correu = new SimpleMailMessage();

        correu.setFrom(correuEmissor);
        correu.setTo(usuari.getEmail());
        correu.setSubject(missatge("recuperacio.correu.assumpte"));
        correu.setText(missatge("recuperacio.correu.cos", usuari.getNomComplet(), enllac, duracioTokenMinuts));

        javaMailSender.send(correu);
    }


    /**
     * OBTENCIÓ DEL TOKEN VÀLID.
     *
     * Localitzat el registre corresponent al token protegit rebut
     * i descartat quan ja no es troba dins del termini permès.
     *
     * @param token token rebut des de l'enllaç de recuperació
     * @return token vàlid localitzat, si existeix
     */
    private Optional<TokenRecuperacioContrasenya> obtenirTokenValid(String token) {

        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return tokenRepository.findByTokenHashAndUtilitzatFalse(calcularHash(token))
                .filter(registre -> registre.getDataCaducitat().isAfter(LocalDateTime.now()));
    }


    /**
     * GENERACIÓ DEL TOKEN.
     *
     * Generat un valor aleatori segur que es podrà utilitzar una sola vegada
     * durant el procés de recuperació de contrasenya.
     *
     * @return token temporal generat
     */
    private String generarToken() {
        byte[] dades = new byte[32];
        secureRandom.nextBytes(dades);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(dades);
    }


    /**
     * PROTECCIÓ DEL TOKEN.
     *
     * Calculat el hash SHA-256 del token perquè el valor enviat per correu
     * no quedi emmagatzemat directament a la base de dades.
     *
     * @param token token que s'ha de protegir
     * @return hash hexadecimal del token rebut
     */
    private String calcularHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder resultat = new StringBuilder();

            for (byte valor : hash) {
                resultat.append(String.format("%02x", valor));
            }

            return resultat.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No s'ha pogut protegir el token de recuperació.", ex);
        }
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
