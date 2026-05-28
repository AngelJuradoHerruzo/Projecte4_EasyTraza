/*********************       .ELEMENTS DEL FORMULARI.       *********************/
const form = document.getElementById('usuariForm');

const dniInput = document.getElementById('dni');
const nomInput = document.getElementById('nomComplet');
const emailInput = document.getElementById('email');
const rolSelect = document.getElementById('rolUsuari');
const passwordInput = document.getElementById('password');
const togglePassword = document.getElementById('togglePassword');

const avatarInput = document.getElementById('avatarFile');
const avatarPreview = document.getElementById('avatarPreview');
let avatarPreviewImage = document.getElementById('avatarPreviewImage');
let avatarPreviewIcon = document.getElementById('avatarPreviewIcon');

let avatarPreviewUrl = null;


/*********************       .SUPORT VISUAL DEL FORMULARI.       *********************/
if (form) {

    // MOSTRAR O OCULTAR LA CONTRASENYA INTRODUÏDA
    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', function () {

            const icon = this.querySelector('i');
            const passwordVisible = passwordInput.type === 'text';

            passwordInput.type = passwordVisible ? 'password' : 'text';

            if (icon) {
                icon.className = passwordVisible ? 'bi bi-eye' : 'bi bi-eye-slash';
            }

            this.setAttribute(
                'aria-label',
                passwordVisible ? form.dataset.showPassword : form.dataset.hidePassword
            );
        });
    }


    // ACTUALITZAR LA VISTA PRÈVIA DE L'AVATAR SELECCIONAT
    if (avatarInput && avatarPreview) {
        avatarInput.addEventListener('change', function () {

            const file = this.files && this.files.length > 0 ? this.files[0] : null;

            if (!file || !file.type.startsWith('image/')) {
                return;
            }

            if (avatarPreviewUrl) {
                URL.revokeObjectURL(avatarPreviewUrl);
            }

            avatarPreviewUrl = URL.createObjectURL(file);

            if (!avatarPreviewImage) {
                avatarPreviewImage = document.createElement('img');
                avatarPreviewImage.id = 'avatarPreviewImage';
                avatarPreviewImage.alt = form.dataset.avatarPreview;
                avatarPreview.appendChild(avatarPreviewImage);
            }

            avatarPreviewImage.src = avatarPreviewUrl;

            if (avatarPreviewIcon) {
                avatarPreviewIcon.remove();
                avatarPreviewIcon = null;
            }
        });
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
        dniInput,
        nomInput,
        emailInput,
        rolSelect,
        passwordInput,
        avatarInput
    ].filter(Boolean);

    formFields.forEach(field => {
        field.addEventListener('change', function () {
            updateFieldState(this);
        });

        if (field.type !== 'file' && field.tagName !== 'SELECT') {
            field.addEventListener('input', function () {
                updateFieldState(this);
            });
        }
    });


    // ALLIBERAR LA URL TEMPORAL DE LA PREVISUALITZACIÓ EN SORTIR DE LA PÀGINA
    window.addEventListener('beforeunload', function () {
        if (avatarPreviewUrl) {
            URL.revokeObjectURL(avatarPreviewUrl);
        }
    });
}
