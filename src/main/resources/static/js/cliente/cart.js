// js/cart.js

document.addEventListener('DOMContentLoaded', () => {
    // --- SELECTORES DEL DOM ---
    const cartSidebar = document.getElementById('cart-sidebar');
    const cartOverlay = document.getElementById('cart-overlay');
    const openCartButton = document.querySelector('.carrito-btn'); // Botón del header
    const closeCartButton = document.getElementById('close-cart-btn');
    const cartItemsContainer = document.getElementById('cart-items-container');
    const cartSubtotalElement = document.getElementById('cart-subtotal');
    const cartTotalElement = document.getElementById('cart-total');
    const cartCounterHeader = document.querySelector('.carrito-contador');
    const cartCountSidebar = document.getElementById('cart-count-sidebar');
    const checkoutNowButton = document.querySelector('.checkout-now-btn');
    const productListContainer = document.querySelector('main'); // Contenedor principal donde están los productos, o un selector más específico si tienes.

    // --- ESTADO DEL CARRITO ---
    let cart = [];

    // --- FUNCIONES DEL CARRITO ---

    /**
     * Carga el carrito desde localStorage.
     */
    function loadCart() {
        try {
            const storedCart = localStorage.getItem('shoppingCartFarma');
            cart = storedCart ? JSON.parse(storedCart) : [];
        } catch (error) {
            console.error("Error al parsear el carrito desde localStorage:", error);
            cart = []; // Iniciar con carrito vacío si hay error
        }
    }

    /**
     * Guarda el carrito en localStorage.
     */
    function saveCart() {
        localStorage.setItem('shoppingCartFarma', JSON.stringify(cart));
    }

    /**
     * Abre el sidebar del carrito.
     */
    function openCart() {
        if (cartSidebar) cartSidebar.classList.add('open');
        if (cartOverlay) cartOverlay.classList.add('open');
        document.body.style.overflow = 'hidden';
        renderCartItems(); // Asegurar que el contenido esté actualizado
    }

    /**
     * Cierra el sidebar del carrito.
     */
    function closeCart() {
        if (cartSidebar) cartSidebar.classList.remove('open');
        if (cartOverlay) cartOverlay.classList.remove('open');
        document.body.style.overflow = '';
    }

    /**
     * Renderiza los items en el carrito.
     */
    function renderCartItems() {
        if (!cartItemsContainer) return;
        cartItemsContainer.innerHTML = ''; // Limpiar items actuales

        if (cart.length === 0) {
            // El CSS puede manejar el mensaje de carrito vacío con :empty::after
            // Alternativamente, puedes añadir un mensaje aquí:
            // cartItemsContainer.innerHTML = '<p class="cart-empty-message">Tu carrito está vacío.</p>';
            updateCartTotals();
            updateCartCounters();
            return;
        }

        cart.forEach(item => {
            const itemElement = document.createElement('div');
            itemElement.classList.add('cart-item');
            itemElement.dataset.itemId = item.id; // Útil para futuras actualizaciones de cantidad

            const regularPriceHTML = item.precioRegular && item.precioRegular > item.precio
                ? `<p class="cart-item-price-regular">S/ ${parseFloat(item.precioRegular).toFixed(2)}</p>`
                : '';

            itemElement.innerHTML = `
                <img src="${item.imagen || 'placeholder.jpg'}" alt="${item.nombre}" class="cart-item-image" onerror="this.onerror=null; this.src='placeholder.jpg'; console.error('Error al cargar imagen: ${item.imagen}')">
                <div class="cart-item-details">
                    <p class="cart-item-name">${item.nombre}</p>
                    <p class="cart-item-presentation">${item.presentacion || 'Unidad'}</p>
                    <p class="cart-item-quantity">CANTIDAD: ${item.cantidad}</p>
                </div>
                <div class="cart-item-pricing">
                    ${regularPriceHTML}
                    <p class="cart-item-price-final">S/ ${parseFloat(item.precio * item.cantidad).toFixed(2)}</p>
                    <button class="cart-item-remove" data-id="${item.id}">Eliminar</button>
                </div>
            `;
            cartItemsContainer.appendChild(itemElement);
        });

        updateCartTotals();
        updateCartCounters();
    }

    /**
     * Añade un producto al carrito o incrementa su cantidad si ya existe.
     * @param {object} product - El objeto del producto a añadir.
     */
    function addItemToCart(product) {
        if (!product || !product.id) {
            console.error("Intento de añadir producto inválido:", product);
            return;
        }
        const existingItem = cart.find(item => item.id === product.id);
        if (existingItem) {
            existingItem.cantidad += 1;
        } else {
            cart.push({ ...product, cantidad: 1 });
        }
        saveCart();
        renderCartItems();
        // Opcional: abrir el carrito al agregar un item.
        // openCart();
    }

    /**
     * Elimina un producto del carrito.
     * @param {string} productId - El ID del producto a eliminar.
     */
    function removeItemFromCart(productId) {
        cart = cart.filter(item => item.id !== productId);
        saveCart();
        renderCartItems();
    }

    /**
     * Actualiza los totales (subtotal y total) del carrito.
     */
    function updateCartTotals() {
        const subtotal = cart.reduce((sum, item) => sum + (item.precio * item.cantidad), 0);
        // Aquí podrías añadir lógica de descuentos, envío, etc.
        const total = subtotal;

        if (cartSubtotalElement) cartSubtotalElement.textContent = `S/ ${subtotal.toFixed(2)}`;
        if (cartTotalElement) cartTotalElement.textContent = `S/ ${total.toFixed(2)}`;
    }

    /**
     * Actualiza los contadores de ítems del carrito (en header y sidebar).
     */
    function updateCartCounters() {
        const totalItems = cart.reduce((sum, item) => sum + item.cantidad, 0);
        if (cartCounterHeader) {
            cartCounterHeader.textContent = totalItems;
        }
        if (cartCountSidebar) {
            cartCountSidebar.textContent = `Mi carrito (${totalItems})`; // Actualizado para coincidir con la imagen de muestra
        }
    }


    // --- EVENT LISTENERS ---

    /**
     * Configura los event listeners iniciales.
     */
    function setupEventListeners() {
        // Abrir carrito
        if (openCartButton) {
            openCartButton.addEventListener('click', openCart);
        }

        // Cerrar carrito
        if (closeCartButton) {
            closeCartButton.addEventListener('click', closeCart);
        }
        if (cartOverlay) {
            cartOverlay.addEventListener('click', closeCart);
        }
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && cartSidebar && cartSidebar.classList.contains('open')) {
                closeCart();
            }
        });

        // Botón "Comprar ahora" en el sidebar
        if (checkoutNowButton) {
            checkoutNowButton.addEventListener('click', () => {
                if (cart.length > 0) {
                    window.location.href = 'pasarela'; // Asumiendo que 'pasarela' es la ruta correcta
                } else {
                    alert("Tu carrito está vacío. Añade productos antes de comprar.");
                }
            });
        }

        // Delegación de eventos para botones "Eliminar" dentro del carrito
        if (cartItemsContainer) {
            cartItemsContainer.addEventListener('click', (e) => {
                if (e.target.classList.contains('cart-item-remove')) {
                    const productId = e.target.dataset.id;
                    if (productId) {
                        removeItemFromCart(productId);
                    }
                }
            });
        }

        // Delegación de eventos para botones "Agregar al carrito" y redirección de productos
        if (productListContainer) {
            productListContainer.addEventListener('click', (e) => {
                const productElement = e.target.closest('.producto'); // Elemento que contiene los data-attributes
                
                // Botón "Agregar al carrito"
                if (e.target.classList.contains('add-to-cart')) {
                    e.preventDefault(); // Prevenir comportamiento por defecto si es un enlace
                    if (!productElement) {
                        console.error("No se encontró el elemento '.producto' padre del botón 'add-to-cart'.", e.target);
                        return;
                    }
                    
                    const productId = productElement.dataset.id;
                    const productName = productElement.dataset.nombre;
                    const productPrice = parseFloat(productElement.dataset.precio);
                    const productImage = productElement.dataset.imagen; // Asegúrate que esta URL sea correcta
                    const productPresentation = productElement.dataset.presentacion || productElement.querySelector('.presentacion-producto')?.textContent || 'Unidad';
                    const productPriceRegular = parseFloat(productElement.dataset.precioregular) || null;

                    if (!productId || !productName || isNaN(productPrice) || !productImage) {
                        console.error("Datos incompletos para agregar producto al carrito:", productElement.dataset);
                        alert("No se pudo agregar el producto, información faltante.");
                        return;
                    }

                    const product = {
                        id: productId,
                        nombre: productName,
                        precio: productPrice,
                        imagen: productImage,
                        presentacion: productPresentation,
                        precioRegular: productPriceRegular
                    };
                    addItemToCart(product);

                    // Feedback visual (opcional)
                    const button = e.target;
                    const originalText = button.textContent;
                    button.textContent = 'Añadido!';
                    button.disabled = true;
                    setTimeout(() => {
                        button.textContent = originalText;
                        button.disabled = false;
                    }, 1500);

                } else if (productElement && !e.target.closest('button, a')) { // Redirección si se hace clic en el producto (no en un botón o enlace dentro de él)
                    const id = productElement.dataset.id;
                    if (id) {
                        window.location.href = `detalle_producto?id=${id}`;
                    }
                }
            });
        }
    }

    // --- INICIALIZACIÓN ---
    function init() {
        loadCart();
        renderCartItems(); // Renderiza el estado inicial del carrito (incluyendo contadores y totales)
        setupEventListeners();
    }

    init(); // Ejecutar al cargar la página
});