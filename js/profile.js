// Check authentication
function checkAuth() {
  const isAuthenticated = localStorage.getItem("isAuthenticated");
  const token = localStorage.getItem("authToken");

  if (isAuthenticated !== "true" || !token) {
    window.location.href = "../index.html";
    return false;
  }

  // Set token in apiService
  if (apiService) {
    apiService.setAuthToken(token);
  }
  return true;
}

// Global variable to store profile data
let currentProfileData = null;

const POSTS_PAGE_SIZE = 12;
const MAX_POST_PAGES = 12; // safety guard to avoid infinite loops
const PRODUCTS_PAGE_SIZE = 6;

const productsState = {
  container: null,
  page: 0,
  loading: false,
  finished: false,
  initialized: false,
  active: false,
};

// Load profile data
async function loadProfile() {
  if (!checkAuth()) return;

  try {
    const initialProfile = await apiService.getMyProfile(0, POSTS_PAGE_SIZE);
    currentProfileData = initialProfile;
    updateProfileUI(initialProfile);

    const posts = await fetchAllProfilePosts(initialProfile.posts || []);
    loadUserPosts(posts);
  } catch (error) {
    handleProfileError(error);
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

      if (batch.length < POSTS_PAGE_SIZE) {
        break;
      }
      page += 1;
    } catch (error) {
      console.warn("Erro ao buscar página de posts:", error);
      break;
    }
  }

  return sortPostsByDate(aggregated);
}

function sortPostsByDate(posts) {
  return [...posts].sort((a, b) => {
    const dateA = a?.createdAt ? new Date(a.createdAt).getTime() : 0;
    const dateB = b?.createdAt ? new Date(b.createdAt).getTime() : 0;
    return dateB - dateA;
  });
}

function handleProfileError(error) {
  console.error("Error loading profile:", error);
  if (error?.message && (error.message.includes("401") || error.message.includes("403"))) {
    localStorage.removeItem("isAuthenticated");
    localStorage.removeItem("authToken");
    localStorage.removeItem("userData");
    window.location.href = "../index.html";
  } else if (typeof toast !== "undefined") {
    toast.error("Erro ao carregar perfil.");
  }
}

function updateProfileUI(data) {
  // Update name and username
  const nameElement = document.querySelector(".user-name");
  const usernameElement = document.querySelector(".user-username");

  if (nameElement) nameElement.textContent = data.name || "Usuário";
  if (usernameElement)
    usernameElement.textContent = `@${data.username || "usuario"}`;

  // Update stats
  const postsCount = document.querySelector(".posts-count");
  const followersCount = document.querySelector(".followers-count");
  const followingCount = document.querySelector(".following-count");

  if (postsCount) postsCount.textContent = data.posts ? data.posts.length : 0;
  if (followersCount) followersCount.textContent = data.followersCount || 0;
  if (followingCount) followingCount.textContent = data.followingCount || 0;

  // Update bio and contact info if available
  const contactText = document.querySelector(".contact-text");
  if (contactText) {
    let info = [];
    if (data.bio) info.push(data.bio);
    if (data.publicEmail) info.push(data.publicEmail);
    if (data.phone) info.push(data.phone);

    contactText.innerHTML = info.join("<br>");
  }

  // Update profile picture
  const profilePhoto = document.querySelector(".img-user-icon");
  if (profilePhoto && data.profile_picture_url) {
    const backendUrl = "https://20252-inti-production.up.railway.app";
    const fullProfileImageUrl = data.profile_picture_url.startsWith("http")
      ? data.profile_picture_url
      : backendUrl + data.profile_picture_url;

    setBackgroundImageWithBearer(
      profilePhoto,
      fullProfileImageUrl,
      apiService.token
    );
  }
}

