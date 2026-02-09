const DAY_MS = 1000 * 60 * 60 * 24;
const HEALTH_DAYS = 45;
const API_BASE = "http://localhost:8080/api";
const AUTH_TOKEN_KEY = "bookmarklink.token";
const AUTH_REMEMBER_KEY = "bookmarklink.remember";
const AUTH_EMAIL_KEY = "bookmarklink.email";

const elements = {
  linkForm: document.getElementById("linkForm"),
  linksGrid: document.getElementById("linksGrid"),
  searchInput: document.getElementById("searchInput"),
  sortSelect: document.getElementById("sortSelect"),
  activeCount: document.getElementById("activeCount"),
  expiringCount: document.getElementById("expiringCount"),
  archivedCount: document.getElementById("archivedCount"),
  settingsDrawer: document.getElementById("settingsDrawer"),
  archiveDrawer: document.getElementById("archiveDrawer"),
  openSettings: document.getElementById("openSettings"),
  openArchive: document.getElementById("openArchive"),
  archiveList: document.getElementById("archiveList"),
  settingsForm: document.getElementById("settingsForm"),
  toast: document.getElementById("toast"),
  logoutButton: document.getElementById("logoutButton"),
};

const state = {
  links: [],
  settings: {
    email: "",
    emailEnabled: true,
    reminderWindow: 5,
  },
};

const getAuthToken = () =>
  localStorage.getItem(AUTH_TOKEN_KEY) ||
  sessionStorage.getItem(AUTH_TOKEN_KEY);

const requireAuth = () => {
  if (!getAuthToken()) {
    window.location.href = "login.html";
  }
};

const fetchJson = async (url, options = {}) => {
  const token = getAuthToken();
  const response = await fetch(url, {
    headers: {
      "Content-Type": "application/json",
      ...(token ? { "X-Auth-Token": token } : {}),
      ...(options.headers || {}),
    },
    ...options,
  });

  if (response.status === 401) {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    sessionStorage.removeItem(AUTH_TOKEN_KEY);
    window.location.href = "login.html";
    throw new Error("Unauthorized");
  }

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "Request failed");
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
};

const api = {
  getLinks: () => fetchJson(`${API_BASE}/links`),
  addLink: (payload) =>
    fetchJson(`${API_BASE}/links`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),
  renewLink: (id) =>
    fetchJson(`${API_BASE}/links/${id}/renew`, { method: "PUT" }),
  archiveLink: (id) =>
    fetchJson(`${API_BASE}/links/${id}/archive`, { method: "PUT" }),
  restoreLink: (id) =>
    fetchJson(`${API_BASE}/links/${id}/restore`, { method: "PUT" }),
  deleteLink: (id) =>
    fetchJson(`${API_BASE}/links/${id}`, { method: "DELETE" }),
  getSettings: () => fetchJson(`${API_BASE}/settings`),
  updateSettings: (payload) =>
    fetchJson(`${API_BASE}/settings`, {
      method: "PUT",
      body: JSON.stringify(payload),
    }),
};

const toTimestamp = (value) => {
  if (!value) return null;
  if (typeof value === "number") return value;
  return new Date(value).getTime();
};

const getHealth = (link) => {
  const base = toTimestamp(link.renewedAt) || toTimestamp(link.createdAt);
  const age = Math.floor((Date.now() - base) / DAY_MS);
  const daysLeft = Math.max(HEALTH_DAYS - age, 0);
  const percentage = Math.max((daysLeft / HEALTH_DAYS) * 100, 0);
  return { daysLeft, percentage, age };
};

const getHealthLabel = (daysLeft) => {
  if (daysLeft === 0) return { label: "Expired", className: "danger" };
  if (daysLeft <= 7) return { label: "Expiring", className: "warning" };
  return { label: "Healthy", className: "good" };
};

const formatDate = (timestamp) =>
  new Date(toTimestamp(timestamp)).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });

