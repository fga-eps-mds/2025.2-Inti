/*
 * Página de detalhes de produto
 * Requisita https://20252-inti-production.up.railway.app/products/{id}
 * e preenche os blocos de vendedor e destaque do produto.
 */

(function () {
    const DEFAULT_BASE_URL = "https://20252-inti-production.up.railway.app";

    document.addEventListener("DOMContentLoaded", () => {
        setupBackButton();
        const productId = getProductIdFromQuery();

        if (!productId) {
            showErrorState("Produto não informado.");
            return;
        }

        hydrateProduct(productId);
    });

    function setupBackButton() {
        const backBtn = document.getElementById("backBtn");
        if (!backBtn) return;

        backBtn.addEventListener("click", () => {
            if (window.history.length > 1) {
                window.history.back();
            } else {
                window.location.href = "../index.html";
            }
        });
    }

    function getProductIdFromQuery() {
        const params = new URLSearchParams(window.location.search);
        return params.get("id") || params.get("productId");
    }

    async function hydrateProduct(productId) {
        setLoadingState(true);

        try {
            const product = await apiRequest(`/products/${productId}`);
            if (!product || !product.id) {
                throw new Error("Produto não encontrado.");
            }

            populateProductSection(product);

            if (product.profileId) {
                const profile = await fetchProfile(product.profileId);
                populateSellerSection(profile, product);
            } else {
                populateSellerSection(null, product);
            }
        } catch (error) {
            console.error("Erro ao carregar produto:", error);
            showErrorState("Não foi possível carregar o produto.");
            if (window.toast) {
                toast.error("Erro ao carregar produto.");
            }
        } finally {
            setLoadingState(false);
        }
    }

    function setLoadingState(isLoading) {
        const productTitle = document.querySelector(".product-title");
        const descriptionCard = document.querySelector(".description-card");
        const price = document.querySelector(".product-price");

        if (isLoading) {
            if (productTitle) productTitle.textContent = "Carregando...";
            if (descriptionCard) descriptionCard.textContent = "";
            if (price) price.textContent = "";
        }
    }

    function showErrorState(message) {
        const productTitle = document.querySelector(".product-title");
        const descriptionCard = document.querySelector(".description-card");

        if (productTitle) productTitle.textContent = message;
        if (descriptionCard) descriptionCard.textContent = "";
    }

    async function fetchProfile(identifier) {
        try {
            return await apiRequest(`/profile/${identifier}`);
        } catch (error) {
            console.warn("Não foi possível carregar o perfil do vendedor:", error);
            return null;
        }
    }

    function populateProductSection(product) {
        const productTitle = document.querySelector(".product-title");
        const productPrice = document.querySelector(".product-price");
        const descriptionCard = document.querySelector(".description-card");
        const productImage = document.querySelector(".product-image");

        if (productTitle) {
            productTitle.textContent = product.title || product.description || "Produto";
        }

        if (productPrice) {
            const formattedPrice = formatCurrency(product.price);
            productPrice.textContent = formattedPrice || "";
        }

        if (descriptionCard) {
            descriptionCard.textContent = product.description || "";
        }

        if (productImage) {
            const imageUrl = resolveAssetUrl(product.imgLink || product.imageUrl);
            if (imageUrl) {
                const token = getAuthToken();
                setBackgroundImageWithBearer(productImage, imageUrl, token);
            }
        }
    }

    function populateSellerSection(profile, product) {
        const sellerName = document.querySelector(".seller-name");
        const sellerHandle = document.querySelector(".seller-handle");
        const sellerPhone = document.querySelector(".seller-phone");
        const sellerAvatar = document.querySelector(".seller-avatar");

        const fallbackName =
            profile?.name ||
            product.profileName ||
            product.ownerName ||
            product.sellerName ||
            "Vendedor";

        const fallbackUsername = profile?.username || product.profileUsername || product.sellerUsername || "";
        const fallbackHandle = formatHandle(fallbackUsername);
        const fallbackPhone = profile?.phone || profile?.contact || product.phone || product.contact || "";

        if (sellerName) {
            sellerName.textContent = fallbackName;
        }

        if (sellerHandle) {
            sellerHandle.textContent = fallbackHandle;
        }

        if (sellerPhone) {
            sellerPhone.textContent = fallbackPhone;
        }

        if (sellerAvatar) {
            const profileImage =
                profile?.profile_picture_url ||
                profile?.profilePictureUrl ||
                product.profilePictureUrl ||
                product.profile_picture_url;

            if (profileImage) {
                const token = getAuthToken();
                const normalizedPath = ensureImagesPrefix(profileImage);
                const imageUrl = resolveAssetUrl(normalizedPath);
                setBackgroundImageWithBearer(sellerAvatar, imageUrl, token);
            } else {
                sellerAvatar.style.backgroundColor = "var(--primary, #592e83)";
            }
        }
    }

    async function setBackgroundImageWithBearer(element, imageUrl, token) {
        try {
            const headers = token ? { Authorization: `Bearer ${token}` } : {};
            const response = await fetch(imageUrl, { headers });

            if (!response.ok) {
                throw new Error(`Erro ao carregar imagem (${response.status})`);
            }

            const blob = await response.blob();
            const objectUrl = URL.createObjectURL(blob);
            element.style.backgroundImage = `url("${objectUrl}")`;
            element.style.backgroundSize = "cover";
            element.style.backgroundPosition = "center";
        } catch (error) {
            console.error("Falha ao definir imagem de fundo:", error);
            element.style.backgroundColor = "#d8c4ec";
        }
    }

    function formatCurrency(value) {
        if (value === null || value === undefined || value === "") {
            return "";
        }

        const numeric = Number(value);
        if (!Number.isNaN(numeric)) {
            return numeric.toLocaleString("pt-BR", {
                style: "currency",
                currency: "BRL",
            });
        }

        const stringValue = String(value).trim();
        if (!stringValue) {
            return "";
        }

        if (stringValue.startsWith("R$")) {
            return stringValue;
        }

        return `R$ ${stringValue}`;
    }

    function resolveAssetUrl(path) {
        if (!path) return "";
        if (path.startsWith("http")) return path;

        const base = (typeof API_CONFIG !== "undefined" && API_CONFIG.baseURL) || DEFAULT_BASE_URL;
        const normalizedPath = path.startsWith("/") ? path : `/${path}`;
        return `${base}${normalizedPath}`;
    }

    function ensureImagesPrefix(path) {
        if (!path) return "";
        if (path.startsWith("http")) return path;
        if (path.startsWith("/")) return path;
        return `/images/${path}`;
    }

    function formatHandle(username) {
        if (!username) return "";
        const sanitized = username.startsWith("@") ? username.slice(1) : username;
        return `@${sanitized}`;
    }

    function getAuthToken() {
        if (typeof apiService !== "undefined" && apiService.token) {
            return apiService.token;
        }
        return localStorage.getItem("authToken");
    }

    async function apiRequest(endpoint, options = {}) {
        if (typeof apiService !== "undefined" && typeof apiService.request === "function") {
            return apiService.request(endpoint, options);
        }

        const token = getAuthToken();
        const base = (typeof API_CONFIG !== "undefined" && API_CONFIG.baseURL) || DEFAULT_BASE_URL;
        const url = `${base}${endpoint}`;

        const headers = {
            Accept: "application/json",
            ...(options.headers || {}),
        };

        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

        const response = await fetch(url, {
            method: options.method || "GET",
            headers,
            body: options.body,
        });

        if (!response.ok) {
            const errorText = await response.text().catch(() => "");
            let message = errorText;
            try {
                const json = errorText ? JSON.parse(errorText) : null;
                message = json?.message || message;
            } catch (error) {
                // body não era JSON
            }
            throw new Error(message || `HTTP ${response.status}`);
        }

        const text = await response.text().catch(() => "");
        if (!text) {
            return null;
        }

        try {
            return JSON.parse(text);
        } catch (error) {
            return text;
        }
    }
})();