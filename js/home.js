// Use the existing apiService from config.js

const toggle = document.getElementById("dropdownToggle");
const menu = document.getElementById("dropdownMenu");
const eventosBtn = document.getElementById("eventosBtn");

// Pagination variables
let currentPage = 0;
const pageSize = 5;
let isLoading = false;
let hasMorePosts = true;
let displayedPostIds = new Set(); // Track IDs of posts already displayed

// Check if user is authenticated
function checkAuth() {
  const isAuthenticated = localStorage.getItem("isAuthenticated");
  const token = localStorage.getItem("authToken");

  if (isAuthenticated !== "true" || !token) {
    // Clear any partial auth data
    localStorage.removeItem("isAuthenticated");
    localStorage.removeItem("authToken");
    localStorage.removeItem("userData");

    // Redirect to login page
    window.location.href = "../index.html";
    return false;
  }

  // Set token in apiService using the proper method
  if (apiService) {
    apiService.setAuthToken(token);
  }

  return true;
}

if (toggle) {
  toggle.addEventListener("click", () => {
    menu.style.display = menu.style.display === "flex" ? "none" : "flex";
  });
}

document.addEventListener("click", (e) => {
  if (
    toggle &&
    menu &&
    !toggle.contains(e.target) &&
    !menu.contains(e.target)
  ) {
    menu.style.display = "none";
  }
});

if (eventosBtn) {
  eventosBtn.addEventListener("click", () => {
    menu.style.display = "none";
    window.location.href = "eventList.html";
  });
}

