// Modo oscuro y claro (funcionalidad de estilo).
// La preferencia se guarda en localStorage para que se mantenga al recargar la página (RF-28).
(function () {
    var CLAVE = 'tema-taller';

    function aplicarTema(tema) {
        document.documentElement.setAttribute('data-theme', tema);
        var boton = document.getElementById('theme-toggle');
        if (boton) {
            var oscuro = tema === 'dark';
            boton.querySelector('i').className = oscuro ? 'bi bi-sun' : 'bi bi-moon';
            // Textos traducidos (i18n) que llegan desde Thymeleaf en data-label-dark / data-label-light
            var textoOscuro = boton.getAttribute('data-label-dark') || 'Modo oscuro';
            var textoClaro = boton.getAttribute('data-label-light') || 'Modo claro';
            boton.querySelector('span').textContent = oscuro ? textoClaro : textoOscuro;
        }
    }

    function leerTema() {
        try { return localStorage.getItem(CLAVE) || 'light'; } catch (e) { return 'light'; }
    }

    // Se aplica de inmediato (el script se carga en <head>) para evitar un parpadeo de colores
    aplicarTema(leerTema());

    document.addEventListener('DOMContentLoaded', function () {
        aplicarTema(leerTema());
        var boton = document.getElementById('theme-toggle');
        if (!boton) return;
        boton.addEventListener('click', function () {
            var nuevo = leerTema() === 'dark' ? 'light' : 'dark';
            try { localStorage.setItem(CLAVE, nuevo); } catch (e) { /* sin almacenamiento */ }
            aplicarTema(nuevo);
        });
    });
})();
