const GATEWAY_URL = 'http://localhost:8080';
const BACKEND_PORT = 3000;

// State management
let token = localStorage.getItem('token') || null;
let user = JSON.parse(localStorage.getItem('user')) || null;
let cart = [];
let defaultCategoryId = null;

// On load
document.addEventListener('DOMContentLoaded', () => {
  updateAuthUI();
  fetchStatus();
  setInterval(fetchStatus, 15000); // Poll status every 15s
  
  if (token) {
    fetchProducts();
    fetchOrders();
    setInterval(fetchOrders, 4000); // Poll orders every 4s to track Saga state
  } else {
    // If not logged in, we can still fetch products (since they are open GET)
    fetchProducts();
  }
});

// Toast Notifications
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  
  let icon = 'fa-circle-info';
  if (type === 'success') icon = 'fa-circle-check';
  if (type === 'error') icon = 'fa-circle-xmark';
  
  toast.innerHTML = `
    <i class="fa-solid ${icon}"></i>
    <span>${message}</span>
  `;
  container.appendChild(toast);
  
  setTimeout(() => {
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// System Status and Orchestration
async function fetchStatus() {
  const servicesList = document.getElementById('services-list');
  try {
    const res = await fetch('/api/status');
    const data = await res.json();
    
    servicesList.innerHTML = '';
    data.forEach(svc => {
      const item = document.createElement('div');
      item.className = 'service-item';
      
      const isUp = svc.status === 'UP';
      const badgeClass = isUp ? 'badge-success' : 'badge-error';
      const icon = isUp ? '<span class="pulsing-dot"></span>' : '<i class="fa-solid fa-circle-xmark"></i>';
      
      item.innerHTML = `
        <div class="service-info">
          <span class="service-name">${svc.name}</span>
          <span class="service-port">Port ${svc.port}</span>
        </div>
        <span class="badge ${badgeClass}">${icon} ${svc.status}</span>
      `;
      servicesList.appendChild(item);
    });
  } catch (error) {
    servicesList.innerHTML = `
      <div class="empty-state-mini">
        <p style="color:var(--danger)"><i class="fa-solid fa-circle-exclamation"></i> Dashboard server disconnected.</p>
      </div>
    `;
  }
}

async function startServices() {
  showToast('Starting all services and infrastructure... This can take up to 60 seconds.', 'info');
  try {
    await fetch('/api/start', { method: 'POST' });
    setTimeout(fetchStatus, 5000);
  } catch (error) {
    showToast('Failed to trigger start command.', 'error');
  }
}

async function stopServices() {
  showToast('Stopping all services...', 'warning');
  try {
    await fetch('/api/stop', { method: 'POST' });
    setTimeout(fetchStatus, 3000);
  } catch (error) {
    showToast('Failed to trigger stop command.', 'error');
  }
}

// Auth UI and Modals
function showAuthModal() {
  document.getElementById('auth-modal').classList.add('active');
}

function closeAuthModal() {
  document.getElementById('auth-modal').classList.remove('active');
}

function switchAuthTab(tab) {
  const isLogin = tab === 'login';
  document.getElementById('tab-login').classList.toggle('active', isLogin);
  document.getElementById('tab-register').classList.toggle('active', !isLogin);
  document.getElementById('login-form').style.display = isLogin ? 'block' : 'none';
  document.getElementById('register-form').style.display = isLogin ? 'none' : 'block';
}

function updateAuthUI() {
  const container = document.getElementById('auth-status-container');
  const addBtn = document.getElementById('btn-add-product');
  
  if (token && user) {
    const isPrivileged = user.role === 'ADMIN' || user.role === 'SELLER';
    if (isPrivileged) {
      addBtn.style.display = 'block';
    } else {
      addBtn.style.display = 'none';
    }
    
    container.innerHTML = `
      <div style="display:flex; align-items:center; gap: 0.75rem;">
        <span class="badge badge-success"><i class="fa-solid fa-user"></i> ${user.name} (${user.role})</span>
        <button class="btn btn-secondary btn-sm" onclick="logout()"><i class="fa-solid fa-right-from-bracket"></i> Logout</button>
      </div>
    `;
  } else {
    addBtn.style.display = 'none';
    container.innerHTML = `
      <span class="badge badge-error"><i class="fa-solid fa-user-slash"></i> Anonymous</span>
      <button class="btn btn-secondary btn-sm" onclick="showAuthModal()"><i class="fa-solid fa-right-to-bracket"></i> Login / Register</button>
    `;
  }
  updateCheckoutButton();
}

async function handleLogin(e) {
  e.preventDefault();
  const email = document.getElementById('login-email').value;
  const password = document.getElementById('login-password').value;
  
  try {
    const res = await fetch(`${GATEWAY_URL}/api/v1/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    
    if (!res.ok) throw new Error('Invalid credentials');
    
    const data = await res.json();
    token = data.token;
    // Decrypt or parse details from jwt
    const payload = JSON.parse(atob(token.split('.')[1]));
    user = {
      id: payload.sub,
      email: payload.email,
      name: payload.name || email.split('@')[0],
      role: payload.role
    };
    
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    
    showToast('Logged in successfully!', 'success');
    closeAuthModal();
    updateAuthUI();
    fetchProducts();
    fetchOrders();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const name = document.getElementById('reg-name').value;
  const email = document.getElementById('reg-email').value;
  const password = document.getElementById('reg-password').value;
  const role = document.getElementById('reg-role').value;
  
  try {
    const res = await fetch(`${GATEWAY_URL}/api/v1/auth/signup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password, role })
    });
    
    if (!res.ok) throw new Error('Registration failed');
    
    showToast('Registration successful! Please login.', 'success');
    switchAuthTab('login');
  } catch (error) {
    showToast(error.message, 'error');
  }
}

