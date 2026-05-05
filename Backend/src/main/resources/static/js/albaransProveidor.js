const form = document.getElementById('albaraProveidorForm');

const dataInput = document.getElementById('dataRecepcio');
const proveidorInput = document.getElementById('proveidor');
const usuariInput = document.getElementById('usuariReceptor');
const lotsContainer = document.getElementById('lotsContainer');
const addLotBtn = document.getElementById('addLotBtn');


/*----------------------- FUNCIONS ESTAT -----------------------*/
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


/*----------------------- VALIDACIÓ -----------------------*/
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


/*----------------------- IDENTIFICADOR LOT -----------------------*/
function formatDateForLot(value) {
    if (!value) return '';

    const datePart = value.split('T')[0];

    if (!datePart) return '';

    const parts = datePart.split('-');

    if (parts.length !== 3) return '';

    return parts[2] + '_' + parts[1] + '_' + parts[0];
}

function updateIdentificadorsLot() {
    const dataFormatada = formatDateForLot(dataInput.value);
    const identificadors = document.querySelectorAll('.identificadorLotPreview');

    identificadors.forEach((input, index) => {
        if (dataFormatada === '') {
            input.value = '';
        }
        else {
            input.value = dataFormatada + '_lote' + (index + 1);
        }
    });
}


/*----------------------- EVENTS VALIDACIÓ -----------------------*/
function getAllFields() {
    return [
        dataInput,
        proveidorInput,
        usuariInput,
        ...document.querySelectorAll('.materiaPrimeraInput'),
        ...document.querySelectorAll('.quantitatInput'),
        ...document.querySelectorAll('.unitatsInput')
    ];
}

getAllFields().forEach(field => {
    field.addEventListener('input', () => validateField(field));
    field.addEventListener('change', () => validateField(field));
});

dataInput.addEventListener('input', updateIdentificadorsLot);
dataInput.addEventListener('change', updateIdentificadorsLot);


/*----------------------- LOTS -----------------------*/
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
    });
}

function bindRemoveButtons() {
    document.querySelectorAll('.remove-lot-btn').forEach(button => {
        button.onclick = function () {
            const rows = document.querySelectorAll('.lot-row');

            if (rows.length > 1) {
                this.closest('.lot-row').remove();
                reindexLots();
                updateIdentificadorsLot();
            }
        };
    });
}

if (addLotBtn) {
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
        updateIdentificadorsLot();
    });
}

bindRemoveButtons();
updateIdentificadorsLot();


/*----------------------- SUBMIT -----------------------*/
form.addEventListener('submit', function (event) {

    let valid = true;

    const fields = getAllFields();

    fields.forEach(field => {
        const ok = validateField(field);
        if (!ok) valid = false;
    });

    if (!valid) {
        event.preventDefault();
    }
});


const ocrFile = document.getElementById('ocrFile');
const ocrPreview = document.getElementById('ocrPreview');
const ocrPreviewEmpty = document.getElementById('ocrPreviewEmpty');
const ocrPreviewContainer = document.getElementById('ocrPreviewContainer');
const ocrFileName = document.getElementById('ocrFileName');

if (ocrFile) {
    ocrFile.addEventListener('change', function () {
        const file = this.files[0];

        if (!file) {
            ocrPreviewContainer.style.display = 'none';
            ocrPreviewEmpty.style.display = 'block';
            ocrPreview.src = '';
            ocrFileName.textContent = '';
            return;
        }

        const reader = new FileReader();

        reader.onload = function (event) {
            ocrPreview.src = event.target.result;
            ocrFileName.textContent = file.name;
            ocrPreviewContainer.style.display = 'block';
            ocrPreviewEmpty.style.display = 'none';
        };

        reader.readAsDataURL(file);
    });
}