// Modified to fetch posts more chronologically and prevent duplicates
async function carregarFeed(page = 0, append = false) {
  // Check authentication first
  if (!checkAuth()) {
    return;
  }

  const grid = document.querySelector(".events-grid");

  // If appending and already loading, skip
  if (append && isLoading) return;

  // If no more posts, skip
  if (!hasMorePosts && append) return;

  isLoading = true;

  // Show loading indicator when appending
  let loadingIndicator;
  if (append) {
    loadingIndicator = document.createElement("div");
    loadingIndicator.className = "loading-indicator";
    loadingIndicator.innerHTML = "<p>Carregando mais posts...</p>";
    grid.appendChild(loadingIndicator);
  } else {
    // Clear grid for initial load and reset displayed posts tracking
    if (!append) {
      grid.innerHTML = "";
      displayedPostIds.clear();
    }
  }

  try {
    // Try to get posts from the feed endpoint first
    const feedItems = await apiService.getFeed(page, pageSize);

    // Remove loading indicator
    if (loadingIndicator) {
      loadingIndicator.remove();
    }

    // Check if we have more posts
    hasMorePosts = feedItems.length === pageSize;
    currentPage = page;

    if (!append && feedItems.length === 0) {
      grid.innerHTML = "<p>Nenhum post encontrado.</p>";
      return;
    }

    // Filter out posts that are already displayed
    const uniqueFeedItems = feedItems.filter((item) => {
      if (displayedPostIds.has(item.id)) {
        return false; // Skip duplicates
      }
      displayedPostIds.add(item.id); // Mark as displayed
      return true;
    });

    // Process unique feed items
    uniqueFeedItems.forEach((item) => {
      const post = document.createElement("div");
      post.className = "post";
      post.dataset.postId = item.id; // Store post ID for like functionality
      const username = item.username || "Usuário";

      // CRITICAL FIX: Use the liked status from backend
      const isLiked = item.liked || false;

      post.innerHTML = `
        <div class="profile">
          <div class="profile-pic-placeholder"></div>
          <p>${username}</p>
        </div>
        <div class="image-post-placeholder" data-post-id="${item.id}"></div>
        <div class="post-info">
          <p class="description">${item.description}</p>
          <div class="like">
            <button class="like-button" data-post-id="${item.id}" data-liked="${isLiked}">
              <img src="../assets/img_Like1.svg" alt="">
            </button>
            <p class="likes-count" data-post-id="${item.id}">${item.likes}</p>
          </div>
        </div>
      `;

      // Load profile image with proper backend URL and authorization
      const profilePicPlaceholder = post.querySelector(
        ".profile-pic-placeholder"
      );
      if (item.imageProfileUrl && item.imageProfileUrl.trim() !== "") {
        const backendUrl = "https://20252-inti-production.up.railway.app";
        // Handle cases where URL might already contain /images/ or not
        let fullProfileImageUrl = item.imageProfileUrl.startsWith("http")
          ? item.imageProfileUrl
          : backendUrl + item.imageProfileUrl;
        setBackgroundImageWithBearer(
          profilePicPlaceholder,
          fullProfileImageUrl,
          apiService.token
        );
      } else {
        // Set a default background
        profilePicPlaceholder.style.backgroundColor = getRandomColor();
      }

      // Load post image with proper backend URL and authorization
      const imagePlaceholder = post.querySelector(".image-post-placeholder");
      if (item.imageUrl && item.imageUrl.trim() !== "") {
        const backendUrl = "https://20252-inti-production.up.railway.app";
        let fullImageUrl = item.imageUrl.startsWith("http")
          ? item.imageUrl
          : backendUrl + item.imageUrl;
        setBackgroundImageWithBearer(
          imagePlaceholder,
          fullImageUrl,
          apiService.token
        );
      } else {
        // Set a default background or placeholder
        imagePlaceholder.style.backgroundColor = getRandomColor();
        imagePlaceholder.style.display = "flex";
        imagePlaceholder.style.alignItems = "center";
        imagePlaceholder.style.justifyContent = "center";
        imagePlaceholder.style.color = "white";
        imagePlaceholder.style.fontWeight = "bold";
        imagePlaceholder.textContent = "No Image";
      }
      grid.appendChild(post);
    });

    // Add event listeners for like buttons (delegation is handled globally, but keeping this if needed for specific logic)
  } catch (err) {
    console.error("Error loading feed:", err);

    // Handle authentication errors specifically
    if (
      err.message &&
      (err.message.includes("401") || err.message.includes("403"))
    ) {
      localStorage.removeItem("isAuthenticated");
      localStorage.removeItem("authToken");
      localStorage.removeItem("userData");
      window.location.href = "../index.html";
      return;
    }

    if (loadingIndicator) loadingIndicator.remove();

    if (!append) {
      grid.innerHTML = "<p>Erro ao carregar o feed.</p>";
    } else if (typeof toast !== "undefined") {
      toast.error("Erro ao carregar mais posts.");
    }
  } finally {
    isLoading = false;
  }
}

// Add event listeners for post clicks to open modal
function addPostClickEventListeners() {
  const feedContainer = document.querySelector(".events-grid");
  if (feedContainer) {
    // Remove any existing event listener to prevent duplicates
    feedContainer.removeEventListener("click", handlePostClick);
    // Add event listener to the container
    feedContainer.addEventListener("click", handlePostClick);
  }
}

// Handle post click events
function handlePostClick(event) {
  if (event.target.closest(".like-button")) {
    return;
  }

  const postElement = event.target.closest(".post");
  if (!postElement) {
    return;
  }

  const imageElement = postElement.querySelector(".image-post-placeholder");
  const postId =
    postElement.dataset.postId ||
    (imageElement ? imageElement.dataset.postId : null);

  if (postId) {
    window.location.href = `post-detail.html?id=${postId}`;
  }
}

