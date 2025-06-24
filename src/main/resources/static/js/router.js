function cargarVista(vista) {
  console.log('Cargando vista:', vista);
  fetch(`/vistas/${vista}.html`)
    .then(res => res.text())
    .then(html => {
      document.getElementById("main-content").innerHTML = html
      console.log('Vista cargada:', vista);
      
      // Cambiar hoja de estilo según la vista
      const existingLink = document.getElementById("vista-css")
      if (existingLink) {
        existingLink.href = `/css/${vista}.css`
      } else {
        const newLink = document.createElement("link")
        newLink.rel = "stylesheet"
        newLink.href = `/css/${vista}.css`
        newLink.id = "vista-css"
        document.head.appendChild(newLink)
      }
      
      // Ejecutar inicialización si es dashboard
      if (vista === "dashboard" && typeof window.initDashboard === "function") {
        console.log('Inicializando dashboard...');
        window.initDashboard();
      }
      
      // Ejecutar inicialización si es productos
      if (vista === "productos") {
        console.log('Vista de productos cargada, esperando inicialización...');
        // Esperar un poco más para asegurar que el DOM esté listo
        setTimeout(() => {
          if (typeof initProductosForm === "function") {
            console.log('Llamando a initProductosForm...');
            initProductosForm();
          } else {
            console.error('initProductosForm no está disponible');
          }
        }, 300);
      }
      // Ejecutar inicialización si es ventas
      if (vista === "ventas") {
        console.log('Vista de ventas cargada, esperando inicialización...');
        // Esperar un poco más para asegurar que el DOM esté listo
        setTimeout(() => {
          if (typeof initVentasView === "function") {
            console.log('Llamando a initVentasView...');
            initVentasView();
          } else {
            console.error('initVentasView no está disponible');
          }
        }, 300);
      }
      // Ejecutar inicialización si es clientes
      if (vista === "clientes") {
        console.log('Vista de clientes cargada, esperando inicialización...');
        // Esperar un poco más para asegurar que el DOM esté listo
        setTimeout(() => {
          if (typeof initClientesTable === "function") {
            console.log('Llamando a initClientesTable...');
            initClientesTable();
          } else {
            console.error('initClientesTable no está disponible');
          }
        }, 300);
      }
    })
    .catch(err => {
      console.error("Error al cargar la vista:", err)
    })
}