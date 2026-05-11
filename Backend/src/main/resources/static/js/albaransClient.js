const searchInput = document.getElementById('searchInput');
const cards = document.querySelectorAll('.albara-card');

const form = document.getElementById('albaraClientForm');

const dataInput = document.getElementById('dataAlbara');
const clientInput = document.getElementById('client');
const liniesContainer = document.getElementById('liniesContainer');
const addLiniaBtn = document.getElementById('addLiniaBtn');


/*----------------------- CERCA -----------------------*/
if (searchInput) {
    searchInput.addEventListener('keyup', function () {
        const filter = this.value.toLowerCase();

        cards.forEach(card => {
            const text = card.textContent.toLowerCase();
            card.style.display = text.includes(filter) ? '' : 'none';
        });
    });
}


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


/*----------------------- CAMPS -----------------------*/
function getAllFields() {
    return [
        dataInput,
        clientInput,
        ...document.querySelectorAll('.producteInput'),
        ...document.querySelectorAll('.quantitatInput')
    ].filter(field => field != null);
}


/*----------------------- PRODUCTES SELECCIONATS -----------------------*/
function updateProductOptions() {
    const productSelects = document.querySelectorAll('.producteInput');
    const selectedValues = [];

    productSelects.forEach(select => {
        if (select.value !== '') {
            selectedValues.push(select.value);
        }
    });

    productSelects.forEach(select => {
        const currentValue = select.value;

        select.querySelectorAll('option').forEach(option => {
            if (option.value === '') {
                option.hidden = false;
                option.disabled = false;
                return;
            }

            if (option.value === currentValue) {
                option.hidden = false;
                option.disabled = false;
                return;
            }

            if (selectedValues.includes(option.value)) {
                option.hidden = true;
                option.disabled = true;
            }
            else {
                option.hidden = false;
                option.disabled = false;
            }
        });
    });
}


/*----------------------- EVENTS VALIDACIÓ -----------------------*/
function bindValidationEvents() {
    getAllFields().forEach(field => {
        field.oninput = () => {
            validateField(field);

            if (field.classList.contains('producteInput')) {
                updateProductOptions();
            }
        };

        field.onchange = () => {
            validateField(field);

            if (field.classList.contains('producteInput')) {
                updateProductOptions();
            }
        };
    });
}

if (form) {
    bindValidationEvents();
    updateProductOptions();
}


/*----------------------- REINDEXAR LÍNIES -----------------------*/
function reindexLinies() {
    const rows = document.querySelectorAll('.linia-row');

    rows.forEach((row, index) => {
        row.querySelectorAll('input, select').forEach(field => {
            const name = field.getAttribute('name');

            if (name) {
                field.setAttribute('name', name.replace(/liniesProduccio\[\d+]/, 'liniesProduccio[' + index + ']'));
            }

            const id = field.getAttribute('id');

            if (id) {
                field.setAttribute('id', id.replace(/liniesProduccio\d+/, 'liniesProduccio' + index));
            }
        });
    });
}


/*----------------------- ELIMINAR LÍNIA -----------------------*/
function bindRemoveButtons() {
    document.querySelectorAll('.remove-linia-btn').forEach(button => {
        button.onclick = function () {
            const rows = document.querySelectorAll('.linia-row');

            if (rows.length > 1) {
                this.closest('.linia-row').remove();

                reindexLinies();
                bindValidationEvents();
                updateProductOptions();
            }
        };
    });
}

bindRemoveButtons();


/*----------------------- AFEGIR LÍNIA -----------------------*/
if (addLiniaBtn) {
    addLiniaBtn.addEventListener('click', function () {
        const rows = document.querySelectorAll('.linia-row');
        const newRow = rows[rows.length - 1].cloneNode(true);

        newRow.querySelectorAll('input, select').forEach(field => {
            field.value = '';
            clearState(field);
        });

        liniesContainer.appendChild(newRow);

        reindexLinies();
        bindRemoveButtons();
        bindValidationEvents();
        updateProductOptions();
    });
}


/*----------------------- SUBMIT -----------------------*/
if (form) {
    form.addEventListener('submit', function (event) {

        let valid = true;

        getAllFields().forEach(field => {
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