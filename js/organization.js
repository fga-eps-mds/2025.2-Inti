// --- Constants & Config ---
const POSTS_PAGE_SIZE = 12;
const GRID_PAGE_SIZE = 6;
const MAX_POST_PAGES = 12;

// --- Unified State Management ---
// This handles the logic for both Products and Events tabs
const productsState = {
  type: 'products',
  container: null,
  page: 0,
  loading: false,
  finished: false,
  initialized: false,
  active: false,
  isPaginated: true, // Products API supports paging
  emptyMessage: "Nenhum produto cadastrado.",
  apiMethod: (page, size) => apiService.getMyProducts(page, size)
};

const eventsState = {
  type: 'events',
  container: null,
  page: 0,
  loading: false,
  finished: false,
  initialized: false,
  active: false,
  isPaginated: false, // Events API does NOT support paging
  emptyMessage: "Nenhum evento cadastrado.",
  apiMethod: () => apiService.getMyEvents() // No parameters for events
};

// --- Initialization ---
document.addEventListener("DOMContentLoaded", () => {
  // 1. Load the User Header (Name, Bio, Posts Count, etc.)
  // We use try-catch to ensure if this fails, the buttons still work
  try {
    loadProfile(); 
  } catch (error) {
    console.error("Critical error loading profile header:", error);
  }

  // 2. Select Tabs
  const publicacoesTab = document.querySelector(".grid-btn");
  const produtosTab = document.querySelector(".product-btn");
  const eventsTab = document.querySelector(".event-btn");
  const postsGrid = document.querySelector(".user-posts-grid");

  // 3. Setup Grid Containers (Products & Events)
  if (postsGrid && postsGrid.parentNode) {
    // Check/Create Products Grid
    let productsContainer = document.querySelector(".user-products-grid");
    if (!productsContainer) {
      productsContainer = document.createElement("div");
      productsContainer.className = "user-products-grid";
      // Insert AFTER posts grid
      postsGrid.parentNode.insertBefore(productsContainer, postsGrid.nextSibling);
    }
    initializeGrid(productsState, productsContainer);

    // Create Events Grid (We reuse the 'user-products-grid' CSS class for styling)
    let eventsContainer = document.createElement("div");
    eventsContainer.className = "user-products-grid"; 
    // Insert AFTER products grid
    productsContainer.parentNode.insertBefore(eventsContainer, productsContainer.nextSibling);
    initializeGrid(eventsState, eventsContainer);
  }

  // 4. Attach Click Listeners
  if (publicacoesTab && produtosTab && eventsTab) {
    
    // TAB: Posts
    publicacoesTab.addEventListener("click", () => {
      setActiveTab([publicacoesTab], [produtosTab, eventsTab]);
      if (postsGrid) postsGrid.style.display = "grid";
      hideGrid(eventsState);
      hideGrid(productsState);
    });

    // TAB: Products
    produtosTab.addEventListener("click", () => {
      setActiveTab([produtosTab], [publicacoesTab, eventsTab]);
      if (postsGrid) postsGrid.style.display = "none";
      hideGrid(eventsState);
      activateGridTab(productsState);
    });

    // TAB: Events
    eventsTab.addEventListener("click", () => {
      console.log("Events tab clicked"); // Debug check
      setActiveTab([eventsTab], [publicacoesTab, produtosTab]);
      if (postsGrid) postsGrid.style.display = "none";
      hideGrid(productsState);
      activateGridTab(eventsState);
    });
  }

  // 5. Infinite Scroll Handler
  window.addEventListener("scroll", handleWindowScroll);
});

// --- Core Grid Logic (Generic) ---

function initializeGrid(state, gridElement) {
  state.container = gridElement;
  state.container.style.display = "none";
  // Ensure CSS grid layout is applied
  state.container.style.gridTemplateColumns = "repeat(2, minmax(0, 1fr))";
  state.container.style.gap = "10px";
  state.container.style.marginTop = "16px";
}

function activateGridTab(state) {
  state.active = true;
  if (!state.container) return;
  
  state.container.style.display = "grid";

  // Check if content is already loaded
  const hasContent = state.container.children.length > 0 && !state.container.querySelector(".empty-state");

  // Load content if first time or empty
  if (!state.initialized || (!hasContent && !state.loading)) {
    state.initialized = true;
    state.page = 0;
    state.finished = false;
    state.container.innerHTML = "";
    loadGridPage(state);
  }
}

function hideGrid(state) {
  state.active = false;
  if (state.container) state.container.style.display = "none";
}

function setActiveTab(activeTabs, inactiveTabs) {
  activeTabs.forEach(t => t.classList.add("active"));
  inactiveTabs.forEach(t => t.classList.remove("active"));
}

