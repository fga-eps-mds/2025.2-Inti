// Gerenciamento de autenticação

// Use the existing apiService from config.js
// It should already be available as a global variable

// Função para fazer login
async function login(email, password) {
  try {
    const token = await apiService.login({ email, password });

    const userData = {
      email: email,
      loginTime: new Date().toISOString(),
    };

    localStorage.setItem("isAuthenticated", "true");
    localStorage.setItem("userData", JSON.stringify(userData));

    return { success: true, token };
  } catch (error) {
    console.error("Login error:", error);
    return { success: false, error: error.message };
  }
}

// Função para fazer cadastro
async function register(name, username, email, password, type = "user") {
  try {
    // API returns JSON with jwt and profile data
    const response = await apiService.register({
      name,
      username,
      email,
      password,
      type,
    });

    console.log("Registration response:", response);
    console.log("Response type:", typeof response);
    console.log("Response keys:", response ? Object.keys(response) : "null");

    // Check if response is null or doesn't have jwt
    if (!response) {
      console.error("Response is null!");
      throw new Error(
        "Resposta vazia do servidor. Por favor, tente novamente."
      );
    }

    if (!response.jwt) {
      console.error(
        "Response doesn't have jwt field. Response:",
        JSON.stringify(response)
      );
      throw new Error(
        "Token não encontrado na resposta. Por favor, tente novamente."
      );
    }

    // Extract JWT from response
    const token = response.jwt;
    console.log("Extracted token:", token);

    // Set the token IMMEDIATELY before any other API calls
    apiService.setAuthToken(token);
    console.log("Token set in apiService");

    // Use the profile data returned by register endpoint
    const userData = {
      id: response.id,
      name: response.name,
      username: response.username,
      email: response.email,
      type: response.type || type,
      registerTime: new Date().toISOString(),
    };

    localStorage.setItem("isAuthenticated", "true");
    localStorage.setItem("userData", JSON.stringify(userData));

    return { success: true, user: userData };
  } catch (error) {
    console.error("Registration error:", error);
    return { success: false, error: error.message };
  }
}

// Função para fazer logout
function logout() {
  apiService.clearAuthToken();
  localStorage.removeItem("isAuthenticated");
  localStorage.removeItem("userData");
  return true;
}

// Função para verificar se usuário está autenticado
function isAuthenticated() {
  return localStorage.getItem("isAuthenticated") === "true";
}

// Função para obter dados do usuário
function getUserData() {
  const userData = localStorage.getItem("userData");
  return userData ? JSON.parse(userData) : null;
}

// Exportar funções se necessário
if (typeof module !== "undefined" && module.exports) {
  module.exports = { login, register, logout, isAuthenticated, getUserData };
} else if (typeof window !== "undefined") {
  // Make functions available globally
  window.login = login;
  window.register = register;
  window.logout = logout;
  window.isAuthenticated = isAuthenticated;
  window.getUserData = getUserData;
}
