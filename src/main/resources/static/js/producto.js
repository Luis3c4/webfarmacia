// Variables globales para paginación
let currentPage = 0;
const pageSize = 10;
let fetchAndRenderFunction = null;
let isFiltered = false; // Variable para controlar si estamos en modo filtrado

// Función para inicializar el formulario de productos
function initProductosForm() {
  console.log("Inicializando formulario de productos...");
  currentPage = 0;
  const tbody = document.querySelector(".productos-tabla tbody");
  const pageInfo = document.querySelector(".page-info");
  const btnPrev = document.querySelector(".btn-prev");
  const btnNext = document.querySelector(".btn-next");
  const btnFiltrar = document.getElementById("btnFiltrar");
  const filterInput = document.getElementById("filterById");

  function fetchAndRender(page) {
    fetch(`/api/productos?page=${page}&size=${pageSize}`)
      .then((response) => response.json())
      .then((data) => {
        console.log("Datos recibidos:", data);
        tbody.innerHTML = "";
        data.content.forEach((producto) => {
          let stockStatus = "";
          if (producto.stock <= 0) {
            stockStatus = '<td class="out-stock">Out of stock</td>';
          } else if (producto.stock <= 10) {
            stockStatus = '<td class="low-stock">Low stock</td>';
          } else {
            stockStatus = '<td class="in-stock">In stock</td>';
          }
          const row = document.createElement("tr");
          row.innerHTML = `
                        <td>${producto.nombre}</td>
                        <td>${producto.precio_unitario}</td>
                        <td>${producto.cantidadIngresada}</td>
                        <td>${producto.stock}</td>
                        ${stockStatus}
                        <td>
                            <button class="btn-editar" data-id="${producto.productoId}" onclick="editarProducto(${producto.productoId})" title="Editar producto">
                                <i class="fas fa-edit"></i>
                                <span>Editar</span>
                            </button>
                            <button class="btn-eliminar" data-id="${producto.productoId}" onclick="eliminarProducto(${producto.productoId})" title="Eliminar producto">
                                <i class="fas fa-trash-alt"></i>
                                <span>Eliminar</span>
                            </button>
                        </td>
                    `;
          tbody.appendChild(row);
        });
        // Actualizar paginación
        pageInfo.textContent = `Page ${data.number + 1} of ${
          data.totalPages || 1
        }`;
        btnPrev.disabled = data.number === 0;
        btnNext.disabled = data.number + 1 >= data.totalPages;
        currentPage = data.number;
        isFiltered = false;
      })
      .catch((error) => {
        console.error("Error:", error);
      });
  }

  // Función para filtrar por nombre
  async function filtrarPorNombre() {
    const nombre = filterInput.value.trim();
    
    if (!nombre) {
      // Si el input está vacío, recargar la paginación normal
      fetchAndRender(currentPage);
      return;
    }

    try {
      const response = await fetch(`/api/productos/buscar?nombre=${encodeURIComponent(nombre)}&page=0&size=${pageSize}`);
      
      if (response.ok) {
        const data = await response.json();
        
        // Limpiar tabla y mostrar los productos encontrados
        tbody.innerHTML = '';
        
        if (data.content && data.content.length > 0) {
          data.content.forEach(producto => {
            let stockStatus = '';
            if (producto.stock <= 0) {
              stockStatus = '<td class="out-stock">Out of stock</td>';
            } else if (producto.stock <= 10) {
              stockStatus = '<td class="low-stock">Low stock</td>';
            } else {
              stockStatus = '<td class="in-stock">In stock</td>';
            }
            
            const row = document.createElement('tr');
            row.innerHTML = `
              <td>${producto.nombre}</td>
              <td>${producto.precio_unitario}</td>
              <td>${producto.cantidadIngresada}</td>
              <td>${producto.stock}</td>
              ${stockStatus}
              <td>
                <button class="btn-editar" data-id="${producto.productoId}" onclick="editarProducto(${producto.productoId})" title="Editar producto">
                  <i class="fas fa-edit"></i>
                  <span>Editar</span>
                </button>
                <button class="btn-eliminar" data-id="${producto.productoId}" onclick="eliminarProducto(${producto.productoId})" title="Eliminar producto">
                  <i class="fas fa-trash-alt"></i>
                  <span>Eliminar</span>
                </button>
              </td>
            `;
            tbody.appendChild(row);
          });
          
          // Actualizar paginación para mostrar resultados filtrados
          pageInfo.textContent = `Encontrados ${data.totalElements} productos con "${nombre}"`;
          btnPrev.disabled = data.number === 0;
          btnNext.disabled = data.number + 1 >= data.totalPages;
          isFiltered = true;
          
        } else {
          // No se encontraron productos
          tbody.innerHTML = `
            <tr>
              <td colspan="6" style="text-align: center; padding: 20px; color: #6c757d; font-style: italic;">
                No se encontraron productos con el nombre: "${nombre}"
              </td>
            </tr>
          `;
          pageInfo.textContent = `No se encontraron productos`;
          btnPrev.disabled = true;
          btnNext.disabled = true;
          isFiltered = true;
        }
        
      } else {
        // Error en la respuesta
        tbody.innerHTML = `
          <tr>
            <td colspan="6" style="text-align: center; padding: 20px; color: #dc3545; font-style: italic;">
              Error al buscar productos. Inténtalo de nuevo.
            </td>
          </tr>
        `;
        pageInfo.textContent = `Error en la búsqueda`;
        btnPrev.disabled = true;
        btnNext.disabled = true;
        isFiltered = true;
      }
    } catch (error) {
      console.error('Error al filtrar:', error);
      tbody.innerHTML = `
        <tr>
          <td colspan="6" style="text-align: center; padding: 20px; color: #dc3545; font-style: italic;">
            Error al buscar el producto. Inténtalo de nuevo.
          </td>
        </tr>
      `;
      pageInfo.textContent = `Error en la búsqueda`;
      btnPrev.disabled = true;
      btnNext.disabled = true;
      isFiltered = true;
    }
  }

  // Asignar la función al scope global
  fetchAndRenderFunction = fetchAndRender;

  // Event listener para el botón de filtrar
  if (btnFiltrar) {
    btnFiltrar.addEventListener("click", filtrarPorNombre);
  }

  // Event listener para filtrar al presionar Enter en el input
  if (filterInput) {
    filterInput.addEventListener("keypress", function (e) {
      if (e.key === "Enter") {
        filtrarPorNombre();
      }
    });
  }

  // Listeners de paginación
  btnPrev.addEventListener("click", () => {
    if (currentPage > 0 && !isFiltered) {
      fetchAndRender(currentPage - 1);
    }
  });
  btnNext.addEventListener("click", () => {
    if (!isFiltered) {
      fetchAndRender(currentPage + 1);
    }
  });

  fetchAndRender(currentPage);
  // Esperar un poco más para asegurar que el DOM esté completamente cargado
  setTimeout(() => {
    const modal = document.getElementById("modalAgregarProducto");
    const modalEditar = document.getElementById("modalEditarProducto");
    const btnAgregar = document.querySelector(".btn-agregar");
    const closeBtn = document.querySelector(".close");
    const closeBtnEditar = document.querySelector(
      "#modalEditarProducto .close"
    );
    const btnCancelar = document.querySelector(".btn-cancelar");
    const btnCancelarEditar = document.querySelector(
      "#modalEditarProducto .btn-cancelar"
    );
    const form = document.getElementById("formAgregarProducto");
    const formEditar = document.getElementById("formEditarProducto");

    console.log("Elementos encontrados:", {
      modal: !!modal,
      modalEditar: !!modalEditar,
      btnAgregar: !!btnAgregar,
      closeBtn: !!closeBtn,
      closeBtnEditar: !!closeBtnEditar,
      btnCancelar: !!btnCancelar,
      btnCancelarEditar: !!btnCancelarEditar,
      form: !!form,
      formEditar: !!formEditar,
    });

    // Verificar que todos los elementos existan
    if (
      !modal ||
      !modalEditar ||
      !btnAgregar ||
      !closeBtn ||
      !closeBtnEditar ||
      !btnCancelar ||
      !btnCancelarEditar ||
      !form ||
      !formEditar
    ) {
      console.error(
        "No se encontraron todos los elementos necesarios para los modales"
      );
      return;
    }

    console.log("Todos los elementos encontrados, configurando eventos...");

    // Remover event listeners previos para evitar duplicados
    btnAgregar.removeEventListener("click", openModal);
    closeBtn.removeEventListener("click", closeModal);
    btnCancelar.removeEventListener("click", closeModal);
    closeBtnEditar.removeEventListener("click", closeModalEditar);
    btnCancelarEditar.removeEventListener("click", closeModalEditar);

    // Abrir modal de agregar
    function openModal() {
      console.log("Botón agregar clickeado");
      modal.style.display = "block";
    }

    btnAgregar.addEventListener("click", openModal);

    // Cerrar modal de agregar
    function closeModal() {
      modal.style.display = "none";
      form.reset();
    }

    closeBtn.addEventListener("click", closeModal);
    btnCancelar.addEventListener("click", closeModal);

    // Cerrar modal de editar
    function closeModalEditar() {
      modalEditar.style.display = "none";
      formEditar.reset();
    }

    closeBtnEditar.addEventListener("click", closeModalEditar);
    btnCancelarEditar.addEventListener("click", closeModalEditar);

    // Cerrar modales al hacer clic fuera de ellos
    window.addEventListener("click", (e) => {
      console.log("Click en ventana:", e.target);
      if (e.target === modal) {
        closeModal();
      }
      if (e.target === modalEditar) {
        closeModalEditar();
      }
    });

    // Manejar envío del formulario de agregar
    form.removeEventListener("submit", handleSubmit);
    form.addEventListener("submit", handleSubmit);

    async function handleSubmit(e) {
      e.preventDefault();
      console.log("Formulario enviado");

      const stock = parseInt(document.getElementById("stock").value);
      let stockStatus;
      if (stock <= 0) {
        stockStatus = "OUT_OF_STOCK";
      } else if (stock <= 10) {
        stockStatus = "LOW_STOCK";
      } else {
        stockStatus = "IN_STOCK";
      }

      const formData = {
        nombre: document.getElementById("nombre").value,
        precio: parseFloat(document.getElementById("precio").value),
        cantidadIngresada: parseInt(
          document.getElementById("cantidadIngresada").value
        ),
        descripcion: document.getElementById("descripcion").value,
        stock: stock,
        stockStatus: stockStatus,
      };

      console.log("Datos del formulario:", formData);

      try {
        const response = await fetch("/api/productos", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(formData),
        });

        if (response.ok) {
          alert("Producto agregado exitosamente");
          closeModal();
          // Recargar la tabla de productos
          location.reload();
        } else {
          const error = await response.text();
          alert("Error al agregar el producto: " + error);
        }
      } catch (error) {
        console.error("Error:", error);
        alert("Error al agregar el producto");
      }
    }

    // Manejar envío del formulario de editar
    formEditar.removeEventListener("submit", handleSubmitEditar);
    formEditar.addEventListener("submit", handleSubmitEditar);

    async function handleSubmitEditar(e) {
      e.preventDefault();
      console.log("Formulario de editar enviado");

      const productoId = document.getElementById("editProductoId").value;
      const stock = parseInt(document.getElementById("editStock").value);
      let stockStatus;
      if (stock <= 0) {
        stockStatus = "OUT_OF_STOCK";
      } else if (stock <= 10) {
        stockStatus = "LOW_STOCK";
      } else {
        stockStatus = "IN_STOCK";
      }

      const formData = {
        nombre: document.getElementById("editNombre").value,
        precio: parseFloat(document.getElementById("editPrecio").value),
        cantidadIngresada: parseInt(
          document.getElementById("editCantidadIngresada").value
        ),
        descripcion: document.getElementById("editDescripcion").value,
        stock: stock,
        stockStatus: stockStatus,
      };

      console.log("Datos del formulario de editar:", formData);

      try {
        const response = await fetch(`/api/productos/${productoId}`, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(formData),
        });

        if (response.ok) {
          alert("Producto actualizado exitosamente");
          closeModalEditar();
          // Recargar la tabla de productos
          location.reload();
        } else {
          const error = await response.text();
          alert("Error al actualizar el producto: " + error);
        }
      } catch (error) {
        console.error("Error:", error);
        alert("Error al actualizar el producto");
      }
    }

    console.log("Formulario de productos inicializado correctamente");
  }, 200); // Aumentar el delay a 200ms
}

