// Search functionality
const searchInput = document.getElementById("searchInput");
const searchBtn = document.getElementById("searchBtn");
const resultsContainer = document.getElementById("searchResults");

// Behavior: user types full username then clicks the search button (or presses Enter)
if (searchBtn && searchInput) {
  searchBtn.addEventListener("click", async () => {
    const query = searchInput.value.trim();
    if (!query) {
      resultsContainer.innerHTML =
        '<div class="initial-message">Digite um username para buscar</div>';
      return;
    }

    await performSearch(query);
  });

  // Allow pressing Enter in the input to trigger the search
  searchInput.addEventListener("keydown", async (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      const query = searchInput.value.trim();
      if (!query) {
        resultsContainer.innerHTML =
          '<div class="initial-message">Digite um username para buscar</div>';
        return;
      }
      await performSearch(query);
    }
  });
} else if (searchInput) {
  // Fallback: if button missing, allow Enter to search
  searchInput.addEventListener("keydown", async (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      const query = searchInput.value.trim();
      if (!query) {
        resultsContainer.innerHTML =
          '<div class="initial-message">Digite um username para buscar</div>';
        return;
      }
      await performSearch(query);
    }
  });
}

async function performSearch(query) {
  try {
    resultsContainer.innerHTML = '<div class="loading">Buscando...</div>';

    // Search by username using the public profile endpoint
    const result = await apiService.getPublicProfile(query);

    if (result) {
      displayResults([result]);
    } else {
      resultsContainer.innerHTML =
        '<div class="no-results">Nenhum resultado encontrado</div>';
    }
  } catch (error) {
    console.error("Search error:", error);
    if (error.message && error.message.includes("404")) {
      resultsContainer.innerHTML =
        '<div class="no-results">Nenhum usuário encontrado</div>';
    } else {
      resultsContainer.innerHTML =
        '<div class="error">Erro ao buscar. Tente novamente.</div>';
    }
  }
}

function displayResults(users) {
  const backendUrl =
    typeof apiService !== "undefined" && apiService.baseURL
      ? apiService.baseURL
      : "https://20252-inti-production.up.railway.app";

  resultsContainer.innerHTML = users
    .map((user) => {
      // Support different field names returned by backend (camelCase or snake_case)
      const picField =
        user.profilePictureUrl ||
        user.profile_picture_url ||
        user.imageUrl ||
        user.profile_image;

      let profilePicUrl = "";
      if (picField) {
        // If backend already returned a full URL
        if (/^https?:\/\//i.test(picField)) {
          profilePicUrl = picField;
        } else if (picField.startsWith("/")) {
          // Path starting with slash (e.g. /images/xxx.jpg)
          profilePicUrl = `${backendUrl}${picField}`;
        } else {
          // Bare filename — hit the /images endpoint
          profilePicUrl = `${backendUrl}/images/${picField}`;
        }
      }

      const avatarStyle = profilePicUrl
        ? `background-image: url('${profilePicUrl}')`
        : `background-color: ${getRandomColor()}`;

      const displayName = user.name || user.displayName || "Sem nome";
      const username = user.username || user.userName || "";

      return `
      <a href="public-profile.html?username=${encodeURIComponent(username)}" class="search-result-item">
        <div class="result-avatar" style="${avatarStyle}"></div>
        <div class="result-info">
          <div class="result-name">${escapeHtml(displayName)}</div>
          <div class="result-username">@${escapeHtml(username)}</div>
        </div>
      </a>
    `;
    })
    .join("");
}

// Simple HTML escape to avoid injecting unsafe content
function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\"/g, "&quot;")
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