const render = () => {
  const searchTerm = elements.searchInput.value.toLowerCase();
  const sort = elements.sortSelect.value;
  const activeLinks = state.links.filter((link) => link.status === "active");
  const archiveLinks = state.links.filter((link) => link.status === "archived");

  const filtered = activeLinks.filter((link) => {
    const text =
      `${link.title} ${link.url} ${link.tags.join(" ")}`.toLowerCase();
    return text.includes(searchTerm);
  });

  const sorted = [...filtered].sort((a, b) => {
    if (sort === "recent") return b.createdAt - a.createdAt;
    if (sort === "title") return a.title.localeCompare(b.title);
    const healthA = getHealth(a).daysLeft;
    const healthB = getHealth(b).daysLeft;
    return healthA - healthB;
  });

  elements.linksGrid.innerHTML = sorted
    .map((link, index) => buildLinkCard(link, index))
    .join("");

  elements.archiveList.innerHTML = archiveLinks
    .map((link, index) => buildArchiveCard(link, index))
    .join("");

  const expiringSoon = activeLinks.filter(
    (link) => getHealth(link).daysLeft <= 7,
  ).length;

  elements.activeCount.textContent = activeLinks.length;
  elements.expiringCount.textContent = expiringSoon;
  elements.archivedCount.textContent = archiveLinks.length;

  renderSettings();

  if (!sorted.length) {
    elements.linksGrid.innerHTML =
      "<div class='card'>No links found. Try a different search.</div>";
  }
  if (!archiveLinks.length) {
    elements.archiveList.innerHTML =
      "<div class='card'>Your archive is empty.</div>";
  }
};

const buildLinkCard = (link, index = 0) => {
  const { daysLeft, percentage } = getHealth(link);
  const badge = getHealthLabel(daysLeft);
  const nextNotice =
    daysLeft === 0 ? "Expired — move to archive" : `${daysLeft} days left`;

  const delay = index * 50; // stagger 50ms per item

  return `
    <article class="link-card" style="animation-delay: ${delay}ms">
      <div class="link-header">
        <div class="link-content">
          <p class="link-title">${link.title}</p>
          <a href="${link.url}" target="_blank" rel="noopener" class="muted link-url">${link.url}</a>
        </div>
        <span class="badge ${badge.className}">${badge.label}</span>
      </div>
      <div class="progress">
        <span style="width: ${percentage}%"></span>
      </div>
      <div class="meta">
        <span>Saved ${formatDate(link.createdAt)}</span>
        <span class="dot"></span>
        <span>${nextNotice}</span>
      </div>
      <div class="tags">
        ${link.tags.map((tag) => `<span class="tag">${tag}</span>`).join("")}
      </div>
      ${link.notes ? `<p class="muted">${link.notes}</p>` : ""}
      <div class="card-actions">
        <button class="ghost" data-action="renew" data-id="${link.id}">Renew 45 days</button>
        <button class="ghost" data-action="archive" data-id="${link.id}">Archive</button>
        <button class="ghost" data-action="delete" data-id="${link.id}">Delete</button>
      </div>
    </article>
  `;
};

const buildArchiveCard = (link, index = 0) => {
  const { daysLeft } = getHealth(link);
  const badge = getHealthLabel(daysLeft);
  const delay = index * 50;

  return `
    <div class="card" style="animation: fadeIn 0.5s ease-out backwards; animation-delay: ${delay}ms">
      <div class="link-header">
        <div class="link-content">
          <p class="link-title">${link.title}</p>
          <a href="${link.url}" target="_blank" rel="noopener" class="muted link-url">${link.url}</a>
        </div>
        <span class="badge ${badge.className}">${badge.label}</span>
      </div>
      <p class="muted">Archived on ${formatDate(link.archivedAt || link.createdAt)}</p>
      <div class="card-actions">
        <button class="ghost" data-action="restore" data-id="${link.id}">Renew & restore</button>
        <button class="ghost" data-action="delete" data-id="${link.id}">Delete</button>
      </div>
    </div>
  `;
};

const addLink = async (event) => {
  event.preventDefault();
  const formData = new FormData(elements.linkForm);
  const title = formData.get("title").trim();
  const url = formData.get("url").trim();
  const tags = formData
    .get("tags")
    .split(",")
    .map((tag) => tag.trim())
    .filter(Boolean);
  const notes = formData.get("notes").trim();

  try {
    const newLink = await api.addLink({ title, url, tags, notes });
    state.links.unshift(newLink);
    elements.linkForm.reset();
    showToast("Link saved with a 45‑day health window.");
    render();
  } catch (error) {
    showToast("Unable to save link. Is the backend running?");
  }
};

const updateLinkStatus = async (actionId, action) => {
  const link = state.links.find((item) => item.id === actionId);
  if (!link) return;

  try {
    if (action === "renew") {
      const updated = await api.renewLink(actionId);
      replaceLink(updated);
      showToast("Link renewed for another 45 days.");
    }
    if (action === "archive") {
      const updated = await api.archiveLink(actionId);
      replaceLink(updated);
      showToast("Link moved to archive.");
    }
    if (action === "restore") {
      const updated = await api.restoreLink(actionId);
      replaceLink(updated);
      showToast("Link restored and renewed.");
    }
    if (action === "delete") {
      const confirmed = window.confirm("Delete this link permanently?");
      if (!confirmed) {
        return;
      }
      await api.deleteLink(actionId);
      state.links = state.links.filter((item) => item.id !== actionId);
      showToast("Link deleted permanently.");
    }
    render();
  } catch (error) {
    showToast("Unable to update link. Is the backend running?");
  }
};