function logout() {
  token = null;
  user = null;
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  cart = [];
  renderCart();
  updateAuthUI();
  document.getElementById('orders-list').innerHTML = `
    <div class="empty-state-mini">
      <p>Log in to view your orders.</p>
    </div>
  `;
  showToast('Logged out.', 'info');
}

// Product Management
async function fetchProducts(keyword = '') {
  const grid = document.getElementById('products-grid');
  try {
    let url = `${GATEWAY_URL}/api/v1/products?size=50`;
    if (keyword) {
      url += `&keyword=${encodeURIComponent(keyword)}`;
    }
    
    const res = await fetch(url);
    if (!res.ok) throw new Error('Could not fetch products');
    
    const data = await res.json();
    const products = data.content || [];
    
    if (products.length === 0) {
      grid.innerHTML = `
        <div class="empty-state">
          <i class="fa-solid fa-box-open"></i>
          <p>No products match your search or catalogue is empty.</p>
        </div>
      `;
      return;
    }
    
    grid.innerHTML = '';
    products.forEach(prod => {
      const item = document.createElement('div');
      item.className = 'product-item';
      
      item.innerHTML = `
        <span class="product-sku">${prod.sku}</span>
        <h3 class="product-name" title="${prod.name}">${prod.name}</h3>
        <p class="product-desc">${prod.description || 'No description provided.'}</p>
        <div class="product-footer">
          <span class="product-price">$${prod.price.toFixed(2)}</span>
          <button class="btn btn-primary btn-sm" onclick="addToCart('${prod.id}', '${prod.name.replace(/'/g, "\\'")}', ${prod.price})">
            <i class="fa-solid fa-cart-plus"></i> Add
          </button>
        </div>
      `;
      grid.appendChild(item);
    });
  } catch (error) {
    grid.innerHTML = `
      <div class="empty-state">
        <i class="fa-solid fa-circle-exclamation" style="color:var(--danger)"></i>
        <p>Could not load products. Please make sure the Product Service and Gateway are running.</p>
      </div>
    `;
  }
}

