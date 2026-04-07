import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './components/Home';
import CreateAd from './components/CreateAd';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import Login from './components/Login';
import EditAd from './components/EditAd';
import AdDetails from './components/AdDetails';
import ErrorPage from './components/ErrorPage';
import SupportChat from './components/SupportChat';
import SuccessfulCreateAd from './components/SuccessfulCreateAd';
import SuccessfulEditAd from './components/SuccessfulEditAd';

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
        <Route path="/edit-ad" element={<EditAd />} />
        <Route path="/successful-create-ad" element={<SuccessfulCreateAd />} />
        <Route path="/successful-edit-ad" element={<SuccessfulEditAd />} />
        <Route path="/ad/:id" element={<AdDetails />} />
        <Route path="/support" element={<SupportChat />} />
        <Route path="/error" element={<ErrorPage />} />
        <Route path="*" element={<ErrorPage defaultCode="404" defaultMessage="Страница не найдена" />} />
      </Routes>
    </Router>
  );
}

export default App;