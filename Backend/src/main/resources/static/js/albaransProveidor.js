// ---------------------------- ELEMENTS PRINCIPALS ----------------------------
const documentOcr = document.getElementById('documentOcr');
const imatgeAlbara = document.getElementById('imatgeAlbara');
const ocrPreviewEmpty = document.getElementById('ocrPreviewEmpty');
const ocrPreviewContainer = document.getElementById('ocrPreviewContainer');
const ocrPreviewFrame = document.getElementById('ocrPreviewFrame');
const ocrFileName = document.getElementById('ocrFileName');
const btnEscanejarOcr = document.getElementById('btnEscanejarOcr');

const lotsContainer = document.getElementById('lotsContainer');
const addLotBtn = document.getElementById('addLotBtn');

let previewObjectUrl = null;


// ---------------------------- DADES ORIGINALS DE LA VISTA PRÈVIA ----------------------------
const nomDocumentInicial = ocrFileName ? ocrFileName.textContent : '';
const srcDocumentInicial = ocrPreviewFrame ? ocrPreviewFrame.getAttribute('src') : '';


// ---------------------------- VISTA PRÈVIA DEL DOCUMENT OCR ----------------------------
if (documentOcr) {
    documentOcr.addEventListener('change', function () {
        mostrarDocumentSeleccionat(this);
    });
}


// ---------------------------- VISTA PRÈVIA DEL DOCUMENT MANUAL ----------------------------
if (imatgeAlbara) {
    imatgeAlbara.addEventListener('change', function () {
        mostrarDocumentSeleccionat(this);
    });
}


// ---------------------------- ESTAT VISUAL DEL BOTÓ OCR ----------------------------
if (btnEscanejarOcr) {
    btnEscanejarOcr.addEventListener('click', function () {
        setTimeout(() => {
            btnEscanejarOcr.disabled = true;
            btnEscanejarOcr.innerHTML = '<i class="bi bi-hourglass-split"></i> Escanejant...';
        }, 0);
    });
}


// ---------------------------- MOSTRAR DOCUMENT SELECCIONAT ----------------------------
function mostrarDocumentSeleccionat(input) {

    const file = input.files[0];

    if (!file) {
        restaurarVistaPreviaOriginal();
        return;
    }

    const esImatge = file.type.startsWith('image/');
    const esPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');

    if (!esImatge && !esPdf) {
        input.value = '';
        alert('El fitxer seleccionat ha de ser una imatge o un PDF.');
        restaurarVistaPreviaOriginal();
        return;
    }

    alliberarObjectUrlPreview();
    previewObjectUrl = URL.createObjectURL(file);

    if (ocrFileName) {
        ocrFileName.textContent = file.name;
    }

    if (ocrPreviewFrame) {
        ocrPreviewFrame.src = previewObjectUrl;
    }

    if (ocrPreviewContainer) {
        ocrPreviewContainer.style.display = 'block';
    }

    if (ocrPreviewEmpty) {
        ocrPreviewEmpty.style.display = 'none';
    }
}


// ---------------------------- RESTAURAR VISTA PRÈVIA ORIGINAL ----------------------------
function restaurarVistaPreviaOriginal() {

    alliberarObjectUrlPreview();

    if (srcDocumentInicial && srcDocumentInicial.trim() !== '') {

        if (ocrPreviewFrame) {
            ocrPreviewFrame.src = srcDocumentInicial;
        }

        if (ocrFileName) {
            ocrFileName.textContent = nomDocumentInicial;
        }

        if (ocrPreviewContainer) {
            ocrPreviewContainer.style.display = 'block';
        }

        if (ocrPreviewEmpty) {
            ocrPreviewEmpty.style.display = 'none';
        }

        return;
    }

    netejarVistaPreviaDocument();
}


// ---------------------------- NETEJAR VISTA PRÈVIA DEL DOCUMENT ----------------------------
function netejarVistaPreviaDocument() {

    if (ocrPreviewContainer) {
        ocrPreviewContainer.style.display = 'none';
    }

    if (ocrPreviewEmpty) {
        ocrPreviewEmpty.style.display = 'block';
    }

    if (ocrPreviewFrame) {
        ocrPreviewFrame.removeAttribute('src');
    }

    if (ocrFileName) {
        ocrFileName.textContent = '';
    }
}


// ---------------------------- ALLIBERAR URL TEMPORAL DE VISTA PRÈVIA ----------------------------
function alliberarObjectUrlPreview() {
    if (previewObjectUrl) {
        URL.revokeObjectURL(previewObjectUrl);
        previewObjectUrl = null;
    }
}


