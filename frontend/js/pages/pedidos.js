/** Página de listado de pedidos del usuario */

document.addEventListener('DOMContentLoaded', async () => {
  if (!Auth.requireAuth()) return;

  Auth.syncUserFromToken();
  Auth.updateNavbar();
  Cart.updateBadge();

  const list = document.getElementById('orders-list');

  try {
    const res = await API.misPedidos();
    const orders = res.data || [];

    if (orders.length === 0) {
      list.innerHTML = `
        <div class="empty-state">
          <div class="empty-state__icon">📦</div>
          <div class="empty-state__title">Aún no tienes pedidos</div>
          <p>Realiza tu primera compra desde el catálogo</p>
          <a href="index.html" class="btn btn--primary" style="margin-top:1.5rem;display:inline-flex">Ver catálogo</a>
        </div>
      `;
      return;
    }

    list.innerHTML = orders.map(order => `
      <div class="order-card" onclick="window.location.href='pedido.html?id=${order.id}'">
        <div class="order-card__header">
          <span class="order-card__id">Pedido #${order.id}</span>
          <span class="order-status order-status--${order.estado?.toLowerCase() || 'pendiente'}">${escapeHtml(order.estado)}</span>
        </div>
        <div class="text-sm text-muted">${formatDate(order.createdAt)}</div>
        <div style="margin-top:0.5rem;font-weight:700;font-size:1.125rem">${formatPrice(order.total)}</div>
      </div>
    `).join('');
  } catch (err) {
    list.innerHTML = `
      <div class="empty-state">
        <div class="empty-state__title">Error al cargar pedidos</div>
        <p>${escapeHtml(err.message)}</p>
      </div>
    `;
  }
});
