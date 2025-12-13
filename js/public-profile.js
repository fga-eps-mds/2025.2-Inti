const BACKEND_URL = "https://20252-inti-production.up.railway.app";
const AUTH_TOKEN = localStorage.getItem("authToken");

let currentPublicProfile = null;
let currentPublicProfileId = null;
let currentPublicProfileUsername = null;
let publicProfileProductsCache = null;
let publicProfileProductsPromise = null;
let publicProfileProductsLoaded = false;
let postsGridElement = null;
let productsGridElement = null;

const publicEventsState = {
  container: null,
  loading: false,
  loaded: false,
  events: [],
};

let pendingViewParam = null;

document.addEventListener("DOMContentLoaded", () => {
  if (!AUTH_TOKEN) {
    console.error("Token não encontrado.");
    window.location.href = "../index.html";
    return;
  }

  const urlParams = new URLSearchParams(window.location.search);
  const username = urlParams.get("username");
  pendingViewParam = urlParams.get("view");

  if (pendingViewParam) {
    const cleanUrl = new URL(window.location.href);
    cleanUrl.searchParams.delete("view");
    window.history.replaceState({}, "", cleanUrl.pathname + cleanUrl.search);
  }

  currentPublicProfileUsername = username;
  currentPublicProfileId = null;

  initializeViewTabs();

  if (!username) {
    showError("Nenhum username informado!");
    return;
  }

  fetchProfileData(username);
  setupModal();
});

