const form = document.getElementById('albaraProveidorForm');

const dataInput = document.getElementById('dataRecepcio');
const proveidorInput = document.getElementById('proveidor');
const usuariInput = document.getElementById('usuariReceptor');
const lotsContainer = document.getElementById('lotsContainer');
const addLotBtn = document.getElementById('addLotBtn');

const ocrFile = document.getElementById('ocrFile');
const ocrPreview = document.getElementById('ocrPreview');
const ocrPreviewEmpty = document.getElementById('ocrPreviewEmpty');
const ocrPreviewContainer = document.getElementById('ocrPreviewContainer');
const ocrFileName = document.getElementById('ocrFileName');


// ---------------------------- FUNCIONS ESTAT ----------------------------
function clearState(field) {
    field.classList.remove('field-valid', 'field-invalid');
}


function markValid(field) {
    field.classList.remove('field-invalid');
    field.classList.add('field-valid');
}


function markInvalid(field) {
    field.classList.remove('field-valid');
    field.classList.add('field-invalid');
}


// ---------------------------- VALIDACIÓ ----------------------------
function validateField(field) {
    const value = field.value.trim();

    if (value === '') {
        clearState(field);
        return !field.hasAttribute('required');
    }

    if (field.checkValidity()) {
        markValid(field);
        return true;
    }

    markInvalid(field);
    return false;
}


// ---------------------------- IDENTIFICADOR LOT ----------------------------
function formatDateForLot(value) {
    if (!value) return '';

    const datePart = value.split('T')[0];

    if (!datePart) return '';

    const parts = datePart.split('-');

    if (parts.length !== 3) return '';

    return parts[2] + '_' + parts[1] + '_' + parts[0];
}


function updateIdentificadorsLot() {
    if (!dataInput) return;

    const dataFormatada = formatDateForLot(dataInput.value);
    const identificadors = document.querySelectorAll('.identificadorLotPreview');

    identificadors.forEach((input, index) => {
        if (dataFormatada === '') {
            input.value = '';
        }
        else {
            input.value = dataFormatada + '_lot' + (index + 1);
        }
    });
}


// ---------------------------- CAMPS DEL FORMULARI ----------------------------
function getAllFields() {
    return [
        dataInput,
        proveidorInput,
        usuariInput,
        ...document.querySelectorAll('.materiaPrimeraInput'),
        ...document.querySelectorAll('.quantitatInput'),
        ...document.querySelectorAll('.unitatsInput')
    ].filter(Boolean);
}


// ---------------------------- EVENTS VALIDACIÓ ----------------------------
if (form) {

    getAllFields().forEach(field => {
        field.addEventListener('input', () => validateField(field));
        field.addEventListener('change', () => validateField(field));
    });

    if (dataInput) {
        dataInput.addEventListener('input', updateIdentificadorsLot);
        dataInput.addEventListener('change', updateIdentificadorsLot);
    }
}


// ---------------------------- LOTS ----------------------------
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

        const unitatsSelect = row.querySelector('.unitat-mesura-select');
        const addUnitatButton = row.querySelector('.btn-add-unitat-mesura');

        if (unitatsSelect) {
            unitatsSelect.setAttribute('id', 'lots' + index + 'Unitats');
        }

        if (addUnitatButton) {
            addUnitatButton.dataset.targetSelect = 'lots' + index + 'Unitats';
        }
    });
}


function bindRemoveButtons() {
    document.querySelectorAll('.remove-lot-btn').forEach(button => {
        button.onclick = function () {
            const rows = document.querySelectorAll('.lot-row');

            if (rows.length > 1) {
                this.closest('.lot-row').remove();
                reindexLots();
                bindLotFields();
                updateIdentificadorsLot();
            }
        };
    });
}


function bindLotFields() {
    getAllFields().forEach(field => {
        field.oninput = () => validateField(field);
        field.onchange = () => validateField(field);
    });

    if (typeof inicialitzarBotonsUnitatsMesura === 'function') {
        inicialitzarBotonsUnitatsMesura();
    }
}


if (addLotBtn && lotsContainer) {
    addLotBtn.addEventListener('click', function () {
        const rows = document.querySelectorAll('.lot-row');
        const newRow = rows[rows.length - 1].cloneNode(true);

        newRow.querySelectorAll('input, select').forEach(field => {
            field.value = '';
            clearState(field);
        });

        lotsContainer.appendChild(newRow);

        reindexLots();
        bindRemoveButtons();
        bindLotFields();
        updateIdentificadorsLot();
    });
}


if (form) {
    bindRemoveButtons();
    bindLotFields();
    updateIdentificadorsLot();
}


// ---------------------------- SUBMIT ----------------------------
if (form) {
    form.addEventListener('submit', function (event) {

        const submitter = event.submitter;

        // Permet guardar una unitat de mesura sense validar tot l'albarà
        if (submitter && submitter.dataset.action === 'guardar-unitat') {
            return;
        }

        let valid = true;

        const fields = getAllFields();

        fields.forEach(field => {
            const ok = validateField(field);

            if (!ok) {
                valid = false;
            }
        });

        if (!valid) {
            event.preventDefault();
        }
    });
}


// ---------------------------- VISTA PRÈVIA OCR ----------------------------
if (ocrFile) {
    ocrFile.addEventListener('change', function () {
        const file = this.files[0];

        if (!file) {
            if (ocrPreviewContainer) {
                ocrPreviewContainer.style.display = 'none';
            }

            if (ocrPreviewEmpty) {
                ocrPreviewEmpty.style.display = 'block';
            }

            if (ocrPreview) {
                ocrPreview.src = '';
                ocrPreview.style.display = 'none';
            }

            if (ocrFileName) {
                ocrFileName.textContent = '';
            }

            return;
        }

        const reader = new FileReader();

        reader.onload = function (event) {
            if (ocrPreview) {
                ocrPreview.src = event.target.result;
                ocrPreview.style.display = 'block';
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
        };

        reader.readAsDataURL(file);
    });
}