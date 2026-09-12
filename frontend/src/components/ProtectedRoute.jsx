import { Navigate } from "react-router-dom";

function ProtectedRoute({ children, allowedRole }) {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  // User is not logged in
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // User doesn't have permission
  if (allowedRole && role !== allowedRole) {
    if (role === "USER") {
      return <Navigate to="/dashboard" replace />;
    } else if (role === "ADMIN") {
      return <Navigate to="/admin" replace />;
    }
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;