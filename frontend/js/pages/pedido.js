/** Página de detalle de pedido (demuestra IDOR) */

document.addEventListener('DOMContentLoaded', async () => {
  if (!Auth.requireAuth()) return;

  Auth.syncUserFromToken();
  Auth.updateNavbar();

  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');
  const container = document.getElementById('pedido-container');

  if (!id) {
    container.innerHTML = '<div class="empty-state"><div class="empty-state__title">Pedido no especificado</div></div>';
    return;
  }

  try {
    const res = await API.obtenerPedido(id);
    const order = res.data;

    if (!order) {
      container.innerHTML = '<div class="empty-state"><div class="empty-state__title">Pedido no encontrado</div></div>';
      return;
    }

    const currentUser = Auth.getUser();
    const isOwner = order.usuario?.id === currentUser?.userId;

    const itemsHtml = (order.items || []).map(item => `
      <tr>
        <td>${escapeHtml(item.producto?.nombre || 'Producto')}</td>
        <td>${item.cantidad}</td>
        <td>${formatPrice(item.precioUnitario)}</td>
        <td>${formatPrice(item.precioUnitario * item.cantidad)}</td>
      </tr>
    `).join('');

    container.innerHTML = `
      ${!isOwner ? `
        <div class="alert alert--danger">
          <span>🚨</span>
          <div>
            <strong>¡Vulnerabilidad IDOR detectada!</strong><br>
            Estás viendo el pedido #${order.id} de otro usuario (${escapeHtml(order.usuario?.username || 'desconocido')}).
            El backend no validó que seas el dueño del pedido.
          </div>
        </div>
      ` : ''}

      <div class="section__header">
        <h1 class="section__title heading-serif">Pedido #${order.id}</h1>
        <span class="order-status order-status--${order.estado?.toLowerCase() || 'pendiente'}">${escapeHtml(order.estado)}</span>
      </div>

      <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;margin-bottom:2rem">
        <div class="cart-summary" style="position:static">
          <div class="cart-summary__title">Información</div>
          <div class="cart-summary__row"><span>Fecha</span><span>${formatDate(order.createdAt)}</span></div>
          <div class="cart-summary__row"><span>Cliente</span><span>${escapeHtml(order.usuario?.username || '—')}</span></div>
          <div class="cart-summary__row"><span>Email</span><span>${escapeHtml(order.usuario?.email || '—')}</span></div>
          <div class="cart-summary__total"><span>Total</span><span>${formatPrice(order.total)}</span></div>
        </div>
      </div>

      <div class="admin-panel">
        <div class="admin-panel__title">Productos</div>
        <table class="data-table">
          <thead>
            <tr>
              <th>Producto</th>
              <th>Cantidad</th>
              <th>Precio unit.</th>
              <th>Subtotal</th>
            </tr>
          </thead>
          <tbody>${itemsHtml || '<tr><td colspan="4">Sin items</td></tr>'}</tbody>
        </table>
      </div>

      <div style="margin-top:1.5rem;display:flex;gap:1rem">
        <a href="pedidos.html" class="btn btn--ghost">← Volver a mis pedidos</a>
        <div class="form-group" style="margin:0;flex:1;max-width:200px">
          <input type="number" id="idor-test" class="form-input" placeholder="Probar ID..." min="1" value="${parseInt(id) + 1}">
        </div>
        <button class="btn btn--ghost" id="idor-btn">Probar IDOR</button>
      </div>
    `;

    document.getElementById('idor-btn').addEventListener('click', () => {
      const testId = document.getElementById('idor-test').value;
      if (testId) window.location.href = `pedido.html?id=${testId}`;
    });
  } catch (err) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state__title">Error al cargar pedido</div>
        <p>${escapeHtml(err.message)}</p>
      </div>
    `;
  }
});
