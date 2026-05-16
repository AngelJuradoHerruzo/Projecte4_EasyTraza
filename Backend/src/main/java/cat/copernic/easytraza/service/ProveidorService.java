package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.Proveidor;
import cat.copernic.easytraza.repository.ProveidorRepository;

@Service
@Transactional
public class ProveidorService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final ProveidorRepository proveidorRepository;

    public ProveidorService(ProveidorRepository proveidorRepository) {
        this.proveidorRepository = proveidorRepository;
    }


    // OBTENIR TOTS ELS PROVEÏDORS
    public List<Proveidor> getAllProveidors() {
        return proveidorRepository.findAll();
    }


    // OBTENIR PROVEÏDOR PER ID
    public Proveidor getProveidorById(Long id) {
        Optional<Proveidor> proveidor = proveidorRepository.findById(id);
        return proveidor.orElse(null);
    }


    // CREAR PROVEÏDOR
    public Proveidor createProveidor(Proveidor proveidor) {
        validarDadesProveidor(proveidor);

        Optional<Proveidor> proveidorExistent = proveidorRepository.findByCif(proveidor.getCif());
        if (proveidorExistent.isPresent()) {
            throw new RuntimeException("Ja existeix un proveïdor amb aquest CIF o DNI.");
        }

        return proveidorRepository.save(proveidor);
    }


    // ACTUALITZAR PROVEÏDOR
    public Proveidor updateProveidor(Long id, Proveidor proveidor) {

        Optional<Proveidor> proveidorOpt = proveidorRepository.findById(id);

        if (proveidorOpt.isPresent()) {

            Proveidor proveidorActual = proveidorOpt.get();

            proveidor.setId(id);

            // El CIF o DNI no es pot modificar un cop creat el proveïdor
            proveidor.setCif(proveidorActual.getCif());

            validarDadesProveidor(proveidor);

            proveidorActual.setNomProveidor(proveidor.getNomProveidor().trim());
            proveidorActual.setAdreca(proveidor.getAdreca() != null ? proveidor.getAdreca().trim() : null);
            proveidorActual.setDescripcio(proveidor.getDescripcio() != null ? proveidor.getDescripcio().trim() : null);

            return proveidorRepository.save(proveidorActual);
        }

        return null;
    }


    // ELIMINAR PROVEÏDOR
    public void deleteProveidor(Long id) {
        proveidorRepository.deleteById(id);
    }


    // VALIDAR DADES DEL PROVEÏDOR
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
            throw new RuntimeException("El CIF o DNI és obligatori.");
        }

        if (!esCifNifValid(proveidor.getCif())) {
            throw new RuntimeException("El CIF o DNI no és correcte.");
        }

        if (proveidor.getNomProveidor() == null || proveidor.getNomProveidor().isBlank()) {
            throw new RuntimeException("El nom del proveïdor és obligatori.");
        }

        if (proveidor.getDescripcio() != null && proveidor.getDescripcio().length() > 50) {
            throw new RuntimeException("La descripció no pot superar els 50 caràcters.");
        }
    }


    // VERIFICAR DNI: LA LLETRA HA DE COINCIDIR AMB EL NÚMERO MÒDUL 23
    // VERIFICAR CIF: EL DÍGIT O LLETRA DE CONTROL HA DE COINCIDIR AMB EL CARÀCTER FINAL SEGONS EL TIPUS D’ENTITAT
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

        // Si no compleix cap dels formats anteriors → no vàlid
        return false;
    }
}