function loadUserPosts(posts) {
  const postsGrid = document.querySelector(".user-posts-grid");
  if (!postsGrid) return;

  postsGrid.innerHTML = "";

  if (posts.length === 0) {
    postsGrid.innerHTML =
      '<p style="grid-column: 1/-1; text-align: center; padding: 20px;">Nenhuma publicação ainda.</p>';
    return;
  }

  posts.forEach((post) => {
    const postItem = document.createElement("div");
    postItem.className = "user-post-item";

    if (post.imgLink) {
      const backendUrl = "https://20252-inti-production.up.railway.app";
      const fullImageUrl = post.imgLink.startsWith("http")
        ? post.imgLink
        : backendUrl + post.imgLink;
      setBackgroundImageWithBearer(postItem, fullImageUrl, apiService.token);
    } else {
      postItem.style.backgroundColor = getRandomColor();
    }

    // Add click event to open post details
    postItem.addEventListener("click", () => {
      // Redirect to post detail page
      if (post.id) {
        window.location.href = `./post-detail.html?id=${post.id}`;
      }
    });

    postsGrid.appendChild(postItem);
  });
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
  const colors = ["#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7"];
  return colors[Math.floor(Math.random() * colors.length)];
}

// Logout function
function logout() {
  localStorage.removeItem("isAuthenticated");
  localStorage.removeItem("authToken");
  localStorage.removeItem("userData");
  window.location.href = "../index.html";
}

// Initialize
document.addEventListener("DOMContentLoaded", () => {
  loadProfile();

  // Tab switching functionality
  const publicacoesTab = document.querySelector(".grid-btn"); // Using existing class from HTML
  const produtosTab = document.querySelector(".product-btn"); // Using existing class from HTML
  const postsGrid = document.querySelector(".user-posts-grid");

  // Create a container for products if it doesn't exist
  let productsGrid = document.querySelector(".user-products-grid");
  if (!productsGrid && postsGrid) {
    productsGrid = document.createElement("div");
    productsGrid.className = "user-products-grid";
    postsGrid.parentNode.insertBefore(productsGrid, postsGrid.nextSibling);
  }

  if (productsGrid) {
    initializeProductsGrid(productsGrid);
  }

  if (publicacoesTab && produtosTab) {
    publicacoesTab.addEventListener("click", () => {
      publicacoesTab.classList.add("active");
      produtosTab.classList.remove("active");
      if (postsGrid) postsGrid.style.display = "grid";
      hideProductsGrid();
    });

    produtosTab.addEventListener("click", () => {
      produtosTab.classList.add("active");
      publicacoesTab.classList.remove("active");
      if (postsGrid) postsGrid.style.display = "none";
      activateProductsTab();
    });
  }

  window.addEventListener("scroll", handleProductsScroll);
});

function initializeProductsGrid(grid) {
  productsState.container = grid;
  grid.style.display = "none";
  grid.style.gridTemplateColumns = "repeat(2, minmax(0, 1fr))";
  grid.style.gap = "10px";
  grid.style.marginTop = "16px";
}

function activateProductsTab() {
  productsState.active = true;
  if (!productsState.container) return;
  productsState.container.style.display = "grid";

  const hasRenderedProducts = Boolean(
    productsState.container.querySelector(".user-product-card")
  );

  if (
    !productsState.initialized ||
    (!hasRenderedProducts && !productsState.loading)
  ) {
    productsState.initialized = true;
    productsState.page = 0;
    productsState.finished = false;
    productsState.container.innerHTML = "";
    loadNextProductsPage();
  }
}

function hideProductsGrid() {
  productsState.active = false;
  if (productsState.container) {
    productsState.container.style.display = "none";
  }
}

async function loadNextProductsPage() {
  if (productsState.loading || productsState.finished) return;
  if (!productsState.container) return;

  productsState.loading = true;
  setProductsLoadingIndicator(true);

  try {
    const response = await apiService.getMyProducts(
      productsState.page,
      PRODUCTS_PAGE_SIZE
    );

    const products = extractProductsFromResponse(response);

    if (productsState.page === 0 && products.length === 0) {
      showProductsEmptyState("Nenhum produto cadastrado.");
      productsState.finished = true;
      return;
    }

    if (products.length > 0) {
      appendProducts(products);
      productsState.page += 1;

      if (products.length < PRODUCTS_PAGE_SIZE) {
        productsState.finished = true;
      }
    } else {
      productsState.finished = true;
    }
  } catch (error) {
    console.error("Erro ao carregar produtos:", error);
    if (typeof toast !== "undefined") {
      toast.error("Erro ao carregar produtos.");
    }
    if (productsState.page === 0) {
      showProductsEmptyState("Erro ao carregar produtos.");
    }
  } finally {
    productsState.loading = false;
    setProductsLoadingIndicator(false);
  }
}

