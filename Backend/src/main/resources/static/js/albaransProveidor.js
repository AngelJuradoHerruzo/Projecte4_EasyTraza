const ocrFile = document.getElementById('ocrFile');
const ocrPreviewEmpty = document.getElementById('ocrPreviewEmpty');
const ocrPreviewContainer = document.getElementById('ocrPreviewContainer');
const ocrFileName = document.getElementById('ocrFileName');

const lotsContainer = document.getElementById('lotsContainer');
const addLotBtn = document.getElementById('addLotBtn');


// ---------------------------- IMATGE DE VISTA PRÈVIA ACTUAL ----------------------------
function obtenirImatgeVistaPrevia() {
    return document.getElementById('ocrPreview') || document.getElementById('ocrPreviewSaved');
}


// ---------------------------- DADES ORIGINALS DE LA IMATGE ----------------------------
const previewImageInicial = obtenirImatgeVistaPrevia();
const nomImatgeInicial = ocrFileName ? ocrFileName.textContent : '';
const srcImatgeInicial = previewImageInicial ? previewImageInicial.getAttribute('src') : '';


// ---------------------------- VISTA PRÈVIA DE LA IMATGE ----------------------------
if (ocrFile) {
    ocrFile.addEventListener('change', function () {
        mostrarImatgeSeleccionada(this);
    });
}


// ---------------------------- MOSTRAR IMATGE SELECCIONADA ----------------------------
function mostrarImatgeSeleccionada(input) {

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

    if (ocrFileName) {
        ocrFileName.textContent = file.name;
    }

    if (ocrPreviewContainer) {
        ocrPreviewContainer.style.display = 'block';
    }

    if (ocrPreviewEmpty) {
        ocrPreviewEmpty.style.display = 'none';
    }

    if (esPdf) {
        const previewImage = obtenirImatgeVistaPrevia();

        if (previewImage) {
            previewImage.removeAttribute('src');
            previewImage.alt = 'PDF seleccionat';
        }

        return;
    }

    const reader = new FileReader();

    reader.onload = function (event) {
        const previewImage = obtenirImatgeVistaPrevia();

        if (previewImage) {
            previewImage.src = event.target.result;
            previewImage.alt = 'Vista prèvia de l\'albarà';
        }
    };

    reader.readAsDataURL(file);
}


// ---------------------------- RESTAURAR VISTA PRÈVIA ORIGINAL ----------------------------
function restaurarVistaPreviaOriginal() {

    const previewImage = obtenirImatgeVistaPrevia();

    if (srcImatgeInicial && srcImatgeInicial.trim() !== '') {

        if (previewImage) {
            previewImage.src = srcImatgeInicial;
        }

        if (ocrFileName) {
            ocrFileName.textContent = nomImatgeInicial;
        }

        if (ocrPreviewContainer) {
            ocrPreviewContainer.style.display = 'block';
        }

        if (ocrPreviewEmpty) {
            ocrPreviewEmpty.style.display = 'none';
        }

        return;
    }

    netejarVistaPreviaImatge();
}


// ---------------------------- NETEJAR VISTA PRÈVIA DE LA IMATGE ----------------------------
function netejarVistaPreviaImatge() {

    const previewImage = obtenirImatgeVistaPrevia();

    if (ocrPreviewContainer) {
        ocrPreviewContainer.style.display = 'none';
    }

    if (ocrPreviewEmpty) {
        ocrPreviewEmpty.style.display = 'block';
    }

    if (previewImage) {
        previewImage.src = '';
    }

    if (ocrFileName) {
        ocrFileName.textContent = '';
    }
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

                if (typeof inicialitzarBotonsUnitatsMesura === 'function') {
                    inicialitzarBotonsUnitatsMesura();
                }

                bindRemoveButtons();
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

        lotsContainer.appendChild(newRow);

        reindexLots();

        if (typeof inicialitzarBotonsUnitatsMesura === 'function') {
            inicialitzarBotonsUnitatsMesura();
        }

        bindRemoveButtons();
    });
}


// ---------------------------- INICIALITZAR BOTONS ----------------------------
bindRemoveButtons();