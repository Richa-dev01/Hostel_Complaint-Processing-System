const SERVER = "http://43.204.142.187:8080";
const API = SERVER + "/api/complaints";
const UPLOAD_API = SERVER + "/api/uploads";
const MAX_IMAGE_BYTES = 5 * 1024 * 1024;

const CATEGORIES = {
  1: "Electrical", 2: "Plumbing", 3: "Cleaning", 4: "Wi-Fi/Internet",
  5: "Furniture", 6: "Water supply", 7: "Maintenance", 8: "Other"
};

// ---- must be logged in as a student, otherwise go to the login page ----
const token = localStorage.getItem("token");
const role = localStorage.getItem("role");
if (!token || role !== "STUDENT") {
  window.location.replace("login.html");
}

document.getElementById("userName").textContent = localStorage.getItem("name") || "";
document.getElementById("logoutBtn").addEventListener("click", () => {
  localStorage.clear();
  window.location.replace("login.html");
});

function authHeaders() {
  return { "Content-Type": "application/json", "Authorization": "Bearer " + token };
}

// token missing, wrong or expired -> back to login
function sessionExpired(response) {
  if (response.status === 401) {
    localStorage.clear();
    window.location.replace("login.html");
    return true;
  }
  return false;
}

const form = document.getElementById("complaintForm");
const message = document.getElementById("message");
const rows = document.getElementById("complaintRows");
const emptyMsg = document.getElementById("emptyMsg");
const photoInput = document.getElementById("photo");

function showError(text) {
  message.className = "err";
  message.textContent = text;
}

// step 1 of a complaint with a photo: upload the image, get back its reference
async function uploadPhoto(file) {
  const formData = new FormData();
  formData.append("file", file);
  // no Content-Type header here: the browser sets it for file uploads
  const response = await fetch(UPLOAD_API, {
    method: "POST",
    headers: { "Authorization": "Bearer " + token },
    body: formData
  });
  if (sessionExpired(response)) return null;
  let data = {};
  try { data = await response.json(); } catch (e) { /* not JSON */ }
  if (!response.ok) throw new Error(data.message || "Image upload failed (" + response.status + ")");
  return data.imageUrl;
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  message.textContent = "";

  try {
    // 1. upload the photo first (if one was chosen)
    let imageUrl = null;
    const file = photoInput.files[0];
    if (file) {
      if (file.size > MAX_IMAGE_BYTES) {
        showError("The image must be 5 MB or smaller.");
        return;
      }
      imageUrl = await uploadPhoto(file);
      if (imageUrl === null) return;          // session expired, redirecting
    }

    // 2. submit the complaint with the image reference (not the image itself)
    // no studentId here: the server reads it from the login token
    const complaint = {
      categoryId: Number(document.getElementById("categoryId").value),
      roomNo: document.getElementById("roomNo").value,
      description: document.getElementById("description").value,
      priority: document.getElementById("priority").value,
      imageUrl: imageUrl
    };

    const response = await fetch(API, {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify(complaint)
    });
    if (sessionExpired(response)) return;
    if (!response.ok) throw new Error("Server returned " + response.status);

    const result = await response.json();
    message.className = "ok";
    message.textContent = result.message + " ID: " + result.complaintId;
    form.reset();
    loadComplaints();
  } catch (error) {
    showError("Could not submit complaint. " + error.message);
  }
});

async function loadComplaints() {
  rows.innerHTML = "";
  try {
    const response = await fetch(API, { headers: authHeaders() });
    if (sessionExpired(response)) return;
    const complaints = await response.json();

    complaints.sort((a, b) => (b.createdAt || "").localeCompare(a.createdAt || ""));
    emptyMsg.textContent = complaints.length === 0 ? "No complaints yet." : "";

    complaints.forEach((c) => {
      const tr = document.createElement("tr");
      const values = [
        c.complaintId, c.roomNo, CATEGORIES[c.categoryId] || c.categoryId,
        c.description, c.priority, c.status, c.assignedTo || "", c.resolutionRemarks || "",
        "",
        c.createdAt ? c.createdAt.replace("T", " ").substring(0, 16) : ""
      ];
      values.forEach((value, index) => {
        const td = document.createElement("td");
        if (index === 5) {                      // status column
          const span = document.createElement("span");
          span.className = "badge" + (value === "RESOLVED" ? " resolved" : "");
          span.textContent = value;
          td.appendChild(span);
        } else if (index === 8) {               // photo column
          if (c.imageUrl) {
            const link = document.createElement("a");
            link.href = SERVER + c.imageUrl;
            link.target = "_blank";
            link.rel = "noopener";
            link.textContent = "View";
            td.appendChild(link);
          }
        } else {
          td.textContent = value;               // textContent keeps user text safe
        }
        tr.appendChild(td);
      });
      rows.appendChild(tr);
    });
  } catch (error) {
    emptyMsg.textContent = "Cannot reach the API. Is it running?";
  }
}

document.getElementById("refreshBtn").addEventListener("click", loadComplaints);
loadComplaints();