function appendProducts(products = []) {
  if (!productsState.container) return;

  removeProductsEmptyState();

  products.forEach((product) => {
    const card = createProductCard(product);
    productsState.container.appendChild(card);
  });
}

function createProductCard(product = {}) {
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

  const imageUrl = getProductImageUrl(product);
  if (imageUrl) {
    setBackgroundImageWithBearer(imageWrapper, imageUrl, apiService.token);
  } else {
    imageWrapper.style.display = "flex";
    imageWrapper.style.alignItems = "center";
    imageWrapper.style.justifyContent = "center";
    imageWrapper.style.color = "#592e83";
    imageWrapper.style.fontWeight = "600";
    const fallbackLetter = getProductTitle(product).charAt(0).toUpperCase();
    imageWrapper.textContent = fallbackLetter || "P";
  }

  const infoContainer = document.createElement("div");
  infoContainer.className = "product-card-body";
  infoContainer.style.display = "flex";
  infoContainer.style.flexDirection = "column";
  infoContainer.style.gap = "4px";

  const titleElement = document.createElement("p");
  titleElement.className = "product-card-title";
  titleElement.textContent = getProductTitle(product);
  titleElement.style.fontWeight = "600";
  titleElement.style.color = "#592e83";

  const description = getProductDescription(product);
  if (description) {
    const descriptionElement = document.createElement("p");
    descriptionElement.className = "product-card-description";
    descriptionElement.textContent = description;
    descriptionElement.style.fontSize = "12px";
    descriptionElement.style.color = "#525252";
    infoContainer.appendChild(descriptionElement);
  }

  const price = formatProductPrice(product);
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

  return card;
}

function getProductTitle(product = {}) {
  return (
    product.title ||
    product.name ||
    product.productName ||
    product.displayName ||
    "Produto"
  );
}

function getProductDescription(product = {}) {
  return (
    product.description ||
    product.details ||
    product.summary ||
    product.about ||
    ""
  );
}

function formatProductPrice(product = {}) {
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

function getProductImageUrl(product = {}) {
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

  const baseUrl =
    (typeof apiService !== "undefined" && apiService.baseURL) ||
    "https://20252-inti-production.up.railway.app";
  const normalizedPath = trimmed.startsWith("/") ? trimmed : `/${trimmed}`;
  return `${baseUrl}${normalizedPath}`;
}

function extractProductsFromResponse(response) {
  if (!response) return [];
  if (Array.isArray(response)) return response;
  if (Array.isArray(response.content)) return response.content;
  if (Array.isArray(response.items)) return response.items;
  if (Array.isArray(response.data)) return response.data;
  return [];
}

function showProductsEmptyState(message) {
  if (!productsState.container) return;
  productsState.container.innerHTML = "";

  const emptyState = document.createElement("p");
  emptyState.className = "products-empty-state";
  emptyState.textContent = message;
  emptyState.style.gridColumn = "1 / -1";
  emptyState.style.textAlign = "center";
  emptyState.style.padding = "20px";
  emptyState.style.color = "#737373";

  productsState.container.appendChild(emptyState);
}

function removeProductsEmptyState() {
  if (!productsState.container) return;
  const emptyState = productsState.container.querySelector(
    ".products-empty-state"
  );
  if (emptyState) {
    emptyState.remove();
  }
}

function setProductsLoadingIndicator(isLoading) {
  if (!productsState.container) return;
  let indicator = productsState.container.querySelector(".products-loading");

  if (isLoading) {
    if (!indicator) {
      indicator = document.createElement("div");
      indicator.className = "products-loading";
      indicator.style.gridColumn = "1 / -1";
      indicator.style.textAlign = "center";
      indicator.style.padding = "16px";
      indicator.style.color = "#592e83";
      productsState.container.appendChild(indicator);
    }
    indicator.textContent = "Carregando...";
  } else if (indicator) {
    indicator.remove();
  }
}

function handleProductsScroll() {
  if (!productsState.active) return;
  if (productsState.loading || productsState.finished) return;

  const threshold = 200;
  const doc = document.documentElement;
  const totalHeight = Math.max(doc.offsetHeight, document.body.offsetHeight);
  const scrolledToBottom =
    window.innerHeight + window.scrollY >= totalHeight - threshold;

  if (scrolledToBottom) {
    loadNextProductsPage();
  }
}

// Expose logout globally
window.logout = logout;
