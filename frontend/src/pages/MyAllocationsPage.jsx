import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../services/api";

function MyAllocationsPage() {
  const navigate = useNavigate();

  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const token = localStorage.getItem("token");

  const fetchBookings = async () => {
    if (!token) {
      navigate("/login");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await fetch(`${API_BASE_URL}/api/bookings`, {
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
        throw new Error(`Failed to load allocations (Status: ${response.status})`);
      }

      const data = await response.json();
      setBookings(data);
    } catch (err) {
      console.error("Error fetching bookings:", err);
      setError(err.message || "Failed to load allocations. Please check backend connection.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
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
          <button className="text-btn" onClick={() => navigate("/requests")}>
            My Requests
          </button>
          <button className="text-btn active" onClick={() => navigate("/bookings")}>
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
            <p className="dashboard-label">RESOURCE ALLOCATIONS</p>
            <h1>My Allocations</h1>
            <p className="dashboard-subtitle">
              View all resources allocated to your confirmed requests.
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
                onClick={fetchBookings}
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
              <h3>Loading your allocations...</h3>
            </div>
          ) : bookings.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">BK</div>
              <h3>No allocations yet</h3>
              <p>You have no active resource allocations at this time.</p>
              <button className="secondary-action" onClick={() => navigate("/request")}>
                Create Resource Request
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
                    <th style={{ padding: "12px 16px" }}>Booking ID</th>
                    <th style={{ padding: "12px 16px" }}>Resource Name</th>
                    <th style={{ padding: "12px 16px" }}>Type</th>
                    <th style={{ padding: "12px 16px" }}>Capacity</th>
                    <th style={{ padding: "12px 16px" }}>Location</th>
                    <th style={{ padding: "12px 16px" }}>Req ID</th>
                    <th style={{ padding: "12px 16px" }}>Start Time</th>
                    <th style={{ padding: "12px 16px" }}>End Time</th>
                    <th style={{ padding: "12px 16px" }}>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {bookings.map((booking) => (
                    <tr
                      key={booking.id}
                      style={{
                        borderBottom: "1px solid #f1f5f9",
                        transition: "background 0.15s ease",
                      }}
                    >
                      <td style={{ padding: "14px 16px", fontWeight: "600", color: "#1e293b" }}>
                        #{booking.id}
                      </td>
                      <td style={{ padding: "14px 16px", fontWeight: "600", color: "#4f46e5" }}>
                        {booking.resourceName || "-"}
                      </td>
                      <td style={{ padding: "14px 16px", fontWeight: "500" }}>
                        {booking.resourceType || "-"}
                      </td>
                      <td style={{ padding: "14px 16px" }}>{booking.resourceCapacity || "-"}</td>
                      <td style={{ padding: "14px 16px", color: "#475569" }}>
                        {booking.resourceLocation || "-"}
                      </td>
                      <td style={{ padding: "14px 16px", fontWeight: "500" }}>
                        {booking.requestId ? `#${booking.requestId}` : "-"}
                      </td>
                      <td style={{ padding: "14px 16px", color: "#475569" }}>
                        {formatDate(booking.startTime)}
                      </td>
                      <td style={{ padding: "14px 16px", color: "#475569" }}>
                        {formatDate(booking.endTime)}
                      </td>
                      <td style={{ padding: "14px 16px" }}>
                        <span className={`request-status allocated`}>
                          {booking.status || "CONFIRMED"}
                        </span>
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

export default MyAllocationsPage;
