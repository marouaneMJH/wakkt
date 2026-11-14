// Minimal slide deck: arrows/space/PageUp/PageDown, swipe, click zones, F = fullscreen, #n deep links.
(function () {
  var slides = [], i = 0;
  function fit() {
    var s = Math.min(window.innerWidth / 1300, (window.innerHeight - 96) / 740);
    document.documentElement.style.setProperty("--scale", Math.max(0.2, s));
  }
  function show(n) {
    i = Math.max(0, Math.min(slides.length - 1, n));
    slides.forEach(function (el, k) { el.classList.toggle("active", k === i); el.setAttribute("aria-hidden", k === i ? "false" : "true"); });
    document.querySelector(".progress i").style.width = ((i + 1) / slides.length * 100) + "%";
    document.querySelector(".count").textContent = (i + 1) + " / " + slides.length;
    if (location.hash !== "#" + (i + 1)) history.replaceState(null, "", "#" + (i + 1));
  }
  document.addEventListener("DOMContentLoaded", function () {
    slides = Array.prototype.slice.call(document.querySelectorAll(".slide"));
    fit(); window.addEventListener("resize", fit);
    show((parseInt(location.hash.slice(1), 10) || 1) - 1);
    document.querySelector("[data-prev]").onclick = function () { show(i - 1); };
    document.querySelector("[data-next]").onclick = function () { show(i + 1); };
    document.querySelector("[data-full]").onclick = toggleFull;
    document.addEventListener("keydown", function (e) {
      if (["ArrowRight", "PageDown", " ", "Enter"].indexOf(e.key) >= 0) { e.preventDefault(); show(i + 1); }
      else if (["ArrowLeft", "PageUp", "Backspace"].indexOf(e.key) >= 0) { e.preventDefault(); show(i - 1); }
      else if (e.key === "Home") show(0);
      else if (e.key === "End") show(slides.length - 1);
      else if (e.key === "f" || e.key === "F") toggleFull();
    });
    var x0 = null;
    document.addEventListener("touchstart", function (e) { x0 = e.touches[0].clientX; }, { passive: true });
    document.addEventListener("touchend", function (e) {
      if (x0 === null) return;
      var dx = e.changedTouches[0].clientX - x0; x0 = null;
      if (Math.abs(dx) > 40) show(i + (dx < 0 ? 1 : -1));
    });
  });
  function toggleFull() {
    if (!document.fullscreenElement) { document.documentElement.requestFullscreen && document.documentElement.requestFullscreen(); }
    else { document.exitFullscreen && document.exitFullscreen(); }
  }
})();
