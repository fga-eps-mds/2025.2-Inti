console.log("MUSA carregado");

// Função para verificar autenticação
function checkAuth() {
  const isAuthenticated = localStorage.getItem("isAuthenticated");
  return isAuthenticated === "true";
}

// Função para fazer logout
function logout() {
  // Clear auth token from API service
  if (typeof apiService !== "undefined") {
    apiService.clearAuthToken();
  }
  localStorage.removeItem("isAuthenticated");
  localStorage.removeItem("userData");
  window.location.href = "../index.html";
}

// Adicionar listener para botões de logout se existirem
document.addEventListener("DOMContentLoaded", function () {
  const logoutBtns = document.querySelectorAll(".logout-btn");
  logoutBtns.forEach((btn) => {
    btn.addEventListener("click", logout);
  });
});
