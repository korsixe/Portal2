import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './components/Home';
import CreateAd from './components/CreateAd';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import Login from './components/Login';

function App() {
  return (
    <Router>
      <Routes>
        {/* Когда адрес в браузере просто /, показываем главную */}
        <Route path="/" element={<Home />} />

        {/* Когда адрес /create, показываем создание */}
        <Route path="/create" element={<CreateAd />} />

        {/* Когда адрес /register, показываем регистрацию */}
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
      </Routes>
    </Router>
  );
}

export default App;