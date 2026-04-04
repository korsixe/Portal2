import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './components/Home';
import CreateAd from './components/CreateAd';
import Register from './components/Register';

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
      </Routes>
    </Router>
  );
}

export default App;