// cart-sync.js - Sincronización del carrito en todas las páginas
document.addEventListener('DOMContentLoaded', function() {
    // Función para actualizar contadores del carrito en el header
    function updateCartCounters() {
        try {
            const cart = JSON.parse(localStorage.getItem('cart')) || [];
            const totalItems = cart.reduce((sum, item) => sum + (item.cantidad || 1), 0);
            
            // Actualizar todos los contadores del carrito en la página
            const cartCounters = document.querySelectorAll('.carrito-contador');
            cartCounters.forEach(counter => {
                counter.textContent = totalItems;
            });
            
            // Actualizar contador específico de pasarela si existe
            const pasarelaCounter = document.getElementById('headerCartCounterPasarela');
            if (pasarelaCounter) {
                pasarelaCounter.textContent = totalItems;
            }
            
            console.log('Contadores del carrito actualizados:', totalItems);
        } catch (error) {
            console.error('Error al actualizar contadores del carrito:', error);
        }
    }

    // Función para abrir el carrito
    function openCart() {
        const cartSidebar = document.getElementById('cart-sidebar');
        const cartOverlay = document.getElementById('cart-overlay');
        
        if (cartSidebar) {
            cartSidebar.classList.add('open');
        }
        if (cartOverlay) {
            cartOverlay.style.display = 'block';
        }
    }

    // Configurar event listeners para botones del carrito
    function setupCartEventListeners() {
        // Botón para abrir carrito
        const openCartButtons = document.querySelectorAll('.carrito-btn, #openCartBtnHeaderPasarela');
        openCartButtons.forEach(button => {
            button.addEventListener('click', openCart);
        });

        // Botón para cerrar carrito
        const closeCartButton = document.getElementById('close-cart-btn');
        if (closeCartButton) {
            closeCartButton.addEventListener('click', () => {
                const cartSidebar = document.getElementById('cart-sidebar');
                const cartOverlay = document.getElementById('cart-overlay');
                
                if (cartSidebar) {
                    cartSidebar.classList.remove('open');
                }
                if (cartOverlay) {
                    cartOverlay.style.display = 'none';
                }
            });
        }

        // Overlay para cerrar carrito
        const cartOverlay = document.getElementById('cart-overlay');
        if (cartOverlay) {
            cartOverlay.addEventListener('click', () => {
                const cartSidebar = document.getElementById('cart-sidebar');
                if (cartSidebar) {
                    cartSidebar.classList.remove('open');
                }
                cartOverlay.style.display = 'none';
            });
        }
    }

    // Inicializar
    updateCartCounters();
    setupCartEventListeners();

    // Escuchar cambios en localStorage para sincronizar entre pestañas
    window.addEventListener('storage', function(e) {
        if (e.key === 'cart') {
            updateCartCounters();
        }
    });

    // Actualizar contadores cuando la página se vuelve visible
    document.addEventListener('visibilitychange', function() {
        if (!document.hidden) {
            updateCartCounters();
        }
    });
}); 