async function loadGridPage(state) {
  if (state.loading || state.finished) return;
  
  state.loading = true;
  setLoadingIndicator(state, true);

  try {
    // Call the specific API method for this state (Products or Events)
    const response = await state.apiMethod(state.page, GRID_PAGE_SIZE);
    const items = extractDataFromResponse(response);

    // Handle Empty State
    if (state.page === 0 && items.length === 0) {
      showGridEmptyState(state.emptyMessage, state);
      state.finished = true;
      return;
    }

    if (items.length > 0) {
      removeEmptyState(state);
      items.forEach((item) => {
        const card = createGenericCard(item);
        state.container.appendChild(card);
      });

      state.page += 1;

      // Stop loading if API isn't paginated OR we received fewer items than requested
      if (!state.isPaginated || items.length < GRID_PAGE_SIZE) {
        state.finished = true;
      }
    } else {
      state.finished = true;
    }
  } catch (error) {
    console.error(`Error loading ${state.type}:`, error);
    if (state.page === 0) {
      showGridEmptyState(`Erro ao carregar ${state.type}.`, state);
    }
  } finally {
    state.loading = false;
    setLoadingIndicator(state, false);
  }
}

// --- UI Card Creator ---

function createGenericCard(data = {}) {
  // We reuse the CSS class "user-product-card" so Events look exactly like Products
  const card = document.createElement("div");
  card.className = "user-product-card"; 
  
  // Inline styles backup
  card.style.background = "#f2ebfb";
  card.style.borderRadius = "12px";
  card.style.padding = "10px";
  card.style.display = "flex";
  card.style.flexDirection = "column";
  card.style.gap = "8px";

  // Image Area
  const imageWrapper = document.createElement("div");
  imageWrapper.style.width = "100%";
  imageWrapper.style.height = "120px";
  imageWrapper.style.borderRadius = "10px";
  imageWrapper.style.backgroundColor = "#d9d9d9";
  imageWrapper.style.backgroundSize = "cover";
  imageWrapper.style.backgroundPosition = "center";

  const imageUrl = getImageUrl(data);
  if (imageUrl) {
    setBackgroundImageWithBearer(imageWrapper, imageUrl, apiService.token);
  } else {
    // Fallback if no image
    imageWrapper.style.display = "flex";
    imageWrapper.style.alignItems = "center";
    imageWrapper.style.justifyContent = "center";
    imageWrapper.style.color = "#592e83";
    imageWrapper.style.fontWeight = "600";
    const title = getItemTitle(data);
    imageWrapper.textContent = title ? title.charAt(0).toUpperCase() : "?";
  }

  // Info Area
  const infoContainer = document.createElement("div");
  infoContainer.style.display = "flex";
  infoContainer.style.flexDirection = "column";
  infoContainer.style.gap = "4px";

  const titleElement = document.createElement("p");
  titleElement.textContent = getItemTitle(data);
  titleElement.style.fontWeight = "600";
  titleElement.style.color = "#592e83";

  // Description / Date
  const subTextElement = document.createElement("p");
  subTextElement.style.fontSize = "12px";
  subTextElement.style.color = "#525252";
  
  if(data.date) {
      // It is an event, show date
      const dateObj = new Date(data.date);
      subTextElement.textContent = dateObj.toLocaleDateString('pt-BR');
  } else {
      // It is a product, show description
      subTextElement.textContent = getItemDescription(data);
  }

  // Price (if applicable)
  const price = formatItemPrice(data);
  if (price) {
    const priceElement = document.createElement("p");
    priceElement.textContent = price;
    priceElement.style.fontWeight = "700";
    priceElement.style.color = "#171717";
    infoContainer.appendChild(priceElement);
  }

  infoContainer.insertBefore(titleElement, infoContainer.firstChild);
  infoContainer.appendChild(subTextElement);

  card.appendChild(imageWrapper);
  card.appendChild(infoContainer);

  return card;
}

// --- Data Normalization ---

function getItemTitle(item = {}) {
  return item.title || item.name || item.productName || item.eventName || "Item";
}

function getItemDescription(item = {}) {
  return item.description || item.details || item.summary || "";
}

function formatItemPrice(item = {}) {
  const rawPrice = item.price ?? item.value ?? item.cost ?? null;
  if (rawPrice === null || rawPrice === "") return "";
  const numericPrice = Number(rawPrice);
  return !Number.isNaN(numericPrice) ? numericPrice.toLocaleString("pt-BR", { style: "currency", currency: "BRL" }) : rawPrice;
}

function getImageUrl(item = {}) {
  const imagePath = item.imageUrl || item.imgLink || item.image || item.coverImage;
  if (!imagePath) return "";
  const trimmed = String(imagePath).trim();
  if (trimmed.startsWith("http")) return trimmed;
  const baseUrl = (typeof apiService !== "undefined" && apiService.baseURL) || "https://20252-inti-production.up.railway.app";
  return `${baseUrl}${trimmed.startsWith("/") ? trimmed : "/" + trimmed}`;
}

function extractDataFromResponse(response) {
  if (!response) return [];
  if (Array.isArray(response)) return response;
  return response.content || response.items || response.data || [];
}

// --- Loading & Empty State UI ---

