/** Cliente HTTP para la API REST del backend */

async function apiRequest(path, options = {}) {
  const url = `${CONFIG.API_BASE}${path}`;
  const headers = { 'Content-Type': 'application/json', ...options.headers };

  const token = Auth.getToken();
  if (token && !options.skipAuth) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(url, { ...options, headers });
  let body = null;

  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    body = await response.json();
  } else {
    body = await response.text();
  }

  if (!response.ok) {
    const msg =
      typeof body === 'object' && body?.message
        ? body.message
        : `Error ${response.status}`;
    throw new Error(msg);
  }

  return body;
}

const API = {
  // Auth
  login: (username, password) =>
    apiRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
      skipAuth: true,
    }),

  registro: (data) =>
    apiRequest('/auth/registro', {
      method: 'POST',
      body: JSON.stringify(data),
      skipAuth: true,
    }),

  // Productos
  listarProductos: () => apiRequest('/productos', { skipAuth: true }),

  buscarProductos: (q) =>
    apiRequest(`/productos/buscar?q=${encodeURIComponent(q)}`, { skipAuth: true }),

  obtenerProducto: (id) =>
    apiRequest(`/productos/${id}`, { skipAuth: true }),

  subirImagen: async (productoId, file) => {
    const formData = new FormData();
    formData.append('archivo', file);

    const headers = {};
    const token = Auth.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(
      `${CONFIG.API_BASE}/productos/${productoId}/imagen`,
      { method: 'POST', headers, body: formData }
    );

    const body = await response.json();
    if (!response.ok) throw new Error(body.message || 'Error al subir imagen');
    return body;
  },

  // Carrito
  verCarrito: (cartId) =>
    apiRequest(`/carrito/${cartId}`, { skipAuth: true }),

  agregarAlCarrito: (cartId, productoId, cantidad) =>
    apiRequest(`/carrito/${cartId}/agregar`, {
      method: 'POST',
      body: JSON.stringify({ productoId, cantidad }),
      skipAuth: true,
    }),

  totalCarrito: (cartId) =>
    apiRequest(`/carrito/${cartId}/total`, { skipAuth: true }),

  // Pedidos
  checkout: (cartId) =>
    apiRequest(`/pedidos/checkout/${cartId}`, { method: 'POST' }),

  misPedidos: () => apiRequest('/pedidos/mis-pedidos'),

  obtenerPedido: (id) => apiRequest(`/pedidos/${id}`),

  // Admin
  listarUsuarios: () => apiRequest('/admin/usuarios'),

  listarPedidosAdmin: () => apiRequest('/admin/pedidos'),

  pingHost: (host) =>
    apiRequest(`/admin/reportes/ping?host=${encodeURIComponent(host)}`),
};
