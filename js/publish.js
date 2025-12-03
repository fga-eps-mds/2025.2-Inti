document.addEventListener("DOMContentLoaded", () => {
  const imgSelection = document.getElementById("imgSelection");
  const fileInput = document.getElementById("fileInput");
  const imgPlaceholder = document.getElementById("imgPlaceholder");
  const imagePreview = document.getElementById("imagePreview");
  const descriptionInput = document.getElementById("descriptionInput");
  const publishButton = document.getElementById("publishButton");
  const loadingOverlay = document.getElementById("loadingOverlay");
  const isAuthenticated = localStorage.getItem("isAuthenticated");
  const authToken = localStorage.getItem("authToken");

  if (
    !imgSelection ||
    !fileInput ||
    !imgPlaceholder ||
    !imagePreview ||
    !descriptionInput ||
    !publishButton ||
    !loadingOverlay
  ) {
    return;
  }

  if (isAuthenticated !== "true" || !authToken) {
    window.location.href = "../index.html";
    return;
  }

  if (typeof apiService !== "undefined") {
    apiService.setAuthToken(authToken);
  }

  let selectedImg = null;

  imgSelection.addEventListener("click", () => {
    fileInput.click();
  });

  fileInput.addEventListener("change", (event) => {
    const file = event.target.files[0];

    if (!file) {
      return;
    }

    if (!file.type.startsWith("image/")) {
      showErrorMessage("Selecione um arquivo de imagem válido.");
      return;
    }

    const maxSize = 10 * 1024 * 1024; // 10 MB limit
    if (file.size > maxSize) {
      showErrorMessage("A imagem deve ter no máximo 10MB.");
      return;
    }

    selectedImg = file;

    const reader = new FileReader();
    reader.onload = (e) => {
      imagePreview.src = e.target.result;
      imagePreview.style.display = "block";
      imgPlaceholder.style.display = "none";
    };
    reader.readAsDataURL(file);

    checkFormValidity();
  });

  descriptionInput.addEventListener("input", () => {
    checkFormValidity();
  });

  publishButton.addEventListener("click", async () => {
    if (!selectedImg || !descriptionInput.value.trim()) {
      return;
    }

    loadingOverlay.style.display = "flex";

    try {
      const formData = new FormData();
      formData.append("image", selectedImg);
      formData.append("description", descriptionInput.value.trim());

      const status = await submitPost(formData);
      handleSuccessFeedback(status);
      resetForm();
    } catch (error) {
      const serverMessage =
        error?.message && error.message.includes("Failed to fetch")
          ? "Erro ao conectar com o servidor. Verifique sua conexão."
          : error?.message || "Erro ao publicar. Tente novamente.";
      showErrorMessage(serverMessage);
    } finally {
      loadingOverlay.style.display = "none";
    }
  });

  function handleSuccessFeedback(status) {
    const successMessage = "Publicação criada com sucesso!";
    if (status === 201 && typeof toast !== "undefined" && toast.success) {
      toast.success(successMessage);
    } else {
      showSuccessMessage(successMessage);
    }
  }

  async function submitPost(formData) {
    if (typeof apiService !== "undefined" && apiService.createPost) {
      await apiService.createPost(formData);
      return 201;
    }

    const baseURL =
      (typeof API_CONFIG !== "undefined" && API_CONFIG.baseURL) ||
      "https://20252-inti-production.up.railway.app";
    const headers = authToken ? { Authorization: `Bearer ${authToken}` } : {};

    const response = await fetch(`${baseURL}/post`, {
      method: "POST",
      headers,
      body: formData,
    });

    if (!response.ok) {
      const errorPayload = await response.text().catch(() => "");
      throw new Error(errorPayload || `Erro ${response.status} ao publicar.`);
    }

    return response.status;
  }

  function checkFormValidity() {
    const hasImage = selectedImg !== null;
    const hasDescription = descriptionInput.value.trim().length > 0;
    publishButton.disabled = !(hasImage && hasDescription);
  }

  function resetForm() {
    selectedImg = null;
    fileInput.value = "";
    descriptionInput.value = "";
    imagePreview.style.display = "none";
    imgPlaceholder.style.display = "block";
    imagePreview.src = "";
    publishButton.disabled = true;
  }

  function showSuccessMessage(message) {
    const messageDiv = createMessageDiv(message, "success");
    document.body.appendChild(messageDiv);

    setTimeout(() => {
      messageDiv.classList.add("fade-out");
      setTimeout(() => {
        document.body.removeChild(messageDiv);
      }, 300);
    }, 3000);
  }

  function showErrorMessage(message) {
    const messageDiv = createMessageDiv(message, "error");
    document.body.appendChild(messageDiv);

    setTimeout(() => {
      messageDiv.classList.add("fade-out");
      setTimeout(() => {
        document.body.removeChild(messageDiv);
      }, 300);
    }, 3000);
  }

  function createMessageDiv(message, type) {
    const div = document.createElement("div");
    div.className = `message-toast ${type}`;
    div.textContent = message;
    div.style.position = "fixed";
    div.style.top = "20px";
    div.style.left = "50%";
    div.style.transform = "translateX(-50%)";
    div.style.padding = "15px 30px";
    div.style.borderRadius = "10px";
    div.style.color = "#ffffff";
    div.style.fontFamily = "Maitree, sans-serif";
    div.style.fontSize = "14px";
    div.style.fontWeight = "bold";
    div.style.zIndex = "2000";
    div.style.boxShadow = "0 4px 12px rgba(0, 0, 0, 0.15)";

    if (type === "success") {
      div.style.backgroundColor = "#4caf50";
    } else {
      div.style.backgroundColor = "#f44336";
    }

    return div;
  }
});
