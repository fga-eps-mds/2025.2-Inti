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
    productsGrid.style.display = "none";
    productsGrid.innerHTML =
      '<p style="text-align: center; padding: 20px;">Nenhum produto/serviço cadastrado.</p>';
    postsGrid.parentNode.insertBefore(productsGrid, postsGrid.nextSibling);
  }

  if (publicacoesTab && produtosTab) {
    publicacoesTab.addEventListener("click", () => {
      publicacoesTab.classList.add("active");
      produtosTab.classList.remove("active");
      if (postsGrid) postsGrid.style.display = "grid";
      if (productsGrid) productsGrid.style.display = "none";
    });

    produtosTab.addEventListener("click", () => {
      produtosTab.classList.add("active");
      publicacoesTab.classList.remove("active");
      if (postsGrid) postsGrid.style.display = "none";
      if (productsGrid) productsGrid.style.display = "block"; // Or grid
    });
  }
});

// Expose logout globally
window.logout = logout;
