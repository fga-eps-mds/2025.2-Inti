document.addEventListener("DOMContentLoaded", () => {
  const editBtn = document.getElementById("edit-biography-btn");
  const modal = document.getElementById("edit-biography-modal");
  const cancelBtn = document.getElementById("cancel-edit-biography");
  const form = document.getElementById("edit-biography-form");

  // Elements to populate
  const phoneInput = document.getElementById("edit-phone");
  const emailInput = document.getElementById("edit-email");
  const bioInput = document.getElementById("edit-bio");

  if (editBtn) {
    editBtn.addEventListener("click", () => {
      // Populate form with current data if available
      if (typeof currentProfileData !== "undefined" && currentProfileData) {
        if (phoneInput) phoneInput.value = currentProfileData.phone || "";
        if (emailInput) emailInput.value = currentProfileData.publicEmail || "";
        if (bioInput) bioInput.value = currentProfileData.bio || "";
      }

      modal.style.display = "block";
    });
  }

  if (cancelBtn) {
    cancelBtn.addEventListener("click", () => {
      modal.style.display = "none";
    });
  }

  if (form) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();

      const phone = phoneInput.value;
      const email = emailInput.value;
      const bio = bioInput.value;

      const formData = new FormData();
      if (phone) formData.append("phone", phone);
      if (email) formData.append("publicemail", email);
      if (bio) formData.append("userBio", bio);

      // Check if any change was made
      if (!phone && !email && !bio) {
        if (typeof toast !== "undefined")
          toast.warning("Nenhuma alteração foi feita!");
        else alert("Nenhuma alteração foi feita!");
        return;
      }

      try {
        await apiService.updateProfile(formData);
        if (typeof toast !== "undefined")
          toast.success("Perfil atualizado com sucesso!");
        else alert("Perfil atualizado com sucesso!");

        modal.style.display = "none";

        // Reload profile
        if (typeof loadProfile === "function") {
          loadProfile();
        } else {
          window.location.reload();
        }
      } catch (error) {
        console.error("Error updating profile:", error);
        if (typeof toast !== "undefined")
          toast.error("Erro ao atualizar o perfil. Tente novamente");
        else alert("Erro ao atualizar o perfil. Tente novamente");
      }
    });
  }
});
