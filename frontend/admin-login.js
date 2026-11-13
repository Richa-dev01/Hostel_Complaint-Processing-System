const ADMIN_LOGIN_API = "http://43.204.142.187:8080/api/auth/admin/login";

// already logged in as admin? go straight to the dashboard
if (localStorage.getItem("token") && localStorage.getItem("role") === "ADMIN") {
  window.location.replace("admin.html");
}

document.getElementById("adminLoginForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const msg = document.getElementById("adminMsg");
  try {
    const response = await fetch(ADMIN_LOGIN_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: document.getElementById("adminEmail").value,
        password: document.getElementById("adminPassword").value
      })
    });
    const data = await response.json();
    if (!response.ok) {
      msg.className = "err";
      msg.textContent = data.message || "Login failed.";
      return;
    }
    localStorage.setItem("token", data.token);
    localStorage.setItem("name", data.name);
    localStorage.setItem("role", data.role);
    window.location.replace("admin.html");
  } catch (error) {
    msg.className = "err";
    msg.textContent = "Cannot reach the server. Is the API running?";
  }
});
