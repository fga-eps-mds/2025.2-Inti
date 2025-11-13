// Sistema simples de roteamento

// Função para navegar entre páginas
function navigateTo(page) {
  const routes = {
    home: "pages/home.html",
    login: "../index.html",
    cadastro: "pages/cadastro.html",
    profile: "pages/profile.html",
    dashboard: "pages/dashboard.html",
  };

  if (routes[page]) {
    window.location.href = routes[page];
  } else {
    console.error("Rota não encontrada:", page);
  }
}

// Função para obter parâmetros da URL
function getUrlParams() {
  const params = {};
  const queryString = window.location.search.substring(1);
  const queries = queryString.split("&");

  queries.forEach((query) => {
    const [key, value] = query.split("=");
    if (key) {
      params[decodeURIComponent(key)] = decodeURIComponent(value || "");
    }
  });

  return params;
}

// Função para adicionar parâmetro à URL
function addUrlParam(key, value) {
  const url = new URL(window.location.href);
  url.searchParams.set(key, value);
  window.history.pushState({}, "", url);
}

// Exportar funções se necessário
if (typeof module !== "undefined" && module.exports) {
  module.exports = { navigateTo, getUrlParams, addUrlParam };
}
