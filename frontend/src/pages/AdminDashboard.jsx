import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../services/api";

function AdminDashboard() {
  const navigate = useNavigate();

  const [stats, setStats] = useState({
    totalResources: 0,
    availableResources: 0,
    maintenanceResources: 0,
    inactiveResources: 0,
    totalRequests: 0,
    allocatedRequests: 0,
    pendingRequests: 0,
    conflictRequests: 0,
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const token = localStorage.getItem("token");

  const fetchStats = async () => {
    if (!token) {
      navigate("/login");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await fetch(`${API_BASE_URL}/api/admin/stats`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        navigate("/login");
        return;
      }

      if (!response.ok) {
        throw new Error(`Failed to load admin statistics (Status: ${response.status})`);
      }

      const data = await response.json();
      setStats(data);
    } catch (err) {
      console.error("Error loading admin stats:", err);
      setError(err.message || "Failed to load admin statistics.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    navigate("/login");
  };

  return (
    <div className="dashboard-page">
      {/* Navbar */}
      <header className="dashboard-navbar">
        <div className="dashboard-logo" onClick={() => navigate("/admin")} style={{ cursor: "pointer" }}>
          <span className="logo-mark">O</span>
          <span>OptiAlloc</span>
        </div>

        <div className="dashboard-nav-right">
          <button className="text-btn active" onClick={() => navigate("/admin")}>
            Dashboard
          </button>
          <button className="text-btn" onClick={() => navigate("/admin/resources")}>
            Resources
          </button>
          <button className="text-btn" onClick={() => navigate("/admin/requests")}>
            Requests
          </button>
          <span className="user-role" style={{ background: "#e0e7ff", color: "#4338ca" }}>
            ADMIN
          </span>
          <button className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="dashboard-content">
        <section className="dashboard-welcome">
          <div>
            <p className="dashboard-label">ADMINISTRATION CONTROL PANEL</p>
            <h1>System Overview</h1>
            <p className="dashboard-subtitle">
              Monitor resources, request allocations, and operational metrics in real-time.
            </p>
          </div>

          <div style={{ display: "flex", gap: "12px" }}>
            <button className="primary-action" onClick={() => navigate("/admin/resources")}>
              Manage Resources
            </button>
            <button className="secondary-action" onClick={() => navigate("/admin/requests")}>
              Review Requests
            </button>
          </div>
        </section>

        {error && (
          <div className="request-error" style={{ marginBottom: "24px" }}>
            {error}
            <button
              onClick={fetchStats}
              style={{
                marginLeft: "12px",
                background: "none",
                border: "none",
                color: "#b91c1c",
                cursor: "pointer",
                fontWeight: "600",
              }}
            >
              Retry
            </button>
          </div>
        )}

        {/* Resources Metrics Section */}
        <h2 style={{ fontSize: "18px", fontWeight: "700", color: "#1e293b", marginBottom: "16px" }}>
          Resource Metrics
        </h2>

        <div className="stats-grid" style={{ marginBottom: "32px" }}>
          <div className="stat-card">
            <span className="stat-label">Total Resources</span>
            <span className="stat-value">{loading ? "..." : stats.totalResources}</span>
            <span className="stat-hint">Registered system assets</span>
          </div>

          <div className="stat-card">
            <span className="stat-label">Available</span>
            <span className="stat-value" style={{ color: "#16a34a" }}>
              {loading ? "..." : stats.availableResources}
            </span>
            <span className="stat-hint">Ready for allocation</span>
          </div>

          <div className="stat-card">
            <span className="stat-label">In Maintenance</span>
            <span className="stat-value" style={{ color: "#d97706" }}>
              {loading ? "..." : stats.maintenanceResources}
            </span>
            <span className="stat-hint">Service / repair lock</span>
          </div>

          <div className="stat-card">
            <span className="stat-label">Inactive</span>
            <span className="stat-value" style={{ color: "#dc2626" }}>
              {loading ? "..." : stats.inactiveResources}
            </span>
            <span className="stat-hint">Decommissioned / offline</span>
          </div>
        </div>

        {/* Requests Metrics Section */}
        <h2 style={{ fontSize: "18px", fontWeight: "700", color: "#1e293b", marginBottom: "16px" }}>
          Allocation Request Metrics
        </h2>

        <div className="stats-grid" style={{ marginBottom: "32px" }}>
          <div className="stat-card">
            <span className="stat-label">Total Requests</span>
            <span className="stat-value">{loading ? "..." : stats.totalRequests}</span>
            <span className="stat-hint">User submissions</span>
          </div>

          <div className="stat-card">
            <span className="stat-label">Allocated</span>
            <span className="stat-value" style={{ color: "#16a34a" }}>
              {loading ? "..." : stats.allocatedRequests}
            </span>
            <span className="stat-hint">Successfully assigned</span>
          </div>

          <div className="stat-card">
            <span className="stat-label">Pending</span>
            <span className="stat-value" style={{ color: "#2563eb" }}>
              {loading ? "..." : stats.pendingRequests}
            </span>
            <span className="stat-hint">Awaiting processing</span>
          </div>

          <div className="stat-card">
            <span className="stat-label">Conflicts</span>
            <span className="stat-value" style={{ color: "#dc2626" }}>
              {loading ? "..." : stats.conflictRequests}
            </span>
            <span className="stat-hint">Schedule overlaps</span>
          </div>
        </div>

        {/* Quick Actions Grid */}
        <div className="dashboard-grid">
          <div className="dashboard-card" style={{ padding: "24px" }}>
            <h3 style={{ fontSize: "18px", fontWeight: "700", color: "#1e293b", marginBottom: "8px" }}>
              Resource Management
            </h3>
            <p style={{ color: "#64748b", fontSize: "14px", marginBottom: "20px" }}>
              Add, update, or adjust operational status for classrooms, labs, meeting rooms, and equipment.
            </p>
            <button className="primary-action" onClick={() => navigate("/admin/resources")}>
              Manage Resources →
            </button>
          </div>

          <div className="dashboard-card" style={{ padding: "24px" }}>
            <h3 style={{ fontSize: "18px", fontWeight: "700", color: "#1e293b", marginBottom: "8px" }}>
              Request Overview
            </h3>
            <p style={{ color: "#64748b", fontSize: "14px", marginBottom: "20px" }}>
              View and audit all user allocation requests across the entire organization.
            </p>
            <button className="secondary-action" onClick={() => navigate("/admin/requests")}>
              View All Requests →
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

export default AdminDashboard;