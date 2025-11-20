// public-profile.js

// TOKEN: mantenha seguro (não comitar em repositórios públicos)
const AUTH_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJub3ZvdCIsImV4cCI6MTc2NjA5ODg4MH0.BIBmMxWoq7em60fQWioz2qTin4g0TwZUaMHwioLe6JU';
window.AUTH_TOKEN = AUTH_TOKEN;

const urlParams = new URLSearchParams(window.location.search);
// const username = urlParams.get('user');

const username = 'morettipdr';

document.addEventListener('DOMContentLoaded', () => {
    if (!AUTH_TOKEN) {
        console.error('Token não encontrado.');
        return;
    }

    if (!username) {
        showError('Nenhum username informado!');
        return;
    }

    fetchProfileData(username);
});

async function fetchProfileData(username) {
    try {
        const token = AUTH_TOKEN;
        const size = 10;
        const page = 0;
        
        console.log('Buscando perfil de:', username);
        
        const response = await fetch(`https://20252-inti-production.up.railway.app/profile/${username}?size=${size}&page=${page}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Erro na requisição: ${response.status}`);
        }

        const profileData = await response.json();
        console.log('Dados recebidos:', profileData);
        
        populateProfileData(profileData);
        
    } catch (error) {
        console.error('Erro ao carregar perfil:', error);
        showError('Erro ao carregar perfil. Tente novamente.');
    }
}

function populateProfileData(data) {
    const userNameElement = document.querySelector('.container-header .user-name');
    const userUsernameElement = document.querySelector('.container-header .user-username');
    
    if (userNameElement) {
        userNameElement.textContent = data.name || 'Nome não informado';
    } else {
        console.error('Elemento .user-name não encontrado');
    }
    
    if (userUsernameElement) {
        userUsernameElement.textContent = data.username ? `@${data.username}` : '@usuário';
    } else {
        console.error('Elemento .user-username não encontrado');
    }
    
    // Foto de perfil
    const profileImg = document.querySelector('.img-user-icon');
    if (profileImg) {
        if (data.profile_picture_url) {
            const backendUrl = 'https://20252-inti-production.up.railway.app';
            const fullImageUrl = backendUrl + data.profile_picture_url;
            setBackgroundImageWithBearer(profileImg, fullImageUrl, AUTH_TOKEN);
        } else {
            // imagem padrão local
            profileImg.style.backgroundImage = `url("../assets/image-user-icon.png")`;
            profileImg.style.backgroundSize = 'cover';
            profileImg.style.backgroundPosition = 'center';
        }
    }
    
    // Bio/Informações de contato
    const contactInfo = document.querySelector('.contact-text');
    if (contactInfo && data.bio) {
        contactInfo.innerHTML = data.bio.replace(/\n/g, '<br>');
    }
    
    // Contadores (seguidores, seguindo, posts)
    updateProfileCounters(data);

    // Inicializar estado do botão follow
    initializeFollowButton(data);
    
    // Posts do usuário
    console.log('Chamando populateUserPosts com:', data.posts);
    populateUserPosts(data.posts || []);
}

function initializeFollowButton(data) {
    const followBtn = document.querySelector('.follow-icon');
    if (!followBtn) return;

    const img = followBtn.querySelector('img');
    if (!img) return;

    // Define as URLs dos endpoints
    followBtn.dataset.followUrl = `/profile/${data.username}/follow`;
    followBtn.dataset.unfollowUrl = `/profile/${data.username}/unfollow`;

    // Detecta se o usuário atual está seguindo este perfil
    const isFollowing = data.following ?? data.is_following ?? data.followingCountIsMine ?? false;

    // Atualiza o estado visual do botão
    updateFollowButtonState(followBtn, isFollowing);

    // Adiciona o event listener
    followBtn.addEventListener('click', handleFollowClick);
}

function updateFollowButtonState(followBtn, isFollowing) {
    const img = followBtn.querySelector('img');
    
    if (isFollowing) {
        // Estado: Seguindo
        followBtn.classList.add('active');
        img.src = img.src.replace('follow-icon', 'unfollow-icon');
        followBtn.dataset.following = 'true';
        followBtn.title = 'Deixar de seguir';
    } else {
        // Estado: Não seguindo
        followBtn.classList.remove('active');
        img.src = img.src.replace('unfollow-icon', 'follow-icon');
        followBtn.dataset.following = 'false';
        followBtn.title = 'Seguir';
    }
}

