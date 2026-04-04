import React, { useState } from 'react';
import './Register.css'; // Скопируй сюда стили из тега <style> файла register.jsp

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
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Валидация на стороне клиента (Лекция 4)
        if (formData.password !== formData.passwordAgain) {
            setMessage({ type: 'error', text: 'Пароли не совпадают!' });
            return;
        }

        try {
            // Отправляем JSON на REST API регистрации
            const response = await fetch('http://localhost:8080/api/users/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                setMessage({ type: 'success', text: 'Регистрация прошла успешно! Вы будете перенаправлены на страницу входа.' });
                setTimeout(() => {
                    window.location.href = '/login';
                }, 2000);
            } else {
                // Если бэкенд (Spring Validator) вернул ошибку
                const errorData = await response.json();
                setMessage({ type: 'error', text: errorData.message || 'Ошибка регистрации. Проверьте данные.' });
            }
        } catch (error) {
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
                        <label>Город</label>
                        <input type="text" name="addressCity" placeholder="Москва" onChange={handleChange} />
                    </div>
                    <div className="row">
                        <div className="form-group">
                            <label>Улица</label>
                            <input type="text" name="addressStreet" onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label>Дом</label>
                            <input type="text" name="addressHouseNumber" onChange={handleChange} />
                        </div>
                    </div>
                </div>

                <div className="form-group">
                    <label>Учебная программа *</label>
                    <select name="studyProgram" required onChange={handleChange}>
                        <option value="Не указывать">Не указывать</option>
                        <option value="ФПМИ">ФПМИ</option>
                        <option value="ЛФИ">ЛФИ</option>
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