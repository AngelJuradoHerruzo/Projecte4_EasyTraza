package cat.copernic.easytraza.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.Proveidor;
import cat.copernic.easytraza.repository.ProveidorRepository;

/**
 * SERVEI DE PROVEÏDORS.
 *
 * Gestionades les operacions de consulta, creació, modificació i eliminació dels proveïdors.
 * També aplicats els filtres, ordenacions i validacions pròpies de les dades del proveïdor.
 *
 * @author Ángel Jurado Herruz
 */
@Service
@Transactional
public class ProveidorService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final ProveidorRepository proveidorRepository;
    private final MessageSource messageSource;

    public ProveidorService(ProveidorRepository proveidorRepository, MessageSource messageSource) {
        this.proveidorRepository = proveidorRepository;
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
    public List<Proveidor> getAllProveidors() {
        return proveidorRepository.findAll();
    }


    // PREPARAR LLISTAT WEB DE PROVEÏDORS AMB FILTRES I ORDENACIÓ OPCIONALS
    public List<Proveidor> getProveidorsLlistat(String nomProveidor,
                                                String cif,
                                                String ordre,
                                                String direccio) {

        List<Proveidor> proveidors = new ArrayList<>(proveidorRepository.findAll());

        if (nomProveidor != null && !nomProveidor.isBlank()) {
            proveidors.removeIf(proveidor -> !conteText(proveidor.getNomProveidor(), nomProveidor));
        }

        if (cif != null && !cif.isBlank()) {
            proveidors.removeIf(proveidor -> !conteText(proveidor.getCif(), cif));
        }

        ordenarProveidors(proveidors, ordre, direccio);

        return proveidors;
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
    public Proveidor getProveidorById(Long id) {
        Optional<Proveidor> proveidor = proveidorRepository.findById(id);
        return proveidor.orElse(null);
    }


    /**
     * CREACIÓ DEL REGISTRE.
     *
     * Creat un nou registre o objecte a partir de les dades rebudes,
     * aplicant prèviament les comprovacions necessàries.
     *
     * @param proveidor valor de proveidor utilitzat pel mètode
     * @return registre resultant de l'operació
     */
    public Proveidor createProveidor(Proveidor proveidor) {
        validarDadesProveidor(proveidor);

        Optional<Proveidor> proveidorExistent = proveidorRepository.findByCif(proveidor.getCif());
        if (proveidorExistent.isPresent()) {
            throw new RuntimeException(missatge("service.proveidor.cifDuplicat"));
        }

        return proveidorRepository.save(proveidor);
    }


    /**
     * ACTUALITZACIÓ DEL REGISTRE.
     *
     * Actualitzat el registre indicat amb les dades rebudes, mantenint
     * les validacions pròpies del servei.
     *
     * @param id identificador utilitzat en l'operació
     * @param proveidor valor de proveidor utilitzat pel mètode
     * @return registre resultant de l'operació
     */
    public Proveidor updateProveidor(Long id, Proveidor proveidor) {

        Optional<Proveidor> proveidorOpt = proveidorRepository.findById(id);

        if (proveidorOpt.isPresent()) {

            Proveidor proveidorActual = proveidorOpt.get();

            proveidor.setId(id);

            // El CIF o DNI no es pot modificar un cop creat el proveïdor
            proveidor.setCif(proveidorActual.getCif());

            validarDadesProveidor(proveidor);

            proveidorActual.setNomProveidor(proveidor.getNomProveidor().trim());
            proveidorActual.setAdreca(proveidor.getAdreca().trim());
            proveidorActual.setDescripcio(proveidor.getDescripcio() != null ? proveidor.getDescripcio().trim() : null);

            return proveidorRepository.save(proveidorActual);
        }

        return null;
    }


    /**
     * ELIMINACIÓ DEL REGISTRE.
     *
     * Eliminat el registre identificat quan el servei permet completar
     * l'operació sol·licitada.
     *
     * @param id identificador utilitzat en l'operació
     */
    public void deleteProveidor(Long id) {
        proveidorRepository.deleteById(id);
    }


    /**
     * VALIDACIÓ DE DADES.
     *
     * Comprovades les dades rebudes abans de continuar amb el procés,
     * llençant un error quan alguna condició no és correcta.
     *
     * @param proveidor valor de proveidor utilitzat pel mètode
     */
    private void validarDadesProveidor(Proveidor proveidor) {

        if (proveidor.getCif() != null) {
            proveidor.setCif(proveidor.getCif().trim().toUpperCase());
        }

        if (proveidor.getNomProveidor() != null) {
            proveidor.setNomProveidor(proveidor.getNomProveidor().trim());
        }

        if (proveidor.getAdreca() != null) {
            proveidor.setAdreca(proveidor.getAdreca().trim());
        }

        if (proveidor.getDescripcio() != null) {
            proveidor.setDescripcio(proveidor.getDescripcio().trim());
        }

        if (proveidor.getCif() == null || proveidor.getCif().isBlank()) {
            throw new RuntimeException(missatge("service.proveidor.cifObligatori"));
        }

        if (!esCifNifValid(proveidor.getCif())) {
            throw new RuntimeException(missatge("service.proveidor.cifIncorrecte"));
        }

        if (proveidor.getNomProveidor() == null || proveidor.getNomProveidor().isBlank()) {
            throw new RuntimeException(missatge("service.proveidor.nomObligatori"));
        }

        if (proveidor.getAdreca() == null || proveidor.getAdreca().isBlank()) {
            throw new RuntimeException(missatge("service.proveidor.adrecaObligatoria"));
        }

        if (proveidor.getDescripcio() != null && proveidor.getDescripcio().length() > 50) {
            throw new RuntimeException(missatge("service.proveidor.descripcioMax"));
        }
    }


    /**
     * ORDENACIÓ DE DADES.
     *
     * Ordenada la llista rebuda segons el criteri indicat o segons
     * l'ordre propi del servei.
     *
     * @param proveidors valor de proveidors utilitzat pel mètode
     * @param ordre camp utilitzat per ordenar les dades
     * @param direccio direcció de l'ordenació
     */
    private void ordenarProveidors(List<Proveidor> proveidors, String ordre, String direccio) {

        String campOrdre = ordre != null && !ordre.isBlank() ? ordre : "nomProveidor";

        Comparator<Proveidor> comparator;

        switch (campOrdre) {
            case "cif":
                comparator = Comparator.comparing(
                    proveidor -> valorText(proveidor.getCif()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "adreca":
                comparator = Comparator.comparing(
                    proveidor -> valorText(proveidor.getAdreca()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "descripcio":
                comparator = Comparator.comparing(
                    proveidor -> valorText(proveidor.getDescripcio()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "nomProveidor":
            default:
                comparator = Comparator.comparing(
                    proveidor -> valorText(proveidor.getNomProveidor()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;
        }

        if ("desc".equalsIgnoreCase(direccio)) {
            comparator = comparator.reversed();
        }

        proveidors.sort(comparator.thenComparing(Proveidor::getId));
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
     * COMPROVACIÓ DE CONDICIÓ.
     *
     * Comprovada la condició indicada a partir dels valors rebuts
     * i retornat el resultat de la verificació.
     *
     * @param valor valor que s'ha de processar
     * @return cert si es compleix la condició indicada
     */
    private boolean esCifNifValid(String valor) {

        // Si és null o buit → no vàlid
        if (valor == null || valor.isBlank()) {
            return false;
        }

        valor = valor.trim().toUpperCase(); // Eliminem espais i passem a majúscules per evitar errors de format

        // -------------------- DNI --------------------
        if (valor.matches("\\d{8}[A-Z]")) { // Comprovem si compleix el patró de DNI

            String lletres = "TRWAGMYFPDXBNJZSQVHLCKE"; // Taula de lletres del DNI

            // Extreiem la part numèrica i calculem la lletra amb el mòdul 23
            int numero = Integer.parseInt(valor.substring(0, 8));
            char lletraCorrecta = lletres.charAt(numero % 23);

            return valor.charAt(8) == lletraCorrecta; // Comparem la lletra introduïda amb la calculada
        }

        // -------------------- CIF --------------------
        if (valor.matches("[ABCDEFGHJKLMNPQRSUVW]\\d{7}[0-9A-J]")) { // Comprovem si compleix el patró de CIF

            // Primera lletra del CIF
            char lletraInicial = valor.charAt(0);

            // Part numèrica central
            String numeros = valor.substring(1, 8);

            // Dígit o lletra de control final
            char control = valor.charAt(8);

            int sumaParells = 0;    // Suma de posicions parells
            int sumaSenars = 0;     // Suma de posicions senars

            // Recorrem els 7 dígits
            for (int i = 0; i < numeros.length(); i++) {

                int digit = Character.getNumericValue(numeros.charAt(i));

                // Posicions senars (índex parell: 0,2,4,6)
                if (i % 2 == 0) {

                    // Multipliquem per 2
                    int dobleDigit = digit * 2;

                    // Sumem les xifres del resultat (ex: 12 → 1 + 2)
                    sumaSenars += (dobleDigit / 10) + (dobleDigit % 10);

                } else {
                    // Posicions parells (índex imparell: 1,3,5)
                    sumaParells += digit;
                }
            }

            // Suma total
            int sumaTotal = sumaParells + sumaSenars;

            // Càlcul del dígit de control
            int digitControl = (10 - (sumaTotal % 10)) % 10;

            // Lletra equivalent al dígit de control
            char lletraControl = "JABCDEFGHI".charAt(digitControl);

            // A, B, E, H → només número
            if ("ABEH".indexOf(lletraInicial) >= 0) {
                return control == Character.forDigit(digitControl, 10);
            }

            // K, P, Q, S → només lletra
            if ("KPQS".indexOf(lletraInicial) >= 0) {
                return control == lletraControl;
            }

            // La resta → pot ser número o lletra
            return control == Character.forDigit(digitControl, 10) || control == lletraControl;
        }

        // Si no encaixa ni amb DNI ni amb CIF → no vàlid
        return false;
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