async function fetchProfileData(username) {
  try {
    const token = AUTH_TOKEN;
    const size = 10;
    const page = 0;

    console.log("Buscando perfil de:", username);

    const response = await fetch(
      `${BACKEND_URL}/profile/${username}?size=${size}&page=${page}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error(`Erro na requisição: ${response.status}`);
    }

    const profileData = await response.json();
    console.log("Dados recebidos:", profileData);

    currentPublicProfile = profileData;
    currentPublicProfileId =
      profileData.id ||
      profileData.profileId ||
      profileData.profile_id ||
      extractExplicitProfileId(profileData) ||
      extractProfileIdFromObject(profileData);
    publicProfileProductsCache = null;
    publicProfileProductsLoaded = false;
    publicProfileProductsPromise = null;
    resetPublicEventsState();

    populateProfileData(profileData);
  } catch (error) {
    console.error("Erro ao carregar perfil:", error);
    showError("Erro ao carregar perfil. Tente novamente.");
  }
}

function populateProfileData(data) {
  const userNameElement = document.querySelector(
    ".container-header .user-name"
  );
  const userUsernameElement = document.querySelector(
    ".container-header .user-username"
  );

  if (userNameElement) {
    userNameElement.textContent = data.name || "Nome não informado";
  }

  if (userUsernameElement) {
    userUsernameElement.textContent = data.username
      ? `@${data.username}`
      : "@usuário";
  }

  // Foto de perfil
  const profileImg = document.querySelector(".img-user-icon");
  if (profileImg) {
    if (data.profile_picture_url) {
      const fullImageUrl = data.profile_picture_url.startsWith("http")
        ? data.profile_picture_url
        : BACKEND_URL + data.profile_picture_url;
      setBackgroundImageWithBearer(profileImg, fullImageUrl, AUTH_TOKEN);
    } else {
      profileImg.style.backgroundImage = `url("../assets/profilePic.svg")`;
      profileImg.style.backgroundSize = "cover";
      profileImg.style.backgroundPosition = "center";
    }
  }

  const contactInfo = document.querySelector(".contact-text");
  if (contactInfo && data.bio) {
    contactInfo.innerHTML = data.bio.replace(/\n/g, "<br>");
  }

  updateProfileCounters(data);

  initializeFollowButton(data);

  toggleEventsTabVisibility(isOrganizationProfile(data));

  console.log("Chamando populateUserPosts com:", data.posts);
  populateUserPosts(data.posts || []);

  showPostsGrid();
  applyPendingViewParam();
}

function initializeFollowButton(data) {
  const followBtn = document.querySelector(".follow-icon");
  if (!followBtn) return;

  const img = followBtn.querySelector("img");
  if (!img) return;

  followBtn.dataset.followUrl = `/profile/${data.username}/follow`;
  followBtn.dataset.unfollowUrl = `/profile/${data.username}/unfollow`;

  const isFollowing =
    data.isFollowing ??
    data.following ??
    data.is_following ??
    data.followingCountIsMine ??
    false;

  updateFollowButtonState(followBtn, isFollowing);

  // Remove existing listeners to avoid duplicates if called multiple times
  const newBtn = followBtn.cloneNode(true);
  followBtn.parentNode.replaceChild(newBtn, followBtn);

  newBtn.addEventListener("click", handleFollowClick);
}

function updateFollowButtonState(followBtn, isFollowing) {
  const img = followBtn.querySelector("img");

  if (isFollowing) {
    followBtn.classList.add("active");
    img.src = "../assets/unfollow-icon.svg"; // Ensure asset exists or use text
    // Fallback if image doesn't exist, maybe change style
    followBtn.dataset.following = "true";
    followBtn.title = "Deixar de seguir";
  } else {
    followBtn.classList.remove("active");
    img.src = "../assets/follow-icon.svg";
    followBtn.dataset.following = "false";
    followBtn.title = "Seguir";
  }
}

async function handleFollowClick(event) {
  event.preventDefault();

  const followBtn = event.currentTarget;

  if (followBtn.classList.contains("loading")) return;

  followBtn.classList.add("loading");

  try {
    const isCurrentlyFollowing = followBtn.dataset.following === "true";
    const endpoint = isCurrentlyFollowing
      ? followBtn.dataset.unfollowUrl
      : followBtn.dataset.followUrl;
    const method = isCurrentlyFollowing ? "DELETE" : "POST";

    const fullUrl = BACKEND_URL + endpoint;

    console.log(`${method} para: ${fullUrl}`);

    const response = await fetch(fullUrl, {
      method: method,
      headers: {
        Authorization: `Bearer ${AUTH_TOKEN}`,
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error(`Erro na requisição: ${response.status}`);
    }

    const newFollowingState = !isCurrentlyFollowing;
    updateFollowButtonState(followBtn, newFollowingState);
    if (currentPublicProfile) {
      currentPublicProfile.isFollowing = newFollowingState;
    }

    // Update followers counter
    const followersCountElement = document.querySelector(
      ".profile-seguidores + .profile-number"
    );
    if (followersCountElement) {
      let count =
        parseInt(followersCountElement.textContent.replace("k", "000")) || 0; // Simple parsing
      if (newFollowingState) count++;
      else count = Math.max(0, count - 1);
      followersCountElement.textContent = formatNumber(count);
    }

    console.log(
      `Success: ${isCurrentlyFollowing ? "Unfollow" : "Follow"} realizado com sucesso`
    );
  } catch (error) {
    console.error("Erro ao executar follow/unfollow:", error);
    showError("Erro ao executar ação. Tente novamente.");
  } finally {
    followBtn.classList.remove("loading");
  }
}

async function updateFollowersCounter(isFollowing) {
  // Optimized to just increment/decrement locally instead of refetching
}

function updateProfileCounters(data) {
  console.log("Atualizando contadores...");

  const profileItems = document.querySelectorAll(".profile-item");

  profileItems.forEach((item) => {
    const label = item.querySelector(
      ".profile-seguindo, .profile-post, .profile-seguidores"
    );
    const numberElement = item.querySelector(".profile-number");

    if (!label || !numberElement) return;

    if (label.classList.contains("profile-seguidores")) {
      numberElement.textContent = formatNumber(data.followersCount);
    } else if (label.classList.contains("profile-seguindo")) {
      numberElement.textContent = formatNumber(data.followingCount);
    } else if (label.classList.contains("profile-post")) {
      numberElement.textContent = formatNumber(resolveTotalPosts(data));
    }
  });
}

function toggleEventsTabVisibility(shouldShow) {
  const eventsTab = document.querySelector(".event-btn");
  const eventsDivider = document.querySelector(".linha-events");
  if (!eventsTab) return;

  if (shouldShow) {
    eventsTab.style.display = "flex";
    if (eventsDivider) eventsDivider.style.display = "block";
  } else {
    eventsTab.style.display = "none";
    if (eventsDivider) eventsDivider.style.display = "none";
    hideEventsGrid();
  }
}

function isOrganizationProfile(profile = {}) {
  const type = typeof profile.type === "string" ? profile.type : "";
  return type.toLowerCase() === "organization";
}

function populateUserPosts(posts) {
  const postsGrid =
    postsGridElement || document.querySelector(".user-posts-grid");

  if (!postsGrid) {
    console.error("Elemento .user-posts-grid não encontrado");
    return;
  }

  postsGridElement = postsGrid;
  postsGrid.style.gridTemplateColumns = "";
  postsGrid.style.gap = "";

  postsGrid.innerHTML = "";

  if (!posts || posts.length === 0) {
    console.log("Nenhum post para exibir");
    postsGrid.innerHTML = '<p class="no-posts">Nenhum post ainda</p>';
    return;
  }

  const sortedPosts = [...posts].sort((a, b) => {
    const dateA = new Date(a.createdAt);
    const dateB = new Date(b.createdAt);
    return dateB - dateA;
  });

  sortedPosts.forEach((post, index) => {
    const postItem = createPostElement(post, index);
    postsGrid.appendChild(postItem);
  });
}

function initializeViewTabs() {
  postsGridElement = document.querySelector(".user-posts-grid");
  productsGridElement = document.querySelector(".user-products-grid");
  const eventsGrid = document.querySelector(".user-events-grid");

  if (eventsGrid) {
    initializeEventsGrid(eventsGrid);
  } else {
    publicEventsState.container = null;
  }

  const postsBtn = document.querySelector(".grid-btn");
  const productsBtn = document.querySelector(".product-btn");
  const eventsBtn = document.querySelector(".event-btn");
  const viewButtons = Array.from(document.querySelectorAll(".view-btn"));

  const setActiveTab = (button) => {
    viewButtons.forEach((btn) => {
      if (!btn) return;
      if (btn === button) btn.classList.add("active");
      else btn.classList.remove("active");
    });
  };

  if (postsBtn) {
    postsBtn.addEventListener("click", () => {
      setActiveTab(postsBtn);
      showPostsGrid();
    });
  }

  if (productsBtn) {
    productsBtn.addEventListener("click", () => {
      setActiveTab(productsBtn);
      showProductsGrid();
    });
  }

  if (eventsBtn) {
    eventsBtn.addEventListener("click", () => {
      setActiveTab(eventsBtn);
      showEventsGrid();
    });
  }

  if (postsBtn) {
    setActiveTab(postsBtn);
  }
}

function showPostsGrid() {
  postsGridElement =
    postsGridElement || document.querySelector(".user-posts-grid");
  if (postsGridElement) {
    postsGridElement.style.display = "grid";
  }
  if (productsGridElement) {
    productsGridElement.style.display = "none";
  }
  hideEventsGrid();
}

function showProductsGrid() {
  postsGridElement =
    postsGridElement || document.querySelector(".user-posts-grid");
  if (postsGridElement) {
    postsGridElement.style.display = "none";
  }

  productsGridElement =
    productsGridElement || document.querySelector(".user-products-grid");
  if (productsGridElement) {
    productsGridElement.style.display = "grid";
  }
  hideEventsGrid();
  showPublicProfileProducts();
}

function initializeEventsGrid(grid) {
  publicEventsState.container = grid;
  if (!grid) return;
  grid.style.display = "none";
  grid.style.gridTemplateColumns = "1fr";
  grid.style.gap = "12px";
  grid.style.marginTop = "16px";
}

function showEventsGrid() {
  if (!publicEventsState.container) return;
  if (!isOrganizationProfile(currentPublicProfile)) {
    showPublicEventsEmptyState("Eventos disponíveis apenas para organizações.");
    publicEventsState.loaded = true;
    return;
  }

  if (postsGridElement) {
    postsGridElement.style.display = "none";
  }
  if (productsGridElement) {
    productsGridElement.style.display = "none";
  }

  publicEventsState.container.style.display = "grid";

  if (!publicEventsState.loaded && !publicEventsState.loading) {
    loadPublicOrganizationEvents();
  }
}

function hideEventsGrid() {
  if (!publicEventsState.container) return;
  publicEventsState.container.style.display = "none";
}

function applyPendingViewParam() {
  if (!pendingViewParam) return;

  const postsBtn = document.querySelector(".grid-btn");
  const productsBtn = document.querySelector(".product-btn");
  const eventsBtn = document.querySelector(".event-btn");
  const targetView = pendingViewParam;
  pendingViewParam = null;

  if (targetView === "products" && productsBtn) {
    productsBtn.click();
    return;
  }

  if (
    targetView === "events" &&
    eventsBtn &&
    eventsBtn.style.display !== "none"
  ) {
    eventsBtn.click();
    return;
  }

  if (targetView === "posts" && postsBtn) {
    postsBtn.click();
  }
}

function createPostElement(post, index) {
  const postDiv = document.createElement("div");
  postDiv.className = `user-post-item rect-${(index % 5) + 1}`;
  postDiv.style.position = "relative";
  postDiv.dataset.postId = post.id;

  if (post.imgLink) {
    const fullImageUrl = post.imgLink.startsWith("http")
      ? post.imgLink
      : BACKEND_URL + post.imgLink;
    setBackgroundImageWithBearer(postDiv, fullImageUrl, AUTH_TOKEN);
  } else {
    const randomColor = getRandomColor();
    postDiv.style.backgroundColor = randomColor;
    postDiv.style.display = "flex";
    postDiv.style.alignItems = "center";
    postDiv.style.justifyContent = "center";
    postDiv.style.color = "white";
    postDiv.style.fontWeight = "bold";
    postDiv.textContent = "Post";
  }

  // Add click listener to open modal
  postDiv.addEventListener("click", () => openPostModal(post.id));

  return postDiv;
}

async function showPublicProfileProducts() {
  const productsGrid =
    productsGridElement || document.querySelector(".user-products-grid");
  if (!productsGrid) return;
  productsGridElement = productsGrid;

  if (publicProfileProductsLoaded) {
    populateUserProducts(publicProfileProductsCache || []);
    return;
  }

  productsGrid.innerHTML = '<p class="no-posts">Carregando produtos...</p>';

  try {
    const products = await fetchPublicProfileProducts();
    populateUserProducts(products);
  } catch (error) {
    console.error("Erro ao carregar produtos do perfil público:", error);
    const fallback = Array.isArray(currentPublicProfile?.products)
      ? currentPublicProfile.products
      : [];

    if (fallback.length) {
      populateUserProducts(fallback);
      return;
    }

    productsGrid.innerHTML =
      '<p class="no-posts">Erro ao carregar produtos.</p>';
  }
}

async function loadPublicOrganizationEvents() {
  if (publicEventsState.loading || publicEventsState.loaded) return;
  const eventsGrid = publicEventsState.container;

  if (!eventsGrid) return;

  if (!isOrganizationProfile(currentPublicProfile)) {
    showPublicEventsEmptyState("Este perfil não é uma organização.");
    publicEventsState.loaded = true;
    return;
  }

  const username =
    currentPublicProfile?.username || currentPublicProfileUsername || null;
  if (!username) {
    showPublicEventsEmptyState("Organização não encontrada.");
    publicEventsState.loaded = true;
    return;
  }

  publicEventsState.loading = true;
  setPublicEventsLoadingIndicator(true);

  try {
    const response = await fetch(
      `${BACKEND_URL}/org/${encodeURIComponent(username)}/events`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${AUTH_TOKEN}`,
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error(`Erro ao buscar eventos: ${response.status}`);
    }

    const data = await response.json();
    const events = normalizeEventsResponse(data);
    publicEventsState.events = events;

    if (!events.length) {
      showPublicEventsEmptyState("Nenhum evento publicado.");
      return;
    }

    renderPublicEventCards(events);
  } catch (error) {
    console.error("Erro ao carregar eventos públicos:", error);
    showPublicEventsEmptyState("Erro ao carregar eventos.");
  } finally {
    publicEventsState.loading = false;
    publicEventsState.loaded = true;
    setPublicEventsLoadingIndicator(false);
  }
}