function showGridEmptyState(message, state) {
  if (!state.container) return;
  state.container.innerHTML = "";
  const emptyState = document.createElement("p");
  emptyState.className = "empty-state";
  emptyState.textContent = message;
  emptyState.style.gridColumn = "1 / -1";
  emptyState.style.textAlign = "center";
  emptyState.style.padding = "20px";
  emptyState.style.color = "#737373";
  state.container.appendChild(emptyState);
}

function removeEmptyState(state) {
  if (!state.container) return;
  const emptyState = state.container.querySelector(".empty-state");
  if (emptyState) emptyState.remove();
}

function setLoadingIndicator(state, isLoading) {
  if (!state.container) return;
  let indicator = state.container.querySelector(".loading-indicator");
  if (isLoading) {
    if (!indicator) {
      indicator = document.createElement("div");
      indicator.className = "loading-indicator";
      indicator.textContent = "Carregando...";
      indicator.style.gridColumn = "1 / -1";
      indicator.style.textAlign = "center";
      indicator.style.padding = "16px";
      state.container.appendChild(indicator);
    }
  } else if (indicator) {
    indicator.remove();
  }
}

function handleWindowScroll() {
  const threshold = 200;
  const doc = document.documentElement;
  const totalHeight = Math.max(doc.offsetHeight, document.body.offsetHeight);
  const scrolledToBottom = window.innerHeight + window.scrollY >= totalHeight - threshold;

  if (scrolledToBottom) {
    if (productsState.active) loadGridPage(productsState);
    if (eventsState.active) loadGridPage(eventsState);
  }
}

// ==========================================
// PROFILE HEADER & POSTS LOGIC (Restored)
// ==========================================

async function loadProfile() {
  if (!checkAuth()) return;

  try {
    const initialProfile = await apiService.getMyProfile(0, POSTS_PAGE_SIZE);
    updateProfileUI(initialProfile);

    // Fetch and display posts
    const posts = await fetchAllProfilePosts(initialProfile.posts || []);
    loadUserPosts(posts);
  } catch (error) {
    console.error("Error loading profile:", error);
  }
}

async function fetchAllProfilePosts(initialPosts = []) {
  const aggregated = [...initialPosts];
  let page = 1;

  while (page < MAX_POST_PAGES) {
    try {
      const response = await apiService.getMyProfile(page, POSTS_PAGE_SIZE);
      const batch = response?.posts || [];
      if (!batch.length) break;
      aggregated.push(...batch);
      if (batch.length < POSTS_PAGE_SIZE) break;
      page += 1;
    } catch (error) {
      break;
    }
  }
  return aggregated.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
}

function updateProfileUI(data) {
  document.querySelector(".user-name").textContent = data.name || "Usuário";
  document.querySelector(".user-username").textContent = `@${data.username || "usuario"}`;
  document.querySelector(".posts-count").textContent = data.posts ? data.posts.length : 0;
  document.querySelector(".followers-count").textContent = data.followersCount || 0;
  document.querySelector(".following-count").textContent = data.followingCount || 0;

  const contactText = document.querySelector(".contact-text");
  if (contactText) {
    let info = [data.bio, data.publicEmail, data.phone].filter(Boolean);
    contactText.innerHTML = info.join("<br>");
  }

  const profilePhoto = document.querySelector(".img-user-icon");
  if (profilePhoto && data.profile_picture_url) {
    const url = getImageUrl({ imageUrl: data.profile_picture_url });
    setBackgroundImageWithBearer(profilePhoto, url, apiService.token);
  }
}

function loadUserPosts(posts) {
  const postsGrid = document.querySelector(".user-posts-grid");
  if (!postsGrid) return;
  postsGrid.innerHTML = "";

  if (posts.length === 0) {
    postsGrid.innerHTML = '<p class="no-posts">Nenhuma publicação ainda.</p>';
    return;
  }

  posts.forEach((post) => {
    const postItem = document.createElement("div");
    postItem.className = "user-post-item";
    
    if (post.imgLink) {
      const url = getImageUrl({ imageUrl: post.imgLink });
      setBackgroundImageWithBearer(postItem, url, apiService.token);
    } else {
      postItem.style.backgroundColor = "#e0e0e0";
    }

    postItem.addEventListener("click", () => {
      if (post.id) window.location.href = `./post-detail.html?id=${post.id}`;
    });

    postsGrid.appendChild(postItem);
  });
}

async function setBackgroundImageWithBearer(element, imageUrl, token) {
  try {
    const response = await fetch(imageUrl, {
      method: "GET",
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!response.ok) throw new Error("Image load fail");
    const blob = await response.blob();
    element.style.backgroundImage = `url("${URL.createObjectURL(blob)}")`;
    element.style.backgroundSize = "cover";
    element.style.backgroundPosition = "center";
  } catch (error) {
    // Fail silently or set a placeholder color
    element.style.backgroundColor = "#ccc";
  }
}

function checkAuth() {
  const token = localStorage.getItem("authToken");
  if (!token) {
    window.location.href = "../index.html";
    return false;
  }
  if (typeof apiService !== "undefined") {
    apiService.setAuthToken(token);
  }
  return true;
}