import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import axios from "axios";

function LoginPage() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();

    setError("");
    setLoading(true);

    try {
      const response = await axios.post(
        "http://localhost:8080/api/auth/login",
        {
          email,
          password,
        }
      );

      const { token, role } = response.data;

      // Make sure backend returned the required data
      if (!token || !role) {
        setError("Invalid server response.");
        return;
      }

      // Store authentication information
      localStorage.setItem("token", token);
      localStorage.setItem("role", role);

      // Role-based navigation
      if (role === "ADMIN") {
        navigate("/admin");
      } else if (role === "USER") {
        navigate("/dashboard");
      } else {
        localStorage.removeItem("token");
        localStorage.removeItem("role");

        setError("Unknown user role.");
      }

    } catch (err) {
      console.error("Login error:", err);

      if (err.response?.status === 401) {
        setError("Invalid email or password.");
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError("Unable to login. Please try again.");
      }

    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">

      <div className="login-card">

        <h1>Welcome Back</h1>

        <p>
          Sign in to continue to OptiAlloc
        </p>

        <form onSubmit={handleLogin}>

          <label htmlFor="email">
            Email
          </label>

          <input
            id="email"
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <label htmlFor="password">
            Password
          </label>

          <input
            id="password"
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          {error && (
            <div className="login-error">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
          >
            {loading ? "Signing In..." : "Sign In"}
          </button>

        </form>
        <div className="auth-switch">
            Don't have an account?{" "}
                <Link to="/register">
                    Create Account
                </Link>
        </div>

        <div
          style={{
            marginTop: "22px",
            textAlign: "center",
            fontSize: "14px",
            color: "#64748b"
          }}
        >
          Don't have an account?{" "}
          <Link
            to="/register"
            style={{
              color: "#4f46e5",
              fontWeight: "600"
            }}
          >
            Create one
          </Link>
        </div>

      </div>

    </div>
  );
}

export default LoginPage;