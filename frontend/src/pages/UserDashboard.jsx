import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function UserDashboard() {
  const navigate = useNavigate();

  const [userEmail, setUserEmail] = useState("");
  const [requests, setRequests] = useState([]);
  const [loadingRequests, setLoadingRequests] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
      return;
    }

    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      setUserEmail(payload.sub || "");
    } catch (error) {
      console.error("Unable to read user information");
    }

    // Fetch requests from backend
    const fetchRequests = async () => {
      try {
        const response = await fetch(
          "http://localhost:8080/api/requests",
          {
            method: "GET",
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!response.ok) {
          throw new Error("Failed to fetch requests");
        }

        const data = await response.json();
        setRequests(data);
      } catch (error) {
        console.error("Unable to fetch requests:", error);
      } finally {
        setLoadingRequests(false);
      }
    };

    fetchRequests();
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    navigate("/login");
  };

  // Statistics derived from actual user requests
  const totalRequests = requests.length;

  const pendingRequests = requests.filter(
    (request) => request.status === "PENDING"
  ).length;

  const allocatedRequests = requests.filter(
    (request) =>
      request.status === "ALLOCATED" ||
      request.status === "APPROVED"
  ).length;

  const conflictRequests = requests.filter(
    (request) => request.status === "CONFLICT"
  ).length;

  // Show only latest 5 requests
  const recentRequests = requests.slice(-5).reverse();

  return (
    <div className="dashboard-page">

      {/* Dashboard Navbar */}
      <header className="dashboard-navbar">

        <div className="dashboard-logo" onClick={() => navigate("/dashboard")} style={{ cursor: "pointer" }}>
          <span className="logo-mark">O</span>
          <span>OptiAlloc</span>
        </div>

        <div className="dashboard-nav-right">

          <button className="text-btn active" onClick={() => navigate("/dashboard")}>
            Dashboard
          </button>

          <button className="text-btn" onClick={() => navigate("/requests")}>
            My Requests
          </button>

          <button className="text-btn" onClick={() => navigate("/bookings")}>
            My Allocations
          </button>

          <span className="user-role">
            USER
          </span>

          <button
            className="logout-btn"
            onClick={handleLogout}
          >
            Logout
          </button>

        </div>

      </header>


      {/* Main Content */}
      <main className="dashboard-content">

        {/* Welcome Section */}
        <section className="dashboard-welcome">

          <div>

            <p className="dashboard-label">
              USER DASHBOARD
            </p>

            <h1>
              Welcome back
            </h1>

            <p className="dashboard-subtitle">
              Manage your resource requests and track allocations
              from one place.
            </p>

            {userEmail && (
              <p className="dashboard-email">
                {userEmail}
              </p>
            )}

          </div>


          <button
            className="primary-action"
            onClick={() => navigate("/request")}
          >
            + Create Request
          </button>

        </section>


        {/* Statistics */}
        <section className="stats-grid">

          <div className="stat-card">

            <div className="stat-icon">
              RQ
            </div>

            <div>
              <p>Total Requests</p>
              <h2>{totalRequests}</h2>
            </div>

          </div>


          <div className="stat-card">

            <div className="stat-icon">
              AL
            </div>

            <div>
              <p>Allocated</p>
              <h2>{allocatedRequests}</h2>
            </div>

          </div>


          <div className="stat-card">

            <div className="stat-icon">
              PN
            </div>

            <div>
              <p>Pending</p>
              <h2>{pendingRequests}</h2>
            </div>

          </div>


          <div className="stat-card">

            <div className="stat-icon">
              CF
            </div>

            <div>
              <p>Conflicts</p>
              <h2>{conflictRequests}</h2>
            </div>

          </div>

        </section>


        {/* Main Dashboard Grid */}
        <section className="dashboard-grid">

          {/* Recent Requests */}
          <div className="dashboard-card requests-card">

            <div className="card-header">

              <div>
                <h2>Recent Requests</h2>

                <p>
                  Your latest resource allocation requests
                </p>
              </div>

              <button
                className="text-btn"
                onClick={() => navigate("/requests")}
              >
                View All
              </button>

            </div>


            {loadingRequests ? (

              <div className="empty-state">

                <h3>
                  Loading requests...
                </h3>

              </div>

            ) : requests.length === 0 ? (

              <div className="empty-state">

                <div className="empty-icon">
                  RQ
                </div>

                <h3>
                  No requests yet
                </h3>

                <p>
                  Create your first resource request to get started.
                </p>

                <button
                  className="secondary-action"
                  onClick={() => navigate("/request")}
                >
                  Create Resource Request
                </button>

              </div>

            ) : (

              <div className="recent-request-list">

                {recentRequests.map((request) => (

                  <div
                    className="recent-request-item"
                    key={request.id}
                  >

                    <div className="recent-request-info">

                      <strong>
                        {request.resourceType} (#{request.id})
                      </strong>

                      <span>
                        Capacity: {request.capacityRequired}
                      </span>

                    </div>


                    <div className="recent-request-meta">

                      <span className={`request-status ${request.status?.toLowerCase()}`}>
                        {request.status}
                      </span>

                      <span>
                        Priority {request.priority}
                      </span>

                    </div>

                  </div>

                ))}

              </div>

            )}

          </div>


          {/* Quick Actions */}
          <div className="dashboard-card">

            <div className="card-header">

              <div>

                <h2>
                  Quick Actions
                </h2>

                <p>
                  Common tasks
                </p>

              </div>

            </div>


            <div className="quick-actions">

              <button
                className="quick-action"
                onClick={() => navigate("/request")}
              >

                <div className="quick-icon">
                  +
                </div>

                <div>
                  <strong>
                    Create Request
                  </strong>

                  <span>
                    Request a resource
                  </span>
                </div>

              </button>


              <button
                className="quick-action"
                onClick={() => navigate("/requests")}
              >

                <div className="quick-icon">
                  RQ
                </div>

                <div>
                  <strong>
                    My Requests
                  </strong>

                  <span>
                    Track your requests
                  </span>
                </div>

              </button>


              <button
                className="quick-action"
                onClick={() => navigate("/bookings")}
              >

                <div className="quick-icon">
                  BK
                </div>

                <div>
                  <strong>
                    My Allocations
                  </strong>

                  <span>
                    View allocated resources
                  </span>
                </div>

              </button>

            </div>

          </div>

        </section>


        {/* How Allocation Works */}
        <section className="dashboard-card allocation-info">

          <div className="card-header">

            <div>

              <h2>
                How OptiAlloc Works
              </h2>

              <p>
                Resources are automatically matched based on constraints
                and priorities.
              </p>

            </div>

          </div>


          <div className="allocation-steps">

            <div className="allocation-step">

              <span>
                01
              </span>

              <div>

                <strong>
                  Create Request
                </strong>

                <p>
                  Specify the resource type, capacity and required time.
                </p>

              </div>

            </div>


            <div className="step-line"></div>


            <div className="allocation-step">

              <span>
                02
              </span>

              <div>

                <strong>
                  Constraint Check
                </strong>

                <p>
                  OptiAlloc checks availability, capacity and conflicts.
                </p>

              </div>

            </div>


            <div className="step-line"></div>


            <div className="allocation-step">

              <span>
                03
              </span>

              <div>

                <strong>
                  Smart Allocation
                </strong>

                <p>
                  The scheduling engine selects the best available resource.
                </p>

              </div>

            </div>

          </div>

        </section>

      </main>

    </div>
  );
}

export default UserDashboard;