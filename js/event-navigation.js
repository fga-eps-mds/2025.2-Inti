(function () {
  function buildEventDetailUrl(eventId) {
    if (!eventId) {
      return null;
    }
    const isInPagesDir = window.location.pathname.includes("/pages/");
    const basePath = isInPagesDir
      ? "./event-detail.html"
      : "pages/event-detail.html";
    return `${basePath}?id=${encodeURIComponent(eventId)}`;
  }

  function goToEventDetail(eventId) {
    const targetUrl = buildEventDetailUrl(eventId);
    if (targetUrl) {
      window.location.href = targetUrl;
    }
  }

  window.EventNavigation = {
    buildEventDetailUrl,
    goToEventDetail,
  };
})();
