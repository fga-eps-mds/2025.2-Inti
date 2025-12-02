let currentEventId = null;
let currentProfileId = null;
let isUserSubscribed = false;
let attendanceButton = null;
let attendanceRequestInFlight = false;

document.addEventListener("DOMContentLoaded", async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const eventId = urlParams.get("id");
  attendanceButton = document.getElementById("btn-confirm");
  currentEventId = eventId;

  updateAttendanceButton();

  if (!eventId) {
    notifyError("Evento não encontrado");
    setTimeout(() => (window.location.href = "eventos.html"), 1500);
    return;
  }

  if (!window.apiService) {
    notifyError("Serviço indisponível. Recarregue a página após o login.");
    return;
  }

  try {
    const [event, profile] = await Promise.all([
      apiService.getEventDetail(eventId),
      apiService
        .getMyProfile()
        .then((data) => {
          currentProfileId = extractProfileId(data);
          return data;
        })
        .catch((error) => {
          console.warn("Não foi possível obter o perfil do usuário:", error);
          return null;
        }),
    ]);

    isUserSubscribed = inferUserSubscription(event, currentProfileId);
    renderEventDetails(event);
    updateAttendanceButton();
  } catch (error) {
    console.error("Error loading event details:", error);
    const isAuthError =
      error?.message?.includes("401") || error?.message?.includes("403");
    notifyError(
      isAuthError
        ? "Faça login novamente para ver os detalhes do evento."
        : "Erro ao carregar detalhes do evento"
    );
  }
});

function renderEventDetails(event) {
  const titleElement = document.querySelector(".event-title");
  if (titleElement) {
    titleElement.textContent = event?.title || "Evento";
  }

  const imageElement = document.getElementById("eventImage");
  if (imageElement) {
    const imageUrl = buildAbsoluteUrl(event?.imageUrl);
    if (imageUrl) {
      imageElement.src = imageUrl;
    }
  }

  const orgSection = document.querySelector(".event-organization");
  if (orgSection) {
    const orgNameElement = document.getElementById("orgName");
    const orgAvatarElement = document.getElementById("orgAvatar");
    if (event?.organization) {
      if (orgNameElement) {
        orgNameElement.textContent =
          event.organization.name || event.organization.username || "";
      }
      if (orgAvatarElement && event.organization.profilePictureUrl) {
        const avatarUrl = buildAbsoluteUrl(
          event.organization.profilePictureUrl
        );
        if (avatarUrl) {
          orgAvatarElement.style.backgroundImage = `url('${avatarUrl}')`;
        }
      }
    } else {
      orgSection.style.display = "none";
    }
  }

  const descriptionElement = document.getElementById("eventDescription");
  if (descriptionElement) {
    descriptionElement.textContent =
      event?.description || "Sem descrição disponível.";
  }

  const dateElement = document.getElementById("eventDate");
  const timeElement = document.getElementById("eventTime");
  if (event?.eventTime) {
    const date = new Date(event.eventTime);
    if (dateElement) {
      dateElement.textContent = date.toLocaleDateString("pt-BR");
    }
    if (timeElement) {
      timeElement.textContent = date.toLocaleTimeString("pt-BR", {
        hour: "2-digit",
        minute: "2-digit",
      });
    }
  } else {
    if (dateElement) dateElement.textContent = "Data não informada";
    if (timeElement) timeElement.textContent = "--:--";
  }

  const priceElement = document.getElementById("eventPrice");
  if (priceElement) {
    priceElement.textContent = formatPrice(event?.price);
  }

  const locationElement = document.getElementById("eventLocation");
  if (locationElement) {
    locationElement.textContent =
      formatAddress(event?.address) || "Local a definir";
  }

  const coordinatesElement = document.getElementById("eventCoordinates");
  const latitude = parseFloat(event?.latitude);
  const longitude = parseFloat(event?.longitude);
  const hasCoords = Number.isFinite(latitude) && Number.isFinite(longitude);
  if (coordinatesElement) {
    coordinatesElement.textContent = hasCoords
      ? `Lat: ${latitude}, Lng: ${longitude}`
      : "Coordenadas não informadas";
  }
  updateMap(latitude, longitude, hasCoords);
}

