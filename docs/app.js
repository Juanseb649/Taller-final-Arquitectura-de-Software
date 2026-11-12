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

const sinMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

const alAparecer = (elementos, accion, umbral = 0.4) => {
  if (!("IntersectionObserver" in window)) {
    elementos.forEach(accion);
    return;
  }
  const obs = new IntersectionObserver((entradas) => {
    entradas.forEach((e) => {
      if (e.isIntersecting) {
        accion(e.target);
        obs.unobserve(e.target);
      }
    });
  }, { threshold: umbral });
  elementos.forEach((el) => obs.observe(el));
};

alAparecer([...document.querySelectorAll(".metrica")], (m) => {
  m.classList.add("visible");
  const cifra = m.querySelector(".cifra");
  const hasta = Number(cifra.dataset.hasta);
  const sufijo = cifra.dataset.sufijo || "";
  if (sinMovimiento) {
    cifra.textContent = hasta + sufijo;
    return;
  }
  const duracion = 1200;
  const inicio = performance.now();
  const paso = (ahora) => {
    const t = Math.min(1, (ahora - inicio) / duracion);
    const suave = 1 - Math.pow(1 - t, 3);
    cifra.textContent = Math.round(hasta * suave) + sufijo;
    if (t < 1) requestAnimationFrame(paso);
  };
  cifra.textContent = "0" + sufijo;
  requestAnimationFrame(paso);
});

alAparecer([...document.querySelectorAll(".medidor")], (m) => m.classList.add("visible"), 0.6);

document.querySelectorAll(".anotado").forEach((bloque) => {
  const lineas = [...bloque.querySelectorAll(".ln")];
  const notas = [...bloque.querySelectorAll(".notas li")];

  const activar = (nota, desplazar) => {
    const desde = Number(nota.dataset.desde);
    const hasta = Number(nota.dataset.hasta);
    notas.forEach((n) => n.classList.toggle("activa", n === nota));
    lineas.forEach((l) => {
      const n = Number(l.dataset.n);
      l.classList.toggle("resaltada", n >= desde && n <= hasta);
    });
    if (desplazar) nota.scrollIntoView({ block: "nearest", behavior: sinMovimiento ? "auto" : "smooth" });
  };

  notas.forEach((nota) => {
    nota.addEventListener("mouseenter", () => activar(nota, false));
    nota.addEventListener("focus", () => activar(nota, false));
    nota.addEventListener("click", () => activar(nota, false));
  });

  lineas.forEach((l) => {
    l.addEventListener("click", () => {
      const n = Number(l.dataset.n);
      const nota = notas.find((x) => n >= Number(x.dataset.desde) && n <= Number(x.dataset.hasta));
      if (nota) activar(nota, true);
    });
  });
});

const sim = document.getElementById("sim");