function renderPublicEventCards(events = []) {
  const eventsGrid = publicEventsState.container;
  if (!eventsGrid) return;

  removePublicEventsEmptyState();
  eventsGrid.innerHTML = "";

  const orderedEvents = [...events].sort((a, b) => {
    const dateA = a?.date || a?.data || a?.eventTime;
    const dateB = b?.date || b?.data || b?.eventTime;
    const timeA = dateA ? new Date(dateA).getTime() : Number.MAX_SAFE_INTEGER;
    const timeB = dateB ? new Date(dateB).getTime() : Number.MAX_SAFE_INTEGER;
    return timeA - timeB;
  });

  orderedEvents.forEach((event) => {
    const card = createPublicEventCard(event);
    eventsGrid.appendChild(card);
  });
}

function createPublicEventCard(event = {}) {
  const card = document.createElement("div");
  card.className = "user-event-card";
  card.style.background = "#f2ebfb";
  card.style.borderRadius = "12px";
  card.style.padding = "12px";
  card.style.display = "flex";
  card.style.flexDirection = "column";
  card.style.gap = "8px";
  card.tabIndex = 0;

  const imageWrapper = document.createElement("div");
  imageWrapper.className = "event-card-image";
  imageWrapper.style.width = "100%";
  imageWrapper.style.height = "140px";
  imageWrapper.style.borderRadius = "10px";
  imageWrapper.style.backgroundColor = "#d9d9d9";
  imageWrapper.style.backgroundSize = "cover";
  imageWrapper.style.backgroundPosition = "center";

  const imageUrl = buildPublicEventImageUrl(
    event.imageUrl || event.imgLink || event.bannerUrl || ""
  );
  if (imageUrl) {
    setBackgroundImageWithBearer(imageWrapper, imageUrl, AUTH_TOKEN);
  } else {
    imageWrapper.style.display = "flex";
    imageWrapper.style.alignItems = "center";
    imageWrapper.style.justifyContent = "center";
    imageWrapper.style.color = "#592e83";
    imageWrapper.textContent = "SEM IMAGEM";
  }

  const infoWrapper = document.createElement("div");
  infoWrapper.className = "event-card-info";
  infoWrapper.style.display = "flex";
  infoWrapper.style.flexDirection = "column";
  infoWrapper.style.gap = "4px";

  const title = document.createElement("p");
  title.className = "event-card-title";
  title.textContent = event.title || event.name || "Evento";
  title.style.fontWeight = "600";
  title.style.color = "#592e83";

  const date = document.createElement("p");
  date.className = "event-card-date";
  date.textContent = formatPublicEventDate(
    event.date || event.data || event.eventTime
  );
  date.style.fontSize = "0.9rem";
  date.style.color = "#4b4b4b";

  infoWrapper.appendChild(title);
  infoWrapper.appendChild(date);

  card.appendChild(imageWrapper);
  card.appendChild(infoWrapper);

  if (event.id) {
    card.addEventListener("click", () => navigateToPublicEventDetail(event.id));
    card.addEventListener("keydown", (evt) => {
      if (evt.key === "Enter" || evt.key === " ") {
        evt.preventDefault();
        navigateToPublicEventDetail(event.id);
      }
    });
  }

  return card;
}

