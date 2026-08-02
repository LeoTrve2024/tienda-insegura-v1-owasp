/** Página de detalle de producto */

document.addEventListener('DOMContentLoaded', async () => {
  Auth.syncUserFromToken();
  Auth.updateNavbar();
  Cart.updateBadge();

  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');
  const container = document.getElementById('product-container');

  if (!id) {
    container.innerHTML = '<div class="empty-state"><div class="empty-state__title">Producto no encontrado</div></div>';
    return;
  }

  try {
    const res = await API.obtenerProducto(id);
    const product = res.data;

    if (!product) {
      container.innerHTML = '<div class="empty-state"><div class="empty-state__title">Producto no encontrado</div></div>';
      return;
    }

    document.title = `${product.nombre} — Tienda Insegura`;

    container.innerHTML = `
      <div class="product-detail">
        <div class="product-detail__image-wrap">
          <img class="product-detail__image" src="${productImageUrl(product)}" alt="${escapeHtml(product.nombre)}">
        </div>
        <div class="product-detail__info">
          <div class="product-detail__category">${escapeHtml(categoryLabel(product))}</div>
          <h1 class="product-detail__title heading-serif">${escapeHtml(product.nombre)}</h1>
          <div class="product-detail__price">${formatPrice(product.precio)}</div>
          <p class="product-detail__desc">${escapeHtml(product.descripcion || 'Sin descripción disponible.')}</p>
          <p class="text-sm text-muted" style="margin-bottom:1.5rem">${product.stock} unidades disponibles</p>

          <div class="product-detail__actions">
            <div class="qty-control">
              <button type="button" id="qty-minus">−</button>
              <input type="number" id="qty-input" value="1" min="1" max="${product.stock}">
              <button type="button" id="qty-plus">+</button>
            </div>
            <button class="btn btn--primary btn--lg" id="add-cart-btn" style="flex:1">
              Agregar al carrito
            </button>
          </div>

          <a href="index.html" class="btn btn--ghost" style="margin-top:1rem;display:inline-flex">
            ← Seguir comprando
          </a>
        </div>
      </div>
    `;

    const qtyInput = document.getElementById('qty-input');
    document.getElementById('qty-minus').addEventListener('click', () => {
      qtyInput.value = Math.max(1, parseInt(qtyInput.value) - 1);
    });
    document.getElementById('qty-plus').addEventListener('click', () => {
      qtyInput.value = Math.min(product.stock, parseInt(qtyInput.value) + 1);
    });

    document.getElementById('add-cart-btn').addEventListener('click', async () => {
      const qty = parseInt(qtyInput.value) || 1;
      const btn = document.getElementById('add-cart-btn');
      btn.disabled = true;
      try {
        await Cart.add(product.id, qty);
      } catch (err) {
        showToast(err.message, 'error');
      } finally {
        btn.disabled = false;
      }
    });
  } catch (err) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state__icon">⚠️</div>
        <div class="empty-state__title">Error al cargar producto</div>
        <p>${escapeHtml(err.message)}</p>
      </div>
    `;
  }
});