// Open post modal with details
async function openPostModal(postId) {
  console.log("Opening post modal for postId:", postId);

  if (!checkAuth()) return;

  const modal = document.getElementById("postModal");
  const modalImage = document.getElementById("modalPostImage");
  const modalProfilePic = document.getElementById("modalProfilePic");
  const modalUsername = document.getElementById("modalUsername");
  const modalDescription = document.getElementById("modalDescription");
  const modalDate = document.getElementById("modalDate");
  const modalLikes = document.getElementById("modalLikes");

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
    const post = await apiService.getPostDetail(postId);

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
      const backendUrl = "https://20252-inti-production.up.railway.app";
      const fullProfileImageUrl = backendUrl + post.author.profilePictureUrl;
      setBackgroundImageWithBearer(
        modalProfilePic,
        fullProfileImageUrl,
        apiService.token
      );
    } else {
      modalProfilePic.style.backgroundColor = getRandomColor();
    }

    // Load post image
    if (post.imageUrl && post.imageUrl.trim() !== "") {
      const backendUrl = "https://20252-inti-production.up.railway.app";
      const fullImageUrl = backendUrl + post.imageUrl;
      modalImage.src = fullImageUrl;
      modalImage.style.display = "block";
    } else {
      modalImage.style.display = "none";
    }
  } catch (error) {
    console.error("Error loading post details:", error);

    if (
      error.message &&
      (error.message.includes("401") || error.message.includes("403"))
    ) {
      localStorage.removeItem("isAuthenticated");
      localStorage.removeItem("authToken");
      localStorage.removeItem("userData");
      window.location.href = "../index.html";
      return;
    }

    modalUsername.textContent = "Erro";
    modalDescription.textContent =
      "Não foi possível carregar os detalhes do post.";
  }
}

// Close modal when clicking on close button or outside the modal
document.addEventListener("DOMContentLoaded", function () {
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

  // Initialize feed
  if (checkAuth()) {
    carregarFeed(0, false);
    addPostClickEventListeners();
  }
});

// Add event listeners for like buttons
document.addEventListener("click", function (e) {
  // Handle like button clicks
  if (e.target.closest(".like-button")) {
    const likeButton = e.target.closest(".like-button");
    const postId = likeButton.dataset.postId;
    const isLiked = likeButton.dataset.liked === "true";

    if (postId) {
      toggleLike(postId, isLiked, likeButton);
    }
  }
});

// Infinite scroll implementation
window.addEventListener("scroll", () => {
  if (
    window.innerHeight + window.scrollY >=
    document.body.offsetHeight - 1000
  ) {
    if (hasMorePosts && !isLoading) {
      carregarFeed(currentPage + 1, true);
    }
  }
});

async function toggleLike(postId, isLiked, likeButton) {
  if (!checkAuth()) return;

  try {
    if (isLiked) {
      await apiService.unlikePost(postId);
      updateLikeUI(postId, false);
    } else {
      await apiService.likePost(postId);
      updateLikeUI(postId, true);
    }
  } catch (error) {
    if (
      error.message &&
      (error.message.includes("401") || error.message.includes("403"))
    ) {
      localStorage.removeItem("isAuthenticated");
      localStorage.removeItem("authToken");
      localStorage.removeItem("userData");
      window.location.href = "../index.html";
      return;
    }

    if (
      error.message &&
      (error.message.includes("409") || error.message.includes("Conflict"))
    ) {
      // If conflict (already liked/unliked), just sync UI
      updateLikeUI(postId, !isLiked);
    } else {
      console.error("Error toggling like:", error);
      if (typeof toast !== "undefined") {
        toast.error("Erro ao curtir/descurtir o post.");
      } else {
        alert("Erro ao curtir/descurtir o post.");
      }
      // Revert UI
      likeButton.dataset.liked = isLiked.toString();
    }
  }
}

function updateLikeUI(postId, isLiked) {
  const likeButtons = document.querySelectorAll(
    `.like-button[data-post-id="${postId}"]`
  );
  const likeCounts = document.querySelectorAll(
    `.likes-count[data-post-id="${postId}"]`
  );

  likeButtons.forEach((button) => {
    button.dataset.liked = isLiked ? "true" : "false";
  });

  // Optimistically update count (optional, but good UX)
  likeCounts.forEach((count) => {
    let current = parseInt(count.textContent) || 0;
    if (isLiked) count.textContent = current + 1;
    else count.textContent = Math.max(0, current - 1);
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

// Expose functions globally if needed
window.toggleLike = toggleLike;
window.openPostModal = openPostModal;
