const AUTH_TOKEN = (window.AUTH_TOKEN = AUTH_TOKEN);

document.addEventListener("DOMContentLoaded", function () {
  if (!AUTH_TOKEN) {
    console.error("Token não encontrado.");
    return;
  }

  fetchProfileData();
});

async function fetchProfileData() {
  try {
    const token = AUTH_TOKEN;
    const size = 10;
    const page = 0;

    console.log("Fazendo requisição para /me...");

    const response = await fetch(
      `https://20252-inti-production.up.railway.app/profile/me?size=${size}&page=${page}`,
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
  } else {
    console.error("Elemento .user-name não encontrado");
  }

  if (userUsernameElement) {
    userUsernameElement.textContent = data.username
      ? `@${data.username}`
      : "@usuário";
  } else {
    console.error("Elemento .user-username não encontrado");
  }

  const profileImg = document.querySelector(".img-user-icon");
  if (profileImg) {
    if (data.profile_picture_url) {
      const backendUrl = "https://20252-inti-production.up.railway.app";
      const fullImageUrl = backendUrl + data.profile_picture_url;
      setBackgroundImageWithBearer(profileImg, fullImageUrl, AUTH_TOKEN);
    } else {
      // imagem padrão local
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

  populateUserPosts(data.posts || []);
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

  if (post.imgLink) {
    const backendUrl = "https://20252-inti-production.up.railway.app";
    const fullImageUrl = backendUrl + post.imgLink;
    setBackgroundImageWithBearer(postDiv, fullImageUrl, AUTH_TOKEN);
  } else {
    postDiv.style.backgroundColor = getRandomColor();
    postDiv.style.display = "flex";
    postDiv.style.alignItems = "center";
    postDiv.style.justifyContent = "center";
    postDiv.style.color = "white";
    postDiv.style.fontWeight = "bold";
  }

  return postDiv;
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

const editIcon = document.querySelector(".edit-icon");
if (editIcon) {
  editIcon.addEventListener("click", function () {});
} else {
  console.warn("Elemento .edit-icon não encontrado");
}
