
const BASE_API = 'http://localhost:8080';
const BEARER_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJuYXRhbjg2NDMiLCJleHAiOjE3NjYzMzE1NzJ9.5RCNVk9mQBj2buXY-uMDXKQ34_nZ4loD3oE6flLSPos';

const toggle = document.getElementById("dropdownToggle");
const menu = document.getElementById("dropdownMenu");

toggle.addEventListener("click", () => {
  menu.style.display = menu.style.display === "flex" ? "none" : "flex";
});

document.addEventListener("click", (e) => {
  if (!toggle.contains(e.target) && !menu.contains(e.target)) {
    menu.style.display = "none";
  }
});

async function carregarFeed() {
  const grid = document.querySelector('.events-grid');
  grid.innerHTML = "";

  try {
    const response = await fetch(`${BASE_API}/feed`, {
      headers: {
        'Authorization': 'Bearer ' + BEARER_TOKEN,
        'Accept': 'application/json'
      }
    });
    if (!response.ok) {
      grid.innerHTML = "<p>Não foi possível carregar o feed.</p>";
      return;
    }
    const items = await response.json();

    items.forEach(item => {
      const post = document.createElement('div');
      post.className = 'post';
      const profilePic = item.profilePicture && item.profilePicture.trim() !== "" ? item.profilePicture : "../assets/profilePic.svg";
      const username = item.username || 'Usuário';

      post.innerHTML = `
        <div class="profile">
          <img src="${profilePic}" alt="Foto de perfil">
          <p>${username}</p>
        </div>
        <img class="image-post" src="${item.imageUrl || '../assets/default.jpg'}" alt="">
        <div class="post-info">
          <p class="description">${item.description}</p>
          <div class="like">
            <button class="like-button">
              <img src="../assets/img_Like1.svg" alt="">
            </button>
            <p>${item.likes}</p>
          </div>
        </div>
      `;
      grid.appendChild(post);
    });
  } catch (err) {
    grid.innerHTML = "<p>Erro ao carregar o feed.</p>";
  }
}

document.addEventListener("DOMContentLoaded", carregarFeed);
