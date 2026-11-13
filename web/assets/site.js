// Theme toggle (light / dark / system). Preference is a per-browser convenience only.
(function () {
  var root = document.documentElement;
  var KEY = "wakkt-theme";
  try {
    var saved = localStorage.getItem(KEY);
    if (saved === "light" || saved === "dark") root.setAttribute("data-theme", saved);
  } catch (e) {}
  document.addEventListener("DOMContentLoaded", function () {
    var btn = document.querySelector(".theme-toggle");
    if (!btn) return;
    btn.addEventListener("click", function () {
      var current = root.getAttribute("data-theme") ||
        (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
      var next = current === "dark" ? "light" : "dark";
      root.setAttribute("data-theme", next);
      try { localStorage.setItem(KEY, next); } catch (e) {}
    });
    var y = document.querySelectorAll("[data-year]");
    for (var i = 0; i < y.length; i++) y[i].textContent = new Date().getFullYear();
  });
})();
