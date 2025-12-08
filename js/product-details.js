// Minimal interactions for product details page
document.addEventListener("DOMContentLoaded", function () {
    const backBtn = document.getElementById("backBtn");
    if (backBtn) {
        backBtn.addEventListener("click", function () {
            if (window.history.length > 1) {
                window.history.back();
            } else {
                window.location.href = "../index.html";
            }
        });
    }

    // Placeholder: if you want to dynamically load product data later,
    // implement fetch using apiService from config.js like in home.js
    // Product details page: carrega dados do backend (produto + vendedor)
    // Busca por query params: ?id=<productId>&profileId=<profileId> (ou ?productId, ?sellerId, ?username)
    // Usa `apiService` (de ../js/config.js) quando disponível, fallback para fetch direto.

    document.addEventListener("DOMContentLoaded", async function () {
        const params = new URLSearchParams(window.location.search);
        const productId = params.get("id") || params.get("productId");
        const profileId = params.get("profileId") || params.get("sellerId") || params.get("profile");
        const username = params.get("username");

        const backBtn = document.getElementById("backBtn");
        if (backBtn) {
            backBtn.addEventListener("click", function () {
                if (window.history.length > 1) {
                    window.history.back();
                } else {
                    window.location.href = "../index.html";
                }
            });
        }

        if (!productId) {
            console.warn("Nenhum productId informado na URL. Ex: ?id=<productId>");
            return;
        }

        // Helper para carregar imagens protegidas com Bearer (reaproveitado de public-profile.js)
        async function setBackgroundImageWithBearer(element, imageUrl, token) {
            try {
                const headers = token ? { Authorization: `Bearer ${token}` } : {};
                const response = await fetch(imageUrl, { method: "GET", headers });
                if (!response.ok) throw new Error(`Erro ao carregar imagem: ${response.status}`);
                const blob = await response.blob();
                const objectUrl = URL.createObjectURL(blob);
                element.style.backgroundImage = `url("${objectUrl}")`;
                element.style.backgroundSize = "cover";
                element.style.backgroundPosition = "center";
            } catch (error) {
                console.error("Erro ao carregar imagem:", error);
                element.style.backgroundColor = "#DDA0DD"; // fallback color
            }
        }

        // Função genérica para fazer requisições usando apiService quando disponível
        async function apiRequest(endpoint, options = {}) {
            if (typeof apiService !== "undefined" && typeof apiService.request === "function") {
                return apiService.request(endpoint, options);
            } else {
                const token = localStorage.getItem("authToken");
                const baseURL = (typeof API_CONFIG !== "undefined" && API_CONFIG.baseURL) ? API_CONFIG.baseURL : "https://20252-inti-production.up.railway.app";
                const url = baseURL + endpoint;
                const headers = Object.assign({ "Content-Type": "application/json" }, token ? { Authorization: `Bearer ${token}` } : {});
                const config = Object.assign({ headers, method: "GET" }, options);
                const resp = await fetch(url, config);
                if (!resp.ok) {
                    const text = await resp.text().catch(() => "");
                    let json = null;
                    try { json = text ? JSON.parse(text) : null; } catch { }
                    throw new Error((json && json.message) || `HTTP ${resp.status}`);
                }
                const contentType = resp.headers.get("content-type") || "";
                if (contentType.includes("application/json")) return resp.json();
                return resp.text();
            }
        }

        // Tenta obter produto por endpoint direto (caso exista)
        async function fetchProductDirect(id) {
            try {
                // tenta rota comum /products/{id} — alguns backends expõem esse recurso
                return await apiRequest(`/products/${id}`);
            } catch (err) {
                console.debug("fetchProductDirect falhou:", err.message);
                return null;
            }
        }

        // Busca produtos do perfil e retorna o que tiver id igual a productId
        async function findProductInProfile(profileIdentifier, idToFind) {
            try {
                // profileIdentifier pode ser UUID ou username; backend ProfileController tem /profile/{profileId}/products
                // Tentamos com profileId direto:
                let page = 0;
                const size = 50;
                // Repetimos páginas enquanto houver (simplificado: uma página grande)
                const endpoint = `/profile/${profileIdentifier}/products?page=${page}&size=${size}`;
                const pageResp = await apiRequest(endpoint);
                // pageResp pode ser um objeto Page ou uma lista - tentamos extrair conteúdo
                let items = [];
                if (Array.isArray(pageResp)) items = pageResp;
                else if (pageResp && pageResp.content) items = pageResp.content;
                else if (pageResp && pageResp.items) items = pageResp.items;
                const found = items.find((p) => String(p.id) === String(idToFind));
                return found || null;
            } catch (err) {
                console.debug("findProductInProfile falhou:", err.message);
                return null;
            }
        }

        // Busca produto em /products (lista global) como fallback
        async function searchProductInProductsList(idToFind) {
            try {
                const page = 0;
                const size = 100;
                const resp = await apiRequest(`/products?page=${page}&size=${size}`);
                let items = [];
                if (Array.isArray(resp)) items = resp;
                else if (resp && resp.content) items = resp.content;
                else if (resp && resp.items) items = resp.items;
                return items.find((p) => String(p.id) === String(idToFind)) || null;
            } catch (err) {
                console.debug("searchProductInProductsList falhou:", err.message);
                return null;
            }
        }

        // Busca dados do perfil (por id ou username)
        async function fetchProfile(identifier) {
            try {
                // Se identifier for username, endpoint `/profile/${username}` também funciona (ver public-profile.js)
                return await apiRequest(`/profile/${identifier}`);
            } catch (err) {
                console.debug("fetchProfile falhou:", err.message);
                return null;
            }
        }

        // Preenchimento do DOM com os dados do produto e vendedor
        function populateUI(product, profile) {
            // Seller
            const sellerAvatar = document.querySelector(".seller-avatar");
            const sellerName = document.querySelector(".seller-name");
            const sellerHandle = document.querySelector(".seller-handle");
            const sellerPhone = document.querySelector(".seller-phone");

            if (profile) {
                if (sellerName) sellerName.textContent = profile.name || profile.displayName || "Nome não informado";
                if (sellerHandle) sellerHandle.textContent = profile.username ? `@${profile.username}` : (profile.handle || "");
                if (sellerPhone) {
                    // profile may contain phone or contact in different fields
                    const phone = profile.phone || profile.contact || profile.telefone || profile.telefone_celular;
                    sellerPhone.textContent = phone || sellerPhone.textContent || "";
                }
                if (sellerAvatar) {
                    if (profile.profile_picture_url || profile.profilePictureUrl) {
                        const imageUrl = profile.profile_picture_url || profile.profilePictureUrl;
                        const token = (typeof apiService !== "undefined" && apiService.token) ? apiService.token : localStorage.getItem("authToken");
                        const backendUrl = (typeof API_CONFIG !== "undefined" && API_CONFIG.baseURL) ? API_CONFIG.baseURL : "https://20252-inti-production.up.railway.app";
                        const fullUrl = imageUrl.startsWith("http") ? imageUrl : backendUrl + imageUrl;
                        setBackgroundImageWithBearer(sellerAvatar, fullUrl, token);
                    } else {
                        sellerAvatar.style.backgroundColor = "var(--primary)";
                    }
                }
            } else {
                if (sellerName) sellerName.textContent = product.sellerName || product.ownerName || sellerName.textContent;
                if (sellerHandle) sellerHandle.textContent = product.sellerUsername ? `@${product.sellerUsername}` : sellerHandle.textContent;
                if (sellerAvatar) sellerAvatar.style.backgroundColor = "var(--primary)";
            }

            // Product
            const productTitle = document.querySelector(".product-title");
            const productImage = document.querySelector(".product-image");
            const productPrice = document.querySelector(".product-price");
            const descriptionCard = document.querySelector(".description-card");

            if (productTitle) productTitle.textContent = product.title || product.name || product.productName || product.descriptionTitle || product.shortDescription || "Produto";
            if (productPrice) {
                // tentar diferentes campos de preço
                const price = product.price || product.value || product.preco || product.priceCents || product.amount;
                let priceText = "";
                if (typeof price === "number") {
                    // se estiver em centavos, heurística: se > 1000 e inteiro, assumir centavos? não forçar — mostrar como R$ X
                    priceText = `R$ ${price}`;
                } else if (typeof price === "string" && price.trim() !== "") {
                    priceText = `R$ ${price}`;
                } else {
                    priceText = product.priceText || "R$ 0";
                }
                productPrice.textContent = priceText;
            }
            if (descriptionCard) {
                descriptionCard.textContent = product.description || product.longDescription || product.details || descriptionCard.textContent;
            }

            if (productImage) {
                // imagem pode vir em campos imageUrl, imgLink, image_url
                const imageUrl = product.imgLink || product.image_url || product.imageUrl || product.img || product.picture;
                if (imageUrl) {
                    const token = (typeof apiService !== "undefined" && apiService.token) ? apiService.token : localStorage.getItem("authToken");
                    const backendUrl = (typeof API_CONFIG !== "undefined" && API_CONFIG.baseURL) ? API_CONFIG.baseURL : "https://20252-inti-production.up.railway.app";
                    const fullUrl = imageUrl.startsWith("http") ? imageUrl : backendUrl + imageUrl;
                    setBackgroundImageWithBearer(productImage, fullUrl, token);
                } else {
                    // mantém a imagem de placeholder do HTML
                }
            }
        }

        // Orquestração: tentar obter o produto pelas várias estratégias
        let product = null;
        let profile = null;

        // 1) tentar endpoint direto /products/{id}
        product = await fetchProductDirect(productId);

        // 2) se não achar e houver profileId, procurar na lista do profile
        if (!product && profileId) {
            product = await findProductInProfile(profileId, productId);
        }

        // 3) se não achar e houver username, tentar profile por username e procurar seus products
        if (!product && username) {
            // Busca profile por username para pegar id (se necessário) e products
            const profileData = await fetchProfile(username);
            if (profileData) {
                profile = profileData;
                // tentar buscar produtos via profile id ou username
                const candidate = await findProductInProfile(profile.id || username, productId);
                product = product || candidate;
            }
        }

        // 4) fallback: procurar na lista global de products
        if (!product) {
            product = await searchProductInProductsList(productId);
        }

        // 5) se ainda não achou, informar no console
        if (!product) {
            console.warn("Produto não encontrado pelo productId:", productId);
            return;
        }

        // Se ainda não possuímos profile, tentar obter a partir do produto (se produto contiver owner info)
        if (!profile) {
            // tentar campos comuns
            const ownerId = product.ownerId || product.profileId || product.sellerId;
            const ownerUsername = product.sellerUsername || product.username || product.ownerUsername;
            if (ownerId) {
                profile = await fetchProfile(ownerId);
            } else if (ownerUsername) {
                profile = await fetchProfile(ownerUsername);
            }
        }

        // Finalmente popula UI
        populateUI(product, profile);
    });
});