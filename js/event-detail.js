document.addEventListener("DOMContentLoaded", () => {
  const urlParams = new URLSearchParams(window.location.search);
  const eventId = urlParams.get("id");

  if (!eventId) {
    // console.warn('No Event ID provided');
    // return;
  }

  if (eventId) {
    loadEventDetails(eventId);
  }
});

async function loadEventDetails(eventId) {
  try {
    const token = localStorage.getItem("authToken") || "dummy-token";

    const response = await axios.get(`${CONFIG.API_URL}/event/${eventId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const event = response.data;
    renderEvent(event);
  } catch (error) {
    console.error("Error loading event:", error);
    if (error.response && error.response.status === 404) {
      alert("Event not found");
    } else {
      console.log("Using mock data for event");
      renderEvent(getMockEvent());
    }
  }
}

function renderEvent(event) {
  const eventImage = document.getElementById("event-image");
  const eventTitle = document.getElementById("event-title");
  const organizerImg = document.getElementById("organizer-img");
  const eventDesc = document.getElementById("event-description");
  const eventInfo = document.getElementById("event-info");
  const friendsList = document.getElementById("friends-list");

  if (event.imageUrl) {
    eventImage.src = event.imageUrl;
    eventImage.style.display = "block";
  }

  if (eventTitle) eventTitle.textContent = event.title;

  if (event.organizer && event.organizer.profilePictureUrl) {
    organizerImg.src = event.organizer.profilePictureUrl;
    organizerImg.style.display = "block";
  }

  if (eventDesc) eventDesc.textContent = event.description;

  if (eventInfo) {
    // Sempre formatar data e hora
    const date = new Date(event.date);
    const dateStr = date.toLocaleDateString("pt-BR", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
    const timeStr = date.toLocaleTimeString("pt-BR", {
      hour: "2-digit",
      minute: "2-digit",
    });

    eventInfo.innerHTML = `${dateStr}<br>${timeStr}<br>${event.price ? `R$ ${event.price}` : "Grátis"}<br>${event.location}`;
  }

  if (friendsList && event.friends) {
    friendsList.innerHTML = "";
    event.friends.slice(0, 3).forEach((friend) => {
      const friendDiv = document.createElement("div");
      friendDiv.className = "friend-avatar";
      if (friend.profilePictureUrl) {
        const img = document.createElement("img");
        img.src = friend.profilePictureUrl;
        friendDiv.appendChild(img);
      } else {
        friendDiv.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="#592e83" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`;
      }
      friendsList.appendChild(friendDiv);
    });

    if (event.friends.length > 3) {
      const moreSpan = document.createElement("span");
      moreSpan.className = "more-friends";
      moreSpan.textContent = `+${event.friends.length - 3}`;
      friendsList.appendChild(moreSpan);
    }
  }
}

function getMockEvent() {
  return {
    title: "HH UnB",
    description:
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt u",
    imageUrl: "https://via.placeholder.com/350x175",
    organizer: {
      profilePictureUrl: "https://via.placeholder.com/60",
    },
    date: new Date().toISOString(),
    price: 0,
    location: "Teatro ICC Sul",
    friends: [
      { profilePictureUrl: null },
      { profilePictureUrl: null },
      { profilePictureUrl: null },
      { profilePictureUrl: null },
    ],
  };
}
