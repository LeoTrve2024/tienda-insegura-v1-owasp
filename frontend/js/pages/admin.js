/** Panel de administración — demuestra A01, A04, A05, A08 */

const AdminPanels = {
  async usuarios() {
    const res = await API.listarUsuarios();
    const users = res.data || [];

    const rows = users.map(u => `
      <tr>
        <td>${u.id}</td>
        <td>${escapeHtml(u.username)}</td>
        <td>${escapeHtml(u.email)}</td>
        <td class="password-cell">${escapeHtml(u.password)}</td>
        <td><span class="order-status ${u.role === 'ADMIN' ? 'order-status--completado' : 'order-status--pendiente'}">${escapeHtml(u.role)}</span></td>
        <td class="text-sm text-muted">${formatDate(u.createdAt)}</td>
      </tr>
    `).join('');

    return `
      <div class="admin-panel">
        <h2 class="admin-panel__title">Usuarios registrados</h2>
        <p class="admin-panel__desc">
          A04: Las contraseñas se muestran en texto plano porque el backend no las oculta con @JsonIgnore.
        </p>
        <div style="overflow-x:auto">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Usuario</th>
                <th>Email</th>
                <th>Password ⚠️</th>
                <th>Rol</th>
                <th>Registro</th>
              </tr>
            </thead>
            <tbody>${rows}</tbody>
          </table>
        </div>
      </div>
    `;
  },

  async pedidos() {
    const res = await API.listarPedidosAdmin();
    const orders = res.data || [];

    const rows = orders.map(o => `
      <tr>
        <td>${o.id}</td>
        <td>${escapeHtml(o.usuario?.username || '—')}</td>
        <td>${formatPrice(o.total)}</td>
        <td><span class="order-status order-status--${o.estado?.toLowerCase()}">${escapeHtml(o.estado)}</span></td>
        <td class="text-sm text-muted">${formatDate(o.createdAt)}</td>
      </tr>
    `).join('');

    return `
      <div class="admin-panel">
        <h2 class="admin-panel__title">Todos los pedidos</h2>
        <p class="admin-panel__desc">A01: Cualquier usuario autenticado puede ver todos los pedidos del sistema.</p>
        <div style="overflow-x:auto">
          <table class="data-table">
            <thead>
              <tr><th>ID</th><th>Cliente</th><th>Total</th><th>Estado</th><th>Fecha</th></tr>
            </thead>
            <tbody>${rows || '<tr><td colspan="5">Sin pedidos</td></tr>'}</tbody>
          </table>
        </div>
      </div>
    `;
  },

  diagnostico() {
    return `
      <div class="admin-panel">
        <h2 class="admin-panel__title">Diagnóstico de conectividad</h2>
        <p class="admin-panel__desc">
          A05 — OS Command Injection: el parámetro <code>host</code> se concatena directamente en un comando del sistema.
          <br><br>
          <strong>Payloads de ejemplo (solo en laboratorio):</strong><br>
          <code>127.0.0.1; whoami</code><br>
          <code>127.0.0.1 && dir</code> (Windows) / <code>127.0.0.1 && ls</code> (Linux)
        </p>
        <div class="form-group">
          <label class="form-label" for="ping-host">Host a verificar</label>
          <input type="text" id="ping-host" class="form-input" placeholder="127.0.0.1" value="127.0.0.1">
        </div>
        <button class="btn btn--primary" id="ping-btn">Ejecutar ping</button>
        <pre id="ping-result" style="margin-top:1.5rem;padding:1rem;background:#1a1814;color:#a3e635;border-radius:var(--radius-md);font-size:0.8125rem;white-space:pre-wrap;min-height:80px;display:none"></pre>
      </div>
    `;
  },

  upload() {
    return `
      <div class="admin-panel">
        <h2 class="admin-panel__title">Subir imagen de producto</h2>
        <p class="admin-panel__desc">
          A08 — Unrestricted File Upload: no se valida tipo MIME ni extensión. Se usa el nombre original del archivo.
        </p>
        <div class="form-group">
          <label class="form-label" for="upload-product-id">ID del producto</label>
          <input type="number" id="upload-product-id" class="form-input" value="1" min="1">
        </div>
        <div class="form-group">
          <label class="form-label" for="upload-file">Archivo</label>
          <input type="file" id="upload-file" class="form-input">
        </div>
        <button class="btn btn--primary" id="upload-btn">Subir archivo</button>
        <pre id="upload-result" style="margin-top:1rem;font-size:0.875rem;color:var(--color-text-muted);display:none"></pre>
      </div>
    `;
  },
};

async function showPanel(name) {
  const content = document.getElementById('admin-content');
  content.innerHTML = '<div class="spinner"></div>';

  document.querySelectorAll('.admin-nav__link').forEach(link => {
    link.classList.toggle('admin-nav__link--active', link.dataset.panel === name);
  });

  try {
    const html = typeof AdminPanels[name] === 'function'
      ? await AdminPanels[name]()
      : '<div class="empty-state">Panel no encontrado</div>';

    content.innerHTML = html;
    bindPanelEvents(name);
  } catch (err) {
    content.innerHTML = `
      <div class="admin-panel">
        <div class="empty-state">
          <div class="empty-state__title">Error</div>
          <p>${escapeHtml(err.message)}</p>
        </div>
      </div>
    `;
  }
}

function bindPanelEvents(name) {
  if (name === 'diagnostico') {
    document.getElementById('ping-btn')?.addEventListener('click', async () => {
      const host = document.getElementById('ping-host').value;
      const result = document.getElementById('ping-result');
      const btn = document.getElementById('ping-btn');

      btn.disabled = true;
      btn.textContent = 'Ejecutando...';
      result.style.display = 'block';
      result.textContent = 'Esperando respuesta...';

      try {
        const res = await API.pingHost(host);
        result.textContent = res.data || res.message || 'Sin respuesta';
      } catch (err) {
        result.textContent = `Error: ${err.message}`;
      } finally {
        btn.disabled = false;
        btn.textContent = 'Ejecutar ping';
      }
    });
  }

  if (name === 'upload') {
    document.getElementById('upload-btn')?.addEventListener('click', async () => {
      const productId = document.getElementById('upload-product-id').value;
      const file = document.getElementById('upload-file').files[0];
      const result = document.getElementById('upload-result');

      if (!file) {
        showToast('Selecciona un archivo', 'error');
        return;
      }

      try {
        const res = await API.subirImagen(productId, file);
        result.style.display = 'block';
        result.textContent = res.message + ': ' + (res.data || '');
        showToast('Archivo subido', 'success');
      } catch (err) {
        showToast(err.message, 'error');
      }
    });
  }
}

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.requireAuth()) return;

  Auth.syncUserFromToken();
  Auth.updateNavbar();

  document.querySelectorAll('.admin-nav__link').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      showPanel(link.dataset.panel);
    });
  });

  showPanel('usuarios');
});
