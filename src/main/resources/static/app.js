const API_BASE = '/api';
let currentUser = null;

// DOM Elements
const views = document.querySelectorAll('.view');
const navCart = document.getElementById('nav-cart');
const navLogin = document.getElementById('nav-login');
const navSignup = document.getElementById('nav-signup');
const navLogout = document.getElementById('nav-logout');
const cartCount = document.getElementById('cart-count');
const notification = document.getElementById('notification');

// Initialization
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadProducts();
    setupEventListeners();
});

function setupEventListeners() {
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('signup-form').addEventListener('submit', handleSignup);
}

// View Management
function showView(viewId) {
    views.forEach(view => {
        if (view.id === viewId) {
            view.classList.remove('hidden');
        } else {
            view.classList.add('hidden');
        }
    });

    if (viewId === 'cart-view') {
        loadCart();
    }
}

// Notification Helper
function notify(message, type = 'success') {
    notification.textContent = message;
    notification.className = `notification ${type}`;
    notification.classList.remove('hidden');
    setTimeout(() => {
        notification.classList.add('hidden');
    }, 5000);
}

// API Helper
async function apiFetch(endpoint, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include', // Crucial for cookies
    };

    const response = await fetch(`${API_BASE}${endpoint}`, { ...defaultOptions, ...options });
    
    if (response.status === 401 && !endpoint.includes('/auth/')) {
        currentUser = null;
        updateUIForAuth();
        showView('login-view');
        throw new Error('Unauthorized');
    }

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.message || 'Something went wrong');
    }
    return data;
}

// Auth Actions
async function checkAuth() {
    try {
        currentUser = await apiFetch('/auth/user');
        updateUIForAuth();
    } catch (err) {
        currentUser = null;
        updateUIForAuth();
    }
}

function updateUIForAuth() {
    if (currentUser) {
        navCart.classList.remove('hidden');
        navLogout.classList.remove('hidden');
        navLogin.classList.add('hidden');
        navSignup.classList.add('hidden');
        loadCartCount();
    } else {
        navCart.classList.add('hidden');
        navLogout.classList.add('hidden');
        navLogin.classList.remove('hidden');
        navSignup.classList.remove('hidden');
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const userName = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    try {
        currentUser = await apiFetch('/auth/signin', {
            method: 'POST',
            body: JSON.stringify({ userName, password })
        });
        notify('Login successful!');
        updateUIForAuth();
        showView('products-view');
    } catch (err) {
        notify(err.message, 'error');
    }
}

async function handleSignup(e) {
    e.preventDefault();
    const userName = document.getElementById('signup-username').value;
    const email = document.getElementById('signup-email').value;
    const password = document.getElementById('signup-password').value;

    try {
        await apiFetch('/auth/signup', {
            method: 'POST',
            body: JSON.stringify({ userName, email, password })
        });
        notify('Account created! Please login.');
        showView('login-view');
    } catch (err) {
        notify(err.message, 'error');
    }
}

async function handleLogout() {
    try {
        await apiFetch('/auth/signout', { method: 'POST' });
        currentUser = null;
        updateUIForAuth();
        showView('products-view');
        notify('Logged out.');
    } catch (err) {
        notify(err.message, 'error');
    }
}

// Product Actions
async function loadProducts() {
    const grid = document.getElementById('product-grid');
    try {
        const data = await apiFetch('/public/products');
        const products = data.content || [];
        
        if (products.length === 0) {
            grid.innerHTML = '<p>No products available.</p>';
            return;
        }

        grid.innerHTML = products.map(p => `
            <div class="product-card">
                <img src="${p.imagePath || 'https://via.placeholder.com/250'}" alt="${p.productName}" class="product-image">
                <div class="product-info">
                    <h3 class="product-title">${p.productName}</h3>
                    <p class="product-price">$${p.specialPrice.toFixed(2)}</p>
                    <button class="btn btn-primary" onclick="addToCart(${p.productId})">Add to Cart</button>
                </div>
            </div>
        `).join('');
    } catch (err) {
        grid.innerHTML = `<p class="error">Failed to load products: ${err.message}</p>`;
    }
}

// Cart Actions
async function loadCartCount() {
    if (!currentUser) return;
    try {
        const cart = await apiFetch('/carts/me');
        const count = cart.products ? cart.products.reduce((acc, p) => acc + p.quantity, 0) : 0;
        cartCount.textContent = count;
    } catch (err) {
        console.error('Failed to load cart count', err);
    }
}

async function addToCart(productId) {
    if (!currentUser) {
        notify('Please login to add items to cart', 'error');
        showView('login-view');
        return;
    }

    try {
        await apiFetch(`/carts/products/${productId}/quantity/1`, { method: 'POST' });
        notify('Product added to cart!');
        loadCartCount();
    } catch (err) {
        notify(err.message, 'error');
    }
}

async function loadCart() {
    const cartItemsDiv = document.getElementById('cart-items');
    const summary = document.getElementById('cart-summary');
    const totalPriceSpan = document.getElementById('cart-total-price');

    try {
        const cart = await apiFetch('/carts/me');
        const products = cart.products || [];

        if (products.length === 0) {
            cartItemsDiv.innerHTML = '<p>Your cart is empty.</p>';
            summary.classList.add('hidden');
            cartCount.textContent = '0';
            return;
        }

        cartItemsDiv.innerHTML = products.map(p => `
            <div class="cart-item">
                <div class="cart-item-info">
                    <h4>${p.productName}</h4>
                    <p>$${p.specialPrice.toFixed(2)} each</p>
                </div>
                <div class="cart-item-actions">
                    <button class="qty-btn" onclick="updateQty(${p.productId}, 'delete')">-</button>
                    <span>${p.quantity}</span>
                    <button class="qty-btn" onclick="updateQty(${p.productId}, 'add')">+</button>
                    <button class="btn btn-error" style="width: auto; background: var(--error);" onclick="removeItem(${cart.cartID}, ${p.productId})">Remove</button>
                </div>
            </div>
        `).join('');

        totalPriceSpan.textContent = cart.totalPrice.toFixed(2);
        summary.classList.remove('hidden');
        loadCartCount();
    } catch (err) {
        cartItemsDiv.innerHTML = `<p class="error">Failed to load cart: ${err.message}</p>`;
    }
}

async function updateQty(productId, operation) {
    try {
        await apiFetch(`/cart/products/${productId}/quantity/${operation}`, { method: 'PATCH' });
        loadCart();
    } catch (err) {
        notify(err.message, 'error');
    }
}

async function removeItem(cartId, productId) {
    try {
        await apiFetch(`/carts/${cartId}/product/${productId}`, { method: 'DELETE' });
        notify('Item removed from cart');
        loadCart();
    } catch (err) {
        notify(err.message, 'error');
    }
}
