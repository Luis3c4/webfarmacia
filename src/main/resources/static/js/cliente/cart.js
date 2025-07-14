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
            const storedCart = localStorage.getItem('cart');
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
        localStorage.setItem('cart', JSON.stringify(cart));
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
            // Si el producto ya existe, sumar la cantidad especificada
            existingItem.cantidad += (product.cantidad || 1);
        } else {
            // Si es un producto nuevo, asegurar que tenga cantidad
            cart.push({ ...product, cantidad: product.cantidad || 1 });
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
                    window.location.href = '/pasarela'; // Corregido: agregada la barra inicial
                } else {
                    alert("Tu carrito está vacío. Añade productos antes de comprar.");
                }
            });
        }

        // Botón "Ir a mi carrito" en el sidebar
        const viewCartBtn = document.querySelector('.view-cart-btn');
        if (viewCartBtn) {
            viewCartBtn.addEventListener('click', () => {
                window.location.href = '/carrito';
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
                    e.stopPropagation(); // Evitar propagación del evento
                    
                    // Si el botón tiene sus propios data attributes, usarlos directamente
                    const button = e.target;
                    let productId, productName, productPrice, productImage, productPresentation, productPriceRegular;
                    
                    if (button.dataset.id && button.dataset.nombre && button.dataset.precio) {
                        // Usar datos del botón directamente (para página de detalle)
                        productId = button.dataset.id;
                        productName = button.dataset.nombre;
                        productPrice = parseFloat(button.dataset.precio);
                        productImage = button.dataset.imagen;
                        productPresentation = button.dataset.presentacion || 'Sin descripción';
                        productPriceRegular = parseFloat(button.dataset.precioregular) || null;
                    } else if (productElement) {
                        // Usar datos del elemento padre (para lista de productos)
                        productId = productElement.dataset.id;
                        productName = productElement.dataset.nombre;
                        productPrice = parseFloat(productElement.dataset.precio);
                        productImage = productElement.dataset.imagen;
                        productPresentation = productElement.dataset.presentacion || productElement.querySelector('.presentacion-producto')?.textContent || 'Unidad';
                        productPriceRegular = parseFloat(productElement.dataset.precioregular) || null;
                    } else {
                        console.error("No se encontró el elemento '.producto' padre del botón 'add-to-cart'.", e.target);
                        return;
                    }

                    if (!productId || !productName || isNaN(productPrice) || !productImage) {
                        console.error("Datos incompletos para agregar producto al carrito:", { productId, productName, productPrice, productImage });
                        alert("No se pudo agregar el producto, información faltante.");
                        return;
                    }

                    // Obtener cantidad del input si existe (para página de detalle)
                    let cantidad = 1;
                    const cantidadInput = document.getElementById('cantidadProducto');
                    if (cantidadInput) {
                        cantidad = parseInt(cantidadInput.value) || 1;
                    }

                    const product = {
                        id: productId,
                        nombre: productName,
                        precio: productPrice,
                        imagen: productImage,
                        presentacion: productPresentation,
                        precioRegular: productPriceRegular,
                        cantidad: cantidad
                    };
                    addItemToCart(product);

                    // Feedback visual
                    const originalText = button.textContent;
                    button.textContent = '✓ Agregado';
                    button.style.background = '#4CAF50';
                    button.style.color = 'white';
                    button.disabled = true;
                    setTimeout(() => {
                        button.textContent = originalText;
                        button.style.background = '';
                        button.style.color = '';
                        button.disabled = false;
                    }, 1500);

                } else if (productElement && !e.target.closest('button, a')) { // Redirección si se hace clic en el producto (no en un botón o enlace dentro de él)
                    const id = productElement.dataset.id;
                    if (id) {
                        window.location.href = `/producto/detalle/${id}`;
                    }
                }
            });
        }

        // Event listener global para botones "add-to-cart" que puedan estar fuera del contenedor principal
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('add-to-cart')) {
                const button = e.target;
                
                // Verificar si el botón tiene los datos necesarios
                if (button.dataset.id && button.dataset.nombre && button.dataset.precio) {
                    e.preventDefault();
                    e.stopPropagation();
                    
                    const productId = button.dataset.id;
                    const productName = button.dataset.nombre;
                    const productPrice = parseFloat(button.dataset.precio);
                    const productImage = button.dataset.imagen;
                    const productPresentation = button.dataset.presentacion || 'Sin descripción';
                    
                    // Obtener cantidad del input si existe
                    let cantidad = 1;
                    const cantidadInput = document.getElementById('cantidadProducto');
                    if (cantidadInput) {
                        cantidad = parseInt(cantidadInput.value) || 1;
                    }

                    if (!productId || !productName || isNaN(productPrice) || !productImage) {
                        console.error("Datos incompletos para agregar producto al carrito:", button.dataset);
                        alert("No se pudo agregar el producto, información faltante.");
                        return;
                    }

                    const product = {
                        id: productId,
                        nombre: productName,
                        precio: productPrice,
                        imagen: productImage,
                        presentacion: productPresentation,
                        cantidad: cantidad
                    };
                    addItemToCart(product);

                    // Feedback visual
                    const originalText = button.textContent;
                    button.textContent = '✓ Agregado';
                    button.style.background = '#4CAF50';
                    button.style.color = 'white';
                    button.disabled = true;
                    setTimeout(() => {
                        button.textContent = originalText;
                        button.style.background = '';
                        button.style.color = '';
                        button.disabled = false;
                    }, 1500);
                }
            }
        });

    }

    // --- INICIALIZACIÓN ---
    function init() {
        loadCart();
        renderCartItems(); // Renderiza el estado inicial del carrito (incluyendo contadores y totales)
        setupEventListeners();
    }

    init(); // Ejecutar al cargar la página

    // --- FUNCIONES GLOBALES ---
    // Hacer las funciones accesibles globalmente para otras páginas
    window.addItemToCartGlobal = addItemToCart;
    window.openCartGlobal = openCart;
    window.closeCartGlobal = closeCart;
    window.getCartGlobal = () => cart;
    window.updateCartCountersGlobal = updateCartCounters;
});