function formatPublicEventDate(rawDate) {
  if (!rawDate) return "Data a definir";
  const date = new Date(rawDate);
  if (Number.isNaN(date.getTime())) return "Data a definir";
  const datePart = date.toLocaleDateString("pt-BR");
  const timePart = date.toLocaleTimeString("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  });
  return `${datePart} às ${timePart}`;
}

function setPublicEventsLoadingIndicator(isLoading) {
  const eventsGrid = publicEventsState.container;
  if (!eventsGrid) return;

  if (isLoading && !publicEventsState.loaded) {
    eventsGrid.innerHTML =
      '<p class="events-loading">Carregando eventos...</p>';
  } else if (!isLoading) {
    const loadingEl = eventsGrid.querySelector(".events-loading");
    if (loadingEl) loadingEl.remove();
  }
}

function showPublicEventsEmptyState(message) {
  const eventsGrid = publicEventsState.container;
  if (!eventsGrid) return;
  eventsGrid.innerHTML = `<p class="events-empty">${message}</p>`;
}

function removePublicEventsEmptyState() {
  const eventsGrid = publicEventsState.container;
  if (!eventsGrid) return;
  const emptyEl = eventsGrid.querySelector(".events-empty");
  if (emptyEl) emptyEl.remove();
}

function buildPublicEventImageUrl(path) {
  if (!path || typeof path !== "string") return "";
  if (path.startsWith("http")) return path;
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return `${BACKEND_URL}${normalizedPath}`;
}

function navigateToPublicEventDetail(eventId) {
  if (!eventId) return;
  const basePath = "./event-detail.html";
  const separator = basePath.includes("?") ? "&" : "?";
  window.location.href = `${basePath}${separator}id=${encodeURIComponent(
    eventId
  )}`;
}

function resetPublicEventsState() {
  publicEventsState.loading = false;
  publicEventsState.loaded = false;
  publicEventsState.events = [];
  if (publicEventsState.container) {
    publicEventsState.container.innerHTML = "";
    publicEventsState.container.style.display = "none";
  }
}

async function fetchPublicProfileProducts() {
  if (publicProfileProductsLoaded) {
    return publicProfileProductsCache || [];
  }

  if (publicProfileProductsPromise) {
    return publicProfileProductsPromise;
  }

  publicProfileProductsPromise = (async () => {
    const profileId = await resolvePublicProfileId();

    if (!profileId) {
      const fallback = Array.isArray(currentPublicProfile?.products)
        ? currentPublicProfile.products
        : [];

      if (fallback.length) {
        publicProfileProductsCache = fallback;
        publicProfileProductsLoaded = true;
        return fallback;
      }

      throw new Error("ID do perfil não encontrado para carregar produtos.");
    }

    const response = await fetch(
      `${BACKEND_URL}/products/profile/${profileId}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${AUTH_TOKEN}`,
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error(`Erro ao buscar produtos: ${response.status}`);
    }

    const data = await response.json();
    const products = normalizeProductsResponse(data);
    publicProfileProductsCache = products;
    publicProfileProductsLoaded = true;
    return products;
  })();

  try {
    return await publicProfileProductsPromise;
  } finally {
    publicProfileProductsPromise = null;
  }
}

function normalizeProductsResponse(response) {
  if (!response) return [];
  if (Array.isArray(response)) return response;
  if (Array.isArray(response.content)) return response.content;
  if (Array.isArray(response.data)) return response.data;
  if (Array.isArray(response.items)) return response.items;
  return [];
}

function normalizeEventsResponse(response) {
  if (!response) return [];
  if (Array.isArray(response)) return response;
  if (Array.isArray(response.content)) return response.content;
  if (Array.isArray(response.data)) return response.data;
  if (Array.isArray(response.items)) return response.items;
  if (Array.isArray(response.events)) return response.events;
  return [];
}

async function resolvePublicProfileId() {
  const cachedId = getPublicProfileId();
  if (cachedId) return cachedId;

  if (!currentPublicProfileUsername) return null;

  try {
    const response = await fetch(
      `${BACKEND_URL}/search/${encodeURIComponent(
        currentPublicProfileUsername
      )}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${AUTH_TOKEN}`,
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error(`Erro ao consultar perfil público: ${response.status}`);
    }

    const data = await response.json();
    const candidates = normalizeProfilesResponse(data);

    for (const profile of candidates) {
      const candidateId = extractProfileIdFromObject(profile);
      if (!candidateId) continue;

      if (
        typeof profile.username === "string" &&
        typeof currentPublicProfileUsername === "string" &&
        profile.username.toLowerCase() ===
          currentPublicProfileUsername.toLowerCase()
      ) {
        currentPublicProfileId = candidateId;
        return candidateId;
      }
    }
    return null;
  } catch (error) {
    console.warn("Falha ao resolver ID do perfil público:", error);
    return null;
  }
}

function normalizeProfilesResponse(response) {
  if (!response) return [];
  if (Array.isArray(response)) return response;
  if (Array.isArray(response.content)) return response.content;
  if (Array.isArray(response.data)) return response.data;
  if (Array.isArray(response.items)) return response.items;
  if (Array.isArray(response.results)) return response.results;
  return [];
}

function getPublicProfileId() {
  if (!currentPublicProfile) return null;

  if (currentPublicProfileId) {
    return currentPublicProfileId;
  }

  const directId =
    currentPublicProfile.id ||
    currentPublicProfile.profileId ||
    currentPublicProfile.profile_id;
  if (directId) {
    currentPublicProfileId = directId;
    return directId;
  }

  const directCandidate = extractProfileIdFromObject(currentPublicProfile);
  if (directCandidate) return directCandidate;

  const postsCandidate = extractProfileIdFromCollection(
    currentPublicProfile.posts
  );
  if (postsCandidate) return postsCandidate;

  const productsCandidate = extractProfileIdFromCollection(
    currentPublicProfile.products
  );
  if (productsCandidate) return productsCandidate;

  return null;
}

function extractProfileIdFromCollection(items) {
  if (!Array.isArray(items)) return null;

  for (const item of items) {
    const candidate = extractProfileIdFromObject(item);
    if (candidate) {
      if (!currentPublicProfileId) {
        currentPublicProfileId = candidate;
      }
      return candidate;
    }
  }

  return null;
}

function extractProfileIdFromObject(entity) {
  if (!entity || typeof entity !== "object") return null;

  const candidates = extractExplicitProfileId(entity);
  if (candidates) {
    return candidates;
  }

  const relationships = [
    entity.profile,
    entity.user,
    entity.owner,
    entity.author,
  ];
  for (const relation of relationships) {
    const relationCandidate = extractExplicitProfileId(relation);
    if (relationCandidate) {
      return relationCandidate;
    }
  }

  return null;
}

function extractExplicitProfileId(entity) {
  if (!entity || typeof entity !== "object") return null;

  const candidates = [
    entity.profileId,
    entity.profile_id,
    entity.userId,
    entity.user_id,
    entity.profileUuid,
    entity.profile_uuid,
    entity.userUuid,
    entity.user_uuid,
    entity.ownerId,
    entity.owner_id,
    entity.accountId,
    entity.account_id,
    entity.profile?.id,
    entity.profile?.profileId,
    entity.profile?.profile_id,
    entity.profile?.userId,
    entity.profile?.user_id,
    entity.user?.profileId,
    entity.user?.profile_id,
    entity.user?.id,
    entity.user?.userId,
    entity.user?.user_id,
    entity.owner?.profileId,
    entity.owner?.profile_id,
  ].filter(Boolean);

  return candidates.length > 0 ? candidates[0] : null;
}

function populateUserProducts(products) {
  const productsGrid =
    productsGridElement || document.querySelector(".user-products-grid");

  if (!productsGrid) return;

  productsGridElement = productsGrid;
  productsGrid.style.gridTemplateColumns = "repeat(2, minmax(0, 1fr))";
  productsGrid.style.gap = "10px";

  productsGrid.innerHTML = "";

  if (!products || products.length === 0) {
    productsGrid.innerHTML = '<p class="no-posts">Nenhum produto ainda</p>';
    return;
  }

  const sortedProducts = [...products].sort((a, b) => {
    if (a.createdAt && b.createdAt) {
      const dateA = new Date(a.createdAt);
      const dateB = new Date(b.createdAt);
      return dateB - dateA;
    }
    return 0;
  });

  sortedProducts.forEach((product) => {
    const card = createPublicProductCard(product);
    productsGrid.appendChild(card);
  });
}

function createPublicProductCard(product = {}) {
  const card = document.createElement("div");
  card.className = "user-product-card";
  card.style.background = "#f2ebfb";
  card.style.borderRadius = "12px";
  card.style.padding = "10px";
  card.style.display = "flex";
  card.style.flexDirection = "column";
  card.style.gap = "8px";

  const imageWrapper = document.createElement("div");
  imageWrapper.className = "product-card-image";
  imageWrapper.style.width = "100%";
  imageWrapper.style.height = "120px";
  imageWrapper.style.borderRadius = "10px";
  imageWrapper.style.backgroundColor = "#d9d9d9";
  imageWrapper.style.backgroundSize = "cover";
  imageWrapper.style.backgroundPosition = "center";

  const imageUrl = getPublicProductImageUrl(product);
  if (imageUrl) {
    setBackgroundImageWithBearer(imageWrapper, imageUrl, AUTH_TOKEN);
  } else {
    imageWrapper.style.display = "flex";
    imageWrapper.style.alignItems = "center";
    imageWrapper.style.justifyContent = "center";
    imageWrapper.style.color = "#592e83";
    imageWrapper.style.fontWeight = "600";
    const fallbackLetter = getPublicProductTitle(product)
      .charAt(0)
      .toUpperCase();
    imageWrapper.textContent = fallbackLetter || "P";
  }

  const infoContainer = document.createElement("div");
  infoContainer.className = "product-card-body";
  infoContainer.style.display = "flex";
  infoContainer.style.flexDirection = "column";
  infoContainer.style.gap = "4px";

  const titleElement = document.createElement("p");
  titleElement.className = "product-card-title";
  titleElement.textContent = getPublicProductTitle(product);
  titleElement.style.fontWeight = "600";
  titleElement.style.color = "#592e83";

  const description = getPublicProductDescription(product);
  if (description) {
    const descriptionElement = document.createElement("p");
    descriptionElement.className = "product-card-description";
    descriptionElement.textContent = description;
    descriptionElement.style.fontSize = "12px";
    descriptionElement.style.color = "#525252";
    infoContainer.appendChild(descriptionElement);
  }

  const price = formatPublicProductPrice(product);
  if (price) {
    const priceElement = document.createElement("p");
    priceElement.className = "product-card-price";
    priceElement.textContent = price;
    priceElement.style.fontWeight = "700";
    priceElement.style.color = "#171717";
    infoContainer.appendChild(priceElement);
  }

  infoContainer.insertBefore(titleElement, infoContainer.firstChild);

  card.appendChild(imageWrapper);
  card.appendChild(infoContainer);

  const productId = product.id || product.productId;
  if (productId) {
    card.tabIndex = 0;
    card.addEventListener("click", () =>
      navigateToPublicProductDetail(productId)
    );
    card.addEventListener("keydown", (event) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        navigateToPublicProductDetail(productId);
      }
    });
  }

  return card;
}

