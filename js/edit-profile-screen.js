document.addEventListener("DOMContentLoaded", () => {
  initEditProfileScreen();
});

let editProfileState = {
  profile: null,
  avatarPreviewUrl: null,
  isSubmitting: false,
};

function initEditProfileScreen() {
  const form = document.querySelector(".container-edit form");
  if (!form) return;

  if (!ensureEditScreenAuth()) return;

  bindEditScreenEvents(form);
  loadEditScreenProfile();
}

function ensureEditScreenAuth() {
  const isAuthenticated = localStorage.getItem("isAuthenticated");
  const token = localStorage.getItem("authToken");

  if (isAuthenticated !== "true" || !token) {
    window.location.href = "../index.html";
    return false;
  }

  if (typeof apiService !== "undefined") {
    apiService.setAuthToken(token);
  }
  return true;
}

function bindEditScreenEvents(form) {
  const avatarInput = document.getElementById("avatar-upload");
  if (avatarInput) {
    avatarInput.addEventListener("change", handleAvatarSelection);
  }

  form.addEventListener("submit", handleProfileSubmit);
}

async function loadEditScreenProfile() {
  setEditScreenLoading(true);
  try {
    const response = await apiService.getMyProfile();
    editProfileState.profile = response;
    populateEditProfileForm(response);
  } catch (error) {
    handleEditProfileError(error);
  } finally {
    setEditScreenLoading(false);
  }
}

function populateEditProfileForm(data) {
  const usernameInput = document.getElementById("username-input");
  const nameInput = document.getElementById("nome-input");
  const phoneInput = document.getElementById("telefone-input");
  const emailInput = document.getElementById("email-input");
  const bioInput = document.getElementById("bio-input");

  if (usernameInput) usernameInput.value = data.username || "";
  if (nameInput) nameInput.value = data.name || "";
  if (phoneInput) phoneInput.value = data.phone || "";
  if (emailInput) emailInput.value = data.publicEmail || "";
  if (bioInput) bioInput.value = data.bio || "";

  updateAvatarPreviewFromProfile(data.profile_picture_url);
}

function updateAvatarPreviewFromProfile(imagePath) {
  const previewImg = document.querySelector(".icon-user");
  if (!previewImg) return;

  if (!imagePath) {
    previewImg.src = "../assets/img_user.svg";
    return;
  }

  fetchProtectedImage(imagePath)
    .then((objectUrl) => {
      revokeAvatarPreviewUrl();
      editProfileState.avatarPreviewUrl = objectUrl;
      previewImg.src = objectUrl;
    })
    .catch(() => {
      previewImg.src = "../assets/img_user.svg";
    });
}

async function fetchProtectedImage(path) {
  const fullUrl = path.startsWith("http")
    ? path
    : `${apiService.baseURL}${path}`;
  const headers = apiService.token
    ? { Authorization: `Bearer ${apiService.token}` }
    : {};
  const response = await fetch(fullUrl, { headers });
  if (!response.ok) {
    throw new Error("Erro ao carregar imagem");
  }
  const blob = await response.blob();
  return URL.createObjectURL(blob);
}

function handleAvatarSelection(event) {
  const file = event.target.files[0];
  const previewImg = document.querySelector(".icon-user");
  if (!previewImg) return;

  if (!file) {
    return;
  }

  if (!file.type.startsWith("image/")) {
    if (typeof toast !== "undefined") {
      toast.error("Selecione um arquivo de imagem válido.");
    }
    event.target.value = "";
    return;
  }

  revokeAvatarPreviewUrl();
  const objectUrl = URL.createObjectURL(file);
  editProfileState.avatarPreviewUrl = objectUrl;
  previewImg.src = objectUrl;
}

function revokeAvatarPreviewUrl() {
  if (editProfileState.avatarPreviewUrl) {
    URL.revokeObjectURL(editProfileState.avatarPreviewUrl);
    editProfileState.avatarPreviewUrl = null;
  }
}

