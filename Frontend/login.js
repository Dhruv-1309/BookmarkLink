const API_BASE = "http://localhost:8080/api";
const AUTH_TOKEN_KEY = "bookmarklink.token";
const AUTH_REMEMBER_KEY = "bookmarklink.remember";
const AUTH_EMAIL_KEY = "bookmarklink.email";

const authContainer = document.getElementById("authContainer");
const signInBtn = document.getElementById("signIn");
const signUpBtn = document.getElementById("signUp");
const mobileSignInBtn = document.getElementById("mobileSignIn");
const mobileSignUpBtn = document.getElementById("mobileSignUp");

const loginForm = document.getElementById("loginForm");
const signupForm = document.getElementById("signupForm");
const authMessage = document.getElementById("authMessage");
const rememberMeInput = document.getElementById("rememberMe");

const showMessage = (message, isError = false) => {
  if (!authMessage) return;
  authMessage.textContent = message;
  authMessage.hidden = false;
  authMessage.classList.toggle("error", isError);
};

const clearMessage = () => {
  if (!authMessage) return;
  authMessage.hidden = true;
  authMessage.textContent = "";
  authMessage.classList.remove("error");
};

// Sliding animation logic
const showSignUp = () => {
  authContainer.classList.add("right-panel-active");
  clearMessage();
};

const showSignIn = () => {
  authContainer.classList.remove("right-panel-active");
  clearMessage();
};

if (signInBtn) signInBtn.addEventListener("click", showSignIn);
if (signUpBtn) signUpBtn.addEventListener("click", showSignUp);
if (mobileSignInBtn) {
  mobileSignInBtn.addEventListener("click", (e) => {
    e.preventDefault();
    showSignIn();
  });
}
if (mobileSignUpBtn) {
  mobileSignUpBtn.addEventListener("click", (e) => {
    e.preventDefault();
    showSignUp();
  });
}

const fetchJson = async (url, options = {}) => {
  const response = await fetch(url, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
    ...options,
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "Request failed");
  }

  return response.json();
};

const getStoredToken = () =>
  localStorage.getItem(AUTH_TOKEN_KEY) || sessionStorage.getItem(AUTH_TOKEN_KEY);

const storeToken = (token, remember) => {
  if (remember) {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
    sessionStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.setItem(AUTH_REMEMBER_KEY, "true");
  } else {
    sessionStorage.setItem(AUTH_TOKEN_KEY, token);
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(AUTH_REMEMBER_KEY);
  }
};

const storeEmail = (email, remember) => {
  if (remember && email) {
    localStorage.setItem(AUTH_EMAIL_KEY, email);
  } else {
    localStorage.removeItem(AUTH_EMAIL_KEY);
  }
};

const handleLogin = async (event) => {
  event.preventDefault();
  const formData = new FormData(loginForm);
  const remember = Boolean(rememberMeInput && rememberMeInput.checked);
  const payload = {
    email: formData.get("email").trim(),
    password: formData.get("password").trim(),
  };

  try {
    const data = await fetchJson(`${API_BASE}/auth/login`, {
      method: "POST",
      body: JSON.stringify(payload),
    });
    storeToken(data.token, remember);
    storeEmail(payload.email, remember);
    window.location.href = "index.html";
  } catch (error) {
    showMessage("Invalid email or password.", true);
  }
};

const handleSignup = async (event) => {
  event.preventDefault();
  const formData = new FormData(signupForm);
  const payload = {
    name: formData.get("name").trim(),
    email: formData.get("email").trim(),
    password: formData.get("password").trim(),
  };

  try {
    const data = await fetchJson(`${API_BASE}/auth/signup`, {
      method: "POST",
      body: JSON.stringify(payload),
    });
    localStorage.setItem(AUTH_TOKEN_KEY, data.token);
    localStorage.setItem(AUTH_REMEMBER_KEY, "true");
    localStorage.setItem(AUTH_EMAIL_KEY, payload.email);
    window.location.href = "index.html";
  } catch (error) {
    showMessage("Signup failed. Try a different email.", true);
  }
};

const storedEmail = localStorage.getItem(AUTH_EMAIL_KEY);
if (storedEmail) {
  const emailInput = loginForm?.querySelector("input[name='email']");
  if (emailInput) {
    emailInput.value = storedEmail;
  }
}

if (rememberMeInput) {
  rememberMeInput.checked = localStorage.getItem(AUTH_REMEMBER_KEY) === "true";
}

if (getStoredToken()) {
  window.location.href = "index.html";
}

loginForm.addEventListener("submit", handleLogin);
signupForm.addEventListener("submit", handleSignup);