// ---------------------------- NORMALITZAR TEXT PER COMPARACIÓ VISUAL ----------------------------
function normalitzarTextVisual(valor) {
    return (valor || '')
        .toString()
        .toUpperCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/\s+/g, ' ')
        .trim();
}


// ---------------------------- AJUDA VISUAL D'AVISOS OCR DE MATÈRIA ----------------------------
function actualitzarAvisosMateriaOcr() {
    document.querySelectorAll('.lot-row').forEach(row => {
        const select = row.querySelector('.materia-primera-select');
        const avis = row.querySelector('.ocr-materia-avis');

        if (!select || !avis) {
            return;
        }

        const materiaOcr = normalitzarTextVisual(avis.dataset.ocrMateria);
        const textSeleccionat = normalitzarTextVisual(select.options[select.selectedIndex]?.text || '');

        if (materiaOcr !== '' && textSeleccionat !== ''
                && (textSeleccionat.includes(materiaOcr) || materiaOcr.includes(textSeleccionat))) {
            avis.classList.remove('form-error');
            avis.classList.add('form-success');
        }
        else {
            avis.classList.remove('form-success');
            avis.classList.add('form-error');
        }
    });
}


document.addEventListener('change', function (event) {
    if (event.target.classList.contains('materia-primera-select')) {
        actualitzarAvisosMateriaOcr();
    }
});


// ---------------------------- BOTÓ VISUAL PER CREAR MATÈRIA PRIMERA ----------------------------
function inicialitzarBotonsMateriesPrimeres() {
    document.querySelectorAll('.btn-toggle-materia-primera').forEach(button => {
        button.onclick = function () {
            const url = this.dataset.url;

            if (url) {
                window.open(url, '_blank');
            }
        };
    });
}


// ---------------------------- REINDEXAR LOTS ----------------------------
function reindexLots() {

    const rows = document.querySelectorAll('.lot-row');

    rows.forEach((row, index) => {

        row.querySelectorAll('input, select').forEach(field => {
            const name = field.getAttribute('name');

            if (name) {
                field.setAttribute('name', name.replace(/lots\[\d+]/, 'lots[' + index + ']'));
            }

            const id = field.getAttribute('id');

            if (id) {
                field.setAttribute('id', id.replace(/lots\d+/, 'lots' + index));
            }
        });

        const addUnitatButton = row.querySelector('.btn-toggle-unitat-mesura');

        if (addUnitatButton) {
            addUnitatButton.dataset.lotIndex = index;
        }
    });
}


// ---------------------------- ELIMINAR LOT ----------------------------
function bindRemoveButtons() {

    document.querySelectorAll('.remove-lot-btn').forEach(button => {
        button.onclick = function () {
            const rows = document.querySelectorAll('.lot-row');

            if (rows.length > 1) {
                this.closest('.lot-row').remove();

                reindexLots();
                reinicialitzarAjudaLots();
            }
        };
    });
}


// ---------------------------- AFEGIR LOT ----------------------------
if (addLotBtn && lotsContainer) {
    addLotBtn.addEventListener('click', function () {
        const rows = document.querySelectorAll('.lot-row');

        if (rows.length === 0) {
            return;
        }

        const newRow = rows[rows.length - 1].cloneNode(true);

        newRow.querySelectorAll('input, select').forEach(field => {
            field.value = '';

            if (field.type === 'hidden' && field.name && field.name.includes('.id')) {
                field.value = '';
            }
        });

        newRow.querySelectorAll('.ocr-materia-avis, .form-error, .form-success').forEach(element => {
            if (element.classList.contains('ocr-materia-avis')) {
                element.remove();
            }
        });

        lotsContainer.appendChild(newRow);

        reindexLots();
        reinicialitzarAjudaLots();
    });
}


// ---------------------------- REINICIALITZAR AJUDA DE LOTS ----------------------------
function reinicialitzarAjudaLots() {
    if (typeof inicialitzarBotonsUnitatsMesura === 'function') {
        inicialitzarBotonsUnitatsMesura();
    }

    inicialitzarBotonsMateriesPrimeres();
    bindRemoveButtons();
    actualitzarAvisosMateriaOcr();
}


// ---------------------------- INICIALITZAR BOTONS ----------------------------
reinicialitzarAjudaLots();

window.addEventListener('beforeunload', alliberarObjectUrlPreview);
