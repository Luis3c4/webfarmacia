function cargarVista(vista) {
  fetch(`/vistas/${vista}.html`)
    .then(res => res.text())
    .then(html => {
      document.getElementById("main-content").innerHTML = html
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
        window.initDashboard();
      }
      // Ejecutar inicialización si es productos
      if (vista === "productos" && typeof initProductosForm === "function") {
        setTimeout(() => {
          initProductosForm();
        }, 100);
      }
    })
    .catch(err => {
      console.error("Error al cargar la vista:", err)
    })
}
