package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.Client;
import cat.copernic.easytraza.repository.AlbaraClientRepository;
import cat.copernic.easytraza.repository.ClientRepository;

@Service
@Transactional
public class ClientService {

    // ---------------------------- REPOSITORIS I CONSTRUCTOR ----------------------------
    private final ClientRepository clientRepository;
    private final AlbaraClientRepository albaraClientRepository;

    public ClientService(ClientRepository clientRepository,
                         AlbaraClientRepository albaraClientRepository) {
        this.clientRepository = clientRepository;
        this.albaraClientRepository = albaraClientRepository;
    }


    // OBTENIR TOTS ELS CLIENTS ORDENATS PER NOM
    public List<Client> getAllClients() {

        List<Client> clients = clientRepository.findAllByOrderByNomCompletAsc();

        for (Client client : clients) {
            client.setTeAlbarans(albaraClientRepository.existsByClientId(client.getId()));
        }

        return clients;
    }


    // OBTENIR CLIENT PER ID
    public Client getClientById(Long id) {
        Optional<Client> client = clientRepository.findById(id);
        return client.orElse(null);
    }


    // CREAR CLIENT
    public Client createClient(Client client) {
        validarDadesClient(client);
        return clientRepository.save(client);
    }


    // ACTUALITZAR CLIENT
    public Client updateClient(Long id, Client client) {

        Optional<Client> clientOpt = clientRepository.findById(id);

        if (clientOpt.isPresent()) {

            Client clientActual = clientOpt.get();

            client.setId(id);

            // El CIF / DNI no es pot modificar un cop creat el client
            client.setCif(clientActual.getCif());

            validarDadesClient(client);

            clientActual.setNomComplet(client.getNomComplet().trim());
            clientActual.setTelefon(client.getTelefon().trim());
            clientActual.setEmail(client.getEmail() != null ? client.getEmail().trim().toLowerCase() : null);
            clientActual.setAdreca(client.getAdreca().trim());
            clientActual.setObservacions(client.getObservacions() != null ? client.getObservacions().trim() : null);

            return clientRepository.save(clientActual);
        }

        return null;
    }


    // ELIMINAR CLIENT
    public void deleteClient(Long id) {

        if (albaraClientRepository.existsByClientId(id)) {
            throw new RuntimeException("No es pot eliminar aquest client perquè té albarans de client associats.");
        }

        clientRepository.deleteById(id);
    }


    // VALIDAR DADES DEL CLIENT
    private void validarDadesClient(Client client) {

        if (client.getCif() != null) {
            client.setCif(client.getCif().trim().toUpperCase());
        }

        if (client.getNomComplet() != null) {
            client.setNomComplet(client.getNomComplet().trim());
        }

        if (client.getTelefon() != null) {
            client.setTelefon(client.getTelefon().trim());
        }

        if (client.getEmail() != null) {
            client.setEmail(client.getEmail().trim().toLowerCase());

            if (client.getEmail().isBlank()) {
                client.setEmail(null);
            }
        }

        if (client.getAdreca() != null) {
            client.setAdreca(client.getAdreca().trim());
        }

        if (client.getObservacions() != null) {
            client.setObservacions(client.getObservacions().trim());
        }

        if (client.getCif() == null || client.getCif().isBlank()) {
            throw new RuntimeException("El CIF o DNI és obligatori.");
        }

        if (!esCifNifValid(client.getCif())) {
            throw new RuntimeException("El CIF o DNI no és correcte.");
        }

        Optional<Client> clientCifExistent = clientRepository.findByCif(client.getCif());

        if (clientCifExistent.isPresent()
                && !clientCifExistent.get().getId().equals(client.getId())) {
            throw new RuntimeException("Ja existeix un client amb aquest CIF o DNI.");
        }

        if (client.getNomComplet() == null || client.getNomComplet().isBlank()) {
            throw new RuntimeException("El nom complet del client és obligatori.");
        }

        if (!client.getNomComplet().matches("^[A-Za-zÀ-ÿ]+(\\s+[A-Za-zÀ-ÿ]+)+$")) {
            throw new RuntimeException("El nom complet ha d'incloure nom i almenys un cognom.");
        }

        if (client.getTelefon() == null || client.getTelefon().isBlank()) {
            throw new RuntimeException("El telèfon és obligatori.");
        }

        if (!client.getTelefon().matches("\\d{3}\\s\\d{2}\\s\\d{2}\\s\\d{2}")) {
            throw new RuntimeException("El telèfon ha de tenir el format XXX XX XX XX.");
        }

        Optional<Client> clientTelefonExistent = clientRepository.findByTelefon(client.getTelefon());

        if (clientTelefonExistent.isPresent()
                && !clientTelefonExistent.get().getId().equals(client.getId())) {
            throw new RuntimeException("Ja existeix un client amb aquest telèfon.");
        }

        if (client.getEmail() != null
                && !client.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException("El format del correu electrònic no és correcte.");
        }

        if (client.getEmail() != null) {
            Optional<Client> clientEmailExistent = clientRepository.findByEmail(client.getEmail());

            if (clientEmailExistent.isPresent()
                    && !clientEmailExistent.get().getId().equals(client.getId())) {
                throw new RuntimeException("Ja existeix un client amb aquest correu electrònic.");
            }
        }

        if (client.getAdreca() == null || client.getAdreca().isBlank()) {
            throw new RuntimeException("L'adreça és obligatòria.");
        }

        if (client.getObservacions() != null && client.getObservacions().length() > 50) {
            throw new RuntimeException("Les observacions no poden superar els 50 caràcters.");
        }
    }


    // VERIFICAR DNI: LA LLETRA HA DE COINCIDIR AMB EL RESULTAT DEL NÚMERO MÒDUL 23
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