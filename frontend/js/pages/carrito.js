/** Página del carrito de compras */

async function renderCart() {
  const layout = document.getElementById('cart-layout');
  const cartIdLabel = document.getElementById('cart-id-label');
  const cartId = Cart.getId();

  cartIdLabel.textContent = `ID: ${cartId}`;

  try {
    const [itemsRes, totalRes, productsRes] = await Promise.all([
      API.verCarrito(cartId),
      API.totalCarrito(cartId),
      API.listarProductos(),
    ]);

    const items = itemsRes.data || {};
    const total = totalRes.data || 0;
    const products = productsRes.data || [];
    const productMap = Object.fromEntries(products.map(p => [p.id, p]));

    const entries = Object.entries(items);

    if (entries.length === 0) {
      layout.innerHTML = `
        <div class="empty-state" style="grid-column:1/-1">
          <div class="empty-state__icon">🛒</div>
          <div class="empty-state__title">Tu carrito está vacío</div>
          <p>Agrega productos desde el catálogo</p>
          <a href="index.html" class="btn btn--primary" style="margin-top:1.5rem;display:inline-flex">Ver catálogo</a>
        </div>
      `;
      return;
    }

    const itemsHtml = entries.map(([productId, qty]) => {
      const product = productMap[productId];
      if (!product) return '';
      return `
        <div class="cart-item">
          <img class="cart-item__image" src="${productImageUrl(product)}" alt="${escapeHtml(product.nombre)}">
          <div class="cart-item__info">
            <div class="cart-item__name">${escapeHtml(product.nombre)}</div>
            <div class="text-sm text-muted">${formatPrice(product.precio)} × ${qty}</div>
          </div>
          <div style="font-weight:700">${formatPrice(product.precio * qty)}</div>
        </div>
      `;
    }).join('');

    layout.innerHTML = `
      <div class="cart-items">${itemsHtml}</div>
      <div class="cart-summary">
        <div class="cart-summary__title">Resumen</div>
        <div class="cart-summary__row">
          <span>Subtotal (${entries.length} producto${entries.length !== 1 ? 's' : ''})</span>
          <span>${formatPrice(total)}</span>
        </div>
        <div class="cart-summary__row">
          <span>Envío</span>
          <span>Gratis</span>
        </div>
        <div class="cart-summary__total">
          <span>Total</span>
          <span>${formatPrice(total)}</span>
        </div>
        <button class="btn btn--primary btn--block btn--lg" id="checkout-btn" style="margin-top:1.5rem">
          Confirmar pedido
        </button>
        ${!Auth.isLoggedIn() ? '<p class="text-sm text-muted" style="margin-top:0.75rem;text-align:center">Debes iniciar sesión para confirmar</p>' : ''}
      </div>
    `;

    document.getElementById('checkout-btn').addEventListener('click', async () => {
      if (!Auth.isLoggedIn()) {
        window.location.href = 'login.html?redirect=carrito.html';
        return;
      }

      const btn = document.getElementById('checkout-btn');
      btn.disabled = true;
      btn.textContent = 'Procesando...';

      try {
        const res = await API.checkout(cartId);
        Cart.reset();
        showToast('¡Pedido confirmado!', 'success');
        setTimeout(() => {
          window.location.href = `pedido.html?id=${res.data.id}`;
        }, 1000);
      } catch (err) {
        showToast(err.message, 'error');
        btn.disabled = false;
        btn.textContent = 'Confirmar pedido';
      }
    });
  } catch (err) {
    layout.innerHTML = `
      <div class="empty-state" style="grid-column:1/-1">
        <div class="empty-state__title">Error al cargar carrito</div>
        <p>${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  Auth.syncUserFromToken();
  Auth.updateNavbar();
  Cart.updateBadge();
  renderCart();
});
