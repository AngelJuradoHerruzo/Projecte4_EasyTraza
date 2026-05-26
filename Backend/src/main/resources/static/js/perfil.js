/*********************       .ELEMENTS DEL PERFIL.       *********************/
const perfilForm = document.getElementById('perfilForm');
const passwordInput = document.getElementById('password');
const togglePassword = document.getElementById('togglePassword');
const avatarInput = document.getElementById('avatarFile');
const avatarPreview = document.getElementById('avatarPreview');
let avatarPreviewImage = document.getElementById('avatarPreviewImage');
let avatarPreviewIcon = document.getElementById('avatarPreviewIcon');

let avatarPreviewUrl = null;


/*********************       .SUPORT VISUAL DEL FORMULARI.       *********************/
if (perfilForm) {

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
                passwordVisible ? perfilForm.dataset.showPassword : perfilForm.dataset.hidePassword
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
                avatarPreviewImage.alt = perfilForm.dataset.avatarPreview;
                avatarPreview.appendChild(avatarPreviewImage);
            }

            avatarPreviewImage.src = avatarPreviewUrl;

            if (avatarPreviewIcon) {
                avatarPreviewIcon.remove();
                avatarPreviewIcon = null;
            }
        });
    }


    // ALLIBERAR LA URL TEMPORAL DE LA PREVISUALITZACIÓ EN SORTIR DE LA PÀGINA
    window.addEventListener('beforeunload', function () {
        if (avatarPreviewUrl) {
            URL.revokeObjectURL(avatarPreviewUrl);
        }
    });
}