function getPublicProductTitle(product = {}) {
  return (
    product.title ||
    product.name ||
    product.productName ||
    product.displayName ||
    "Produto"
  );
}

function getPublicProductDescription(product = {}) {
  return (
    product.description ||
    product.details ||
    product.summary ||
    product.about ||
    ""
  );
}

function formatPublicProductPrice(product = {}) {
  const rawPrice =
    product.price ?? product.value ?? product.cost ?? product.amount ?? null;

  if (rawPrice === null || rawPrice === undefined || rawPrice === "") {
    return "";
  }

  const numericPrice = Number(rawPrice);
  if (!Number.isNaN(numericPrice)) {
    return numericPrice.toLocaleString("pt-BR", {
      style: "currency",
      currency: "BRL",
    });
  }

  return rawPrice;
}

function getPublicProductImageUrl(product = {}) {
  const imagePath =
    product.imageUrl ||
    product.imgLink ||
    product.image ||
    product.coverImage ||
    product.thumbnail;

  if (!imagePath) return "";

  const trimmed = String(imagePath).trim();
  if (!trimmed) return "";

  if (trimmed.startsWith("http")) {
    return trimmed;
  }

  const normalizedPath = trimmed.startsWith("/") ? trimmed : `/${trimmed}`;
  return `${BACKEND_URL}${normalizedPath}`;
}

