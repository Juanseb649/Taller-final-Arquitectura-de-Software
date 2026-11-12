const enlaces = [...document.querySelectorAll(".sidebar a[href^='#']")];
const secciones = enlaces.map((a) => document.querySelector(a.getAttribute("href"))).filter(Boolean);
const sidebar = document.querySelector(".sidebar");
const boton = document.querySelector(".menu-movil button");

const actualizar = () => {
  const limite = window.innerHeight * 0.3;
  let actual = -1;
  secciones.forEach((s, i) => {
    if (s.getBoundingClientRect().top <= limite) actual = i;
  });
  const alFinal = window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 4;
  if (alFinal) actual = secciones.length - 1;
  enlaces.forEach((a, i) => {
    a.classList.toggle("activo", i === actual);
    a.classList.toggle("visitado", i < actual);
  });
};

let pendiente = false;
window.addEventListener("scroll", () => {
  if (pendiente) return;
  pendiente = true;
  requestAnimationFrame(() => {
    actualizar();
    pendiente = false;
  });
}, { passive: true });

actualizar();

boton?.addEventListener("click", () => sidebar.classList.toggle("abierto"));
enlaces.forEach((a) => a.addEventListener("click", () => sidebar.classList.remove("abierto")));
