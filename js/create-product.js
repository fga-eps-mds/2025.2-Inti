document.addEventListener("DOMContentLoaded", function () {
  const form = document.querySelector(".form-new-product");
  const submitButton = form?.querySelector("button[type='submit']");
  const imageInput = document.getElementById("image");
  const imagePreview = document.getElementById("imagePreview");
  const placeholderIcon = document.querySelector(".image-placeholder-icon");
  const placeholderText = document.querySelector(".image-upload-content .global-font");
  const titleInput = document.getElementById("title");
  const descriptionInput = document.getElementById("description");
  const priceInput = document.getElementById("price");

  if (!form) return;

  if (imageInput) {
    imageInput.addEventListener("change", handleImagePreview);
  }

  form.addEventListener("submit", async function (event) {
    event.preventDefault();

    const title = titleInput?.value.trim();
    const description = descriptionInput?.value.trim();
    const priceRaw = priceInput?.value.trim();

    if (!title || !description || !priceRaw) {
      if (window.toast) toast.warning("Preencha título, descrição e preço.");
      else alert("Preencha título, descrição e preço.");
      return;
    }

    const normalizedPrice = normalizePrice(priceRaw);
    if (!normalizedPrice || Number.isNaN(Number(normalizedPrice))) {
      if (window.toast) toast.warning("Informe um preço válido.");
      else alert("Informe um preço válido.");
      return;
    }

    const formData = new FormData();
    formData.append("title", title);
    formData.append("description", description);
    formData.append("price", normalizedPrice);

    if (imageInput?.files?.length) {
      formData.append("image", imageInput.files[0]);
    }

    try {
      setSubmitting(true);
      await apiService.createProduct(formData);
      if (window.toast) toast.success("Produto publicado com sucesso!");
      else alert("Produto publicado com sucesso!");
      setTimeout(() => {
        window.location.href = "profile.html?view=products";
      }, 800);
    } catch (error) {
      console.error("Erro ao criar produto:", error);
      const message = error?.message || "Erro ao criar produto.";
      if (window.toast) toast.error(message);
      else alert(message);
    } finally {
      setSubmitting(false);
    }
  });

  function normalizePrice(value = "") {
    return value
      .replace(/[^0-9,.-]/g, "")
      .replace(/\./g, "")
      .replace(/,/g, ".")
      .trim();
  }

  function setSubmitting(isSubmitting) {
    if (!submitButton) return;
    submitButton.disabled = isSubmitting;
    submitButton.textContent = isSubmitting ? "Publicando..." : "PUBLICAR";
  }

  function handleImagePreview(event) {
    const file = event.target.files?.[0];
    if (!file) {
      resetImagePreview();
      return;
    }

    const reader = new FileReader();
    reader.onload = function (e) {
      if (imagePreview) {
        imagePreview.src = e.target?.result || "";
        imagePreview.style.display = "block";
      }
      if (placeholderIcon) placeholderIcon.style.display = "none";
      if (placeholderText) placeholderText.style.display = "none";
    };
    reader.readAsDataURL(file);
  }

  function resetImagePreview() {
    if (imagePreview) {
      imagePreview.src = "";
      imagePreview.style.display = "none";
    }
    if (placeholderIcon) placeholderIcon.style.display = "";
    if (placeholderText) placeholderText.style.display = "";
  }
});