const renderSettings = () => {
  elements.settingsForm.email.value = state.settings.email;
  elements.settingsForm.emailEnabled.checked = state.settings.emailEnabled;
  elements.settingsForm.reminderWindow.value = state.settings.reminderWindow;
};

const saveSettings = async (event) => {
  event.preventDefault();
  const formData = new FormData(elements.settingsForm);
  const payload = {
    email: formData.get("email").trim(),
    emailEnabled: formData.get("emailEnabled") === "on",
    reminderWindow: Number(formData.get("reminderWindow")) || 5,
  };
  try {
    state.settings = await api.updateSettings(payload);
    showToast("Settings saved to backend.");
  } catch (error) {
    showToast("Unable to save settings. Is the backend running?");
  }
};

const showToast = (message) => {
  elements.toast.textContent = message;
  elements.toast.classList.add("show");
  clearTimeout(showToast.timeout);
  showToast.timeout = setTimeout(() => {
    elements.toast.classList.remove("show");
  }, 2400);
};

const toggleDrawer = (drawer, open) => {
  drawer.classList.toggle("open", open);
  drawer.setAttribute("aria-hidden", String(!open));
};

const replaceLink = (updated) => {
  if (!updated) return;
  const index = state.links.findIndex((item) => item.id === updated.id);
  if (index === -1) {
    state.links.unshift(updated);
  } else {
    state.links[index] = updated;
  }
};

const refreshExpired = async () => {
  const expired = state.links.filter(
    (link) => link.status === "active" && getHealth(link).daysLeft === 0,
  );
  if (!expired.length) return;

  let moved = 0;
  for (const link of expired) {
    try {
      const updated = await api.archiveLink(link.id);
      replaceLink(updated);
      moved += 1;
    } catch (error) {
      // ignore individual failures
    }
  }

  if (moved) {
    showToast(`${moved} link(s) moved to archive after expiring.`);
    render();
  }
};

const boot = async () => {
  requireAuth();
  try {
    const [links, settings] = await Promise.all([
      api.getLinks(),
      api.getSettings(),
    ]);
    state.links = links || [];
    state.settings = { ...state.settings, ...(settings || {}) };
    render();
    await refreshExpired();
  } catch (error) {
    showToast("Unable to reach backend. Start the Spring Boot server.");
  }
};

boot();

setInterval(() => {
  refreshExpired();
}, DAY_MS / 24);

elements.linkForm.addEventListener("submit", addLink);

const handleLogout = () => {
  localStorage.removeItem(AUTH_TOKEN_KEY);
  sessionStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_REMEMBER_KEY);
  localStorage.removeItem(AUTH_EMAIL_KEY);
  window.location.href = "login.html";
};

const handleCardActions = (event) => {
  const button = event.target.closest("button");
  if (!button) return;
  const action = button.dataset.action;
  const id = button.dataset.id;
  if (!action || !id) return;
  updateLinkStatus(id, action);
};

elements.linksGrid.addEventListener("click", handleCardActions);

// archive list click handler
elements.archiveList.addEventListener("click", handleCardActions);

elements.searchInput.addEventListener("input", render);

// sort selection
elements.sortSelect.addEventListener("change", render);

// settings save
elements.settingsForm.addEventListener("submit", saveSettings);

if (elements.logoutButton) {
  elements.logoutButton.addEventListener("click", handleLogout);
}

// drawer toggles
// open settings
// open archive
// close buttons

// open settings
// open archive

// open drawer handlers

// simple wiring

// open settings
// open archive

// final wiring

elements.openSettings.addEventListener("click", () =>
  toggleDrawer(elements.settingsDrawer, true),
);

elements.openArchive.addEventListener("click", () =>
  toggleDrawer(elements.archiveDrawer, true),
);

document.querySelectorAll("[data-close]").forEach((button) => {
  button.addEventListener("click", () => {
    const target = button.dataset.close;
    if (target === "settings") {
      toggleDrawer(elements.settingsDrawer, false);
    }
    if (target === "archive") {
      toggleDrawer(elements.archiveDrawer, false);
    }
  });
});

// close drawer on backdrop click
[elements.settingsDrawer, elements.archiveDrawer].forEach((drawer) => {
  drawer.addEventListener("click", (event) => {
    if (event.target === drawer) {
      toggleDrawer(drawer, false);
    }
  });
});
