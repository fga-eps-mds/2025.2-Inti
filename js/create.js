document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("createPostForm");
  const fileInput = document.getElementById("postImage");
  const previewImage = document.getElementById("previewImage");
  const uploadText = document.getElementById("uploadText");
  const descriptionInput = document.getElementById("postDescription");

  // Handle file selection
  fileInput.addEventListener("change", function () {
    const file = this.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = function (e) {
        previewImage.src = e.target.result;
        previewImage.style.display = "block";
        uploadText.style.display = "none";
      };
      reader.readAsDataURL(file);
    }
  });

  // Handle form submission
  form.addEventListener("submit", async function (e) {
    e.preventDefault();

    const file = fileInput.files[0];
    const description = descriptionInput.value;

    if (!file) {
      if (typeof toast !== "undefined")
        toast.error("Por favor, selecione uma imagem.");
      else alert("Por favor, selecione uma imagem.");
      return;
    }

    if (!description) {
      if (typeof toast !== "undefined")
        toast.error("Por favor, adicione uma descrição.");
      else alert("Por favor, adicione uma descrição.");
      return;
    }

    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.textContent = "Publicando...";

    try {
      const formData = new FormData();
      formData.append("image", file);
      formData.append("description", description);

      await apiService.createPost(formData);

      if (typeof toast !== "undefined")
        toast.success("Post criado com sucesso!");
      else alert("Post criado com sucesso!");

      // Redirect to home after a short delay
      setTimeout(() => {
        window.location.href = "home.html";
      }, 1500);
    } catch (error) {
      console.error("Error creating post:", error);
      if (typeof toast !== "undefined")
        toast.error("Erro ao criar post: " + error.message);
      else alert("Erro ao criar post: " + error.message);

      submitBtn.disabled = false;
      submitBtn.textContent = originalText;
    }
  });
});
