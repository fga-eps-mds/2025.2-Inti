// Gerenciamento de autenticação

// Função para fazer login
function login(email, password) {
  // Aqui você faria a validação real com um backend
  // Por enquanto, vamos apenas simular

  if (email && password) {
    const userData = {
      email: email,
      loginTime: new Date().toISOString(),
    };

    localStorage.setItem("isAuthenticated", "true");
    localStorage.setItem("userData", JSON.stringify(userData));

    return true;
  }

  return false;
}

// Função para fazer cadastro
function register(nome, username, email, password, isOrganizational) {
  // Aqui você faria o cadastro real com um backend
  // Por enquanto, vamos apenas simular

  if (nome && username && email && password) {
    const userData = {
      nome: nome,
      username: username,
      email: email,
      isOrganizational: isOrganizational,
      registerTime: new Date().toISOString(),
    };

    localStorage.setItem("isAuthenticated", "true");
    localStorage.setItem("userData", JSON.stringify(userData));

    return true;
  }

  return false;
}

// Função para obter dados do usuário
function getUserData() {
  const userData = localStorage.getItem("userData");
  return userData ? JSON.parse(userData) : null;
}

// Exportar funções se necessário
if (typeof module !== "undefined" && module.exports) {
  module.exports = { login, register, getUserData };
}
