import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../services/api";

function AdminResourcesPage() {
  const navigate = useNavigate();

  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingResource, setEditingResource] = useState(null);
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);

  // Form State
  const [formData, setFormData] = useState({
    name: "",
    type: "CLASSROOM",
    capacity: "",
    location: "",
    status: "AVAILABLE",
  });

  const token = localStorage.getItem("token");

  const fetchResources = async () => {
    if (!token) {
      navigate("/login");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await fetch(`${API_BASE_URL}/api/resources`, {
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
        throw new Error(`Failed to load resources (Status: ${response.status})`);
      }

      const data = await response.json();
      setResources(data);
    } catch (err) {
      console.error("Error fetching resources:", err);
      setError(err.message || "Failed to load resources.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchResources();
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    navigate("/login");
  };

  const openCreateModal = () => {
    setEditingResource(null);
    setFormData({
      name: "",
      type: "CLASSROOM",
      capacity: "",
      location: "",
      status: "AVAILABLE",
    });
    setActionError("");
    setIsModalOpen(true);
  };

  const openEditModal = (resource) => {
    setEditingResource(resource);
    setFormData({
      name: resource.name || "",
      type: resource.type || "CLASSROOM",
      capacity: resource.capacity || "",
      location: resource.location || "",
      status: resource.status || "AVAILABLE",
    });
    setActionError("");
    setIsModalOpen(true);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSaveResource = async (e) => {
    e.preventDefault();
    setActionError("");
    setSuccessMsg("");

    const capacityNum = Number(formData.capacity);
    if (!formData.name.trim()) {
      setActionError("Resource name is required.");
      return;
    }
    if (!capacityNum || capacityNum <= 0) {
      setActionError("Capacity must be greater than zero.");
      return;
    }

    const payload = {
      name: formData.name.trim(),
      type: formData.type,
      capacity: capacityNum,
      location: formData.location.trim(),
      status: formData.status,
    };

    const isEdit = !!editingResource;
    const url = isEdit
      ? `${API_BASE_URL}/api/resources/${editingResource.id}`
      : `${API_BASE_URL}/api/resources`;
    const method = isEdit ? "PUT" : "POST";

    try {
      const response = await fetch(url, {
        method,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      const resData = await response.json();

      if (!response.ok) {
        throw new Error(resData.message || "Failed to save resource.");
      }

      setSuccessMsg(isEdit ? "Resource updated successfully!" : "Resource created successfully!");
      setIsModalOpen(false);
      fetchResources();
    } catch (err) {
      console.error("Save resource error:", err);
      setActionError(err.message || "Failed to save resource.");
    }
  };

  const handleDeleteResource = async (id) => {
    setActionError("");
    setSuccessMsg("");

    try {
      const response = await fetch(`${API_BASE_URL}/api/resources/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        const resData = await response.json().catch(() => ({}));
        throw new Error(resData.message || "Failed to delete resource.");
      }

      setSuccessMsg("Resource deleted successfully!");
      setDeleteConfirmId(null);
      fetchResources();
    } catch (err) {
      console.error("Delete resource error:", err);
      setActionError(err.message || "Failed to delete resource.");
      setDeleteConfirmId(null);
    }
  };

  const getStatusBadgeStyle = (status) => {
    switch (status?.toUpperCase()) {
      case "AVAILABLE":
        return { background: "#dcfce7", color: "#15803d", border: "1px solid #bbf7d0" };
      case "MAINTENANCE":
        return { background: "#fef3c7", color: "#b45309", border: "1px solid #fde68a" };
      case "INACTIVE":
        return { background: "#fee2e2", color: "#b91c1c", border: "1px solid #fca5a5" };
      default:
        return { background: "#f1f5f9", color: "#475569", border: "1px solid #e2e8f0" };
    }
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
          <button className="text-btn" onClick={() => navigate("/admin")}>
            Dashboard
          </button>
          <button className="text-btn active" onClick={() => navigate("/admin/resources")}>
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
        <section className="dashboard-welcome" style={{ marginBottom: "24px" }}>
          <div>
            <p className="dashboard-label">RESOURCE INVENTORY</p>
            <h1>Resource Management</h1>
            <p className="dashboard-subtitle">
              Manage facility resources, capacities, equipment, and maintenance statuses.
            </p>
          </div>

          <button className="primary-action" onClick={openCreateModal}>
            + Add Resource
          </button>
        </section>

        {successMsg && (
          <div
            className="request-success"
            style={{
              marginBottom: "20px",
              padding: "12px 16px",
              borderRadius: "8px",
              background: "#dcfce7",
              color: "#15803d",
            }}
          >
            {successMsg}
          </div>
        )}

        {actionError && (
          <div className="request-error" style={{ marginBottom: "20px" }}>
            {actionError}
          </div>
        )}

        {error && (
          <div className="request-error" style={{ marginBottom: "20px" }}>
            {error}
            <button
              onClick={fetchResources}
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

        {/* Resources Table Card */}
        <div className="dashboard-card" style={{ padding: "24px" }}>
          {loading ? (
            <div className="empty-state">
              <h3>Loading resources...</h3>
            </div>
          ) : resources.length === 0 ? (
            <div className="empty-state">
              <h3>No resources found</h3>
              <p>Add your first resource to get started.</p>
              <button className="secondary-action" onClick={openCreateModal}>
                Add Resource
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
                    <th style={{ padding: "12px 16px" }}>ID</th>
                    <th style={{ padding: "12px 16px" }}>Resource Name</th>
                    <th style={{ padding: "12px 16px" }}>Type</th>
                    <th style={{ padding: "12px 16px" }}>Capacity</th>
                    <th style={{ padding: "12px 16px" }}>Location</th>
                    <th style={{ padding: "12px 16px" }}>Status</th>
                    <th style={{ padding: "12px 16px", textAlign: "right" }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {resources.map((res) => (
                    <tr
                      key={res.id}
                      style={{
                        borderBottom: "1px solid #f1f5f9",
                      }}
                    >
                      <td style={{ padding: "14px 16px", fontWeight: "600", color: "#1e293b" }}>
                        #{res.id}
                      </td>
                      <td style={{ padding: "14px 16px", fontWeight: "600", color: "#0f172a" }}>
                        {res.name}
                      </td>
                      <td style={{ padding: "14px 16px", color: "#475569" }}>{res.type}</td>
                      <td style={{ padding: "14px 16px" }}>{res.capacity} seats</td>
                      <td style={{ padding: "14px 16px", color: "#64748b" }}>{res.location || "-"}</td>
                      <td style={{ padding: "14px 16px" }}>
                        <span
                          style={{
                            padding: "4px 10px",
                            borderRadius: "12px",
                            fontSize: "12px",
                            fontWeight: "600",
                            ...getStatusBadgeStyle(res.status),
                          }}
                        >
                          {res.status}
                        </span>
                      </td>
                      <td style={{ padding: "14px 16px", textAlign: "right" }}>
                        <button
                          onClick={() => openEditModal(res)}
                          style={{
                            marginRight: "8px",
                            padding: "6px 12px",
                            borderRadius: "6px",
                            border: "1px solid #cbd5e1",
                            background: "#ffffff",
                            color: "#334155",
                            cursor: "pointer",
                            fontWeight: "500",
                            fontSize: "13px",
                          }}
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => setDeleteConfirmId(res.id)}
                          style={{
                            padding: "6px 12px",
                            borderRadius: "6px",
                            border: "1px solid #fca5a5",
                            background: "#fee2e2",
                            color: "#991b1b",
                            cursor: "pointer",
                            fontWeight: "500",
                            fontSize: "13px",
                          }}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>

      {/* Modal for Create / Edit */}
      {isModalOpen && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: "rgba(15, 23, 42, 0.5)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 1000,
          }}
        >
          <div
            style={{
              background: "#ffffff",
              borderRadius: "12px",
              width: "100%",
              maxWidth: "480px",
              padding: "24px",
              boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.1)",
            }}
          >
            <h2 style={{ fontSize: "20px", fontWeight: "700", marginBottom: "16px" }}>
              {editingResource ? "Edit Resource" : "Create New Resource"}
            </h2>

            <form onSubmit={handleSaveResource}>
              <div className="form-group" style={{ marginBottom: "16px" }}>
                <label style={{ display: "block", marginBottom: "6px", fontWeight: "600", fontSize: "14px" }}>
                  Resource Name
                </label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  placeholder="e.g. Auditorium Hall A"
                  required
                  style={{ width: "100%", padding: "10px", borderRadius: "6px", border: "1px solid #cbd5e1" }}
                />
              </div>

              <div className="form-group" style={{ marginBottom: "16px" }}>
                <label style={{ display: "block", marginBottom: "6px", fontWeight: "600", fontSize: "14px" }}>
                  Resource Type
                </label>
                <select
                  name="type"
                  value={formData.type}
                  onChange={handleInputChange}
                  required
                  style={{ width: "100%", padding: "10px", borderRadius: "6px", border: "1px solid #cbd5e1" }}
                >
                  <option value="CLASSROOM">CLASSROOM</option>
                  <option value="LAB">LAB</option>
                  <option value="MEETING_ROOM">MEETING_ROOM</option>
                  <option value="EQUIPMENT">EQUIPMENT</option>
                </select>
              </div>

              <div className="form-group" style={{ marginBottom: "16px" }}>
                <label style={{ display: "block", marginBottom: "6px", fontWeight: "600", fontSize: "14px" }}>
                  Capacity (Required)
                </label>
                <input
                  type="number"
                  name="capacity"
                  min="1"
                  value={formData.capacity}
                  onChange={handleInputChange}
                  placeholder="e.g. 50"
                  required
                  style={{ width: "100%", padding: "10px", borderRadius: "6px", border: "1px solid #cbd5e1" }}
                />
              </div>

              <div className="form-group" style={{ marginBottom: "16px" }}>
                <label style={{ display: "block", marginBottom: "6px", fontWeight: "600", fontSize: "14px" }}>
                  Location
                </label>
                <input
                  type="text"
                  name="location"
                  value={formData.location}
                  onChange={handleInputChange}
                  placeholder="e.g. Building A - Floor 2"
                  style={{ width: "100%", padding: "10px", borderRadius: "6px", border: "1px solid #cbd5e1" }}
                />
              </div>

              <div className="form-group" style={{ marginBottom: "24px" }}>
                <label style={{ display: "block", marginBottom: "6px", fontWeight: "600", fontSize: "14px" }}>
                  Status
                </label>
                <select
                  name="status"
                  value={formData.status}
                  onChange={handleInputChange}
                  required
                  style={{ width: "100%", padding: "10px", borderRadius: "6px", border: "1px solid #cbd5e1" }}
                >
                  <option value="AVAILABLE">AVAILABLE</option>
                  <option value="MAINTENANCE">MAINTENANCE</option>
                  <option value="INACTIVE">INACTIVE</option>
                </select>
              </div>

              <div style={{ display: "flex", justifyContent: "flex-end", gap: "12px" }}>
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  style={{
                    padding: "10px 16px",
                    borderRadius: "6px",
                    border: "1px solid #cbd5e1",
                    background: "#ffffff",
                    cursor: "pointer",
                    fontWeight: "600",
                  }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="primary-action"
                  style={{ padding: "10px 20px" }}
                >
                  {editingResource ? "Update Resource" : "Create Resource"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deleteConfirmId && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: "rgba(15, 23, 42, 0.5)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 1000,
          }}
        >
          <div
            style={{
              background: "#ffffff",
              borderRadius: "12px",
              width: "100%",
              maxWidth: "400px",
              padding: "24px",
              boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.1)",
            }}
          >
            <h3 style={{ fontSize: "18px", fontWeight: "700", color: "#991b1b", marginBottom: "12px" }}>
              Confirm Resource Deletion
            </h3>
            <p style={{ color: "#475569", fontSize: "14px", marginBottom: "24px" }}>
              Are you sure you want to delete Resource #{deleteConfirmId}? This action cannot be undone.
            </p>

            <div style={{ display: "flex", justifyContent: "flex-end", gap: "12px" }}>
              <button
                onClick={() => setDeleteConfirmId(null)}
                style={{
                  padding: "8px 16px",
                  borderRadius: "6px",
                  border: "1px solid #cbd5e1",
                  background: "#ffffff",
                  cursor: "pointer",
                  fontWeight: "600",
                }}
              >
                Cancel
              </button>
              <button
                onClick={() => handleDeleteResource(deleteConfirmId)}
                style={{
                  padding: "8px 16px",
                  borderRadius: "6px",
                  border: "none",
                  background: "#dc2626",
                  color: "#ffffff",
                  cursor: "pointer",
                  fontWeight: "600",
                }}
              >
                Confirm Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminResourcesPage;
