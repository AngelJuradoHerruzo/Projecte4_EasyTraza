const passwordInput = document.getElementById('password');
const togglePasswordBtn = document.getElementById('togglePassword');


// ---------------------------- MOSTRAR / AMAGAR CONTRASENYA ----------------------------
if (togglePasswordBtn && passwordInput) {

    togglePasswordBtn.addEventListener('click', function () {

        const icon = this.querySelector('i');

        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';

            if (icon) {
                icon.className = 'bi bi-eye-slash';
            }
        }
        else {
            passwordInput.type = 'password';

            if (icon) {
                icon.className = 'bi bi-eye';
            }
        }
    });
}