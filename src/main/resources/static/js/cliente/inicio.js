// Carrusel funcional con animación e indicadores
const slides = document.querySelectorAll('.slide');
const leftBtn = document.querySelector('.left');
const rightBtn = document.querySelector('.right');
let current = 0;

// Crear indicadores
const indicatorContainer = document.createElement('div');
indicatorContainer.className = 'slider-indicators';
document.querySelector('.slider').appendChild(indicatorContainer);

slides.forEach((_, i) => {
  const dot = document.createElement('span');
  dot.className = 'dot' + (i === 0 ? ' active' : '');
  dot.addEventListener('click', () => {
    current = i;
    showSlide(current);
  });
  indicatorContainer.appendChild(dot);
});

function updateDots() {
  document.querySelectorAll('.dot').forEach((dot, i) => {
    dot.classList.toggle('active', i === current);
  });
}

function showSlide(index) {
  slides.forEach((slide, i) => {
    slide.classList.toggle('active', i === index);
  });
  updateDots();
}

function nextSlide() {
  current = (current + 1) % slides.length;
  showSlide(current);
}

function prevSlide() {
  current = (current - 1 + slides.length) % slides.length;
  showSlide(current);
}

rightBtn.addEventListener('click', nextSlide);
leftBtn.addEventListener('click', prevSlide);

// Auto avance cada 5 segundos
let autoSlide = setInterval(nextSlide, 5000);

// Pausa al pasar el mouse por los botones
[leftBtn, rightBtn].forEach(btn => {
  btn.addEventListener('mouseover', () => clearInterval(autoSlide));
  btn.addEventListener('mouseout', () => autoSlide = setInterval(nextSlide, 5000));
});
const bannerCarousel = document.querySelector('.banner-carousel');
if (bannerCarousel) {
  bannerCarousel.addEventListener('wheel', (e) => {
    e.preventDefault();
    bannerCarousel.scrollBy({
      left: e.deltaY < 0 ? -100 : 100,
      behavior: 'smooth'
    });
  });
}
window.addEventListener('scroll', () => {
    const banner = document.querySelector('.banner-marca img');
    const container = document.querySelector('.banner-marca');
  
    if (banner && container) {
      const rect = container.getBoundingClientRect();
      const offset = rect.top;
  
      if (offset < window.innerHeight && offset > -container.offsetHeight) {
        const scrollEffect = offset * -0.08; // movimiento inverso, más suave
        banner.style.transform = `translate(-50%, ${scrollEffect}px)`;
      }
    }
  });
  const miniaturas = document.querySelectorAll('.miniatura');
  const imagenZoom = document.querySelector('.zoom');

  miniaturas.forEach(miniatura => {
    miniatura.addEventListener('click', () => {
      miniaturas.forEach(m => m.classList.remove('active'));
      miniatura.classList.add('active');
      imagenZoom.src = miniatura.src;
    });
  });
