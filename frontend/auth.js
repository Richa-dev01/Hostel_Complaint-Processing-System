const AUTH_API = "http://43.204.142.187:8080/api/auth";

// already logged in as a student? go straight to the dashboard
if (localStorage.getItem("token") && localStorage.getItem("role") === "STUDENT") {
  window.location.replace("index.html");
}

const loginCard = document.getElementById("loginCard");
const registerCard = document.getElementById("registerCard");

document.getElementById("showRegister").addEventListener("click", (e) => {
  e.preventDefault();
  loginCard.classList.add("hidden");
  registerCard.classList.remove("hidden");
});
document.getElementById("showLogin").addEventListener("click", (e) => {
  e.preventDefault();
  registerCard.classList.add("hidden");
  loginCard.classList.remove("hidden");
});

function showMessage(id, text, ok) {
  const el = document.getElementById(id);
  el.className = ok ? "ok" : "err";
  el.textContent = text;
}

document.getElementById("loginForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    const response = await fetch(AUTH_API + "/student/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: document.getElementById("loginEmail").value,
        password: document.getElementById("loginPassword").value
      })
    });
    const data = await response.json();
    if (!response.ok) {
      showMessage("loginMsg", data.message || "Login failed.", false);
      return;
    }
    localStorage.setItem("token", data.token);
    localStorage.setItem("name", data.name);
    localStorage.setItem("role", data.role);
    window.location.replace("index.html");
  } catch (error) {
    showMessage("loginMsg", "Cannot reach the server. Is the API running?", false);
  }
});

document.getElementById("registerForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const email = document.getElementById("regEmail").value;
  try {
    const response = await fetch(AUTH_API + "/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        name: document.getElementById("regName").value,
        email: email,
        roomNo: document.getElementById("regRoom").value,
        password: document.getElementById("regPassword").value
      })
    });
    const data = await response.json();
    if (!response.ok) {
      showMessage("registerMsg", data.message || "Registration failed.", false);
      return;
    }
    // success: switch to the login form with the email filled in
    registerCard.classList.add("hidden");
    loginCard.classList.remove("hidden");
    document.getElementById("loginEmail").value = email;
    showMessage("loginMsg", data.message, true);
  } catch (error) {
    showMessage("registerMsg", "Cannot reach the server. Is the API running?", false);
  }
});
