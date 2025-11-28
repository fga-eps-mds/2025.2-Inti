document.addEventListener("DOMContentLoaded", () => {
  const urlParams = new URLSearchParams(window.location.search);
  const postId = urlParams.get("id");

  if (!postId) {
    // Se o ID nao for fornecido
    // console.warn('No Post ID provided');
    // return;
    // Remova isso em PROD
    // loadPostDetails('test-id');
  }

  if (postId) {
    loadPostDetails(postId);
  }
});

async function loadPostDetails(postId) {
  try {
    // Mock pro token de autenticação
    const token = localStorage.getItem("authToken") || "dummy-token";

    const response = await axios.get(`${CONFIG.API_URL}/post/${postId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const post = response.data;
    renderPost(post);
  } catch (error) {
    console.error("Error loading post:", error);
    if (error.response && error.response.status === 404) {
      alert("Post not found");
    } else {
      console.log("Error loading post details");
    }
  }
}

function renderPost(post) {
  const authorImg = document.getElementById("author-img");
  const defaultAvatar = document.getElementById("default-avatar");
  const authorName = document.getElementById("author-name");
  const authorUsername = document.getElementById("author-username");
  const postDate = document.getElementById("post-date");

  if (post.author.profilePictureUrl) {
    authorImg.src = post.author.profilePictureUrl;
    authorImg.style.display = "block";
    if (defaultAvatar) defaultAvatar.style.display = "none";
  } else {
    authorImg.style.display = "none";
    if (defaultAvatar) defaultAvatar.style.display = "block";
  }

  if (authorName)
    authorName.textContent = post.author.name || post.author.username;
  if (authorUsername) authorUsername.textContent = `@${post.author.username}`;
  if (postDate)
    postDate.textContent = new Date(post.createdAt).toLocaleDateString("pt-BR");

  // Atualizar conteudo do Post
  const postImage = document.getElementById("post-image");
  const postDesc = document.getElementById("post-description");
  const likesCount = document.getElementById("likes-count");

  if (post.imageUrl) {
    postImage.src = post.imageUrl;
    postImage.style.display = "block";
  } else {
    postImage.style.display = "none";
  }

  if (postDesc) postDesc.textContent = post.description;
  if (likesCount) likesCount.textContent = `${post.likesCount} likes`;
}
