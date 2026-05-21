package cat.copernic.easytraza.service;

import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.easytraza.entities.AlbaraProveidor;

/**
 * SERVEI OCR ALBARÀ DE PROVEÏDOR
 *
 * Queda pendent d'implementar en el bloc OCR. En el flux manual no processa ni guarda dades.
 *
 * @author Ángel Jurado
 */
@Service
public class OcrAlbaraProveidorService {

    // PROCESSAR IMATGE OCR I RETORNAR UN ALBARÀ PREOMPLERT
    public AlbaraProveidor processarImatge(MultipartFile file) {
        throw new RuntimeException("El processament OCR encara no està implementat.");
    }


    // CONVERTIR LA IMATGE A BASE64 PER MANTENIR-LA AL FORMULARI SENSE GUARDAR-LA
    public String convertirImatgeBase64(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String contentType = file.getContentType();

            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            return "data:" + contentType + ";base64," + base64;
        }
        catch (Exception e) {
            throw new RuntimeException("No s'ha pogut preparar el fitxer de l'albarà.");
        }
    }
}
