/** Página de registro */

document.addEventListener('DOMContentLoaded', () => {
  if (Auth.isLoggedIn()) {
    window.location.href = 'index.html';
    return;
  }

  const form = document.getElementById('registro-form');
  const errorEl = document.getElementById('registro-error');
  const btn = document.getElementById('registro-btn');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorEl.style.display = 'none';
    btn.disabled = true;
    btn.textContent = 'Creando cuenta...';

    try {
      const data = {
        fullName: document.getElementById('fullName').value.trim(),
        username: document.getElementById('username').value.trim(),
        email: document.getElementById('email').value.trim(),
        password: document.getElementById('password').value,
        role: document.getElementById('role').value,
      };

      const res = await API.registro(data);

      if (res.success) {
        showToast('Cuenta creada. Ahora puedes ingresar.', 'success');
        setTimeout(() => {
          window.location.href = 'login.html';
        }, 1200);
      } else {
        throw new Error(res.message || 'Error al registrar');
      }
    } catch (err) {
      errorEl.textContent = err.message;
      errorEl.style.display = 'block';
    } finally {
      btn.disabled = false;
      btn.textContent = 'Crear cuenta';
    }
  });
});
