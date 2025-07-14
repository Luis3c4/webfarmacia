// auth.js - Manejo de autenticación y estado del usuario
document.addEventListener('DOMContentLoaded', function() {
    updateUserDisplay();
});

// Función para actualizar la visualización del usuario
function updateUserDisplay() {
    const loginBtn = document.getElementById('loginBtnHeader');
    const userInfo = document.getElementById('userInfoHeader');
    const userName = document.getElementById('userNameHeader');
    const logoutBtnHeader = document.getElementById('logoutBtnHeader');
    
    // Obtener datos del usuario del localStorage
    const idUsuario = localStorage.getItem('id_usuario');
    const nombre = localStorage.getItem('nombre'); // Cambiado de 'nombres' a 'nombre'
    const apellidos = localStorage.getItem('apellidos');
    
    if (idUsuario && nombre) {
        // Usuario está logueado
        if (loginBtn) {
            loginBtn.style.display = 'none';
        }
        
        if (userInfo) {
            userInfo.style.display = 'flex';
        }
        
        if (userName) {
            // Mostrar nombre completo o solo nombre si no hay apellidos
            const nombreCompleto = apellidos ? `${nombre} ${apellidos}` : nombre;
            userName.textContent = nombreCompleto;
        }
        
        // Configurar el botón de logout
        if (logoutBtnHeader) {
            logoutBtnHeader.addEventListener('click', function(e) {
                e.preventDefault();
                logout();
            });
        }
    } else {
        // Usuario no está logueado
        if (loginBtn) {
            loginBtn.style.display = 'inline-flex';
        }
        
        if (userInfo) {
            userInfo.style.display = 'none';
        }
    }
}

// Función para cerrar sesión
function logout() {
    // Limpiar localStorage
    localStorage.removeItem('id_usuario');
    localStorage.removeItem('nombre');
    localStorage.removeItem('apellidos');
    localStorage.removeItem('email');
    localStorage.removeItem('tipo_usuario');
    localStorage.removeItem('telefono');
    
    // Mostrar mensaje de confirmación
    alert('Sesión cerrada exitosamente.');
    
    // Redirigir a la página de login
    window.location.href = '/login';
}

// Función para verificar si el usuario está logueado
function isUserLoggedIn() {
    return localStorage.getItem('id_usuario') !== null;
}

// Función para obtener el nombre del usuario
function getUserName() {
    const nombre = localStorage.getItem('nombre');
    const apellidos = localStorage.getItem('apellidos');
    
    if (nombre && apellidos) {
        return `${nombre} ${apellidos}`;
    } else if (nombre) {
        return nombre;
    }
    
    return 'Usuario';
}

// Función para obtener el ID del usuario
function getUserId() {
    return localStorage.getItem('id_usuario');
}

// Exportar funciones para uso en otros archivos
window.authUtils = {
    updateUserDisplay,
    logout,
    isUserLoggedIn,
    getUserName,
    getUserId
}; 