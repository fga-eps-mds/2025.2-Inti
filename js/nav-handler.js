const ACTION_MODAL_ROUTES = {
  post: "pages/publish.html",
  product: "pages/service.html",
  event: "pages/create-event.html",
};

let actionModalElement = null;
let actionModalEscapeListenerAttached = false;

function getUserType() {
  try {
    const stored = localStorage.getItem("userData");
    if (!stored) return null;
    const parsed = JSON.parse(stored);
    return parsed?.type || null;
  } catch (error) {
    console.warn("Failed to parse userData from localStorage", error);
    return null;
  }
}

function canCreateEvents() {
  return (getUserType() || "user").toLowerCase() === "organization";
}

function getAdjustedRoute(route) {
  const isInPagesDir = window.location.pathname.includes("/pages/");
  if (isInPagesDir && !route.startsWith("../")) {
    return "../" + route;
  }
  return route;
}

function ensureActionModal() {
  if (actionModalElement && document.body.contains(actionModalElement)) {
    return actionModalElement;
  }

  let modal = document.getElementById("actionModal");
  if (!modal) {
    const allowEventCreation = canCreateEvents();
    const eventButtonMarkup = allowEventCreation
      ? '<button class="modal-item" type="button" data-action="event" style="color: #592e83; font-family: Maitree, serif; font-weight: 600; font-size: 18px;">📅 Criar evento</button>'
      : "";

    modal = document.createElement("div");
    modal.id = "actionModal";
    modal.className = "action-modal";
    modal.setAttribute("aria-hidden", "true");
    modal.innerHTML = `
      <div class="modal-content" role="dialog" aria-modal="true" aria-labelledby="actionModalTitle">
        <div style="position: relative; text-align: center;">
          <h3 class="modal-title" id="actionModalTitle" style="color: #592e83; font-family: Maitree, serif; font-weight: bold;">Selecione uma opção:</h3>
        </div>
        <button class="modal-item" type="button" data-action="post" style="color: #592e83; font-family: Maitree, serif; font-weight: 600; font-size: 18px;">📸 Criar post</button>
        <button class="modal-item" type="button" data-action="product" style="color: #592e83; font-family: Maitree, serif; font-weight: 600; font-size: 18px;">🛒 Criar produto</button>
        ${eventButtonMarkup}
      </div>
    `;
    document.body.appendChild(modal);
  }

  actionModalElement = modal;
  attachActionModalEvents(modal);
  return modal;
}

function attachActionModalEvents(modal) {
  if (!modal || modal.dataset.modalInitialized === "true") return;

  const closeBtn = modal.querySelector(".close-modal");
  if (closeBtn) {
    closeBtn.addEventListener("click", function (event) {
      event.preventDefault();
      closeActionModal();
    });
  }

  modal.addEventListener("click", function (event) {
    if (event.target === modal) {
      closeActionModal();
    }
  });

  const actionButtons = modal.querySelectorAll(".modal-item[data-action]");
  actionButtons.forEach((button) => {
    button.addEventListener("click", function () {
      const actionType = button.getAttribute("data-action");
      const route = getAdjustedRoute(
        ACTION_MODAL_ROUTES[actionType] || ACTION_MODAL_ROUTES.post
      );
      closeActionModal();
      window.location.href = route;
    });
  });

  if (!actionModalEscapeListenerAttached) {
    document.addEventListener("keydown", handleActionModalKeydown);
    actionModalEscapeListenerAttached = true;
  }

  modal.dataset.modalInitialized = "true";
}

function openActionModal(event) {
  if (event) {
    event.preventDefault();
    event.stopImmediatePropagation();
  }

  const modal = ensureActionModal();
  if (!modal) return;
  modal.setAttribute("aria-hidden", "false");
  document.body.classList.add("modal-open");
}

function closeActionModal() {
  if (!actionModalElement) return;
  actionModalElement.setAttribute("aria-hidden", "true");
  document.body.classList.remove("modal-open");
}

function handleActionModalKeydown(event) {
  if (event.key === "Escape" && actionModalElement) {
    const isOpen = actionModalElement.getAttribute("aria-hidden") === "false";
    if (isOpen) {
      closeActionModal();
    }
  }
}

// Navigation Handler for Navbar Buttons
document.addEventListener("DOMContentLoaded", function () {
  if (document.querySelector(".navbar .nav-btn")) {
    const homeBtn = document.querySelector(".navbar .nav-btn:nth-child(1)");
    if (homeBtn) {
      homeBtn.addEventListener("click", function () {
        window.location.href = getAdjustedRoute("pages/home.html");
      });
    }

    const exploreBtn = document.querySelector(".navbar .nav-btn:nth-child(2)");
    if (exploreBtn) {
      exploreBtn.addEventListener("click", function () {
        window.location.href = getAdjustedRoute("pages/search.html");
      });
    }

    const addBtn = document.querySelector(".navbar .nav-btn-add");
    if (addBtn) {
      addBtn.addEventListener("click", openActionModal);
    }

    const calendarBtn = document.querySelector(".navbar .nav-btn:nth-child(4)");
    if (calendarBtn) {
      calendarBtn.addEventListener("click", function () {
        window.location.href = getAdjustedRoute("pages/my-events.html");
      });
    }

    const profileBtn = document.querySelector(".navbar .nav-btn:nth-child(5)");
    if (profileBtn) {
      profileBtn.addEventListener("click", function () {
        window.location.href = getAdjustedRoute("pages/profile.html");
      });
    }
  }

  if (document.querySelector(".navbar img")) {
    const homeImg = document.querySelector(".nav-home");
    if (homeImg) {
      homeImg.addEventListener("click", function () {
        window.location.href = getAdjustedRoute("pages/home.html");
      });
    }

    const searchImg = document.querySelector(".nav-search");
    if (searchImg) {
      searchImg.addEventListener("click", function () {
        window.location.href = getAdjustedRoute("pages/search.html");
      });
    }

    const createImg = document.querySelector(".nav-create");
    if (createImg) {
      createImg.addEventListener("click", openActionModal);
    }

    const calendarImg = document.querySelector(".nav-calendar");
    if (calendarImg) {
      calendarImg.addEventListener("click", function () {
        window.location.href = getAdjustedRoute("pages/my-events.html");
      });
    }

    const profileImg = document.querySelector(".nav-user");
    if (profileImg) {
      profileImg.addEventListener("click", function () {
        window.location.href = getAdjustedRoute("pages/profile.html");
      });
    }
  }
});