async function handleProfileSubmit(event) {
  event.preventDefault();
  if (editProfileState.isSubmitting) return;

  const profileData = editProfileState.profile;
  if (!profileData) return;

  const form = event.currentTarget;
  const submitBtn = form.querySelector(".botao-edit");
  const avatarInput = document.getElementById("avatar-upload");
  const usernameInput = document.getElementById("username-input");
  const nameInput = document.getElementById("nome-input");
  const phoneInput = document.getElementById("telefone-input");
  const emailInput = document.getElementById("email-input");
  const bioInput = document.getElementById("bio-input");

  const username = usernameInput ? usernameInput.value.trim() : "";
  const name = nameInput ? nameInput.value.trim() : "";
  const phone = phoneInput ? phoneInput.value.trim() : "";
  const email = emailInput ? emailInput.value.trim() : "";
  const bio = bioInput ? bioInput.value.trim() : "";

  if (!username) {
    if (typeof toast !== "undefined")
      toast.warning("Informe um nome de usuário.");
    return;
  }

  const hasAvatarSelection =
    avatarInput && avatarInput.files && avatarInput.files[0];
  if (!hasAvatarSelection && !profileData.profile_picture_url) {
    if (typeof toast !== "undefined")
      toast.warning("Envie uma foto de perfil antes de salvar.");
    return;
  }

  const formData = new FormData();
  let hasChanges = false;

  const appendIfChanged = (key, newValue, originalValue) => {
    const normalizedOriginal = originalValue ?? "";
    if (newValue !== normalizedOriginal) {
      formData.append(key, newValue);
      hasChanges = true;
    }
  };

  appendIfChanged("username", username, profileData.username);
  appendIfChanged("name", name, profileData.name);
  appendIfChanged("phone", phone, profileData.phone);
  appendIfChanged("publicemail", email, profileData.publicEmail);
  appendIfChanged("userBio", bio, profileData.bio);

  if (!hasChanges && !hasAvatarSelection) {
    if (typeof toast !== "undefined")
      toast.warning("Nenhuma alteração foi feita.");
    return;
  }

  if (hasAvatarSelection) {
    formData.append("profilePicture", avatarInput.files[0]);
  }

  editProfileState.isSubmitting = true;
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.textContent = "Salvando...";
  }

  try {
    if (hasChanges || hasAvatarSelection) {
      await apiService.updateProfile(formData);
    }

    if (typeof toast !== "undefined") {
      toast.success("Perfil atualizado com sucesso!");
    }

    await loadEditScreenProfile();
    if (avatarInput) avatarInput.value = "";
  } catch (error) {
    handleProfileSubmitError(error);
  } finally {
    editProfileState.isSubmitting = false;
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.textContent = "Concluído";
    }
  }
}

function handleProfileSubmitError(error) {
  console.error("Erro ao atualizar perfil:", error);
  const message = (error && error.message) || "Erro ao atualizar perfil.";
  if (message.toLowerCase().includes("username")) {
    if (typeof toast !== "undefined")
      toast.error("Nome de usuário indisponível. Escolha outro.");
    return;
  }

  if (typeof toast !== "undefined")
    toast.error("Não foi possível atualizar o perfil. Tente novamente.");
}

function handleEditProfileError(error) {
  console.error("Erro ao carregar perfil para edição:", error);
  if (
    error &&
    error.message &&
    (error.message.includes("401") || error.message.includes("403"))
  ) {
    localStorage.removeItem("isAuthenticated");
    localStorage.removeItem("authToken");
    localStorage.removeItem("userData");
    window.location.href = "../index.html";
    return;
  }

  if (typeof toast !== "undefined") {
    toast.error("Erro ao carregar informações do perfil.");
  }
}

function setEditScreenLoading(isLoading) {
  const form = document.querySelector(".container-edit form");
  const submitBtn = form ? form.querySelector(".botao-edit") : null;
  if (!form) return;

  const elements = form.querySelectorAll("input, textarea, button");
  elements.forEach((element) => {
    if (isLoading) {
      element.setAttribute("disabled", "disabled");
    } else {
      element.removeAttribute("disabled");
    }
  });

  if (submitBtn) {
    submitBtn.textContent = isLoading ? "Carregando..." : "Concluído";
  }
}