if (sim) {
  const $ = (id) => document.getElementById(id);
  const escena = $("sim-escena");
  const nVac = $("n-vac");
  const nEx = $("n-ex");
  const qNot = $("q-not");
  const qPai = $("q-pai");
  const nNot = $("n-not");
  const nPai = $("n-pai");
  const oNot = $("o-not");
  const oPai = $("o-pai");
  const dlq = $("dlq");
  const registro = $("sim-log");
  const barra = $("barra-reintentos");
  const hitos = [...sim.querySelectorAll(".hito")];
  const swNot = $("sw-not");
  const swPai = $("sw-pai");
  const ESCALA = 320;

  let gen = 0;
  let contador = 0;
  let colaNot = [];
  let colaPai = [];
  let ocupadoNot = false;
  let ocupadoPai = false;
  let avisados = 0;
  let reportadas = 0;
  let errores = 0;

  const esperar = (ms) => new Promise((r) => setTimeout(r, ms));

  const centro = (el) => {
    const r = el.getBoundingClientRect();
    const e = escena.getBoundingClientRect();
    return [r.left - e.left + r.width / 2, r.top - e.top + r.height / 2];
  };

  const ficha = (texto, clase = "") => {
    const f = document.createElement("span");
    f.className = "ficha " + clase;
    f.textContent = texto;
    escena.appendChild(f);
    return f;
  };

  const ubicar = (f, el) => {
    const [x, y] = centro(el);
    f.style.transform = `translate(${x}px, ${y}px)`;
  };

  const volar = async (f, desde, hacia, ms) => {
    const [x1, y1] = centro(desde);
    const [x2, y2] = centro(hacia);
    f.style.transform = `translate(${x2}px, ${y2}px)`;
    if (sinMovimiento) return;
    await f.animate(
      [
        { transform: `translate(${x1}px, ${y1}px) scale(1)` },
        { transform: `translate(${(x1 + x2) / 2}px, ${(y1 + y2) / 2 - 14}px) scale(1.15)` },
        { transform: `translate(${x2}px, ${y2}px) scale(1)` },
      ],
      { duration: ms, easing: "cubic-bezier(.4,0,.2,1)" }
    ).finished;
  };

  const efecto = (el, clase) => {
    el.classList.remove(clase);
    void el.offsetWidth;
    el.classList.add(clase);
  };

  const hora = () => new Date().toLocaleTimeString("es-CO", { hour12: false });

  const log = (texto, tipo = "") => {
    registro.querySelector(".vacio")?.remove();
    const li = document.createElement("li");
    li.className = tipo;
    li.innerHTML = `<time>${hora()}</time>`;
    li.append(texto);
    registro.appendChild(li);
    registro.scrollTop = registro.scrollHeight;
  };

  const estado = (nodo, texto) => {
    nodo.querySelector(".estado-nodo").textContent = texto;
  };

  const pintar = () => {
    [[qNot, colaNot], [qPai, colaPai]].forEach(([q, cola]) => {
      q.querySelector(".cuenta").textContent = cola.length;
      q.querySelector(".pila").innerHTML = "<i></i>".repeat(Math.min(cola.length, 12));
    });
    oNot.querySelector("b").textContent = avisados;
    oPai.querySelector("b").textContent = reportadas;
    dlq.querySelector("b").textContent = errores;
  };

  const moverBarra = (segundos, ms) => {
    barra.style.transition = sinMovimiento ? "none" : `width ${ms}ms linear`;
    barra.style.width = `${(segundos / 14) * 100}%`;
  };

  const marcarHitos = (k) => hitos.forEach((h, i) => h.classList.toggle("alcanzado", i < k));

  const consumirNot = async () => {
    if (ocupadoNot || !swNot.checked || !colaNot.length) return;
    ocupadoNot = true;
    const g = gen;
    const n = colaNot.shift();
    pintar();
    estado(nNot, "avisando");
    const f = ficha(n);
    ubicar(f, qNot);
    await volar(f, qNot, nNot, 380);
    await volar(f, nNot, oNot, 380);
    f.remove();
    if (g !== gen) return;
    avisados++;
    pintar();
    efecto(oNot, "golpe");
    log(`Notificaciones: acudiente avisado de la dosis #${n}`, "ok");
    estado(nNot, swNot.checked ? "esperando" : "apagado");
    ocupadoNot = false;
    consumirNot();
  };

  const consumirPai = async () => {
    if (ocupadoPai || !colaPai.length) return;
    ocupadoPai = true;
    const g = gen;
    const n = colaPai.shift();
    pintar();
    const f = ficha(n);
    ubicar(f, qPai);
    await volar(f, qPai, nPai, 380);
    if (g !== gen) return f.remove();
    const esperas = [2, 4, 8];
    let t = 0;
    marcarHitos(0);
    moverBarra(0, 0);
    for (let k = 1; k <= 4; k++) {
      if (swPai.checked) {
        nPai.classList.remove("fallando");
        estado(nPai, `intento ${k}: responde`);
        await volar(f, nPai, oPai, 380);
        f.remove();
        if (g !== gen) return;
        reportadas++;
        pintar();
        efecto(oPai, "golpe");
        log(`PAIWEB: dosis #${n} reportada${k > 1 ? ` en el intento ${k}` : ""}`, "ok");
        break;
      }
      marcarHitos(k);
      nPai.classList.add("fallando");
      efecto(nPai, "temblor");
      log(`PAIWEB no responde: falló el intento ${k} de 4 para la dosis #${n}`, "mal");
      if (k === 4) {
        estado(nPai, "a la cola de errores");
        await volar(f, nPai, dlq, 480);
        f.remove();
        if (g !== gen) return;
        errores++;
        pintar();
        efecto(dlq, "golpe");
        log(`Dosis #${n} pasó a la cola de errores tras 4 intentos (≈14 s). No se perdió.`, "mal");
        break;
      }
      const espera = esperas[k - 1];
      estado(nPai, `reintento en ${espera} s`);
      moverBarra(t + espera, espera * ESCALA);
      await esperar(espera * ESCALA);
      t += espera;
      if (g !== gen) return f.remove();
    }
    nPai.classList.remove("fallando");
    estado(nPai, "esperando");
    await esperar(500);
    if (g !== gen) return;
    marcarHitos(0);
    moverBarra(0, 0);
    ocupadoPai = false;
    consumirPai();
  };

  const publicar = async () => {
    const g = gen;
    const n = ++contador;
    efecto(nVac, "golpe");
    log(`Dosis #${n}: cumple las 5 reglas del esquema`, "info");
    const f1 = ficha(n);
    ubicar(f1, nVac);
    await volar(f1, nVac, nEx, 450);
    if (g !== gen) return f1.remove();
    log(`DosisAplicada #${n} publicada en pediatria.eventos`, "ok");
    const f2 = ficha(n);
    ubicar(f2, nEx);
    await Promise.all([volar(f1, nEx, qNot, 450), volar(f2, nEx, qPai, 450)]);
    f1.remove();
    f2.remove();
    if (g !== gen) return;
    colaNot.push(n);
    colaPai.push(n);
    pintar();
    efecto(qNot, "golpe");
    efecto(qPai, "golpe");
    if (!swNot.checked) log(`Notificaciones está apagado: la dosis #${n} espera en su cola`, "info");
    consumirNot();
    consumirPai();
  };

  const rechazar = async () => {
    efecto(nVac, "temblor");
    const f = ficha("✕", "rechazada");
    ubicar(f, nVac);
    log("422: la dosis llega a 10 días de la anterior y el mínimo es 28. No se publica ningún evento.", "mal");
    await esperar(900);
    f.remove();
  };

  const reiniciar = () => {
    gen++;
    contador = 0;
    colaNot = [];
    colaPai = [];
    ocupadoNot = false;
    ocupadoPai = false;
    avisados = 0;
    reportadas = 0;
    errores = 0;
    escena.querySelectorAll(".ficha").forEach((f) => f.remove());
    swNot.checked = true;
    swPai.checked = true;
    nNot.classList.remove("apagado");
    nPai.classList.remove("fallando");
    estado(nNot, "esperando");
    estado(nPai, "esperando");
    marcarHitos(0);
    moverBarra(0, 0);
    registro.innerHTML = '<li class="vacio">Registra una dosis para empezar.</li>';
    pintar();
  };

  sim.addEventListener("click", (e) => {
    const accion = e.target.closest("[data-accion]")?.dataset.accion;
    if (accion === "valida") publicar();
    if (accion === "invalida") rechazar();
    if (accion === "reiniciar") reiniciar();
  });

  swNot.addEventListener("change", () => {
    nNot.classList.toggle("apagado", !swNot.checked);
    if (swNot.checked) {
      log("Notificaciones vuelve a encenderse y procesa lo que esperaba en su cola", "info");
      estado(nNot, "esperando");
      consumirNot();
    } else {
      log("Notificaciones se apaga. Vacunación y PAIWEB siguen funcionando.", "info");
      if (!ocupadoNot) estado(nNot, "apagado");
    }
  });

  swPai.addEventListener("change", () => {
    log(swPai.checked ? "PAIWEB vuelve a responder" : "PAIWEB deja de responder", "info");
  });

  pintar();
}

const abrirDestino = (hash) => {
  if (!hash || hash.length < 2) return;
  const destino = document.getElementById(decodeURIComponent(hash.slice(1)));
  if (!destino) return;
  let nodo = destino;
  while (nodo) {
    if (nodo.tagName === "DETAILS") nodo.open = true;
    nodo = nodo.parentElement;
  }
  requestAnimationFrame(() => destino.scrollIntoView({ behavior: sinMovimiento ? "auto" : "smooth", block: "start" }));
};

document.addEventListener("click", (e) => {
  const a = e.target.closest("a[href^='#']");
  if (!a) return;
  const destino = document.getElementById(decodeURIComponent(a.getAttribute("href").slice(1)));
  if (destino && destino.closest("details")) {
    e.preventDefault();
    history.replaceState(null, "", a.getAttribute("href"));
    abrirDestino(a.getAttribute("href"));
  }
});

abrirDestino(location.hash);
