// Configuração da API
const API_BASE_URL = "http://localhost:8080"; // Ajuste conforme necessário
const BEARER_TOKEN =
  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJub3ZvdCIsImV4cCI6MTc2NjkyNjE0MX0.gkJ5SOqVAkaCzirZvz-itqvM5YlI8eLR51aSswjCjHY";

// Estado da página
let currentEventId = null;
let userProfile = null;
let isAttending = false;

// Inicializar a página
document.addEventListener("DOMContentLoaded", function () {
  const urlParams = new URLSearchParams(window.location.search);
  currentEventId = "abe171a0-e3b4-4b94-80e1-f1c9818e40ea";

  if (!currentEventId) {
    console.error("Event ID não fornecido na URL");
    return;
  }

  // Carregar dados do evento
  loadEventDetails();
});

/**
 * Carrega os detalhes do evento do backend
 */
async function loadEventDetails() {
  try {
    const url = `${API_BASE_URL}/event/${currentEventId}`;
    console.log("Carregando evento de:", url);
    console.log("Token:", BEARER_TOKEN.substring(0, 20) + "...");

    const response = await fetch(url, {
      headers: {
        Authorization: `Bearer ${BEARER_TOKEN}`,
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("Erro na resposta:", response.status, errorText);
      throw new Error(
        `Erro ao carregar evento: ${response.status} ${response.statusText}`
      );
    }

    const eventData = await response.json();
    console.log("Dados do evento recebidos:", eventData);
    populateEventDetails(eventData);
    checkUserAttendance();
  } catch (error) {
    console.error("Erro ao carregar detalhes do evento:", error);
    showNotification("Erro ao carregar evento", "error");
  }
}

/**
 * Preenche o HTML com os dados do evento
 * @param {EventDetailResponse} eventData
 */
function populateEventDetails(eventData) {
  // Título do evento
  const nameEventElement = document.querySelector(".name-event");
  if (nameEventElement) {
    nameEventElement.textContent = eventData.title;
  }

  // Imagem do evento
  const eventImgElement = document.querySelector(".event-img-description img");
  if (eventImgElement && eventData.imageUrl) {
    // Construir a URL da imagem através do endpoint /images
    const imageUrl = `${API_BASE_URL}${eventData.imageUrl}`;
    eventImgElement.src = imageUrl;
    eventImgElement.alt = eventData.title;
    console.log("Imagem do evento carregada de:", imageUrl);
  }

  // Descrição
  const descriptionElement = document.querySelector(".description");
  if (descriptionElement) {
    descriptionElement.textContent = eventData.description;
  }

  // Informações do evento (Data, Horário, Valor, Local)
  const infoComplementElement = document.querySelector(".info-complement");
  if (infoComplementElement) {
    infoComplementElement.innerHTML = `
            <p>${formatDate(eventData.eventTime)}</p>
            <p>${formatTime(eventData.eventTime)}</p>
            <p>Gratuito</p>
            <p>${eventData.address ? formatAddress(eventData.address) : "Local a definir"}</p>
        `;
  }

  // Mapa (se houver coordenadas)
  if (eventData.latitude && eventData.longitude) {
    initializeMap(eventData.latitude, eventData.longitude, eventData.address);
  }
}

/**
 * Atualiza o estado visual do botão
 */
function updateButtonState() {
  const botao = document.getElementById("btn-confirm");

  if (isAttending) {
    botao.classList.remove("confirm-attendance");
    botao.classList.add("cancelar");
    botao.textContent = "Cancelar Inscrição";
  } else {
    botao.classList.remove("cancelar");
    botao.classList.add("confirm-attendance");
    botao.textContent = "Confirmar Presença";
  }
}

/**
 * Listener do botão de confirmação/cancelamento
 */
const botao = document.getElementById("btn-confirm");
botao.addEventListener("click", async function () {
  if (isAttending) {
    // Cancelar inscrição
    await cancelAttendance();
  } else {
    // Confirmar presença
    await confirmAttendance();
  }
});

/**
 * Confirma a presença do usuário no evento
 */
async function confirmAttendance() {
  try {
    botao.disabled = true;
    const url = `${API_BASE_URL}/event/${currentEventId}/attendees`;
    console.log("Confirmando presença em:", url);

    const response = await fetch(url, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${BEARER_TOKEN}`,
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("Erro ao confirmar presença:", response.status, errorText);
      throw new Error(`Erro ao confirmar presença: ${response.status}`);
    }

    const participantData = await response.json();
    console.log("Inscrição confirmada:", participantData);

    isAttending = true;
    updateButtonState();
    showNotification("Presença confirmada com sucesso!", "success");
  } catch (error) {
    console.error("Erro ao confirmar presença:", error);
    showNotification(`Erro ao confirmar presença: ${error.message}`, "error");
  } finally {
    botao.disabled = false;
  }
}

/**
 * Cancela a inscrição do usuário no evento
 */
async function cancelAttendance() {
  try {
    botao.disabled = true;

    const profileId = `47a69222-ce45-4140-8cbb-e528c983f902`;

    if (!profileId) {
      throw new Error("Profile ID não encontrado. Faça login novamente.");
    }

    const url = `${API_BASE_URL}/event/${currentEventId}/attendees/${profileId}`;
    console.log("Cancelando inscrição em:", url);

    const response = await fetch(url, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${BEARER_TOKEN}`,
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("Erro ao cancelar inscrição:", response.status, errorText);
      throw new Error(`Erro ao cancelar inscrição: ${response.status}`);
    }

    console.log("Inscrição cancelada com sucesso");

    isAttending = false;
    updateButtonState();
    showNotification("Inscrição cancelada", "success");
  } catch (error) {
    console.error("Erro ao cancelar inscrição:", error);
    showNotification("Erro ao cancelar inscrição", "error");
  } finally {
    botao.disabled = false;
  }
}

/**
 * Inicializa o mapa com coordenadas
 * @param {number} latitude
 * @param {number} longitude
 * @param {LocalAddress} address
 */
function initializeMap(latitude, longitude, address) {
  const mapElement = document.getElementById("map");

  // Se você estiver usando Google Maps ou Leaflet, adicione aqui
  // Exemplo com Google Maps:
  // const map = new google.maps.Map(mapElement, {
  //     zoom: 15,
  //     center: { lat: latitude, lng: longitude }
  // });
  // new google.maps.Marker({
  //     map: map,
  //     position: { lat: latitude, lng: longitude }
  // });

  // Por enquanto, apenas removemos o ::after e mostramos o endereço
  mapElement.style.backgroundColor = "#f0f0f0";
  mapElement.style.display = "flex";
  mapElement.style.alignItems = "center";
  mapElement.style.justifyContent = "center";
  mapElement.textContent = `📍 ${formatAddress(address)}`;
}

/**
 * Exibe uma notificação ao usuário
 * @param {string} message
 * @param {string} type - 'success', 'error', 'info'
 */
function showNotification(message, type = "info") {
  // Criar elemento de notificação
  const notification = document.createElement("div");
  notification.className = `notification notification-${type}`;
  notification.textContent = message;
  notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 16px 24px;
        background-color: ${type === "success" ? "#10b981" : type === "error" ? "#ef4444" : "#3b82f6"};
        color: white;
        border-radius: 8px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        z-index: 9999;
        animation: slideIn 0.3s ease-out;
    `;

  document.body.appendChild(notification);

  // Remover após 3 segundos
  setTimeout(() => {
    notification.style.animation = "slideOut 0.3s ease-out";
    setTimeout(() => notification.remove(), 300);
  }, 3000);
}

/**
 * Formata uma data ISO para formato brasileiro
 * @param {string} isoDate
 * @returns {string}
 */
function formatDate(isoDate) {
  try {
    const date = new Date(isoDate);
    return date.toLocaleDateString("pt-BR", {
      day: "2-digit",
      month: "long",
      year: "numeric",
    });
  } catch (error) {
    console.error("Erro ao formatar data:", error);
    return isoDate;
  }
}

/**
 * Formata uma hora ISO para formato HH:mm
 * @param {string} isoDate
 * @returns {string}
 */
function formatTime(isoDate) {
  try {
    const date = new Date(isoDate);
    return date.toLocaleTimeString("pt-BR", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
    });
  } catch (error) {
    console.error("Erro ao formatar hora:", error);
    return isoDate;
  }
}

/**
 * Formata um endereço
 * @param {LocalAddress} address
 * @returns {string}
 */
function formatAddress(address) {
  if (!address) return "Local a definir";

  const parts = [
    address.street,
    address.number,
    address.city,
    address.state,
  ].filter(Boolean);

  return parts.join(", ") || "Local a definir";
}

/**
 * Verifica se o usuário já está inscrito no evento
 */
async function checkUserAttendance() {
  try {
    // Aqui você pode verificar se o usuário está inscrito
    // Esta lógica depende de como o seu backend expõe essa informação
    // Por enquanto, vamos assumir que não está inscrito
    isAttending = false;
    updateButtonState();
  } catch (error) {
    console.error("Erro ao verificar inscrição:", error);
  }
}