function formatAddress(address) {
  if (!address || typeof address !== "object") return null;
  const parts = [
    address.streetAddress || address.street,
    address.number || address.houseNumber,
    address.neighborhood,
    address.city,
    address.state,
    address.zipCode || address.postalCode,
  ].filter(Boolean);
  return parts.length ? parts.join(", ") : null;
}

function formatPrice(value) {
  if (value === null || value === undefined || value === "") {
    return "Não informado";
  }
  if (typeof value === "number") {
    return `R$ ${value.toFixed(2).replace(".", ",")}`;
  }
  return value;
}

function updateMap(latitude, longitude, hasCoords) {
  const mapFrame = document.getElementById("eventMapFrame");
  const placeholder = document.getElementById("mapPlaceholder");
  if (!mapFrame || !placeholder) return;

  if (hasCoords) {
    const url = `https://www.google.com/maps?q=${latitude},${longitude}&z=16&output=embed`;
    mapFrame.src = url;
    mapFrame.style.display = "block";
    mapFrame.style.pointerEvents = "auto";
    placeholder.style.display = "none";
  } else {
    mapFrame.src = "";
    mapFrame.style.display = "none";
    mapFrame.style.pointerEvents = "none";
    placeholder.style.display = "flex";
  }
}

function buildAbsoluteUrl(pathOrUrl) {
  if (!pathOrUrl || typeof pathOrUrl !== "string") return null;
  const trimmed = pathOrUrl.trim();
  if (!trimmed) return null;
  return trimmed.startsWith("http")
    ? trimmed
    : `${apiService.baseURL || ""}${trimmed}`;
}

function extractProfileId(profile) {
  if (!profile || typeof profile !== "object") return null;
  return (
    profile.id ||
    profile.profileId ||
    profile.userId ||
    profile.uuid ||
    profile.profile?.id ||
    null
  );
}

function inferUserSubscription(event, profileId) {
  if (!event || typeof event !== "object") return false;
  const possibleFlags = [
    "isSubscribed",
    "subscribed",
    "isRegistered",
    "registered",
    "userSubscribed",
    "userRegistered",
    "attending",
    "isAttending",
    "userIsAttendee",
    "alreadyRegistered",
  ];

  for (const flag of possibleFlags) {
    if (flag in event) {
      return Boolean(event[flag]);
    }
  }

  if (Array.isArray(event.attendees) && profileId) {
    return event.attendees.some((participant) => {
      if (!participant) return false;
      const candidateIds = [
        participant.id,
        participant.profileId,
        participant.userId,
        participant.profile?.id,
      ].filter(Boolean);
      return candidateIds.includes(profileId);
    });
  }

  return false;
}

function notifyError(message) {
  if (typeof toast !== "undefined") toast.error(message);
  else alert(message);
}

function notifySuccess(message) {
  if (typeof toast !== "undefined") toast.success(message);
  else alert(message);
}

function updateAttendanceButton() {
  if (!attendanceButton) return;
  if (attendanceRequestInFlight) {
    attendanceButton.disabled = true;
    attendanceButton.textContent = "Processando...";
    return;
  }

  attendanceButton.disabled = false;
  attendanceButton.textContent = isUserSubscribed
    ? "Cancelar inscrição"
    : "Confirmar presença";
}

async function confirmPresence() {
  if (!currentEventId) {
    notifyError("Evento inválido");
    return;
  }

  if (!window.apiService) {
    notifyError("Serviço indisponível no momento.");
    return;
  }

  if (attendanceRequestInFlight) {
    return;
  }

  attendanceRequestInFlight = true;
  updateAttendanceButton();

  try {
    if (isUserSubscribed) {
      await apiService.cancelEventAttendance(currentEventId);
      isUserSubscribed = false;
      notifySuccess("Inscrição cancelada.");
    } else {
      await apiService.attendEvent(currentEventId);
      isUserSubscribed = true;
      notifySuccess("Presença confirmada!");
    }
  } catch (error) {
    console.error("Erro ao atualizar inscrição:", error);
    const message =
      error?.message?.includes("401") || error?.message?.includes("403")
        ? "Faça login novamente para gerenciar a inscrição."
        : error?.message || "Não foi possível atualizar a inscrição.";
    notifyError(message);
  } finally {
    attendanceRequestInFlight = false;
    updateAttendanceButton();
  }
}

window.confirmPresence = confirmPresence;
