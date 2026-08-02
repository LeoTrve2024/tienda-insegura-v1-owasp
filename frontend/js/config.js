/**
 * Configuración global del frontend.
 *
 * - Con Docker/Nginx (puerto 3000), usa el proxy relativo /api.
 * - Con Live Server u otro servidor de desarrollo, llama al backend
 *   directamente en http://127.0.0.1:8080/api.
 * - Se puede sobrescribir en consola antes de cargar la app con:
 *     localStorage.setItem('tienda_api_base', 'http://127.0.0.1:8080/api')
 *
 * La V1 mantiene CORS abierto a propósito. La V2 usará HTTPS y una
 * lista blanca de orígenes.
 */
const API_BASE_GUARDADA = localStorage.getItem('tienda_api_base');

const EJECUTANDO_EN_PROXY_DOCKER =
  window.location.port === '3000' ||
  window.location.protocol === 'https:';

const CONFIG = {
  API_BASE:
    API_BASE_GUARDADA ||
    (EJECUTANDO_EN_PROXY_DOCKER
      ? '/api'
      : 'http://127.0.0.1:8080/api'),
  CART_KEY: 'tienda_cart_id',
  TOKEN_KEY: 'tienda_token',
  USER_KEY: 'tienda_user',
};
