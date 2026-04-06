import React, { useState } from 'react';
import './Register.css';

const Register = () => {
  const [formData, setFormData] = useState({
    email: '',
    name: '',
    password: '',
    passwordAgain: '',
    studyProgram: 'Не указывать',
    course: 0,
    addressFull: '',
    addressCity: '',
    addressStreet: '',
    addressHouseNumber: '',
    addressBuilding: ''
  });

  const [message, setMessage] = useState({ type: '', text: '' });

  const handleChange = (e) => {
    const value = e.target.name === 'course' ? parseInt(e.target.value) : e.target.value;
    setFormData({ ...formData, [e.target.name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (formData.password !== formData.passwordAgain) {
      setMessage({ type: 'error', text: 'Пароли не совпадают!' });
      return;
    }

    // Отправляем в том же формате, что и JSP
    const requestData = {
      email: formData.email,
      name: formData.name,
      password: formData.password,
      passwordAgain: formData.passwordAgain,
      studyProgram: formData.studyProgram,
      course: formData.course,
      addressFull: formData.addressFull,
      addressCity: formData.addressCity,
      addressStreet: formData.addressStreet,
      addressHouseNumber: formData.addressHouseNumber,
      addressBuilding: formData.addressBuilding
    };

    console.log('Sending data:', requestData);

    try {
      const response = await fetch('http://localhost:8080/api/users/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
      });

      if (response.ok) {
        setMessage({ type: 'success', text: 'Регистрация прошла успешно! Вы будете перенаправлены на страницу входа.' });
        setTimeout(() => {
          window.location.href = '/login';
        }, 2000);
      } else {
        const errorData = await response.text();
        console.error('Server response:', errorData);
        setMessage({ type: 'error', text: errorData || 'Ошибка регистрации. Проверьте данные.' });
      }
    } catch (error) {
      console.error('Network error:', error);
      setMessage({ type: 'error', text: 'Ошибка сети. Сервер недоступен.' });
    }
  };

  return (
      <div className="portal-container">
        <div className="portal-logo">PORTAL</div>
        <div className="portal-subtitle">Регистрация</div>

        {message.text && (
            <div className={`message ${message.type}`}>
              {message.text}
            </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email *</label>
            <input type="email" name="email" placeholder="ivanov.ii@phystech.edu" required onChange={handleChange} />
          </div>

          <div className="form-group">
            <label>Имя пользователя *</label>
            <input type="text" name="name" placeholder="ivanov" required onChange={handleChange} />
          </div>

          <div className="form-group">
            <label>Пароль *</label>
            <div className="password-info">
              Пароль должен содержать минимум 8 символов.
            </div>
            <input type="password" name="password" required onChange={handleChange} />
          </div>

          <div className="form-group">
            <label>Подтверждение пароля *</label>
            <input type="password" name="passwordAgain" required onChange={handleChange}
                   style={{ borderColor: formData.passwordAgain && formData.password !== formData.passwordAgain ? '#dc3545' : '' }} />
          </div>

          <div className="address-section">
            <h3>Адрес проживания</h3>

            <div className="form-group">
              <label>Полный адрес</label>
              <input type="text" name="addressFull" placeholder="г. Москва, ул. Примерная, д. 1" onChange={handleChange} />
            </div>

            <div className="form-group">
              <label>Город</label>
              <input type="text" name="addressCity" placeholder="Москва" onChange={handleChange} />
            </div>

            <div className="row">
              <div className="form-group">
                <label>Улица</label>
                <input type="text" name="addressStreet" placeholder="Примерная" onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>Дом</label>
                <input type="text" name="addressHouseNumber" placeholder="1" onChange={handleChange} />
              </div>
            </div>

            <div className="form-group">
              <label>Корпус</label>
              <input type="text" name="addressBuilding" placeholder="2 (если есть)" onChange={handleChange} />
            </div>
          </div>

          <div className="form-group">
            <label>Учебная программа *</label>
            <select name="studyProgram" required onChange={handleChange}>
              <option value="Не указывать">Не указывать</option>
              <option value="ФПМИ">ФПМИ</option>
              <option value="ВШПИ">ВШПИ</option>
              <option value="ФРКТ">ФРКТ</option>
              <option value="ЛФИ">ЛФИ</option>
              <option value="ФАКТ">ФАКТ</option>
              <option value="ФЭФМ">ФЭФМ</option>
              <option value="ВШМ">ВШМ</option>
              <option value="КНТ">КНТ</option>
              <option value="ФБМФ">ФБМФ</option>
              <option value="ПИШ ФАЛТ">ПИШ ФАЛТ</option>
            </select>
          </div>

          <div className="form-group">
            <label>Курс *</label>
            <select name="course" required onChange={handleChange}>
              <option value="0">Не указывать</option>
              <option value="1">1 курс</option>
              <option value="2">2 курс</option>
              <option value="3">3 курс</option>
              <option value="4">4 курс</option>
              <option value="5">5 курс</option>
              <option value="6">6 курс</option>
            </select>
          </div>

          <div className="button-group">
            <button type="submit" className="btn btn-primary">Зарегистрироваться</button>
            <a href="/login" className="btn btn-secondary">Войти</a>
          </div>
        </form>
      </div>
  );
};

export default Register;