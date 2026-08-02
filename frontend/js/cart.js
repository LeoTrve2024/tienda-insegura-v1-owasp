/** Gestión del carrito de compras (cartId en localStorage) */

const Cart = {
  getId() {
    let id = localStorage.getItem(CONFIG.CART_KEY);
    if (!id) {
      id = generateCartId();
      localStorage.setItem(CONFIG.CART_KEY, id);
    }
    return id;
  },

  reset() {
    localStorage.removeItem(CONFIG.CART_KEY);
  },

  async add(productoId, cantidad = 1) {
    const cartId = this.getId();
    await API.agregarAlCarrito(cartId, productoId, cantidad);
    await this.updateBadge();
    showToast('Producto agregado al carrito', 'success');
  },

  async getItems() {
    const cartId = this.getId();
    const res = await API.verCarrito(cartId);
    return res.data || {};
  },

  async getTotal() {
    const cartId = this.getId();
    const res = await API.totalCarrito(cartId);
    return res.data || 0;
  },

  async updateBadge() {
    const badge = document.getElementById('cart-badge');
    if (!badge) return;

    try {
      const items = await this.getItems();
      const count = Object.values(items).reduce((a, b) => a + b, 0);
      badge.textContent = count;
      badge.style.display = count > 0 ? 'flex' : 'none';
    } catch {
      badge.style.display = 'none';
    }
  },
};
