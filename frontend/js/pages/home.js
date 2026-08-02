/** Página principal — catálogo y búsqueda de productos */

function renderProductCard(product) {
  return `
    <article class="product-card" onclick="window.location.href='producto.html?id=${product.id}'">
      <img class="product-card__image" src="${productImageUrl(product)}" alt="${escapeHtml(product.nombre)}" loading="lazy">
      <div class="product-card__body">
        <div class="product-card__category">${escapeHtml(categoryLabel(product))}</div>
        <h3 class="product-card__name">${escapeHtml(product.nombre)}</h3>
        <div class="product-card__footer">
          <span class="product-card__price">${formatPrice(product.precio)}</span>
          <span class="product-card__stock">${product.stock} en stock</span>
        </div>
      </div>
    </article>
  `;
}

async function loadProducts(query = '') {
  const grid = document.getElementById('product-grid');
  const title = document.getElementById('section-title');
  const count = document.getElementById('product-count');

  setLoading(grid, true);
  grid.innerHTML = '<div class="spinner"></div>';

  try {
    const res = query
      ? await API.buscarProductos(query)
      : await API.listarProductos();

    const products = res.data || [];

    if (query) {
      title.textContent = `Resultados para "${query}"`;
    } else {
      title.textContent = 'Todos los productos';
    }

    count.textContent = `${products.length} producto${products.length !== 1 ? 's' : ''}`;

    if (products.length === 0) {
      grid.innerHTML = `
        <div class="empty-state" style="grid-column: 1/-1">
          <div class="empty-state__icon">🔍</div>
          <div class="empty-state__title">No se encontraron productos</div>
          <p>Intenta con otro término de búsqueda</p>
        </div>
      `;
      return;
    }

    grid.innerHTML = products.map(renderProductCard).join('');
  } catch (err) {
    grid.innerHTML = `
      <div class="empty-state" style="grid-column: 1/-1">
        <div class="empty-state__icon">⚠️</div>
        <div class="empty-state__title">Error al cargar productos</div>
        <p>${escapeHtml(err.message)}</p>
        <p class="text-sm text-muted" style="margin-top:0.5rem">¿Está el backend corriendo en localhost:8080?</p>
      </div>
    `;
  } finally {
    setLoading(grid, false);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  Auth.syncUserFromToken();
  Auth.updateNavbar();
  Cart.updateBadge();

  const params = new URLSearchParams(window.location.search);
  const initialQuery = params.get('q') || '';
  const searchInput = document.getElementById('search-input');

  if (initialQuery) searchInput.value = initialQuery;
  loadProducts(initialQuery);

  const doSearch = debounce(() => {
    const q = searchInput.value.trim();
    const url = q ? `index.html?q=${encodeURIComponent(q)}` : 'index.html';
    window.history.replaceState({}, '', url);
    loadProducts(q);
  }, 400);

  searchInput.addEventListener('input', doSearch);
  document.getElementById('search-btn').addEventListener('click', () => {
    const q = searchInput.value.trim();
    window.history.replaceState({}, '', q ? `index.html?q=${encodeURIComponent(q)}` : 'index.html');
    loadProducts(q);
  });
});
