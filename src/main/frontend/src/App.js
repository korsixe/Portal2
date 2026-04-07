import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './components/Home';
import CreateAd from './components/CreateAd';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import Login from './components/Login';
import AdminDashboard from './components/admin/AdminDashboard';
import ModeratorDashboard from './components/moderator/ModeratorDashboard';
import ModerationHistory from './components/moderator/ModerationHistory';
import EditProfile from './components/EditProfile';

function App() {
  return (
    <Router>
      <Routes>
        {/* Когда адрес в браузере просто /, показываем главную */}
        <Route path="/" element={<Home />} />

        {/* Когда адрес /create, показываем создание */}
        <Route path="/create" element={<CreateAd />} />
        <Route path="/create-ad" element={<CreateAd />} />

        {/* Когда адрес /register, показываем регистрацию */}
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
        <Route path="/moderator" element={<ModeratorDashboard />} />
        <Route path="/moderator/dashboard" element={<ModeratorDashboard />} />
        <Route path="/moderator/history" element={<ModerationHistory />} />
        <Route path="/edit-profile" element={<EditProfile />} />
      </Routes>
    </Router>
  );
}

export default App;