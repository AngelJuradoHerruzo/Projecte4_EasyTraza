const form = document.getElementById('albaraProveidorForm');

const dataInput = document.getElementById('dataRecepcio');
const proveidorInput = document.getElementById('proveidor');
const usuariInput = document.getElementById('usuariReceptor');


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


/*----------------------- EVENTS VALIDACIÓ -----------------------*/
[dataInput, proveidorInput, usuariInput].forEach(field => {
    field.addEventListener('input', () => validateField(field));
    field.addEventListener('change', () => validateField(field));
});


/*----------------------- SUBMIT -----------------------*/
form.addEventListener('submit', function (event) {

    let valid = true;

    const fields = [dataInput, proveidorInput, usuariInput];

    fields.forEach(field => {
        const ok = validateField(field);
        if (!ok) valid = false;
    });

    if (!valid) {
        event.preventDefault();
    }
});