function navigateToPublicProductDetail(productId) {
  if (!productId) return;
  const basePath = "./products-detail.html";
  const separator = basePath.includes("?") ? "&" : "?";
  window.location.href = `${basePath}${separator}id=${encodeURIComponent(
    productId
  )}`;
}

async function setBackgroundImageWithBearer(element, imageUrl, token) {
  try {
    const response = await fetch(imageUrl, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      throw new Error(`Erro ao carregar imagem: ${response.status}`);
    }

    const blob = await response.blob();
    const objectUrl = URL.createObjectURL(blob);

    element.style.backgroundImage = `url("${objectUrl}")`;
    element.style.backgroundSize = "cover";
    element.style.backgroundPosition = "center";
  } catch (error) {
    console.error("Erro ao carregar imagem:", error);
    element.style.backgroundColor = getRandomColor();
  }
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

function resolveTotalPosts(profile = {}) {
  if (profile.totalPosts !== undefined && profile.totalPosts !== null) {
    const total = Number(profile.totalPosts);
    if (Number.isFinite(total) && total >= 0) {
      return total;
    }
  }
  return Array.isArray(profile.posts) ? profile.posts.length : 0;
}

function formatNumber(num) {
  if (num === null || num === undefined || isNaN(num)) {
    return "0";
  }

  if (num >= 1000) {
    return (num / 1000).toFixed(1) + "k";
  }
  return num.toString();
}

function showError(message) {
  const errorDiv = document.createElement("div");
  errorDiv.className = "error-message";
  errorDiv.textContent = message;
  errorDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #ff4444;
        color: white;
        padding: 15px;
        border-radius: 5px;
        z-index: 1000;
    `;

  document.body.appendChild(errorDiv);

  setTimeout(() => {
    errorDiv.remove();
  }, 5000);
}

// Modal Logic
function setupModal() {
  const modal = document.getElementById("postModal");
  const closeBtn = document.querySelector(".close");

  if (closeBtn) {
    closeBtn.addEventListener("click", function () {
      modal.style.display = "none";
    });
  }

  window.addEventListener("click", function (event) {
    if (event.target === modal) {
      modal.style.display = "none";
    }
  });
}

async function openPostModal(postId) {
  console.log("Opening post modal for postId:", postId);

  const modal = document.getElementById("postModal");
  const modalImage = document.getElementById("modalPostImage");
  const modalProfilePic = document.getElementById("modalProfilePic");
  const modalUsername = document.getElementById("modalUsername");
  const modalDescription = document.getElementById("modalDescription");
  const modalDate = document.getElementById("modalDate");
  const modalLikes = document.getElementById("modalLikes");

  if (!modal) {
    console.error("Modal not found!");
    return;
  }

  // Show loading state
  modalImage.style.display = "none";
  modalUsername.textContent = "Carregando...";
  modalDescription.textContent = "";
  modalDate.textContent = "";
  modalLikes.textContent = "";
  modalProfilePic.style.backgroundColor = getRandomColor();

  // Show modal
  modal.style.display = "block";

  try {
    // Use apiService if available, otherwise fetch manually
    // Assuming apiService is global or we fetch manually
    const response = await fetch(`${BACKEND_URL}/post/${postId}`, {
      headers: {
        Authorization: `Bearer ${AUTH_TOKEN}`,
      },
    });

    if (!response.ok) throw new Error("Failed to fetch post details");

    const post = await response.json();

    // Update modal with post details
    modalUsername.textContent = post.author.name || post.author.username;
    modalDescription.textContent = post.description || "";
    modalDate.textContent = new Date(post.createdAt).toLocaleDateString(
      "pt-BR"
    );
    modalLikes.textContent = `${post.likesCount || 0} curtidas`;

    // Load profile picture
    if (
      post.author.profilePictureUrl &&
      post.author.profilePictureUrl.trim() !== ""
    ) {
      const fullProfileImageUrl = post.author.profilePictureUrl.startsWith(
        "http"
      )
        ? post.author.profilePictureUrl
        : BACKEND_URL + post.author.profilePictureUrl;
      setBackgroundImageWithBearer(
        modalProfilePic,
        fullProfileImageUrl,
        AUTH_TOKEN
      );
    } else {
      modalProfilePic.style.backgroundColor = getRandomColor();
    }

    // Load post image
    if (post.imageUrl && post.imageUrl.trim() !== "") {
      const fullImageUrl = post.imageUrl.startsWith("http")
        ? post.imageUrl
        : BACKEND_URL + post.imageUrl;
      modalImage.src = fullImageUrl;
      modalImage.style.display = "block";
    } else {
      modalImage.style.display = "none";
    }
  } catch (error) {
    console.error("Error loading post details:", error);
    modalUsername.textContent = "Erro";
    modalDescription.textContent =
      "Não foi possível carregar os detalhes do post.";
  }
}

// EXPORTAR FUNÇÕES GLOBALMENTE
window.populateUserPosts = populateUserPosts;
window.populateUserProducts = populateUserProducts;
