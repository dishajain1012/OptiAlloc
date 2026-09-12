import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";

function MyRequestsPage() {
  const navigate = useNavigate();

  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const token = localStorage.getItem("token");

  const fetchRequests = async () => {
    if (!token) {
      navigate("/login");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await fetch("http://localhost:8080/api/requests", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        navigate("/login");
        return;
      }

      if (!response.ok) {
        throw new Error(`Failed to load requests (Status: ${response.status})`);
      }

      const data = await response.json();
      setRequests(data);
    } catch (err) {
      console.error("Error fetching requests:", err);
      setError(err.message || "Failed to load requests. Please check backend connection.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    navigate("/login");
  };

  const formatDate = (isoString) => {
    if (!isoString) return "-";
    try {
      const date = new Date(isoString);
      return date.toLocaleString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch (e) {
      return isoString;
    }
  };

  const getPriorityLabel = (priority) => {
    switch (priority) {
      case 1:
        return "1 — Low";
      case 2:
        return "2 — Normal";
      case 3:
        return "3 — High";
      case 4:
        return "4 — Critical";
      default:
        return priority || "-";
    }
  };

  return (
    <div className="dashboard-page">
      {/* Navbar */}
      <header className="dashboard-navbar">
        <div className="dashboard-logo" onClick={() => navigate("/dashboard")} style={{ cursor: "pointer" }}>
          <span className="logo-mark">O</span>
          <span>OptiAlloc</span>
        </div>

        <div className="dashboard-nav-right">
          <button className="text-btn" onClick={() => navigate("/dashboard")}>
            Dashboard
          </button>
          <button className="text-btn active" onClick={() => navigate("/requests")}>
            My Requests
          </button>
          <button className="text-btn" onClick={() => navigate("/bookings")}>
            My Allocations
          </button>
          <span className="user-role">USER</span>
          <button className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="dashboard-content">
        <section className="dashboard-welcome" style={{ marginBottom: "24px" }}>
          <div>
            <p className="dashboard-label">RESOURCE REQUESTS</p>
            <h1>My Requests</h1>
            <p className="dashboard-subtitle">
              Track the status of all your submitted resource allocation requests.
            </p>
          </div>

          <button className="primary-action" onClick={() => navigate("/request")}>
            + Create Request
          </button>
        </section>

        {/* Content Card */}
        <div className="dashboard-card" style={{ padding: "24px" }}>
          {error && (
            <div className="request-error" style={{ marginBottom: "20px" }}>
              {error}
              <button
                onClick={fetchRequests}
                style={{
                  marginLeft: "12px",
                  background: "none",
                  border: "underline",
                  color: "#b91c1c",
                  cursor: "pointer",
                  fontWeight: "600",
                }}
              >
                Retry
              </button>
            </div>
          )}

          {loading ? (
            <div className="empty-state">
              <h3>Loading your requests...</h3>
            </div>
          ) : requests.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">RQ</div>
              <h3>No requests yet</h3>
              <p>You haven't submitted any resource allocation requests.</p>
              <button className="secondary-action" onClick={() => navigate("/request")}>
                Create Request
              </button>
            </div>
          ) : (
            <div style={{ overflowX: "auto" }}>
              <table
                style={{
                  width: "100%",
                  borderCollapse: "collapse",
                  textAlign: "left",
                  fontSize: "14px",
                }}
              >
                <thead>
                  <tr
                    style={{
                      borderBottom: "2px solid #e2e8f0",
                      color: "#64748b",
                      fontWeight: "600",
                    }}
                  >
                    <th style={{ padding: "12px 16px" }}>Req ID</th>
                    <th style={{ padding: "12px 16px" }}>Type</th>
                    <th style={{ padding: "12px 16px" }}>Capacity</th>
                    <th style={{ padding: "12px 16px" }}>Start Time</th>
                    <th style={{ padding: "12px 16px" }}>End Time</th>
                    <th style={{ padding: "12px 16px" }}>Priority</th>
                    <th style={{ padding: "12px 16px" }}>Status</th>
                    <th style={{ padding: "12px 16px" }}>Allocated Resource</th>
                  </tr>
                </thead>
                <tbody>
                  {requests.map((req) => (
                    <tr
                      key={req.id}
                      style={{
                        borderBottom: "1px solid #f1f5f9",
                        transition: "background 0.15s ease",
                      }}
                    >
                      <td style={{ padding: "14px 16px", fontWeight: "600", color: "#1e293b" }}>
                        #{req.id}
                      </td>
                      <td style={{ padding: "14px 16px", fontWeight: "500" }}>{req.resourceType}</td>
                      <td style={{ padding: "14px 16px" }}>{req.capacityRequired}</td>
                      <td style={{ padding: "14px 16px", color: "#475569" }}>
                        {formatDate(req.startTime)}
                      </td>
                      <td style={{ padding: "14px 16px", color: "#475569" }}>
                        {formatDate(req.endTime)}
                      </td>
                      <td style={{ padding: "14px 16px" }}>{getPriorityLabel(req.priority)}</td>
                      <td style={{ padding: "14px 16px" }}>
                        <span className={`request-status ${req.status?.toLowerCase()}`}>
                          {req.status}
                        </span>
                      </td>
                      <td style={{ padding: "14px 16px", color: "#334155" }}>
                        {req.allocatedResource ? (
                          <div>
                            <strong style={{ color: "#1e293b" }}>{req.allocatedResource.name}</strong>
                            {req.allocatedResource.location && (
                              <span style={{ fontSize: "12px", color: "#64748b", display: "block" }}>
                                ({req.allocatedResource.location})
                              </span>
                            )}
                          </div>
                        ) : (
                          <span style={{ color: "#94a3b8", italic: "true" }}>None</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

export default MyRequestsPage;
