import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import axios from "axios";
import { API_BASE_URL } from "../services/api";

function RegisterPage() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handleRegister = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");
    setLoading(true);

    try {
      const response = await axios.post(
        `${API_BASE_URL}/api/auth/register`,
        {
          name: name,
          email: email,
          password: password,
        }
      );

      console.log("Registration response:", response.data);

      setSuccess("Account created successfully!");

      setTimeout(() => {
        navigate("/login");
      }, 1000);

    } catch (err) {
      console.error("Registration error:", err);

      if (err.response) {
        console.error("Backend response:", err.response.data);
        console.error("Status:", err.response.status);

        setError(
          err.response.data?.message ||
          "Registration failed."
        );
      } else {
        setError(
          "Cannot connect to the backend. Make sure Spring Boot is running."
        );
      }

    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">

        <h1>Create Account</h1>

        <p>
          Create your OptiAlloc account to get started
        </p>

        <form onSubmit={handleRegister}>

          <label htmlFor="name">
            Full Name
          </label>

          <input
            id="name"
            type="text"
            placeholder="Enter your name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />

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
            placeholder="Create a password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={6}
          />

          {error && (
            <div className="login-error">
              {error}
            </div>
          )}

          {success && (
            <div className="register-success">
              {success}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
          >
            {loading ? "Creating Account..." : "Create Account"}
          </button>

        </form>

        <div className="auth-switch">
          Already have an account?{" "}
          <Link to="/login">
            Sign In
          </Link>
        </div>

      </div>
    </div>
  );
}

export default RegisterPage;