import { BrowserRouter, Routes, Route } from "react-router-dom";

import LandingPage from "./pages/LandingPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import UserDashboard from "./pages/UserDashboard";
import RequestPage from "./pages/RequestPage";
import MyRequestsPage from "./pages/MyRequestsPage";
import MyAllocationsPage from "./pages/MyAllocationsPage";
import AdminDashboard from "./pages/AdminDashboard";
import AdminResourcesPage from "./pages/AdminResourcesPage";
import AdminRequestsPage from "./pages/AdminRequestsPage";
import ProtectedRoute from "./components/ProtectedRoute";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public pages */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* User dashboard & pages */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute allowedRole="USER">
              <UserDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/request"
          element={
            <ProtectedRoute allowedRole="USER">
              <RequestPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/requests"
          element={
            <ProtectedRoute allowedRole="USER">
              <MyRequestsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/bookings"
          element={
            <ProtectedRoute allowedRole="USER">
              <MyAllocationsPage />
            </ProtectedRoute>
          }
        />

        {/* Admin dashboard & pages */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRole="ADMIN">
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/resources"
          element={
            <ProtectedRoute allowedRole="ADMIN">
              <AdminResourcesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/requests"
          element={
            <ProtectedRoute allowedRole="ADMIN">
              <AdminRequestsPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;