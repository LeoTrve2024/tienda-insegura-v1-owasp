/** Utilidades compartidas del frontend */

function formatPrice(value) {
  const num = typeof value === 'number' ? value : parseFloat(value);
  return new Intl.NumberFormat('es-PE', {
    style: 'currency',
    currency: 'PEN',
  }).format(num || 0);
}

function formatDate(iso) {
  if (!iso) return '—';
  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(iso));
}

function escapeHtml(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast--${type}`;
  toast.innerHTML = `
    <span class="toast__icon">${type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ'}</span>
    <span class="toast__msg">${escapeHtml(message)}</span>
  `;
  container.appendChild(toast);

  requestAnimationFrame(() => toast.classList.add('toast--visible'));

  setTimeout(() => {
    toast.classList.remove('toast--visible');
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

function setLoading(container, loading = true) {
  if (!container) return;
  container.classList.toggle('is-loading', loading);
}

function productImageUrl(product) {
  if (product?.imagenUrl) {
    if (product.imagenUrl.startsWith('http')) return product.imagenUrl;

    const path = product.imagenUrl.startsWith('/')
      ? product.imagenUrl
      : `/${product.imagenUrl}`;

    // Con Docker/Nginx, los uploads se publican por el mismo origen.
    if (CONFIG.API_BASE.startsWith('/')) return path;

    // Con Live Server, apuntar al origen real del backend.
    const backendOrigin = new URL(CONFIG.API_BASE).origin;
    return `${backendOrigin}${path}`;
  }
  return 'img/placeholder.svg';
}

function categoryLabel(product) {
  return product?.categoria?.nombre || 'General';
}

function generateCartId() {
  return 'cart_' + Date.now() + '_' + Math.random().toString(36).slice(2, 9);
}

function debounce(fn, delay = 350) {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), delay);
  };
}
