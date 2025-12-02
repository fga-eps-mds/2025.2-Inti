document.addEventListener("DOMContentLoaded", loadEvents);

async function loadEvents() {
  const container = document.querySelector(".event-list");
  container.innerHTML = "<p>Carregando eventos...</p>";

  if (!window.apiService) {
    container.innerHTML =
      "<p>Serviço de API indisponível. Recarregue a página após o login.</p>";
    return;
  }

  try {
    const events = await apiService.getEvents();

    if (!Array.isArray(events)) {
      container.innerHTML =
        "<pre>Resposta inesperada: " +
        JSON.stringify(events, null, 2) +
        "</pre>";
      return;
    }

    if (events.length === 0) {
      container.innerHTML = "<p>Nenhum evento encontrado.</p>";
      return;
    }

    const baseURL = apiService?.baseURL || API_CONFIG?.baseURL || "";
    container.innerHTML = "";

    events.forEach((event) => {
      const eventId =
        event.id || event.eventId || event.uuid || event.guid || null;
      const detailUrl =
        (window.EventNavigation &&
          EventNavigation.buildEventDetailUrl(eventId)) ||
        null;
      const title = event.title || "Evento";
      const imagePath =
        event.imageUrl ||
        event.image ||
        (event.blobName ? `/images/${event.blobName}` : "");
      const date = event.eventTime || event.data || event.date || "";

      let formattedDate = "";
      if (date) {
        const parsedDate = new Date(date);
        formattedDate = isNaN(parsedDate)
          ? date
          : parsedDate.toLocaleDateString("pt-BR", {
              day: "2-digit",
              month: "2-digit",
            });
      }

      const eventDiv = document.createElement("div");
      eventDiv.className = "event";

      const eventText = document.createElement("div");
      eventText.className = "event-text";

      const h2Title = document.createElement("h2");
      h2Title.textContent = title;
      eventText.appendChild(h2Title);

      const h2Date = document.createElement("h2");
      h2Date.textContent = formattedDate;
      eventText.appendChild(h2Date);

      eventDiv.appendChild(eventText);

      if (typeof imagePath === "string" && imagePath.trim() !== "") {
        const img = document.createElement("img");
        const trimmedPath = imagePath.trim();
        img.src = trimmedPath.startsWith("http")
          ? trimmedPath
          : `${baseURL}${trimmedPath}`;
        img.alt = title;
        eventDiv.appendChild(img);
      }

      if (detailUrl) {
        eventDiv.style.cursor = "pointer";
        eventDiv.addEventListener("click", () => {
          window.location.href = detailUrl;
        });
      }

      container.appendChild(eventDiv);
    });
  } catch (err) {
    const isAuthError =
      err.message.includes("401") || err.message.includes("403");
    container.innerHTML = isAuthError
      ? "<p>Faça login novamente para visualizar os eventos.</p>"
      : "<pre>Erro ao carregar: " + err.message + "</pre>";
    console.error(err);
  }
}
