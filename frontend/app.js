const API = "http://localhost:8080/api/complaints";

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

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  // no studentId here: the server reads it from the login token
  const complaint = {
    categoryId: Number(document.getElementById("categoryId").value),
    roomNo: document.getElementById("roomNo").value,
    description: document.getElementById("description").value,
    priority: document.getElementById("priority").value
  };

  try {
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
    message.className = "err";
    message.textContent = "Could not submit complaint. " + error.message;
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
        c.description, c.priority, c.status,
        c.createdAt ? c.createdAt.replace("T", " ").substring(0, 16) : ""
      ];
      values.forEach((value, index) => {
        const td = document.createElement("td");
        if (index === 5) {                      // status column
          const span = document.createElement("span");
          span.className = "badge";
          span.textContent = value;
          td.appendChild(span);
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
