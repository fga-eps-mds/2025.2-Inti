document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector(".form-new-product");
  if (!form) return;

  const titleInput = document.getElementById("title");
  const descriptionInput = document.getElementById("description");
  const dateInput = document.getElementById("date");
  const timeInput = document.getElementById("time");
  const locationInput = document.getElementById("localizacao_autocomplete");
  const streetInput = document.getElementById("street_address");
  const administrativeRegionInput = document.getElementById("administrativeRegion");
  const referencePointInput = document.getElementById("referencePoint");
  const cityInput = document.getElementById("city");
  const stateInput = document.getElementById("state");
  const latitudeInput = document.getElementById("latitude");
  const longitudeInput = document.getElementById("longitude");
  const imageInput = document.getElementById("image");
  const imagePreview = document.getElementById("imagePreview");
  const imagePlaceholderIcon = document.getElementById("imagePlaceholderIcon");
  const imagePlaceholderText = document.getElementById("imagePlaceholderText");
  const submitBtn = document.getElementById("alerta");
  const mapContainer = document.getElementById("mapPreview");

  let mapInstance = null;
  let markerInstance = null;

  const DEFAULT_COORDS = { lat: -15.793889, lng: -47.882778 }; // Brasília
  const BRAZILIAN_DATE_REGEX = /^(\d{2})\/(\d{2})\/(\d{4})$/;
  const BRAZILIAN_TIME_REGEX = /^([01]\d|2[0-3]):([0-5]\d)$/;

  const notify = (type, message) => {
    if (typeof toast !== "undefined" && typeof toast[type] === "function") {
      toast[type](message);
    } else {
      alert(message);
    }
  };

  const formatCoord = (value) => Number(value).toFixed(6);

  const formatDigitsToBrazilianDate = (digits) => {
    if (digits.length <= 2) return digits;
    if (digits.length <= 4) return `${digits.slice(0, 2)}/${digits.slice(2)}`;
    return `${digits.slice(0, 2)}/${digits.slice(2, 4)}/${digits.slice(4, 8)}`;
  };

  const handleDateInputMask = (event) => {
    const rawDigits = event.target.value.replace(/\D/g, "").slice(0, 8);
    event.target.value = formatDigitsToBrazilianDate(rawDigits);
  };

  const formatDigitsToTime = (digits) => {
    if (!digits) return "";
    const trimmed = digits.slice(0, 4);
    if (trimmed.length <= 2) return trimmed;
    return `${trimmed.slice(0, 2)}:${trimmed.slice(2)}`;
  };

  const handleTimeInputMask = (event) => {
    const rawDigits = event.target.value.replace(/\D/g, "");
    event.target.value = formatDigitsToTime(rawDigits);
  };

  const parseBrazilianDate = (value) => {
    const match = BRAZILIAN_DATE_REGEX.exec(value);
    if (!match) return null;

    const day = parseInt(match[1], 10);
    const month = parseInt(match[2], 10);
    const year = parseInt(match[3], 10);

    const parsedDate = new Date(year, month - 1, day);
    if (
      parsedDate.getFullYear() !== year ||
      parsedDate.getMonth() !== month - 1 ||
      parsedDate.getDate() !== day
    ) {
      return null;
    }

    return { day, month, year, dateObj: parsedDate };
  };

  const isDateBeforeToday = ({ year, month, day }) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const candidate = new Date(year, month - 1, day);
    candidate.setHours(0, 0, 0, 0);
    return candidate < today;
  };

  const validateDateField = (showFeedback = true) => {
    const value = dateInput.value.trim();
    if (!value) return null;

    const parsed = parseBrazilianDate(value);
    if (!parsed) {
      if (showFeedback) notify("error", "Use o formato DD/MM/AAAA para a data.");
      return null;
    }

    if (isDateBeforeToday(parsed)) {
      if (showFeedback) notify("error", "A data do evento não pode ser anterior à data de hoje.");
      return null;
    }

    return parsed;
  };

  const validateTimeField = (showFeedback = true) => {
    if (!timeInput) return null;
    const value = timeInput.value.trim();
    if (!value) return null;

    if (!BRAZILIAN_TIME_REGEX.test(value)) {
      if (showFeedback) {
        notify("error", "Use o formato HH:MM (00-23h) para o horário.");
      }
      return null;
    }

    return value;
  };

  const initMap = () => {
    if (!mapContainer) return;
    if (typeof L === "undefined") {
      notify("error", "Biblioteca de mapa não carregada.");
      return;
    }

    mapInstance = L.map(mapContainer, { zoomControl: false }).setView(
      [DEFAULT_COORDS.lat, DEFAULT_COORDS.lng],
      13
    );

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: "© OpenStreetMap contributors",
    }).addTo(mapInstance);

    mapInstance.on("click", (event) => {
      handleLocationSelection(event.latlng);
    });

    setTimeout(() => {
      mapInstance.invalidateSize();
    }, 250);
  };

  const ensureMarker = (latLng) => {
    if (!mapInstance) return;
    if (!markerInstance) {
      markerInstance = L.marker(latLng, { draggable: true }).addTo(mapInstance);
      markerInstance.on("dragend", async (event) => {
        const nextLatLng = event.target.getLatLng();
        await handleLocationSelection(nextLatLng, true);
      });
    } else {
      markerInstance.setLatLng(latLng);
    }
  };

  const handleLocationSelection = async (latLng, skipMarkerUpdate = false) => {
    if (!mapInstance || !latLng) return;

    if (!skipMarkerUpdate) {
      ensureMarker(latLng);
    }

    const lat = formatCoord(latLng.lat);
    const lng = formatCoord(latLng.lng);

    latitudeInput.value = lat;
    longitudeInput.value = lng;

    const resolvedAddress = await reverseGeocode(latLng.lat, latLng.lng);

    if (!resolvedAddress) {
      const fallback = `Lat ${lat}, Lng ${lng}`;
      if (locationInput) locationInput.value = fallback;
      if (streetInput) streetInput.value = fallback;
    }
  };

  const reverseGeocode = async (lat, lng) => {
    const params = new URLSearchParams({
      format: "jsonv2",
      lat: lat.toString(),
      lon: lng.toString(),
      addressdetails: "1",
      "accept-language": "pt-BR",
    });

    try {
      const response = await fetch(`https://nominatim.openstreetmap.org/reverse?${params.toString()}`, {
        headers: {
          Accept: "application/json",
        },
      });

      if (!response.ok) return null;

      const data = await response.json();
      const address = data.address || {};
      const displayAddress = data.display_name;

      if (locationInput && displayAddress) {
        locationInput.value = displayAddress;
      }

      const streetParts = [address.road, address.house_number].filter(Boolean);
      if (streetParts.length && streetInput) {
        streetInput.value = streetParts.join(", ");
      } else if (displayAddress && streetInput) {
        streetInput.value = displayAddress;
      }

      const resolvedCity =
        address.city ||
        address.town ||
        address.village ||
        address.municipality ||
        address.county;
      if (resolvedCity && cityInput) {
        cityInput.value = resolvedCity;
      }

      if (address.state && stateInput) {
        stateInput.value = address.state;
      }

      return displayAddress || null;
    } catch (error) {
      console.error("Erro ao buscar endereço do mapa:", error);
      return null;
    }
  };

  initMap();

  if (dateInput) {
    dateInput.addEventListener("input", handleDateInputMask);
    dateInput.addEventListener("blur", () => validateDateField(true));
  }

  if (timeInput) {
    timeInput.addEventListener("input", handleTimeInputMask);
    timeInput.addEventListener("blur", () => validateTimeField(true));
  }

  const resetImagePreview = () => {
    if (imagePreview) {
      imagePreview.src = "";
      imagePreview.style.display = "none";
    }
    if (imagePlaceholderIcon) imagePlaceholderIcon.style.display = "block";
    if (imagePlaceholderText) imagePlaceholderText.style.display = "block";
  };

  const showImagePreview = (file) => {
    if (!file) {
      resetImagePreview();
      return;
    }

    if (!file.type.startsWith("image/")) {
      notify("error", "Selecione um arquivo de imagem válido.");
      imageInput.value = "";
      resetImagePreview();
      return;
    }

    const reader = new FileReader();
    reader.onload = (event) => {
      if (!imagePreview) return;
      imagePreview.src = event.target.result;
      imagePreview.style.display = "block";
      if (imagePlaceholderIcon) imagePlaceholderIcon.style.display = "none";
      if (imagePlaceholderText) imagePlaceholderText.style.display = "none";
    };
    reader.readAsDataURL(file);
  };

  resetImagePreview();

  const setSubmitting = (isSubmitting) => {
    if (!submitBtn) return;
    submitBtn.disabled = isSubmitting;
    submitBtn.textContent = isSubmitting ? "Publicando..." : "PUBLICAR";
  };

  const buildEventInstant = (parsedDate, timeValue) => {
    if (!parsedDate || !timeValue) return null;

    const [hourStr, minuteStr] = timeValue.split(":");
    if (hourStr === undefined || minuteStr === undefined) return null;

    const hour = parseInt(hourStr, 10);
    const minute = parseInt(minuteStr, 10);

    if (Number.isNaN(hour) || Number.isNaN(minute)) return null;

    const { year, month, day } = parsedDate;
    const eventDate = new Date(year, month - 1, day, hour, minute);
    return eventDate.toISOString();
  };

  imageInput?.addEventListener("change", () => {
    const file = imageInput.files?.[0];
    if (!file) {
      resetImagePreview();
      return;
    }
    showImagePreview(file);
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const title = titleInput.value.trim();
    const description = descriptionInput.value.trim();
    const administrativeRegion = administrativeRegionInput.value.trim();
    const referencePoint = referencePointInput.value.trim();
    const city = cityInput.value.trim();
    const state = stateInput.value.trim();
    const latitude = latitudeInput.value.trim();
    const longitude = longitudeInput.value.trim();
    const streetAddress = streetInput.value.trim() || locationInput?.value.trim() || "";
    const dateValue = dateInput.value.trim();
    const parsedDate = parseBrazilianDate(dateValue);
    const validTimeValue = validateTimeField(false);
    const eventTime = parsedDate && validTimeValue ? buildEventInstant(parsedDate, validTimeValue) : null;

    const missingFields = [];
    if (!title) missingFields.push("título");
    if (!description) missingFields.push("descrição");
    if (!parsedDate) missingFields.push("data no formato DD/MM/AAAA");
    else if (isDateBeforeToday(parsedDate)) missingFields.push("data igual ou posterior a hoje");
    if (!validTimeValue) missingFields.push("horário no formato HH:MM");
    if (!latitude || !longitude) missingFields.push("localização no mapa");
    if (!streetAddress) missingFields.push("endereço do evento");
    if (!administrativeRegion) missingFields.push("região administrativa");
    if (!referencePoint) missingFields.push("ponto de referência");
    if (!city) missingFields.push("cidade");
    if (!state) missingFields.push("estado");

    if (missingFields.length) {
      notify("error", `Preencha ${missingFields.join(", ")}.`);
      return;
    }

    if (!window.apiService) {
      notify("error", "Serviço de API não encontrado.");
      return;
    }

    const formData = new FormData();
    formData.append("title", title);
    formData.append("eventTime", eventTime);
    formData.append("description", description);
    formData.append("streetAddress", streetAddress);
    formData.append("administrativeRegion", administrativeRegion);
    formData.append("city", city);
    formData.append("state", state);
    formData.append("referencePoint", referencePoint);
    formData.append("latitude", latitude);
    formData.append("longitude", longitude);

    if (locationInput && locationInput.value.trim()) {
      formData.append("location", locationInput.value.trim());
    }

    const imageFile = imageInput?.files?.[0];
    if (imageFile) {
      formData.append("image", imageFile);
    }

    setSubmitting(true);

    try {
      await apiService.createEvent(formData);
      notify("success", "Evento criado com sucesso!");
      form.reset();
      latitudeInput.value = "";
      longitudeInput.value = "";
      if (locationInput) locationInput.value = "";
      streetInput.value = "";
      resetImagePreview();
      if (markerInstance && mapInstance) {
        mapInstance.removeLayer(markerInstance);
        markerInstance = null;
      }
      if (mapInstance) {
        mapInstance.setView([DEFAULT_COORDS.lat, DEFAULT_COORDS.lng], 13);
      }
      setTimeout(() => {
        window.location.href = "eventList.html";
      }, 1200);
    } catch (error) {
      console.error("Erro ao criar evento:", error);
      notify("error", error.message || "Falha ao criar evento");
      setSubmitting(false);
    }
  });
});
