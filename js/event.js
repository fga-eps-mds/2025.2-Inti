// Event management functionality

// Use the existing apiService from config.js

// Create a new event
async function createEvent(eventData) {
  try {
    const formData = new FormData();

    // Append all event data to form
    Object.keys(eventData).forEach((key) => {
      if (key === "image" && eventData[key]) {
        formData.append(key, eventData[key]);
      } else if (key !== "image") {
        formData.append(key, eventData[key]);
      }
    });

    await apiService.createEvent(formData);
    return { success: true };
  } catch (error) {
    console.error("Error creating event:", error);
    return { success: false, error: error.message };
  }
}

// Get all events
async function getEvents() {
  try {
    const events = await apiService.getEvents();
    return { success: true, events };
  } catch (error) {
    console.error("Error getting events:", error);
    return { success: false, error: error.message };
  }
}

// Export functions if in Node.js environment
if (typeof module !== "undefined" && module.exports) {
  module.exports = { createEvent, getEvents };
}
