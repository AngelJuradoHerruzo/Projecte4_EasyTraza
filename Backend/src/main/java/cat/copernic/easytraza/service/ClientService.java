package cat.copernic.easytraza.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;

    public ClientService(ClientRepository clientRepository,
                         AlbaraClientRepository albaraClientRepository,
                         MessageSource messageSource) {
        this.clientRepository = clientRepository;
        this.albaraClientRepository = albaraClientRepository;
        this.messageSource = messageSource;
    }


    // OBTENIR TOTS ELS CLIENTS ORDENATS PER NOM
    public List<Client> getAllClients() {

        List<Client> clients = clientRepository.findAllByOrderByNomCompletAsc();

        informarClientsAmbAlbarans(clients);

        return clients;
    }


    // OBTENIR CLIENT PER ID
    public Client getClientById(Long id) {
        Optional<Client> client = clientRepository.findById(id);
        return client.orElse(null);
    }


    // PREPARAR LLISTAT WEB DE CLIENTS AMB FILTRES I ORDENACIÓ OPCIONALS
    public List<Client> getClientsLlistat(String nomComplet,
                                           String cif,
                                           String email,
                                           String telefon,
                                           String ordre,
                                           String direccio) {

        List<Client> clients = new ArrayList<>(clientRepository.findAll());

        informarClientsAmbAlbarans(clients);

        if (nomComplet != null && !nomComplet.isBlank()) {
            clients.removeIf(client -> !conteText(client.getNomComplet(), nomComplet));
        }

        if (cif != null && !cif.isBlank()) {
            clients.removeIf(client -> !conteText(client.getCif(), cif));
        }

        if (email != null && !email.isBlank()) {
            clients.removeIf(client -> !conteText(client.getEmail(), email));
        }

        if (telefon != null && !telefon.isBlank()) {
            clients.removeIf(client -> !conteTelefon(client.getTelefon(), telefon));
        }

        ordenarClients(clients, ordre, direccio);

        return clients;
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
            clientActual.setEmail(client.getEmail().trim().toLowerCase());
            clientActual.setAdreca(client.getAdreca().trim());
            clientActual.setObservacions(client.getObservacions() != null ? client.getObservacions().trim() : null);

            return clientRepository.save(clientActual);
        }

        return null;
    }


    // ELIMINAR CLIENT
    public void deleteClient(Long id) {

        if (albaraClientRepository.existsByClientId(id)) {
            throw new RuntimeException(missatge("service.client.eliminarRelacionat"));
        }

        clientRepository.deleteById(id);
    }


    // INDICAR QUINS CLIENTS TENEN ALBARANS ASSOCIATS
    private void informarClientsAmbAlbarans(List<Client> clients) {

        for (Client client : clients) {
            client.setTeAlbarans(albaraClientRepository.existsByClientId(client.getId()));
        }
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
        }

        if (client.getAdreca() != null) {
            client.setAdreca(client.getAdreca().trim());
        }

        if (client.getObservacions() != null) {
            client.setObservacions(client.getObservacions().trim());
        }

        if (client.getCif() == null || client.getCif().isBlank()) {
            throw new RuntimeException(missatge("service.client.cifObligatori"));
        }

        if (!esCifNifValid(client.getCif())) {
            throw new RuntimeException(missatge("service.client.cifIncorrecte"));
        }

        Optional<Client> clientCifExistent = clientRepository.findByCif(client.getCif());

        if (clientCifExistent.isPresent()
                && !clientCifExistent.get().getId().equals(client.getId())) {
            throw new RuntimeException(missatge("service.client.cifDuplicat"));
        }

        if (client.getNomComplet() == null || client.getNomComplet().isBlank()) {
            throw new RuntimeException(missatge("service.client.nomObligatori"));
        }

        if (!client.getNomComplet().matches("^[A-Za-zÀ-ÿ]+(\\s+[A-Za-zÀ-ÿ]+)+$")) {
            throw new RuntimeException(missatge("service.client.nomCognom"));
        }

        if (client.getTelefon() == null || client.getTelefon().isBlank()) {
            throw new RuntimeException(missatge("service.client.telefonObligatori"));
        }

        if (!client.getTelefon().matches("\\d{3}\\s\\d{2}\\s\\d{2}\\s\\d{2}")) {
            throw new RuntimeException(missatge("service.client.telefonFormat"));
        }

        Optional<Client> clientTelefonExistent = clientRepository.findByTelefon(client.getTelefon());

        if (clientTelefonExistent.isPresent()
                && !clientTelefonExistent.get().getId().equals(client.getId())) {
            throw new RuntimeException(missatge("service.client.telefonDuplicat"));
        }

        if (client.getEmail() == null || client.getEmail().isBlank()) {
            throw new RuntimeException(missatge("service.client.emailObligatori"));
        }

        if (!client.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException(missatge("service.client.emailFormat"));
        }

        Optional<Client> clientEmailExistent = clientRepository.findByEmail(client.getEmail());

        if (clientEmailExistent.isPresent()
                && !clientEmailExistent.get().getId().equals(client.getId())) {
            throw new RuntimeException(missatge("service.client.emailDuplicat"));
        }

        if (client.getAdreca() == null || client.getAdreca().isBlank()) {
            throw new RuntimeException(missatge("service.client.adrecaObligatoria"));
        }

        if (client.getObservacions() != null && client.getObservacions().length() > 50) {
            throw new RuntimeException(missatge("service.client.observacionsMax"));
        }
    }


    // ORDENAR CLIENTS PEL CAMP SELECCIONAT
    private void ordenarClients(List<Client> clients, String ordre, String direccio) {

        String campOrdre = ordre != null && !ordre.isBlank() ? ordre : "nomComplet";

        Comparator<Client> comparator;

        switch (campOrdre) {
            case "cif":
                comparator = Comparator.comparing(
                    client -> valorText(client.getCif()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "telefon":
                comparator = Comparator.comparing(
                    client -> valorText(client.getTelefon()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "email":
                comparator = Comparator.comparing(
                    client -> valorText(client.getEmail()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "adreca":
                comparator = Comparator.comparing(
                    client -> valorText(client.getAdreca()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "observacions":
                comparator = Comparator.comparing(
                    client -> valorText(client.getObservacions()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "nomComplet":
            default:
                comparator = Comparator.comparing(
                    client -> valorText(client.getNomComplet()),
                    String.CASE_INSENSITIVE_ORDER
                );
                break;
        }

        if ("desc".equalsIgnoreCase(direccio)) {
            comparator = comparator.reversed();
        }

        clients.sort(comparator.thenComparing(Client::getId));
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


    // COMPROVAR SI EL TELÈFON CONTÉ ELS DÍGITS DEL FILTRE
    private boolean conteTelefon(String telefon, String filtre) {

        if (filtre == null || filtre.isBlank()) {
            return true;
        }

        if (telefon == null) {
            return false;
        }

        String telefonNetejat = telefon.replaceAll("\\s", "");
        String filtreNetejat = filtre.replaceAll("\\s", "");

        return telefonNetejat.contains(filtreNetejat);
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

            // Entitats que obligatòriament han de tenir lletra com a control
            if ("PQRSNW".indexOf(lletraInicial) >= 0) {
                return control == lletraControl;
            }

            // Entitats que obligatòriament han de tenir dígit com a control
            if ("ABEH".indexOf(lletraInicial) >= 0) {
                return control == Character.forDigit(digitControl, 10);
            }

            // Altres entitats poden utilitzar dígit o lletra
            return control == Character.forDigit(digitControl, 10) || control == lletraControl;
        }

        return false; // Si no compleix cap format, no és vàlid
    }

    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
