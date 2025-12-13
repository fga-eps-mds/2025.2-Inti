// API Configuration
const API_CONFIG = {
  baseURL: "https://20252-inti-production.up.railway.app",
  token: null,
};

// Centralized API Service
class ApiService {
  constructor() {
    this.baseURL = API_CONFIG.baseURL;
    this.token = localStorage.getItem("authToken");
    console.log("ApiService initialized with token:", this.token);
  }

  // Set authentication token
  setAuthToken(token) {
    console.log("Setting auth token:", token);
    this.token = token;
    localStorage.setItem("authToken", token);
  }

  // Clear authentication token
  clearAuthToken() {
    this.token = null;
    localStorage.removeItem("authToken");
  }

  // Get authentication headers
  getAuthHeaders() {
    console.log("Getting auth headers, token:", this.token);
    return this.token
      ? {
          Authorization: `Bearer ${this.token}`,
          "Content-Type": "application/json",
        }
      : { "Content-Type": "application/json" };
  }

  // Generic request method
  async request(endpoint, options = {}, isTextResponse = false) {
    const url = `${this.baseURL}${endpoint}`;
    const config = {
      headers: this.getAuthHeaders(),
      ...options,
    };

    console.log("Making request to:", url);
    console.log("Request config:", config);

    try {
      const response = await fetch(url, config);

      console.log("Response status:", response.status);
      console.log("Response headers:", [...response.headers.entries()]);

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        console.log("Error data:", errorData);
        throw new Error(errorData.message || `HTTP Error: ${response.status}`);
      }

      const clonedResponse = response.clone();
      const rawText = await clonedResponse.text().catch(() => "");

      // Handle 204 No Content or any empty body
      if (response.status === 204 || !rawText || rawText.trim() === "") {
        return null;
      }

      // Handle text responses (e.g., for login endpoint returning JWT token)
      if (isTextResponse) {
        return rawText;
      }

      const contentType = response.headers.get("content-type");

      // Default: parse as JSON when possible, fallback to raw text
      if (contentType && contentType.includes("application/json")) {
        try {
          return JSON.parse(rawText);
        } catch (parseError) {
          console.warn("Failed to parse JSON response:", parseError);
          return null;
        }
      }

      return rawText;
    } catch (error) {
      console.error(`API Error (${endpoint}):`, error);
      throw error;
    }
  }

  // Authentication endpoints
  async register(userData) {
    return this.request("/auth/register", {
      method: "POST",
      body: JSON.stringify(userData),
    }); // Returns JSON with jwt and profile data
  }

  async login(credentials) {
    const response = await this.request("/auth/login", {
      method: "POST",
      body: JSON.stringify(credentials),
    });

    if (!response || !response.jwt) {
      console.error("Invalid login response:", response);
      throw new Error("Token não encontrado na resposta de login.");
    }

    this.setAuthToken(response.jwt);
    return response;
  }

  // Profile endpoints
  async getMyProfile(page = 0, size = 10) {
    return this.request(`/profile/me?page=${page}&size=${size}`);
  }

  async getPublicProfile(username) {
    // New backend search endpoint for public profiles
    return this.request(`/search/${username}`);
  }

  async searchProfiles(query, limit = 5, signal) {
    if (!query || typeof query !== "string") {
      throw new Error("Termo de busca é obrigatório.");
    }

    const safeLimit = Number.isFinite(limit)
      ? Math.min(Math.max(Math.floor(limit), 1), 20)
      : 5;

    const params = new URLSearchParams({
      query: query.trim(),
      limit: String(safeLimit),
    });

    return this.request(`/profile/search?${params.toString()}`, {
      signal,
    });
  }

  async updateProfile(formData) {
    // For multipart/form-data, we don't set Content-Type header
    // Browser will set it with boundary automatically
    const headers = this.token ? { Authorization: `Bearer ${this.token}` } : {};

    return this.request("/profile/update", {
      method: "PATCH",
      headers,
      body: formData,
    });
  }

  async uploadProfilePicture(formData) {
    const headers = this.token ? { Authorization: `Bearer ${this.token}` } : {};

    return this.request("/profile/upload-me", {
      method: "POST",
      headers,
      body: formData,
    });
  }

  async followUser(username) {
    return this.request(`/profile/${username}/follow`, {
      method: "POST",
    });
  }

  async unfollowUser(username) {
    return this.request(`/profile/${username}/unfollow`, {
      method: "DELETE",
    });
  }

  // Post endpoints
  async createPost(formData) {
    const headers = this.token ? { Authorization: `Bearer ${this.token}` } : {};

    return this.request("/post", {
      method: "POST",
      headers,
      body: formData,
    });
  }

  async createProduct(formData) {
    const headers = this.token ? { Authorization: `Bearer ${this.token}` } : {};

    return this.request("/products", {
      method: "POST",
      headers,
      body: formData,
    });
  }

  async deletePost(postId) {
    return this.request(`/post/${postId}`, {
      method: "DELETE",
    });
  }

  async getPostDetail(postId) {
    console.log("Getting post detail for postId:", postId);
    return this.request(`/post/${postId}`);
  }

  async likePost(postId) {
    return this.request(`/post/${postId}/like`, {
      method: "POST",
    });
  }

  async unlikePost(postId) {
    return this.request(`/post/${postId}/like`, {
      method: "DELETE",
    });
  }

  // Feed endpoints
  async getFeed(page = 0, size = 20) {
    return this.request(`/feed?page=${page}&size=${size}`);
  }

  // Event endpoints
  async createEvent(formData) {
    const headers = this.token ? { Authorization: `Bearer ${this.token}` } : {};

    return this.request("/event", {
      method: "POST",
      headers,
      body: formData,
    });
  }

  async getEvents() {
    return this.request("/event/lists");
  }

  async getMyEvents() {
    return this.request("/event/my");
  }

  async getMyProducts(page = 0, size = 10) {
    return this.request(`/products?page=${page}&size=${size}`);
  }

  async getProductsByProfile(profileId) {
    if (!profileId) {
      throw new Error("ID do perfil é obrigatório para carregar produtos.");
    }

    return this.request(`/products/profile/${profileId}`);
  }

  async getEventDetail(eventId) {
    return this.request(`/event/${eventId}`);
  }

  async getOrganizationEvents() {
    return this.request("/org/events");
  }

  async getOrganizationEventsByUsername(username) {
    if (!username) {
      throw new Error("Username é obrigatório para buscar eventos.");
    }
    return this.request(`/org/${encodeURIComponent(username)}/events`);
  }

  async attendEvent(eventId) {
    return this.request(`/event/${eventId}/attendees`, {
      method: "POST",
    });
  }

  async cancelEventAttendance(eventId) {
    return this.request(`/event/${eventId}/attendees`, {
      method: "DELETE",
    });
  }

  async getEventAttendees(eventId) {
    return this.request(`/event/following?eventId=${eventId}`);
  }
}

// Export singleton instance
const apiService = new ApiService();

// Make apiService globally available
if (typeof window !== "undefined") {
  window.apiService = apiService;
}
