// Search functionality
let searchTimeout;
const searchInput = document.getElementById("searchInput");
const resultsContainer = document.getElementById("searchResults");

if (searchInput) {
  searchInput.addEventListener("input", (e) => {
    const query = e.target.value.trim();

    clearTimeout(searchTimeout);

    if (query.length < 2) {
      resultsContainer.innerHTML =
        '<div class="initial-message">Digite para buscar usuários</div>';
      return;
    }

    searchTimeout = setTimeout(async () => {
      await performSearch(query);
    }, 300); // Debounce 300ms
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
  const backendUrl = "https://20252-inti-production.up.railway.app";

  resultsContainer.innerHTML = users
    .map((user) => {
      const profilePicUrl = user.profile_picture_url
        ? backendUrl + user.profile_picture_url
        : "";
      const avatarStyle = profilePicUrl
        ? `background-image: url('${profilePicUrl}')`
        : `background-color: ${getRandomColor()}`;

      return `
      <a href="public-profile.html?username=${user.username}" class="search-result-item">
        <div class="result-avatar" style="${avatarStyle}"></div>
        <div class="result-info">
          <div class="result-name">${user.name}</div>
          <div class="result-username">@${user.username}</div>
        </div>
      </a>
    `;
    })
    .join("");
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
