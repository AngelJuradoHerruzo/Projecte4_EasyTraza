package cat.copernic.easytraza.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.easytraza.entities.Client;
import cat.copernic.easytraza.repository.ClientRepository;

@Service
@Transactional
public class ClientService {

    // ---------------------------- REPOSITORI I CONSTRUCTOR ----------------------------
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }


    // OBTENIR TOTS ELS CLIENTS
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }


    // OBTENIR CLIENT PER ID
    public Client getClientById(Long id) {
        Optional<Client> client = clientRepository.findById(id);
        return client.orElse(null);
    }


    // CREAR CLIENT
    public Client createClient(Client client) {
        validarDadesClient(client);

        Optional<Client> clientExistent = clientRepository.findByNif(client.getNif());
        if (clientExistent.isPresent()) {
            throw new RuntimeException("Ja existeix un client amb aquest NIF.");
        }

        return clientRepository.save(client);
    }


    // ACTUALITZAR CLIENT
    public Client updateClient(Long id, Client client) {

        Optional<Client> clientOpt = clientRepository.findById(id);

        if (clientOpt.isPresent()) {
            validarDadesClient(client);

            Optional<Client> clientExistent = clientRepository.findByNif(client.getNif());
            if (clientExistent.isPresent() && !clientExistent.get().getId().equals(id)) {
                throw new RuntimeException("Ja existeix un client amb aquest NIF.");
            }

            Client clientActual = clientOpt.get();

            clientActual.setNif(client.getNif().trim().toUpperCase());
            clientActual.setNomComplet(client.getNomComplet().trim());
            clientActual.setTelefon(client.getTelefon() != null ? client.getTelefon().trim() : null);
            clientActual.setEmail(client.getEmail() != null ? client.getEmail().trim().toLowerCase() : null);
            clientActual.setAdreca(client.getAdreca() != null ? client.getAdreca().trim() : null);
            clientActual.setObservacions(client.getObservacions() != null ? client.getObservacions().trim() : null);

            return clientRepository.save(clientActual);
        }

        return null;
    }


    // ELIMINAR CLIENT
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }


    // VALIDAR DADES DEL CLIENT
    private void validarDadesClient(Client client) {

        if (client.getNif() != null) {
            client.setNif(client.getNif().trim().toUpperCase());
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

        if (client.getNif() == null || client.getNif().isBlank()) {
            throw new RuntimeException("El NIF és obligatori.");
        }

        if (!esNifValid(client.getNif())) {
            throw new RuntimeException("El NIF no és correcte.");
        }

        Optional<Client> clientNifExistent = clientRepository.findByNif(client.getNif());

        if (clientNifExistent.isPresent()
                && !clientNifExistent.get().getId().equals(client.getId())) {
            throw new RuntimeException("Ja existeix un client amb aquest NIF.");
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


    // VERIFICAR DNI
    private boolean esNifValid(String nif) {
        if (nif == null || nif.isBlank()) {
            return false;
        }

        nif = nif.trim().toUpperCase();

        if (!nif.matches("\\d{8}[A-Z]")) {
            return false;
        }

        String lletres = "TRWAGMYFPDXBNJZSQVHLCKE";
        int numero = Integer.parseInt(nif.substring(0, 8));
        char lletraCorrecta = lletres.charAt(numero % 23);

        return nif.charAt(8) == lletraCorrecta;
    }
}