/** Página de login */

document.addEventListener('DOMContentLoaded', () => {
  if (Auth.isLoggedIn()) {
    window.location.href = 'index.html';
    return;
  }

  const form = document.getElementById('login-form');
  const errorEl = document.getElementById('login-error');
  const btn = document.getElementById('login-btn');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorEl.style.display = 'none';
    btn.disabled = true;
    btn.textContent = 'Ingresando...';

    try {
      const username = document.getElementById('username').value.trim();
      const password = document.getElementById('password').value;

      const res = await API.login(username, password);

      if (res.success && res.data?.token) {
        Auth.setToken(res.data.token);
        Auth.syncUserFromToken();
        showToast('¡Bienvenido de vuelta!', 'success');

        const params = new URLSearchParams(window.location.search);
        const redirect = params.get('redirect') || 'index.html';
        window.location.href = redirect;
      } else {
        throw new Error(res.message || 'Credenciales incorrectas');
      }
    } catch (err) {
      errorEl.textContent = err.message;
      errorEl.style.display = 'block';
    } finally {
      btn.disabled = false;
      btn.textContent = 'Ingresar';
    }
  });
});
