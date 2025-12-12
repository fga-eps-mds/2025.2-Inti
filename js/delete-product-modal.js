/**
 * Delete Product Modal Handler
 * Gerencia a abertura e fechamento do modal de confirmação de exclusão de produtos
 */

(function () {
  let currentProductId = null;

  document.addEventListener("DOMContentLoaded", () => {
    initDeleteProductModal();
  });

  /**
   * Inicializa os event listeners do modal de deletar produto
   */
  function initDeleteProductModal() {
    const modal = document.getElementById("deleteProductModal");
    const cancelBtn = document.getElementById("cancelDeleteBtn");
    const confirmBtn = document.getElementById("confirmDeleteBtn");

    // Fechar modal ao clicar em Cancelar
    if (cancelBtn) {
      cancelBtn.addEventListener("click", closeDeleteProductModal);
    }

    // Confirmar exclusão
    if (confirmBtn) {
      confirmBtn.addEventListener("click", handleConfirmDelete);
    }

    // Verificar propriedade do produto e mostrar/esconder botão
    checkProductOwnershipAndToggleButton();
  }

  /**
   * Abre o modal de confirmação de exclusão
   * @param {string} productId - ID do produto a ser deletado
   */
  function openDeleteProductModal(productId) {
    const modal = document.getElementById("deleteProductModal");
    currentProductId = productId;

    if (modal) {
      modal.showModal();
    }
  }

  /**
   * Fecha o modal de confirmação de exclusão
   */
  function closeDeleteProductModal() {
    const modal = document.getElementById("deleteProductModal");

    if (modal) {
      modal.close();
      currentProductId = null;
    }
  }

  /**
   * Manipula a confirmação de exclusão do produto
   */
  async function handleConfirmDelete() {
    if (!currentProductId) {
      console.error("Nenhum produto selecionado para deletar");
      return;
    }

    try {
      const token =
        localStorage.getItem("jwtToken") || localStorage.getItem("authToken");
      if (!token) {
        throw new Error("Usuário não autenticado.");
      }

      const baseUrl =
        typeof API_CONFIG !== "undefined" && API_CONFIG.baseURL
          ? API_CONFIG.baseURL
          : "https://20252-inti-production.up.railway.app";

      const response = await fetch(`${baseUrl}/products/${currentProductId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Erro ao deletar o produto");
      }

      // Fechar modal
      closeDeleteProductModal();

      // Mostrar mensagem de sucesso
      if (typeof showToast === "function") {
        showToast("Produto deletado com sucesso!", "success");
      } else if (window.toast && typeof window.toast.success === "function") {
        window.toast.success("Produto deletado com sucesso!");
      } else {
        alert("Produto deletado com sucesso!");
      }

      // Redirecionar após 1.5 segundos
      setTimeout(() => {
        window.location.href = "../pages/profile.html";
      }, 1500);
    } catch (error) {
      console.error("Erro ao deletar produto:", error);

      if (typeof showToast === "function") {
        showToast("Erro ao deletar o produto: " + error.message, "error");
      } else if (window.toast && typeof window.toast.error === "function") {
        window.toast.error("Erro ao deletar o produto: " + error.message);
      } else {
        alert("Erro ao deletar o produto: " + error.message);
      }
    }
  }

  /**
   * Verifica a propriedade do produto e mostra/esconde o botão de deletar
   */
  async function checkProductOwnershipAndToggleButton() {
    try {
      const token =
        localStorage.getItem("jwtToken") || localStorage.getItem("authToken");
      const deleteBtn = document.getElementById("deleteProductBtn");

      console.log("[DeleteModal] Iniciando verificação de propriedade");
      console.log("[DeleteModal] Token disponível:", !!token);
      console.log("[DeleteModal] Botão encontrado:", !!deleteBtn);

      // Extrair ID do produto da URL
      const params = new URLSearchParams(window.location.search);
      const productId = params.get("id") || params.get("productId");

      console.log("[DeleteModal] Product ID:", productId);

      if (!productId) {
        console.log("[DeleteModal] Sem product ID, desabilitando botão");
        if (deleteBtn) {
          deleteBtn.disabled = true;
          deleteBtn.style.opacity = "0.5";
          deleteBtn.style.cursor = "not-allowed";
        }
        return;
      }

      // Se não tem token, desabilita o botão
      if (!token) {
        console.log("[DeleteModal] Sem token, desabilitando botão");
        if (deleteBtn) {
          deleteBtn.disabled = true;
          deleteBtn.style.opacity = "0.5";
          deleteBtn.style.cursor = "not-allowed";
          deleteBtn.title = "Faça login para deletar";
        }
        return;
      }

      const baseUrl =
        typeof API_CONFIG !== "undefined" && API_CONFIG.baseURL
          ? API_CONFIG.baseURL
          : "https://20252-inti-production.up.railway.app";

      console.log("[DeleteModal] Buscando dados do produto...");

      // Buscar dados do produto
      const response = await fetch(`${baseUrl}/products/${productId}`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        console.log("[DeleteModal] Erro ao buscar produto:", response.status);
        if (deleteBtn) {
          deleteBtn.disabled = true;
          deleteBtn.style.opacity = "0.5";
          deleteBtn.style.cursor = "not-allowed";
        }
        return;
      }

      const product = await response.json();
      console.log("[DeleteModal] Produto obtido:", product);
      console.log(
        "[DeleteModal] Product Owner ID:",
        product.profileId || product.userId || product.createdBy
      );
      console.log(
        "[DeleteModal] Product Owner Username:",
        product.profileUsername || product.username || product.ownerUsername
      );

      // Buscar dados do usuário logado - tenta vários endpoints
      let currentUserId = null;
      let currentUsername = null;

      const storedUser = getStoredUserData();
      if (storedUser) {
        currentUserId =
          storedUser.id ||
          storedUser.profileId ||
          storedUser.userId ||
          storedUser.profile_id ||
          currentUserId;
        currentUsername =
          storedUser.username ||
          storedUser.userName ||
          storedUser.user ||
          currentUsername;
        console.log("[DeleteModal] Usuário local:", {
          storedId: currentUserId,
          storedUsername: currentUsername,
        });
      }

      // Tentar /auth/me
      let profileResponse = null;
      if (!currentUserId || !currentUsername) {
        console.log("[DeleteModal] Tentando buscar usuário via /auth/me...");
        profileResponse = await fetch(`${baseUrl}/auth/me`, {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }).catch((e) => {
          console.log("[DeleteModal] Erro na requisição /auth/me:", e);
          return null;
        });

        if (profileResponse && profileResponse.ok) {
          try {
            const currentUser = await profileResponse.json();
            currentUserId = currentUser.id || currentUser.profileId;
            currentUsername = currentUser.username;
            console.log(
              "[DeleteModal] Usuário encontrado via /auth/me, ID:",
              currentUserId,
              "Username:",
              currentUsername
            );
          } catch (e) {
            console.log("[DeleteModal] Erro ao parsear /auth/me:", e);
          }
        }
      }

      // Se não encontrou via /auth/me, tenta /profile
      if (!currentUserId || !currentUsername) {
        console.log("[DeleteModal] Tentando buscar usuário via /profile...");
        profileResponse = await fetch(`${baseUrl}/profile`, {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }).catch((e) => {
          console.log("[DeleteModal] Erro na requisição /profile:", e);
          return null;
        });

        if (profileResponse && profileResponse.ok) {
          try {
            const currentUser = await profileResponse.json();
            currentUserId = currentUser.id || currentUser.profileId;
            currentUsername = currentUser.username;
            console.log(
              "[DeleteModal] Usuário encontrado via /profile, ID:",
              currentUserId,
              "Username:",
              currentUsername
            );
          } catch (e) {
            console.log("[DeleteModal] Erro ao parsear /profile:", e);
          }
        }
      }

      // Se ainda não encontrou, tenta decodificar o token JWT
      if (!currentUserId && !currentUsername) {
        console.log("[DeleteModal] Tentando decodificar JWT...");
        try {
          const decoded = decodeJWT(token);
          // No JWT, 'sub' é geralmente o username/subject
          currentUsername =
            decoded.sub ||
            decoded.username ||
            decoded.preferred_username ||
            decoded.name;
          currentUserId = decoded.id || decoded.profileId;
          if (currentUserId || currentUsername) {
            console.log(
              "[DeleteModal] Encontrado no JWT - ID:",
              currentUserId,
              "Username:",
              currentUsername
            );
          }
        } catch (e) {
          console.log("[DeleteModal] Erro ao decodificar JWT:", e);
        }
      }

      console.log("[DeleteModal] Current User ID final:", currentUserId);
      console.log("[DeleteModal] Current Username final:", currentUsername);

      // Verificar se o usuário é o criador
      const productOwnerId =
        product.profileId || product.userId || product.createdBy;
      const productOwnerUsername =
        product.profileUsername || product.username || product.ownerUsername;

      // Converter para string para comparação segura
      const currentUserIdStr = currentUserId
        ? String(currentUserId).trim().toLowerCase()
        : "";
      const productOwnerIdStr = productOwnerId
        ? String(productOwnerId).trim().toLowerCase()
        : "";
      const currentUsernameStr = currentUsername
        ? String(currentUsername).trim().toLowerCase()
        : "";
      const productOwnerUsernameStr = productOwnerUsername
        ? String(productOwnerUsername).trim().toLowerCase()
        : "";

      console.log("[DeleteModal] Strings para comparação:", {
        currentUserIdStr,
        productOwnerIdStr,
        currentUsernameStr,
        productOwnerUsernameStr,
      });

      // Comparar por ID ou por username
      const isOwnerById =
        currentUserIdStr &&
        productOwnerIdStr &&
        currentUserIdStr === productOwnerIdStr;
      const isOwnerByUsername =
        currentUsernameStr &&
        productOwnerUsernameStr &&
        currentUsernameStr === productOwnerUsernameStr;
      const isOwner = isOwnerById || isOwnerByUsername;

      console.log("[DeleteModal] Resultado comparação:", {
        isOwnerById,
        isOwnerByUsername,
        isOwner,
      });

      // Habilitar ou desabilitar o botão
      if (deleteBtn) {
        if (isOwner) {
          // Mostrar e habilitar para o dono
          deleteBtn.style.display = "block";
          deleteBtn.disabled = false;
          deleteBtn.style.opacity = "1";
          deleteBtn.style.cursor = "pointer";
          deleteBtn.title = "Deletar produto";
          console.log(
            "[DeleteModal]  Botão visível e habilitado (você é o dono)"
          );
        } else {
          // Esconder para outros
          deleteBtn.style.display = "none";
          deleteBtn.disabled = true;
          console.log("[DeleteModal]  Botão ocultado (você não é o dono)");
        }

        // Adicionar event listener ao botão
        deleteBtn.addEventListener("click", (e) => {
          e.preventDefault();
          e.stopPropagation();

          if (deleteBtn.disabled) {
            if (typeof showToast === "function") {
              showToast("Apenas o criador do produto pode deletá-lo", "error");
            } else if (
              window.toast &&
              typeof window.toast.error === "function"
            ) {
              window.toast.error("Apenas o criador do produto pode deletá-lo");
            } else {
              alert("Apenas o criador do produto pode deletá-lo");
            }
            return;
          }

          console.log("[DeleteModal] Abrindo modal para produto:", productId);
          openDeleteProductModal(productId);
        });
      }
    } catch (error) {
      console.error(
        "[DeleteModal] ❌ Erro ao verificar propriedade do produto:",
        error
      );
      const deleteBtn = document.getElementById("deleteProductBtn");
      if (deleteBtn) {
        deleteBtn.disabled = true;
        deleteBtn.style.opacity = "0.5";
        deleteBtn.style.cursor = "not-allowed";
      }
    }
  }

  /**
   * Decodifica um JWT para extrair informações
   */
  function decodeJWT(token) {
    try {
      const base64Url = token.split(".")[1];
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split("")
          .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
          .join("")
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      throw new Error("Erro ao decodificar JWT: " + error.message);
    }
  }

  function getStoredUserData() {
    try {
      const raw = localStorage.getItem("userData");
      return raw ? JSON.parse(raw) : null;
    } catch (error) {
      console.warn("[DeleteModal] Erro ao ler userData local:", error);
      return null;
    }
  }

  // Expor funções globalmente se necessário
  window.deleteProductModal = {
    open: openDeleteProductModal,
    close: closeDeleteProductModal,
  };
})();
