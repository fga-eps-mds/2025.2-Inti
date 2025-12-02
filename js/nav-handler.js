// Navigation Handler for Navbar Buttons
document.addEventListener("DOMContentLoaded", function () {
  // Check if we're in the pages directory (relative path handling)
  const isInPagesDir = window.location.pathname.includes("/pages/");

  // Adjust paths based on current location
  function getAdjustedRoute(route) {
    // If we're in pages directory and route doesn't start with ../, prepend ../
    if (isInPagesDir && !route.startsWith("../")) {
      return "../" + route;
    }
    return route;
  }

  // Handle button-based navbars (pages with .nav-btn elements)
  if (document.querySelector(".navbar .nav-btn")) {
    // Home button - first button in navbar
    const homeBtn = document.querySelector(".navbar .nav-btn:nth-child(1)");
    if (homeBtn) {
      homeBtn.addEventListener("click", function () {
        const route = getAdjustedRoute("pages/home.html");
        window.location.href = route;
      });
    }

    // Explore/Search button - second button in navbar
    const exploreBtn = document.querySelector(".navbar .nav-btn:nth-child(2)");
    if (exploreBtn) {
      exploreBtn.addEventListener("click", function () {
        // Navigate to search page
        const route = getAdjustedRoute("pages/search.html");
        window.location.href = route;
      });
    }

    // Add/Create button - third button in navbar (with nav-btn-add class)
    const addBtn = document.querySelector(".navbar .nav-btn-add");
    if (addBtn) {
      addBtn.addEventListener("click", function () {
        const route = getAdjustedRoute("pages/create.html");
        window.location.href = route;
      });
    }

    // Calendar/Events button - fourth button in navbar
    const calendarBtn = document.querySelector(".navbar .nav-btn:nth-child(4)");
    if (calendarBtn) {
      calendarBtn.addEventListener("click", function () {
        const route = getAdjustedRoute("pages/my-events.html");
        window.location.href = route;
      });
    }

    // Profile button - fifth button in navbar
    const profileBtn = document.querySelector(".navbar .nav-btn:nth-child(5)");
    if (profileBtn) {
      profileBtn.addEventListener("click", function () {
        const route = getAdjustedRoute("pages/profile.html");
        window.location.href = route;
      });
    }
  }

  // Handle image-based navbars (event-detail.html and post-detail.html)
  if (document.querySelector(".navbar img")) {
    // Home button
    const homeImg = document.querySelector(".nav-home");
    if (homeImg) {
      homeImg.addEventListener("click", function () {
        const route = getAdjustedRoute("pages/home.html");
        window.location.href = route;
      });
    }

    // Search button
    const searchImg = document.querySelector(".nav-search");
    if (searchImg) {
      searchImg.addEventListener("click", function () {
        const route = getAdjustedRoute("pages/search.html");
        window.location.href = route;
      });
    }

    // Create button
    const createImg = document.querySelector(".nav-create");
    if (createImg) {
      createImg.addEventListener("click", function () {
        const route = getAdjustedRoute("pages/create.html");
        window.location.href = route;
      });
    }

    // Calendar button
    const calendarImg = document.querySelector(".nav-calendar");
    if (calendarImg) {
      calendarImg.addEventListener("click", function () {
        const route = getAdjustedRoute("pages/my-events.html");
        window.location.href = route;
      });
    }

    // Profile button
    const profileImg = document.querySelector(".nav-user");
    if (profileImg) {
      profileImg.addEventListener("click", function () {
        const route = getAdjustedRoute("pages/profile.html");
        window.location.href = route;
      });
    }
  }
});
