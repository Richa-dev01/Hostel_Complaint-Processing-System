const API = "http://43.204.142.187:8080/api/admin/complaints";

const CATEGORIES = {
  1: "Electrical", 2: "Plumbing", 3: "Cleaning", 4: "Wi-Fi/Internet",
  5: "Furniture", 6: "Water supply", 7: "Maintenance", 8: "Other"
};

// ---- must be logged in as admin, otherwise go to the admin login page ----
const token = localStorage.getItem("token");
if (!token || localStorage.getItem("role") !== "ADMIN") {
  window.location.replace("admin-login.html");
}

document.getElementById("userName").textContent = localStorage.getItem("name") || "";
document.getElementById("logoutBtn").addEventListener("click", () => {
  localStorage.clear();
  window.location.replace("admin-login.html");
});

function authHeaders() {
  return { "Content-Type": "application/json", "Authorization": "Bearer " + token };
}

// token missing/expired (401) or not an admin (403) -> back to login
function sessionExpired(response) {
  if (response.status === 401 || response.status === 403) {
    localStorage.clear();
    window.location.replace("admin-login.html");
    return true;
  }
  return false;
}

const rows = document.getElementById("complaintRows");
const emptyMsg = document.getElementById("emptyMsg");
const panel = document.getElementById("managePanel");
const panelMsg = document.getElementById("panelMsg");
let currentId = null;

async function fetchList(queryString) {
  const url = API + (queryString ? "?" + queryString : "");
  const response = await fetch(url, { headers: authHeaders() });
  if (sessionExpired(response)) return null;
  if (!response.ok) throw new Error("Server returned " + response.status);
  return response.json();
}

async function loadComplaints() {
  rows.innerHTML = "";
  try {
    // all complaints: used for the summary boxes
    const all = await fetchList("");
    if (!all) return;
    document.getElementById("sumTotal").textContent = all.length;
    document.getElementById("sumPending").textContent = all.filter(c => c.status !== "RESOLVED").length;
    document.getElementById("sumResolved").textContent = all.filter(c => c.status === "RESOLVED").length;

    // filtered list: the server does the filtering
    const params = new URLSearchParams();
    const q = document.getElementById("fSearch").value.trim();
    if (q) params.set("q", q);
    if (document.getElementById("fStatus").value) params.set("status", document.getElementById("fStatus").value);
    if (document.getElementById("fCategory").value) params.set("categoryId", document.getElementById("fCategory").value);
    if (document.getElementById("fPriority").value) params.set("priority", document.getElementById("fPriority").value);
    const queryString = params.toString();

    const list = queryString ? await fetchList(queryString) : all;
    if (!list) return;

    emptyMsg.textContent = list.length === 0 ? "No complaints match." : "";
    list.forEach(renderRow);
  } catch (error) {
    emptyMsg.textContent = "Cannot reach the API. Is it running?";
  }
}

function renderRow(c) {
  const tr = document.createElement("tr");
  const values = [
    c.complaintId, c.studentId, c.roomNo, CATEGORIES[c.categoryId] || c.categoryId,
    c.description, c.priority, c.status, c.assignedTo || "",
    c.createdAt ? c.createdAt.replace("T", " ").substring(0, 16) : ""
  ];
  values.forEach((value, index) => {
    const td = document.createElement("td");
    if (index === 6) {                          // status column
      const span = document.createElement("span");
      span.className = "badge" + (value === "RESOLVED" ? " resolved" : "");
      span.textContent = value;
      td.appendChild(span);
    } else {
      td.textContent = value;
    }
    tr.appendChild(td);
  });

  const actionTd = document.createElement("td");
  const manageBtn = document.createElement("button");
  manageBtn.type = "button";
  manageBtn.textContent = "Manage";
  manageBtn.addEventListener("click", () => openPanel(c));
  actionTd.appendChild(manageBtn);
  tr.appendChild(actionTd);

  rows.appendChild(tr);
}

function openPanel(c) {
  currentId = c.complaintId;
  panelMsg.textContent = "";
  document.getElementById("panelId").textContent = c.complaintId;
  document.getElementById("panelInfo").textContent =
    "Room " + c.roomNo + " | " + (CATEGORIES[c.categoryId] || c.categoryId) +
    " | " + c.priority + " priority | " + c.description;
  document.getElementById("mStatus").value = c.status;
  document.getElementById("mAssigned").value = c.assignedTo || "";
  document.getElementById("mRemarks").value = c.resolutionRemarks || "";
  const photoBox = document.getElementById("panelPhoto");
  photoBox.textContent = "";
  if (c.imageUrl) {
    const img = document.createElement("img");
    img.src = "http://43.204.142.187:8080" + c.imageUrl;
    img.className = "evidence";
    photoBox.appendChild(img);
  }
  panel.hidden = false;
  panel.scrollIntoView({ behavior: "smooth" });
}

function closePanel() {
  panel.hidden = true;
  currentId = null;
}

function showPanelMessage(text, ok) {
  panelMsg.className = ok ? "ok" : "err";
  panelMsg.textContent = text;
}

document.getElementById("manageForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!currentId) return;
  try {
    const response = await fetch(API + "/" + encodeURIComponent(currentId) + "/status", {
      method: "PUT",
      headers: authHeaders(),
      body: JSON.stringify({
        status: document.getElementById("mStatus").value,
        assignedTo: document.getElementById("mAssigned").value,
        remarks: document.getElementById("mRemarks").value
      })
    });
    if (sessionExpired(response)) return;
    const data = await response.json();
    if (!response.ok) {
      showPanelMessage(data.message || "Update failed.", false);
      return;
    }
    showPanelMessage("Saved.", true);
    loadComplaints();
  } catch (error) {
    showPanelMessage("Cannot reach the API. Is it running?", false);
  }
});

document.getElementById("deleteBtn").addEventListener("click", async () => {
  if (!currentId) return;
  if (!confirm("Delete complaint " + currentId + "? This cannot be undone.")) return;
  try {
    const response = await fetch(API + "/" + encodeURIComponent(currentId), {
      method: "DELETE",
      headers: authHeaders()
    });
    if (sessionExpired(response)) return;
    const data = await response.json();
    if (!response.ok) {
      showPanelMessage(data.message || "Delete failed.", false);
      return;
    }
    closePanel();
    loadComplaints();
  } catch (error) {
    showPanelMessage("Cannot reach the API. Is it running?", false);
  }
});

document.getElementById("closeBtn").addEventListener("click", closePanel);
document.getElementById("applyBtn").addEventListener("click", loadComplaints);
document.getElementById("clearBtn").addEventListener("click", () => {
  document.getElementById("fSearch").value = "";
  document.getElementById("fStatus").value = "";
  document.getElementById("fCategory").value = "";
  document.getElementById("fPriority").value = "";
  loadComplaints();
});
document.getElementById("fSearch").addEventListener("keydown", (e) => {
  if (e.key === "Enter") loadComplaints();
});

loadComplaints();
