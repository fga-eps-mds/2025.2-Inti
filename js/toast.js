// Toast Notification System

class ToastManager {
  constructor() {
    this.container = null;
    this.toasts = [];
    this.init();
  }

  init() {
    // Create toast container if it doesn't exist
    if (!document.getElementById("toast-container")) {
      this.container = document.createElement("div");
      this.container.id = "toast-container";
      this.container.className = "toast-container";
      document.body.appendChild(this.container);
    } else {
      this.container = document.getElementById("toast-container");
    }
  }

  show(message, type = "info", duration = 3000) {
    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;

    // Create icon based on type
    const icon = this.getIcon(type);

    toast.innerHTML = `
      <div class="toast-icon">${icon}</div>
      <div class="toast-message">${message}</div>
      <button class="toast-close" aria-label="Fechar">&times;</button>
    `;

    // Add to container
    this.container.appendChild(toast);
    this.toasts.push(toast);

    // Trigger animation
    setTimeout(() => {
      toast.classList.add("toast-show");
    }, 10);

    // Setup close button
    const closeBtn = toast.querySelector(".toast-close");
    closeBtn.addEventListener("click", () => {
      this.hide(toast);
    });

    // Auto hide after duration
    if (duration > 0) {
      setTimeout(() => {
        this.hide(toast);
      }, duration);
    }

    return toast;
  }

  hide(toast) {
    toast.classList.remove("toast-show");
    toast.classList.add("toast-hide");

    setTimeout(() => {
      if (toast.parentNode) {
        toast.parentNode.removeChild(toast);
      }
      const index = this.toasts.indexOf(toast);
      if (index > -1) {
        this.toasts.splice(index, 1);
      }
    }, 300);
  }

  getIcon(type) {
    const icons = {
      success: "✓",
      error: "✕",
      warning: "⚠",
      info: "ℹ",
    };
    return icons[type] || icons.info;
  }

  success(message, duration = 3000) {
    return this.show(message, "success", duration);
  }

  error(message, duration = 4000) {
    return this.show(message, "error", duration);
  }

  warning(message, duration = 3500) {
    return this.show(message, "warning", duration);
  }

  info(message, duration = 3000) {
    return this.show(message, "info", duration);
  }
}

// Create global instance
const toastManager = new ToastManager();

// Export for use in other scripts
if (typeof window !== "undefined") {
  window.toast = {
    show: (message, type, duration) =>
      toastManager.show(message, type, duration),
    success: (message, duration) => toastManager.success(message, duration),
    error: (message, duration) => toastManager.error(message, duration),
    warning: (message, duration) => toastManager.warning(message, duration),
    info: (message, duration) => toastManager.info(message, duration),
  };
}