function handleSearch() {
  const val = document.getElementById('search-input').value;
  fetchProducts(val);
}

function showAddProductModal() {
  document.getElementById('product-modal').classList.add('active');
}

function closeProductModal() {
  document.getElementById('product-modal').classList.remove('active');
}

// Auto-resolves categories so user doesn't hit constraints
async function getOrCreateCategory() {
  if (defaultCategoryId) return defaultCategoryId;
  
  try {
    const listRes = await fetch(`${GATEWAY_URL}/api/v1/categories`);
    if (listRes.ok) {
      const categories = await listRes.json();
      if (categories && categories.length > 0) {
        defaultCategoryId = categories[0].id;
        return defaultCategoryId;
      }
    }
    
    // Create new category
    const createRes = await fetch(`${GATEWAY_URL}/api/v1/categories`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ name: 'General', description: 'General Electronics' })
    });
    
    if (createRes.ok) {
      const newCat = await createRes.json();
      defaultCategoryId = newCat.id;
      return defaultCategoryId;
    }
  } catch (e) {
    console.error('Error fetching/creating category:', e);
  }
  
  return 'default-cat-id'; // Fallback
}

async function handleAddProduct(e) {
  e.preventDefault();
  if (!token) return showToast('You must be logged in as ADMIN/SELLER to create products', 'error');
  
  const name = document.getElementById('prod-name').value;
  const sku = document.getElementById('prod-sku').value;
  const price = parseFloat(document.getElementById('prod-price').value);
  const stock = parseInt(document.getElementById('prod-stock').value);
  const description = document.getElementById('prod-desc').value;
  
  try {
    const categoryId = await getOrCreateCategory();
    
    // 1. Create Product
    const res = await fetch(`${GATEWAY_URL}/api/v1/products`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ name, sku, price, description, categoryId })
    });
    
    if (!res.ok) throw new Error('Failed to create product in catalogue');
    const product = await res.json();
    
    // 2. Set stock in Inventory Service
    try {
      await fetch(`${GATEWAY_URL}/api/v1/inventory`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ productId: product.id, quantity: stock })
      });
    } catch (invErr) {
      console.warn('Failed to update inventory stock:', invErr);
    }
    
    showToast('Product added successfully!', 'success');
    closeProductModal();
    document.getElementById('product-form').reset();
    fetchProducts();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

// Cart Management
function addToCart(productId, name, price) {
  const existing = cart.find(i => i.productId === productId);
  if (existing) {
    existing.quantity += 1;
  } else {
    cart.push({ productId, name, price, quantity: 1 });
  }
  showToast(`${name} added to cart`, 'info');
  renderCart();
}

function renderCart() {
  const container = document.getElementById('cart-items');
  const totalVal = document.getElementById('cart-total-value');
  
  if (cart.length === 0) {
    container.innerHTML = `
      <div class="empty-state-mini">
        <p>Your checkout cart is empty.</p>
      </div>
    `;
    totalVal.innerText = '$0.00';
    updateCheckoutButton();
    return;
  }
  
  container.innerHTML = '';
  let total = 0;
  
  cart.forEach((item, index) => {
    const itemEl = document.createElement('div');
    itemEl.className = 'cart-item';
    itemEl.innerHTML = `
      <div>
        <span class="cart-item-name">${item.name}</span><br>
        <span class="cart-item-qty">Qty: ${item.quantity}</span>
      </div>
      <div style="display:flex; align-items:center; gap: 0.5rem;">
        <span class="cart-item-price">$${(item.price * item.quantity).toFixed(2)}</span>
        <button class="btn btn-icon btn-sm" onclick="removeFromCart(${index})" style="color:var(--danger)"><i class="fa-solid fa-trash"></i></button>
      </div>
    `;
    container.appendChild(itemEl);
    total += item.price * item.quantity;
  });
  
  totalVal.innerText = `$${total.toFixed(2)}`;
  updateCheckoutButton();
}

