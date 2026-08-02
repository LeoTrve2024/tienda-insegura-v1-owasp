/** Gestión de sesión JWT en localStorage */

const Auth = {
  getToken() {
    return localStorage.getItem(CONFIG.TOKEN_KEY);
  },

  setToken(token) {
    localStorage.setItem(CONFIG.TOKEN_KEY, token);
  },

  getUser() {
    const raw = localStorage.getItem(CONFIG.USER_KEY);
    try {
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  },

  setUser(user) {
    localStorage.setItem(CONFIG.USER_KEY, JSON.stringify(user));
  },

  isLoggedIn() {
    return !!this.getToken();
  },

  isAdmin() {
    const user = this.getUser();
    return user?.role === 'ADMIN';
  },

  logout() {
    localStorage.removeItem(CONFIG.TOKEN_KEY);
    localStorage.removeItem(CONFIG.USER_KEY);
    window.location.href = 'login.html';
  },

  /** Decodifica el payload del JWT (sin verificar firma — solo UI) */
  decodeToken() {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      let base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      while (base64.length % 4) base64 += '=';
      return JSON.parse(atob(base64));
    } catch {
      return null;
    }
  },

  syncUserFromToken() {
    const claims = this.decodeToken();
    if (claims) {
      this.setUser({
        username: claims.sub,
        role: claims.role,
        userId: claims.userId,
      });
    }
  },

  requireAuth(redirectTo = 'login.html') {
    if (!this.isLoggedIn()) {
      window.location.href = `${redirectTo}?redirect=${encodeURIComponent(window.location.pathname.split('/').pop())}`;
      return false;
    }
    return true;
  },

  updateNavbar() {
    const navAuth = document.getElementById('nav-auth');
    const navAdmin = document.getElementById('nav-admin');
    const navUser = document.getElementById('nav-user');
    if (!navAuth) return;

    if (this.isLoggedIn()) {
      const user = this.getUser() || {};
      navAuth.innerHTML = `
        <span class="nav-user-badge">${escapeHtml(user.username || 'Usuario')}</span>
        <button class="btn btn--ghost btn--sm" id="btn-logout">Salir</button>
      `;
      document.getElementById('btn-logout')?.addEventListener('click', () => this.logout());

      if (navUser) navUser.textContent = user.username || '';
      if (navAdmin) navAdmin.style.display = this.isAdmin() ? '' : 'none';
    } else {
      navAuth.innerHTML = `
        <a href="login.html" class="btn btn--ghost btn--sm">Ingresar</a>
        <a href="registro.html" class="btn btn--primary btn--sm">Registrarse</a>
      `;
      if (navAdmin) navAdmin.style.display = 'none';
    }
  },
};
