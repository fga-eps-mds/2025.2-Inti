const searchInput = document.getElementById("searchInput");
const searchBtn = document.getElementById("searchBtn");
const resultsContainer = document.getElementById("searchResults");

const MAX_RESULTS = 10;
const DEBOUNCE_DELAY = 300;
const INITIAL_MESSAGE =
  '<div class="initial-message">Digite um username para buscar</div>';

let activeSearchController = null;

initializeSearchUI();

function initializeSearchUI() {
  if (resultsContainer) {
    resultsContainer.innerHTML = INITIAL_MESSAGE;
  }

  if (!searchInput) {
    return;
  }

  const debouncedSearch = debounce((value) => performSearch(value), DEBOUNCE_DELAY);

  searchInput.addEventListener("input", (event) => {
    const query = event.target.value.trim();
    if (!query) {
      cancelActiveSearch();
      renderInitialMessage();
      return;
    }
    debouncedSearch(query);
  });

  searchInput.addEventListener("keydown", (event) => {
    if (event.key !== "Enter") return;
    event.preventDefault();
    const query = searchInput.value.trim();
    if (!query) {
      renderInitialMessage();
      return;
    }
    performSearch(query);
  });

  if (searchBtn) {
    searchBtn.addEventListener("click", () => {
      const query = searchInput.value.trim();
      if (!query) {
        renderInitialMessage();
        return;
      }
      performSearch(query);
    });
  }
}

async function performSearch(query) {
  if (!query || !resultsContainer) {
    return;
  }

  cancelActiveSearch();
  activeSearchController = new AbortController();

  resultsContainer.innerHTML = '<div class="loading">Buscando...</div>';

  try {
    const profiles = await apiService.searchProfiles(
      query,
      MAX_RESULTS,
      activeSearchController.signal
    );

    const users = Array.isArray(profiles) ? profiles : [];

    if (!users.length) {
      renderNoResults();
      return;
    }

    displayResults(users);
  } catch (error) {
    if (error.name === "AbortError") {
      return;
    }
    console.error("Search error:", error);
    resultsContainer.innerHTML =
      '<div class="error">Erro ao buscar. Tente novamente.</div>';
  } finally {
    activeSearchController = null;
  }
}

function displayResults(users) {
  if (!resultsContainer) return;

  resultsContainer.innerHTML = users
    .map((user) => {
      const username = sanitizeUsername(user.username || user.userName || "");
      const displayName =
        user.name || user.displayName || username || "Usuário";
      const picField =
        user.profilePictureUrl ||
        user.profile_picture_url ||
        user.imageUrl ||
        user.profile_image;
      const profilePicUrl = buildProfilePictureUrl(picField);

      const avatarStyle = profilePicUrl
        ? `background-image: url('${profilePicUrl}')`
        : `background-color: ${getRandomColor()}`;

      return `
        <a
          href="public-profile.html?username=${encodeURIComponent(username)}"
          class="search-result-item"
          data-username="${escapeHtml(username)}"
        >
          <div class="result-avatar" style="${avatarStyle}"></div>
          <div class="result-info">
            <div class="result-name">${escapeHtml(displayName)}</div>
            <div class="result-username">@${escapeHtml(username)}</div>
          </div>
        </a>
      `;
    })
    .join("");

  attachResultHandlers();
}

function renderInitialMessage() {
  if (resultsContainer) {
    resultsContainer.innerHTML = INITIAL_MESSAGE;
  }
}

function renderNoResults() {
  if (resultsContainer) {
    resultsContainer.innerHTML =
      '<div class="no-results">Nenhum usuário encontrado</div>';
  }
}

function cancelActiveSearch() {
  if (activeSearchController) {
    activeSearchController.abort();
    activeSearchController = null;
  }
}

function buildProfilePictureUrl(picField) {
  if (!picField) return "";
  const backendUrl =
    typeof apiService !== "undefined" && apiService.baseURL
      ? apiService.baseURL
      : "";

  if (/^https?:\/\//i.test(picField)) {
    return picField;
  }

  if (picField.startsWith("/")) {
    return backendUrl ? `${backendUrl}${picField}` : picField;
  }

  return backendUrl ? `${backendUrl}/images/${picField}` : `/images/${picField}`;
}

function sanitizeUsername(username) {
  if (!username || typeof username !== "string") {
    return "";
  }
  return username.trim();
}

function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function getRandomColor() {
  const colors = [
    "#FF6B6B",
    "#4ECDC4",
    "#45B7D1",
    "#96CEB4",
    "#FFEAA7",
    "#DDA0DD",
    "#98D8C8",
  ];
  return colors[Math.floor(Math.random() * colors.length)];
}

function debounce(fn, delay) {
  let timeoutId;
  return (...args) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => {
      Promise.resolve(fn(...args)).catch((error) =>
        console.error("Debounced function error:", error)
      );
    }, delay);
  };
}

function attachResultHandlers() {
  if (!resultsContainer) return;
  const anchors = resultsContainer.querySelectorAll(".search-result-item");
  anchors.forEach((anchor) => {
    anchor.addEventListener("click", (event) => {
      event.preventDefault();
      const username = anchor.dataset.username || "";
      handleProfileNavigation(username);
    });
  });
}

function handleProfileNavigation(targetUsername) {
  const sanitizedTarget = sanitizeUsername(targetUsername);
  if (!sanitizedTarget) {
    return;
  }

  const currentUsername = getCurrentUsername();
  const isCurrentUser =
    currentUsername &&
    sanitizedTarget.toLowerCase() === currentUsername.toLowerCase();

  if (isCurrentUser) {
    window.location.href = "profile.html";
    return;
  }

  window.location.href = `public-profile.html?username=${encodeURIComponent(
    sanitizedTarget
  )}`;
}

function getCurrentUsername() {
  try {
    const stored = localStorage.getItem("userData");
    if (!stored) return null;
    const parsed = JSON.parse(stored);
    const username =
      parsed?.username ||
      parsed?.user?.username ||
      parsed?.profile?.username ||
      null;
    return username ? username.trim() : null;
  } catch (error) {
    console.warn("Não foi possível ler o usuário atual do armazenamento:", error);
    return null;
  }
}
