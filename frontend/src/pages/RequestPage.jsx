import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../services/api";

function RequestPage() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    resourceType: "CLASSROOM",
    capacityRequired: "",
    startTime: "",
    endTime: "",
    priority: 2,
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");
    setLoading(true);

    const token = localStorage.getItem("token");

    if (!token) {
      setError("You are not logged in. Please login again.");
      setLoading(false);
      return;
    }

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/requests`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            resourceType: formData.resourceType,
            capacityRequired: Number(formData.capacityRequired),
            startTime: formData.startTime,
            endTime: formData.endTime,
            priority: Number(formData.priority),
          }),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message || "Unable to create request"
        );
      }

      setSuccess("Request created successfully!");

      setTimeout(() => {
        navigate("/dashboard");
      }, 1000);

    } catch (err) {
      console.error(err);
      setError(
        err.message ||
        "Cannot connect to the backend. Make sure Spring Boot is running."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="request-page">

      <div className="request-container">

        <div className="request-header">
          <button
            className="back-button"
            onClick={() => navigate("/dashboard")}
          >
            ← Back to Dashboard
          </button>

          <div>
            <p className="section-label">RESOURCE MANAGEMENT</p>

            <h1>Create Resource Request</h1>

            <p>
              Submit your resource requirements and let
              OptiAlloc handle the allocation.
            </p>
          </div>
        </div>


        <div className="request-card">

          {error && (
            <div className="request-error">
              {error}
            </div>
          )}

          {success && (
            <div className="request-success">
              {success}
            </div>
          )}


          <form onSubmit={handleSubmit}>

            {/* RESOURCE TYPE */}

            <div className="form-group">

              <label htmlFor="resourceType">
                Resource Type
              </label>

              <select
                id="resourceType"
                name="resourceType"
                value={formData.resourceType}
                onChange={handleChange}
                required
              >
                <option value="CLASSROOM">
                  Classroom
                </option>

                <option value="LAB">
                  Laboratory
                </option>

                <option value="MEETING_ROOM">
                  Meeting Room
                </option>

                <option value="EQUIPMENT">
                  Equipment
                </option>
              </select>

            </div>


            {/* CAPACITY */}

            <div className="form-group">

              <label htmlFor="capacityRequired">
                Required Capacity
              </label>

              <input
                id="capacityRequired"
                name="capacityRequired"
                type="number"
                min="1"
                placeholder="e.g. 50"
                value={formData.capacityRequired}
                onChange={handleChange}
                required
              />

            </div>


            {/* TIME */}

            <div className="form-row">

              <div className="form-group">

                <label htmlFor="startTime">
                  Start Time
                </label>

                <input
                  id="startTime"
                  name="startTime"
                  type="datetime-local"
                  value={formData.startTime}
                  onChange={handleChange}
                  required
                />

              </div>


              <div className="form-group">

                <label htmlFor="endTime">
                  End Time
                </label>

                <input
                  id="endTime"
                  name="endTime"
                  type="datetime-local"
                  value={formData.endTime}
                  onChange={handleChange}
                  required
                />

              </div>

            </div>


            {/* PRIORITY */}

            <div className="form-group">

              <label htmlFor="priority">
                Priority
              </label>

              <select
                id="priority"
                name="priority"
                value={formData.priority}
                onChange={handleChange}
                required
              >
                <option value="1">
                  1 — Low
                </option>

                <option value="2">
                  2 — Normal
                </option>

                <option value="3">
                  3 — High
                </option>

                <option value="4">
                  4 — Critical
                </option>
              </select>

            </div>


            <button
              type="submit"
              className="request-submit-btn"
              disabled={loading}
            >
              {loading
                ? "Creating Request..."
                : "Create Request"}
            </button>

          </form>

        </div>

      </div>

    </main>
  );
}

export default RequestPage;