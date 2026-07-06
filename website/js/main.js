(function () {
  "use strict";

  // Mobile nav toggle
  var navToggle = document.getElementById("navToggle");
  var nav = navToggle ? navToggle.closest(".nav") : null;
  if (navToggle && nav) {
    navToggle.addEventListener("click", function () {
      nav.classList.toggle("open");
    });
    nav.querySelectorAll(".nav-links a").forEach(function (link) {
      link.addEventListener("click", function () {
        nav.classList.remove("open");
      });
    });
  }

  // Waitlist form: static site, no backend yet — capture locally and
  // acknowledge the submission client-side only.
  var form = document.getElementById("waitlistForm");
  var note = document.getElementById("waitlistNote");
  if (form && note) {
    form.addEventListener("submit", function (event) {
      event.preventDefault();
      var input = form.querySelector("input[type=email]");
      if (input && input.value) {
        note.textContent = "Thanks! We'll email " + input.value + " the moment we launch.";
        form.reset();
      }
    });
  }
})();
