/*********************       .ELEMENTS DEL FORMULARI.       *********************/
const proveidorForm = document.getElementById('proveidorForm');
const cifInput = document.getElementById('cif');
const nomProveidorInput = document.getElementById('nomProveidor');
const adrecaInput = document.getElementById('adreca');
const descripcioInput = document.getElementById('descripcio');
const descripcioCounter = document.getElementById('descripcioCounter');
const tipusDocument = document.getElementById('tipusDocument');


/*********************       .SUPORT VISUAL DEL FORMULARI.       *********************/
if (proveidorForm) {

    // MOSTRAR EL TIPUS DE DOCUMENT INTRODUÏT AL CAMP CIF / DNI
    function updateTipusDocument() {

        if (!cifInput || !tipusDocument) {
            return;
        }

        const value = cifInput.value.trim();

        if (value === '') {
            tipusDocument.textContent = '';
            tipusDocument.classList.remove('visible');
            return;
        }

        if (/^\d/.test(value)) {
            tipusDocument.textContent = 'DNI';
        }
        else {
            tipusDocument.textContent = 'CIF';
        }

        tipusDocument.classList.add('visible');
    }


    // ACTUALITZAR EL COMPTADOR VISUAL DE LA DESCRIPCIÓ
    function updateDescripcioCounter() {

        if (!descripcioInput || !descripcioCounter) {
            return;
        }

        const length = descripcioInput.value.length;

        descripcioCounter.textContent = `${length}/50`;
        descripcioCounter.classList.remove('counter-valid', 'counter-limit');

        if (length >= 45) {
            descripcioCounter.classList.add('counter-limit');
        }
        else {
            descripcioCounter.classList.add('counter-valid');
        }
    }


    // ELIMINAR L'ESTAT VISUAL D'UN CAMP
    function clearFieldState(field) {
        field.classList.remove('field-valid', 'field-invalid');
    }


    // ACTUALITZAR L'ESTAT VISUAL SEGONS LA VALIDACIÓ HTML DEL CAMP
    function updateFieldState(field) {

        if (!field || field.readOnly || field.disabled) {
            return;
        }

        if (field.value.trim() === '') {
            clearFieldState(field);
            return;
        }

        if (field.checkValidity()) {
            field.classList.remove('field-invalid');
            field.classList.add('field-valid');
        }
        else {
            field.classList.remove('field-valid');
            field.classList.add('field-invalid');
        }
    }


    // APLICAR RETORN VISUAL ALS CAMPS DEL FORMULARI
    const formFields = [
        cifInput,
        nomProveidorInput,
        adrecaInput,
        descripcioInput
    ].filter(Boolean);

    formFields.forEach(field => {
        field.addEventListener('input', function () {
            updateFieldState(this);
        });

        field.addEventListener('change', function () {
            updateFieldState(this);
        });
    });


    // ACTUALITZAR AJUDES VISUALS DELS CAMPS CORRESPONENTS
    if (cifInput) {
        cifInput.addEventListener('input', updateTipusDocument);
    }

    if (descripcioInput) {
        descripcioInput.addEventListener('input', updateDescripcioCounter);
    }


    // INICIALITZAR ELS ELEMENTS VISUALS DEL FORMULARI
    updateTipusDocument();
    updateDescripcioCounter();
}