async function handleFollowClick(event) {
    event.preventDefault();
    
    const followBtn = event.currentTarget;
    const img = followBtn.querySelector('img');
    
    // Evitar múltiplos cliques simultâneos
    if (followBtn.classList.contains('loading')) return;
    
    followBtn.classList.add('loading');
    
    try {
        const isCurrentlyFollowing = followBtn.dataset.following === 'true';
        const endpoint = isCurrentlyFollowing ? followBtn.dataset.unfollowUrl : followBtn.dataset.followUrl;
        const method = isCurrentlyFollowing ? 'DELETE' : 'POST';
        
        const backendUrl = 'https://20252-inti-production.up.railway.app';
        const fullUrl = backendUrl + endpoint;
        
        console.log(`${method} para: ${fullUrl}`);
        
        const response = await fetch(fullUrl, {
            method: method,
            headers: {
                'Authorization': `Bearer ${AUTH_TOKEN}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Erro na requisição: ${response.status}`);
        }

        // Inverte o estado do botão
        const newFollowingState = !isCurrentlyFollowing;
        updateFollowButtonState(followBtn, newFollowingState);
        
        // Atualiza o contador de seguidores na UI
        await updateFollowersCounter(newFollowingState);
        
        console.log(`Success: ${isCurrentlyFollowing ? 'Unfollow' : 'Follow'} realizado com sucesso`);
        
    } catch (error) {
        console.error('Erro ao executar follow/unfollow:', error);
        showError('Erro ao executar ação. Tente novamente.');
    } finally {
        followBtn.classList.remove('loading');
    }
}

async function updateFollowersCounter(isFollowing) {
    try {
        // Busca os dados atualizados do perfil para obter o contador correto
        const response = await fetch(`https://20252-inti-production.up.railway.app/profile/${username}?size=10&page=0`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${AUTH_TOKEN}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const profileData = await response.json();
            
            // Atualiza o contador de seguidores
            const followersCountElement = document.querySelector('.profile-seguidores + .profile-number');
            if (followersCountElement && profileData.followersCount !== undefined) {
                followersCountElement.textContent = formatNumber(profileData.followersCount);
            }
        }
    } catch (error) {
        console.error('Erro ao atualizar contador de seguidores:', error);
        // Não mostra erro para o usuário, pois a ação principal foi bem sucedida
    }
}

function updateProfileCounters(data) {
    console.log('Atualizando contadores...');
    
    const profileItems = document.querySelectorAll('.profile-item');
    
    profileItems.forEach(item => {
        const label = item.querySelector('.profile-seguindo, .profile-post, .profile-seguidores');
        const numberElement = item.querySelector('.profile-number');
        
        if (!label || !numberElement) return;
        
        if (label.classList.contains('profile-seguidores')) {
            numberElement.textContent = formatNumber(data.followersCount);
        } else if (label.classList.contains('profile-seguindo')) {
            numberElement.textContent = formatNumber(data.followingCount);
        } else if (label.classList.contains('profile-post')) {
            numberElement.textContent = formatNumber(data.posts ? data.posts.length : 0);
        }
    });
}

function populateUserPosts(posts) {
    const postsGrid = document.querySelector('.user-posts-grid');
    
    console.log('populateUserPosts chamada');
    console.log('Posts recebidos:', posts);
    console.log('Grid element:', postsGrid);
    
    if (!postsGrid) {
        console.error('Elemento .user-posts-grid não encontrado');
        return;
    }
    
    // Limpar posts existentes
    postsGrid.innerHTML = '';
    
    if (!posts || posts.length === 0) {
        console.log('Nenhum post para exibir');
        postsGrid.innerHTML = '<p class="no-posts">Nenhum post ainda</p>';
        return;
    }
    
    // ORDENAR POSTS POR DATA (mais recentes primeiro)
    const sortedPosts = [...posts].sort((a, b) => {
        const dateA = new Date(a.createdAt);
        const dateB = new Date(b.createdAt);
        return dateB - dateA;
    });
    
    console.log(`Renderizando ${sortedPosts.length} posts...`);
    
    // Adicionar cada post
    sortedPosts.forEach((post, index) => {
        const postItem = createPostElement(post, index);
        postsGrid.appendChild(postItem);
    });
    
    console.log('Posts renderizados com sucesso!');
}

function createPostElement(post, index) {
    const postDiv = document.createElement('div');
    postDiv.className = `user-post-item rect-${(index % 5) + 1}`;
    postDiv.style.position = 'relative';
    
    console.log(`Criando post ${index}:`, post);
    
    // Se o post tiver imagem, carregar com Bearer token
    if (post.imgLink) {
        const backendUrl = 'https://20252-inti-production.up.railway.app';
        const fullImageUrl = backendUrl + post.imgLink;
        console.log('Carregando imagem:', fullImageUrl);
        setBackgroundImageWithBearer(postDiv, fullImageUrl, AUTH_TOKEN);
    } else {
        // Estilo padrão se não tiver imagem
        const randomColor = getRandomColor();
        console.log('Sem imagem, usando cor:', randomColor);
        postDiv.style.backgroundColor = randomColor;
        postDiv.style.display = 'flex';
        postDiv.style.alignItems = 'center';
        postDiv.style.justifyContent = 'center';
        postDiv.style.color = 'white';
        postDiv.style.fontWeight = 'bold';
    }
    
    return postDiv;
}

// NOVA FUNÇÃO: Renderizar produtos
function populateUserProducts(products) {
    const postsGrid = document.querySelector('.user-posts-grid');
    
    if (!postsGrid) {
        console.error('Elemento .user-posts-grid não encontrado');
        return;
    }
    
    postsGrid.innerHTML = '';
    
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
    const productDiv = document.createElement('div');
    productDiv.className = `user-post-item product-item rect-${(index % 5) + 1}`;
    productDiv.style.position = 'relative';
    
    if (product.imgLink || product.image_url || product.imageUrl) {
        const backendUrl = 'https://20252-inti-production.up.railway.app';
        const imageUrl = product.imgLink || product.image_url || product.imageUrl;
        const fullImageUrl = backendUrl + imageUrl;
        setBackgroundImageWithBearer(productDiv, fullImageUrl, AUTH_TOKEN);
    } else {
        productDiv.style.backgroundColor = getRandomColor();
        productDiv.style.display = 'flex';
        productDiv.style.alignItems = 'center';
        productDiv.style.justifyContent = 'center';
        productDiv.style.color = 'white';
        productDiv.style.fontWeight = 'bold';
    }
    
    const overlay = document.createElement('div');
    overlay.className = 'post-overlay product-overlay';
    
    const productName = product.name || product.title || 'Produto';
    const productPrice = product.price ? `R$ ${parseFloat(product.price).toFixed(2)}` : '';
    
    overlay.innerHTML = `
        <div class="product-info">
            <p class="product-name">${productName}</p>
            ${productPrice ? `<p class="product-price">${productPrice}</p>` : ''}
        </div>
    `;
    
    productDiv.appendChild(overlay);
    
    return productDiv;
}

async function setBackgroundImageWithBearer(element, imageUrl, token) {
    try {
        const response = await fetch(imageUrl, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!response.ok) {
            throw new Error(`Erro ao carregar imagem: ${response.status}`);
        }
        
        const blob = await response.blob();
        const objectUrl = URL.createObjectURL(blob);
        
        element.style.backgroundImage = `url("${objectUrl}")`;
        element.style.backgroundSize = 'cover';
        element.style.backgroundPosition = 'center';
        
    } catch (error) {
        console.error('Erro ao carregar imagem:', error);
        element.style.backgroundColor = getRandomColor();
    }
}

function getRandomColor() {
    const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8'];
    return colors[Math.floor(Math.random() * colors.length)];
}

function formatNumber(num) {
    if (num === null || num === undefined || isNaN(num)) {
        return '0';
    }
    
    if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k';
    }
    return num.toString();
}

function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
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

// EXPORTAR FUNÇÕES GLOBALMENTE
window.populateUserPosts = populateUserPosts;
window.populateUserProducts = populateUserProducts;