const AUTH_TOKEN = localStorage.getItem("authToken");

const urlParams = new URLSearchParams(window.location.search);
// Get username from URL path if possible, otherwise from query param, otherwise default
// The current logic seems to expect it from somewhere.
// The user said "public-profile.html" so it might be /pages/public-profile.html?username=...
// But the code uses `window.location.search` but doesn't extract `username` from it in the provided snippet?
// Ah, line 5: `const username = document.addEventListener...` assigns the return of addEventListener to username? That's wrong.
// I need to fix how username is retrieved.

// Let's fix the whole file structure.

document.addEventListener("DOMContentLoaded", () => {
  if (!AUTH_TOKEN) {
    console.error("Token não encontrado.");
    // Redirect to login if needed, or just show error
    window.location.href = "../index.html";
    return;
  }

  const urlParams = new URLSearchParams(window.location.search);
  const username = urlParams.get("username");

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
      `https://20252-inti-production.up.railway.app/organization/${username}?size=${size}&page=${page}`,
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
      const backendUrl = "https://20252-inti-production.up.railway.app";
      const fullImageUrl = data.profile_picture_url.startsWith("http")
        ? data.profile_picture_url
        : backendUrl + data.profile_picture_url;
      setBackgroundImageWithBearer(profileImg, fullImageUrl, AUTH_TOKEN);
    } else {
      profileImg.style.backgroundImage = `url("../assets/image-user-icon.png")`;
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

  console.log("Chamando populateUserPosts com:", data.posts);
  populateUserPosts(data.posts || []);

  // Setup tab switching
  const viewBtns = document.querySelectorAll(".view-btn");
  viewBtns.forEach((btn) => {
    btn.addEventListener("click", () => {
      // Remove active class from all
      viewBtns.forEach((b) => b.classList.remove("active"));
      // Add active to clicked
      btn.classList.add("active");

      const view = btn.dataset.view;
      if (view === "posts") {
        populateUserPosts(data.posts || []);
      } else if (view === "products") {
        // Assuming products might be in data.products or filtered from posts
        // For now, let's assume data.products exists or pass empty
        populateUserProducts(data.products || []);
      } else if (view === "events") {
        // Handle events if applicable
        populateUserEvents(data.events || []);
      }
    });
  });
}

function initializeFollowButton(data) {
  const followBtn = document.querySelector(".follow-icon");
  if (!followBtn) return;

  const img = followBtn.querySelector("img");
  if (!img) return;

  followBtn.dataset.followUrl = `/organization/${data.username}/follow`;
  followBtn.dataset.unfollowUrl = `/organization/${data.username}/unfollow`;

  const isFollowing =
    data.following ?? data.is_following ?? data.followingCountIsMine ?? false;

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
    img.src = "../assets/unfollow-icon.png"; // Ensure asset exists or use text
    // Fallback if image doesn't exist, maybe change style
    followBtn.dataset.following = "true";
    followBtn.title = "Deixar de seguir";
  } else {
    followBtn.classList.remove("active");
    img.src = "../assets/follow-icon.png";
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

    const backendUrl = "https://20252-inti-production.up.railway.app";
    const fullUrl = backendUrl + endpoint;

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
      numberElement.textContent = formatNumber(
        data.posts ? data.posts.length : 0
      );
    }
  });
}

function populateUserPosts(posts) {
  const postsGrid = document.querySelector(".user-posts-grid");

  if (!postsGrid) {
    console.error("Elemento .user-posts-grid não encontrado");
    return;
  }

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

function createPostElement(post, index) {
  const postDiv = document.createElement("div");
  postDiv.className = `user-post-item rect-${(index % 5) + 1}`;
  postDiv.style.position = "relative";
  postDiv.dataset.postId = post.id;

  if (post.imgLink) {
    const backendUrl = "https://20252-inti-production.up.railway.app";
    const fullImageUrl = post.imgLink.startsWith("http")
      ? post.imgLink
      : backendUrl + post.imgLink;
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

function populateUserEvents(events) {
  const postsGrid = document.querySelector(".user-posts-grid");

  if (!postsGrid) return;

  postsGrid.innerHTML = "";

  if (!products || products.length === 0) {
    postsGrid.innerHTML = '<p class="no-events">Nenhum evento ainda</p>';
    return;
  }

  const sortedEvents = [...events].sort((a, b) => {
    if (a.createdAt && b.createdAt) {
      const dateA = new Date(a.createdAt);
      const dateB = new Date(b.createdAt);
      return dateB - dateA;
    }
    return 0;
  });

  sortedEvents.forEach((event, index) => {
    const eventItem = createEventElement(event, index);
    postsGrid.appendChild(eventItem);
  });
}

function createEventElement(event, index) {
  const eventDiv = document.createElement("div");
  eventDiv.className = `user-post-item event-item rect-${(index % 5) + 1}`;
  eventDiv.style.position = "relative";

  if (event.imgLink || event.image_url || event.imageUrl) {
    const backendUrl = "https://20252-inti-production.up.railway.app";
    const imageUrl = event.imgLink || event.image_url || event.imageUrl;
    const fullImageUrl = imageUrl.startsWith("http")
      ? imageUrl
      : backendUrl + imageUrl;
    setBackgroundImageWithBearer(eventDiv, fullImageUrl, AUTH_TOKEN);
  } else {
    eventDiv.style.backgroundColor = getRandomColor();
    eventDiv.style.display = "flex";
    eventDiv.style.alignItems = "center";
    eventDiv.style.justifyContent = "center";
    eventDiv.style.color = "white";
    eventDiv.style.fontWeight = "bold";
  }

  return eventDiv;
}

function populateUserProducts(products) {
  const postsGrid = document.querySelector(".user-posts-grid");

  if (!postsGrid) return;

  postsGrid.innerHTML = "";

  if (!products || products.length === 0) {
    postsGrid.innerHTML = '<p class="no-posts">Nenhum produto ainda</p>';
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

  sortedProducts.forEach((product, index) => {
    const productItem = createProductElement(product, index);
    postsGrid.appendChild(productItem);
  });
}

function createProductElement(product, index) {
  const productDiv = document.createElement("div");
  productDiv.className = `user-post-item product-item rect-${(index % 5) + 1}`;
  productDiv.style.position = "relative";

  if (product.imgLink || product.image_url || product.imageUrl) {
    const backendUrl = "https://20252-inti-production.up.railway.app";
    const imageUrl = product.imgLink || product.image_url || product.imageUrl;
    const fullImageUrl = imageUrl.startsWith("http")
      ? imageUrl
      : backendUrl + imageUrl;
    setBackgroundImageWithBearer(productDiv, fullImageUrl, AUTH_TOKEN);
  } else {
    productDiv.style.backgroundColor = getRandomColor();
    productDiv.style.display = "flex";
    productDiv.style.alignItems = "center";
    productDiv.style.justifyContent = "center";
    productDiv.style.color = "white";
    productDiv.style.fontWeight = "bold";
  }

  return productDiv;
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
    const backendUrl = "https://20252-inti-production.up.railway.app";
    const response = await fetch(`${backendUrl}/post/${postId}`, {
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
        : backendUrl + post.author.profilePictureUrl;
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
        : backendUrl + post.imageUrl;
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
