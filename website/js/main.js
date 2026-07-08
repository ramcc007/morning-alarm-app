(function () {
  "use strict";

  // Mobile nav toggle
  var navToggle = document.getElementById("navToggle");
  var navInner = navToggle ? navToggle.closest(".nav-inner") : null;
  if (navToggle && navInner) {
    navToggle.addEventListener("click", function () {
      navInner.classList.toggle("open");
    });
    navInner.querySelectorAll(".nav-links a").forEach(function (link) {
      link.addEventListener("click", function () {
        navInner.classList.remove("open");
      });
    });
  }

  // Sunrise scrollytelling: map how far the user has scrolled through the
  // tall .sunrise-track into a 0-1 progress value, then drive the sun's
  // position/glow, star fade, dawn-sky crossfade, and caption reveal purely
  // via CSS custom properties (no animation library).
  var track = document.getElementById("sunrise");
  var pin = document.getElementById("sunrisePin");
  var skyDawn = document.getElementById("skyDawn");
  var stars = document.getElementById("stars");
  var sun = document.getElementById("sun");
  var caption = document.getElementById("sunriseCaption");

  function clamp(value, min, max) {
    return Math.max(min, Math.min(max, value));
  }

  function updateSunrise() {
    if (!track || !pin) return;
    var rect = track.getBoundingClientRect();
    var trackHeight = track.offsetHeight - window.innerHeight;
    if (trackHeight <= 0) return;

    // progress = 0 when the track's top just reaches the viewport top,
    // 1 when the track has scrolled fully past (pin about to release).
    var progress = clamp(-rect.top / trackHeight, 0, 1);

    var sunBottom = -15 + progress * 75; // percent, rises from below frame to upper sky
    var sunGlow = 0.15 + progress * 0.55;
    var starOpacity = clamp(1 - progress * 2.2, 0, 1);
    var dawnOpacity = clamp((progress - 0.15) / 0.7, 0, 1);
    var captionOpacity = clamp((progress - 0.55) / 0.35, 0, 1);
    var captionShift = 12 - captionOpacity * 12;

    pin.style.setProperty("--sun-bottom", sunBottom + "%");
    pin.style.setProperty("--sun-glow", sunGlow.toFixed(2));
    pin.style.setProperty("--star-opacity", starOpacity.toFixed(2));
    pin.style.setProperty("--caption-opacity", captionOpacity.toFixed(2));
    pin.style.setProperty("--caption-shift", captionShift.toFixed(1) + "px");
    if (skyDawn) skyDawn.style.opacity = dawnOpacity.toFixed(2);
  }

  var ticking = false;
  function onScroll() {
    if (ticking) return;
    ticking = true;
    requestAnimationFrame(function () {
      updateSunrise();
      ticking = false;
    });
  }

  if (track) {
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    updateSunrise();
  }

  // Step reveal: highlight each "how it works" card as it crosses the
  // vertical center band of the viewport.
  var stepCards = document.querySelectorAll(".step-card");
  if (stepCards.length && "IntersectionObserver" in window) {
    var observer = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (entry) {
          entry.target.classList.toggle("is-active", entry.isIntersecting);
        });
      },
      { rootMargin: "-40% 0px -40% 0px", threshold: 0 }
    );
    stepCards.forEach(function (card) { observer.observe(card); });
  }
})();
