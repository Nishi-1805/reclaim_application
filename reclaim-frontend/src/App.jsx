import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import RoleGuard from './components/common/RoleGuard';
import DashboardLayout from './components/layout/DashboardLayout';

import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import Dashboard from './pages/Dashboard';
import ItemsList from './pages/items/ItemsList';
import ItemDetails from './pages/items/ItemDetails';
import MatchesList from './pages/matches/MatchesList';
import MatchDetails from './pages/matches/MatchDetails';
import ClaimsList from './pages/claims/ClaimsList';
import NotificationsList from './pages/notifications/NotificationsList';
import Profile from './pages/profile/Profile';
import UsersList from './pages/admin/UsersList';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<DashboardLayout />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/items" element={<ItemsList />} />
            <Route path="/items/:itemId" element={<ItemDetails />} />
            <Route path="/matches" element={<MatchesList />} />
            <Route path="/matches/:matchId" element={<MatchDetails />} />
            <Route path="/claims" element={<ClaimsList />} />
            <Route path="/notifications" element={<NotificationsList />} />
            <Route path="/profile" element={<Profile />} />

            <Route element={<RoleGuard allow={['ADMIN']} />}>
              <Route path="/admin/users" element={<UsersList />} />
            </Route>
          </Route>
        </Route>

        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </AuthProvider>
  );
}