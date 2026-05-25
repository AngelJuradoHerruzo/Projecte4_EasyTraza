const form = document.getElementById('producteForm');
const nomInput = document.getElementById('nomProducte');
const descripcioInput = document.getElementById('descripcio');
const descripcioCounter = document.getElementById('descripcioCounter');


if (form && nomInput && descripcioInput && descripcioCounter) {

    // FORMAT CORRECTE DEL NOM: MAJÚSCULA INICIAL I ESPAIS CONTROLATS
    nomInput.addEventListener('input', function () {
        let value = this.value;

        value = value.replace(/\s+/g, ' ').trimStart();

        value = value
            .split(' ')
            .map(word => word
                ? word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
                : '')
            .join(' ');

        this.value = value;
    });


    // ACTUALITZAR COMPTADOR DE LA DESCRIPCIÓ
    descripcioInput.addEventListener('input', function () {
        updateDescripcioCounter();
    });


    // FUNCIONS D'ESTAT DELS CAMPS
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


    // COMPTADOR DESCRIPCIÓ
    function updateDescripcioCounter() {
        const length = descripcioInput.value.length;

        descripcioCounter.textContent = `${length}/50`;

        descripcioCounter.classList.remove('counter-valid', 'counter-limit');

        if (length >= 45) {
            descripcioCounter.classList.add('counter-limit');
        } else {
            descripcioCounter.classList.add('counter-valid');
        }
    }


    // VALIDACIÓ DELS CAMPS
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


    // EVENTS DE VALIDACIÓ
    [nomInput, descripcioInput].forEach(field => {
        field.addEventListener('input', () => validateField(field));
        field.addEventListener('change', () => validateField(field));
    });


    // SUBMIT
    form.addEventListener('submit', function (event) {

        let valid = true;

        [nomInput, descripcioInput].forEach(field => {
            const fieldValid = validateField(field);

            if (!fieldValid) {
                valid = false;
            }
        });

        if (!valid) {
            event.preventDefault();
        }
    });


    // INIT
    updateDescripcioCounter();
}