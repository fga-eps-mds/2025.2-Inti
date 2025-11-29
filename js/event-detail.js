document.addEventListener("DOMContentLoaded", async () => {
  // Get event ID from URL query parameters
  const urlParams = new URLSearchParams(window.location.search);
  const eventId = urlParams.get("id");

  if (!eventId) {
    if (typeof toast !== "undefined") toast.error("Evento não encontrado");
    else alert("Evento não encontrado");
    setTimeout(() => (window.location.href = "eventos.html"), 1500);
    return;
  }

  try {
    const event = await apiService.getEventDetail(eventId);
    renderEventDetails(event);
  } catch (error) {
    console.error("Error loading event details:", error);
    if (typeof toast !== "undefined")
      toast.error("Erro ao carregar detalhes do evento");
    else alert("Erro ao carregar detalhes do evento");
  }
});

function renderEventDetails(event) {
  // Update header title
  const titleElement = document.querySelector(".event-title");
  if (titleElement) titleElement.textContent = event.title;

  // Update image
  const imageElement = document.getElementById("eventImage");
  if (imageElement && event.imageUrl) {
    const backendUrl = apiService.baseURL;
    const fullImageUrl = event.imageUrl.startsWith("http")
      ? event.imageUrl
      : backendUrl + event.imageUrl;
    imageElement.src = fullImageUrl;
  }

  // Update organization info
  const orgNameElement = document.getElementById("orgName");
  const orgAvatarElement = document.getElementById("orgAvatar");

  if (orgNameElement && event.organization) {
    orgNameElement.textContent =
      event.organization.name || event.organization.username;
  }

  if (
    orgAvatarElement &&
    event.organization &&
    event.organization.profilePictureUrl
  ) {
    const backendUrl = apiService.baseURL;
    const fullProfileUrl = event.organization.profilePictureUrl.startsWith(
      "http"
    )
      ? event.organization.profilePictureUrl
      : backendUrl + event.organization.profilePictureUrl;

    orgAvatarElement.style.backgroundImage = `url('${fullProfileUrl}')`;
  }

  // Update details
  document.getElementById("eventDescription").textContent =
    event.description || "Sem descrição";

  // Format date
  if (event.eventTime) {
    const date = new Date(event.eventTime);
    document.getElementById("eventDate").textContent =
      date.toLocaleDateString("pt-BR");
    document.getElementById("eventTime").textContent = date.toLocaleTimeString(
      "pt-BR",
      { hour: "2-digit", minute: "2-digit" }
    );
  }

  // Location
  document.getElementById("eventLocation").textContent =
    event.streetAddress || "Local a definir";
}

function confirmPresence() {
  // TODO: Implement presence confirmation API call
  if (typeof toast !== "undefined") toast.success("Presença confirmada!");
  else alert("Presença confirmada!");
}

// Expose function globally
window.confirmPresence = confirmPresence;
