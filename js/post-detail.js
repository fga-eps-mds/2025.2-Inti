document.addEventListener("DOMContentLoaded", () => {
  // Check authentication first
  const isAuthenticated = localStorage.getItem("isAuthenticated");
  const token = localStorage.getItem("authToken");

  if (isAuthenticated !== "true" || !token) {
    // Redirect to login
    window.location.href = "../index.html";
    return;
  }

  // Set token in apiService
  if (apiService) {
    apiService.setAuthToken(token);
  }

  const urlParams = new URLSearchParams(window.location.search);
  const postId = urlParams.get("id");

  if (!postId) {
    // Se o ID nao for fornecido
    console.warn('No Post ID provided');
    // return;
    // Remova isso em PROD
    // loadPostDetails('test-id');
  }

  if (postId) {
    loadPostDetails(postId);
  }

  // Add event listener for profile click
  const textInfo = document.querySelector(".text-info");
  if (textInfo) {
    textInfo.addEventListener("click", handleProfileClick);
    textInfo.style.cursor = "pointer";
  }

  // Add event listener for like button
  const likeBtn = document.querySelector(".like button");
  if (likeBtn) {
    likeBtn.addEventListener("click", handleLikeClick);
  }

  // Add event listener for back button
  const backBtn = document.querySelector(".voltar");
  if (backBtn) {
    backBtn.parentElement.addEventListener("click", () => {
      window.history.back();
    });
  }
});

async function loadPostDetails(postId) {
  try {
    // Get token from apiService
    const token = apiService.token || localStorage.getItem("authToken");

    if (!token) {
      console.error("No authentication token found");
      window.location.href = "../index.html";
      return;
    }

    console.log("Loading post with ID:", postId);
    console.log("Using token:", token.substring(0, 20) + "...");

    const response = await fetch(`${apiService.baseURL}/post/${postId}`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });

    console.log("Response status:", response.status);

    if (!response.ok) {
      if (response.status === 403) {
        throw new Error("Você não tem permissão para acessar este post.");
      } else if (response.status === 404) {
        throw new Error("Post não encontrado.");
      } else if (response.status === 401) {
        // Token expirou
        localStorage.removeItem("isAuthenticated");
        localStorage.removeItem("authToken");
        localStorage.removeItem("userData");
        window.location.href = "../index.html";
        return;
      }
      throw new Error(`HTTP Error: ${response.status}`);
    }

    const post = await response.json();
    console.log("Post loaded successfully:", post);
    renderPost(post);
    // Store postId and username for like functionality
    document.querySelector(".post-detail").dataset.postId = postId;
    document.querySelector(".post-detail").dataset.username = post.author.username;
  } catch (error) {
    console.error("Error loading post:", error);
    const postDetail = document.querySelector(".post-detail");
    if (postDetail) {
      postDetail.innerHTML = `<div style="padding: 20px; text-align: center; color: #d32f2f;">
        <p><strong>Erro ao carregar o post</strong></p>
        <p>${error.message}</p>
        <button onclick="window.history.back()" style="margin-top: 10px; padding: 10px 20px; background-color: #592e83; color: white; border: none; border-radius: 8px; cursor: pointer;">Voltar</button>
      </div>`;
    }
  }
}

function renderPost(post) {
  const authorImg = document.querySelector(".info-user img");
  const textInfoParagraphs = document.querySelectorAll(".text-info p");
  const authorName = textInfoParagraphs[0];
  const authorUsername = textInfoParagraphs[1];
  const postImage = document.querySelector(".post-img");
  const postDesc = document.querySelector(".description");
  const likeCount = document.querySelector(".like-count");
  const likeBtn = document.querySelector(".like button img");

    // Usar imagem padrão se não houver profilePictureUrl
    if (post.author && post.author.profilePictureUrl) {
      const fullProfileImageUrl = buildMediaUrl(post.author.profilePictureUrl);
      if (fullProfileImageUrl) {
        setBackgroundImageWithBearer(authorImg, fullProfileImageUrl, apiService.token);
      } else {
        authorImg.src = "../assets/profilePic.svg";
      }
  } else {
    authorImg.src = "../assets/profilePic.svg"; // Imagem padrão
    authorImg.style.display = "block";
  }

  if (authorName)
    authorName.textContent = (post.author && (post.author.name || post.author.username)) || "Usuário";
  if (authorUsername) 
    authorUsername.textContent = `@${(post.author && post.author.username) || "usuario"}`;

  // Atualizar conteudo do Post
    if (post.imageUrl) {
      const fullImageUrl = buildMediaUrl(post.imageUrl);
      if (fullImageUrl) {
        postImage.src = fullImageUrl;
        postImage.style.display = "block";
      } else {
        postImage.style.display = "none";
      }
  } else {
    postImage.style.display = "none";
  }

  if (postDesc) postDesc.textContent = post.description || "";
  if (likeCount) likeCount.textContent = `${post.likesCount || 0} `;

  // Update like button state
  if (post.liked) {
    document.querySelector(".like button").classList.add("liked");
    if (likeBtn) likeBtn.src = "../assets/img_Like1.svg";
  } else {
    document.querySelector(".like button").classList.remove("liked");
    if (likeBtn) likeBtn.src = "../assets/img_Like1.svg";
  }
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
    element.src = "../assets/profilePic.svg";
  }
}

function buildMediaUrl(path) {
  if (!path || typeof path !== "string") {
    return "";
  }

  const trimmedPath = path.trim();
  if (!trimmedPath) {
    return "";
  }

  if (trimmedPath.startsWith("http")) {
    return trimmedPath;
  }

  const baseUrl = apiService?.baseURL || "https://20252-inti-production.up.railway.app";
  const normalizedPath = trimmedPath.startsWith("/") ? trimmedPath : `/${trimmedPath}`;
  return `${baseUrl}${normalizedPath}`;
}

async function handleLikeClick(event) {
  event.preventDefault();
  event.stopPropagation();

  const postId = document.querySelector(".post-detail").dataset.postId;
  if (!postId) return;

  try {
    const likeBtn = event.currentTarget;
    const isLiked = likeBtn.classList.contains("liked");

    if (isLiked) {
      await apiService.unlikePost(postId);
      likeBtn.classList.remove("liked");
      likeBtn.querySelector("img").src = "../assets/img_Like1.svg";
    } else {
      await apiService.likePost(postId);
      likeBtn.classList.add("liked");
      likeBtn.querySelector("img").src = "../assets/img_Like1.svg";
    }

    // Recarregar contagem de likes
    const likeCount = document.querySelector(".like-count");
    const response = await fetch(
      `${apiService.baseURL}/post/${postId}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${apiService.token}`,
          "Content-Type": "application/json",
        },
      }
    );
    const post = await response.json();
    likeCount.textContent = `${post.likesCount || 0} likes`;
  } catch (error) {
    console.error("Error toggling like:", error);
    if (typeof toast !== "undefined") {
      toast.error("Erro ao curtir post");
    }
  }
}

function handleProfileClick(event) {
  const postDetail = document.querySelector(".post-detail");
  const username = postDetail.dataset.username;
  
  if (username) {
    window.location.href = `public-profile.html?username=${username}`;
  }
}