// Inicializar cuando se carga la vista de productos
document.addEventListener("DOMContentLoaded", function () {
  // Verificar si estamos en la vista de productos
  if (
    window.location.hash === "#/productos" ||
    document.querySelector(".productos-tabla")
  ) {
    console.log("Cargando productos...");
    initProductosForm();
  }
});

// Reinicializar cuando cambia la vista (para el router)
window.addEventListener("hashchange", function () {
  if (window.location.hash === "#/productos") {
    setTimeout(initProductosForm, 100); // Pequeño delay para asegurar que el DOM está listo
  }
});

// Función para editar un producto
async function editarProducto(productoId) {
  if (!productoId || productoId === "undefined") {
    console.error("ID de producto inválido:", productoId);
    return;
  }

  try {
    const response = await fetch(`/api/productos/${productoId}`);
    if (response.ok) {
      const producto = await response.json();

      // Llenar el formulario de edición con los datos del producto
      document.getElementById("editProductoId").value = producto.productoId;
      document.getElementById("editNombre").value = producto.nombre;
      document.getElementById("editPrecio").value = producto.precio_unitario;
      document.getElementById("editCantidadIngresada").value =
        producto.cantidadIngresada;
      document.getElementById("editDescripcion").value = producto.descripcion;
      document.getElementById("editStock").value = producto.stock;

      // Mostrar el modal de edición
      document.getElementById("modalEditarProducto").style.display = "block";
    } else {
      alert("Error al cargar los datos del producto");
    }
  } catch (error) {
    console.error("Error:", error);
    alert("Error al cargar los datos del producto");
  }
}

// Función para eliminar un producto
function eliminarProducto(productoId) {
  if (!productoId || productoId === "undefined") {
    console.error("ID de producto inválido:", productoId);
    return;
  }

  if (confirm("¿Estás seguro de que quieres eliminar este producto?")) {
    fetch(`/api/productos/${productoId}`, {
      method: "DELETE",
    })
      .then((response) => {
        if (response.ok) {
          alert("Producto eliminado exitosamente");
          // Recargar la tabla de productos
          if (fetchAndRenderFunction) {
            fetchAndRenderFunction(currentPage);
          } else {
            location.reload();
          }
        } else {
          alert("Error al eliminar el producto");
        }
      })
      .catch((error) => {
        console.error("Error:", error);
        alert("Error al eliminar el producto");
      });
  }
}