function removeFromCart(index) {
  cart.splice(index, 1);
  renderCart();
}

function updateCheckoutButton() {
  const btn = document.getElementById('btn-checkout');
  btn.disabled = !token || cart.length === 0;
}

// Place Order and Monitor Saga Checkout
async function checkout() {
  if (!token) return showToast('Please login to place orders', 'error');
  if (cart.length === 0) return;
  
  const orderItems = cart.map(item => ({
    productId: item.productId,
    quantity: item.quantity,
    price: item.price
  }));
  
  const orderPayload = {
    customerId: user.id,
    items: orderItems
  };
  
  try {
    const res = await fetch(`${GATEWAY_URL}/api/v1/orders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(orderPayload)
    });
    
    if (!res.ok) throw new Error('Saga Order placement failed');
    
    const orderData = await res.json();
    showToast('Saga choreography started! Order created.', 'success');
    
    // Clear cart
    cart = [];
    renderCart();
    
    // Load orders
    fetchOrders();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function fetchOrders() {
  if (!token) return;
  const container = document.getElementById('orders-list');
  
  try {
    const res = await fetch(`${GATEWAY_URL}/api/v1/orders`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    if (!res.ok) throw new Error('Could not fetch orders');
    const orders = await res.json();
    
    if (orders.length === 0) {
      container.innerHTML = `
        <div class="empty-state-mini">
          <p>No active orders placed yet.</p>
        </div>
      `;
      return;
    }
    
    // Sort orders by newest first
    orders.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    
    container.innerHTML = '';
    orders.forEach(order => {
      const orderEl = document.createElement('div');
      orderEl.className = 'order-card';
      
      const status = order.status; // PENDING, CONFIRMED, CANCELLED
      let badgeClass = 'badge-warning';
      let statusIcon = '<i class="fa-solid fa-spinner fa-spin"></i>';
      
      if (status === 'CONFIRMED') {
        badgeClass = 'badge-success';
        statusIcon = '<i class="fa-solid fa-circle-check"></i>';
      } else if (status === 'CANCELLED') {
        badgeClass = 'badge-error';
        statusIcon = '<i class="fa-solid fa-circle-xmark"></i>';
      }
      
      // Determine stages status
      const createdClass = 'success';
      let reservedClass = status === 'PENDING' ? 'active' : (status === 'CONFIRMED' ? 'success' : 'failed');
      let paidClass = status === 'PENDING' ? '' : (status === 'CONFIRMED' ? 'success' : 'failed');
      let completeClass = status === 'PENDING' ? '' : (status === 'CONFIRMED' ? 'success' : 'failed');
      
      orderEl.innerHTML = `
        <div class="order-meta">
          <span>Order ID: <span class="order-id">${order.id.substring(0, 8)}...</span></span>
          <span class="badge ${badgeClass}">${statusIcon} ${status}</span>
        </div>
        <div class="order-body">
          <span class="order-items-summary">${order.items.length} item(s)</span>
          <span>$${order.totalAmount.toFixed(2)}</span>
        </div>
        
        <!-- Saga Progress Visualizer -->
        <div class="saga-stages">
          <div class="saga-stage ${createdClass}">
            <i class="fa-solid fa-file-invoice"></i>
            <span>Created</span>
          </div>
          <div class="saga-stage ${reservedClass}">
            <i class="fa-solid fa-warehouse"></i>
            <span>Stock</span>
          </div>
          <div class="saga-stage ${paidClass}">
            <i class="fa-solid fa-money-bill-wave"></i>
            <span>Paid</span>
          </div>
          <div class="saga-stage ${completeClass}">
            <i class="fa-solid fa-flag-checkered"></i>
            <span>Finished</span>
          </div>
        </div>
      `;
      container.appendChild(orderEl);
    });
  } catch (error) {
    console.error('Error fetching orders:', error);
  }
}
