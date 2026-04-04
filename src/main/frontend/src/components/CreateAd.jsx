import React, { useState } from 'react';
import './CreateAd.css'; // Скопируй сюда стили из тега <style> файла create-ad.jsp

const CreateAd = () => {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        category: '',
        location: '',
        condition: 'USED',
        priceType: 'fixed',
        price: '',
        action: 'publish',
        authorId: 1 // TODO: Брать из авторизации JWT (Лекция 9)
    });
    const [photo, setPhoto] = useState(null);
    const [message, setMessage] = useState({ type: '', text: '' });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Обработка логики цены
        let finalPrice = formData.price;
        if (formData.priceType === 'free') finalPrice = 0;
        if (formData.priceType === 'negotiable') finalPrice = -1;

        // Создаем DTO объект (как прописано в Лекции 4)
        const announcementDto = {
            title: formData.title,
            description: formData.description,
            category: formData.category,
            location: formData.location,
            condition: formData.condition,
            price: parseInt(finalPrice) || 0,
            authorId: formData.authorId
        };

        try {
            // Отправляем JSON на наш REST Controller
            const response = await fetch('http://localhost:8080/api/announcements', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(announcementDto)
            });

            if (response.ok) {
                setMessage({ type: 'success', text: '✅ Объявление успешно создано!' });
                // Если есть фото, здесь можно добавить второй fetch для загрузки фото по ID объявления
            } else {
                setMessage({ type: 'error', text: '❌ Ошибка при создании объявления.' });
            }
        } catch (error) {
            setMessage({ type: 'error', text: '❌ Ошибка сети. Сервер недоступен.' });
        }
    };

    return (
        <div className="container">
            <div className="header"><div className="logo">Portal</div></div>
            <div className="card">
                <div className="card-header">
                    <h1 className="card-title">Создать новое объявление</h1>
                </div>

                {message.text && (
                    <div className={`alert ${message.type === 'success' ? 'alert-success' : 'alert-error'}`}>
                        {message.text}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="form-section">
                        <h3 className="section-title">📝 Основная информация</h3>
                        <div className="form-group">
                            <label className="required">Заголовок объявления</label>
                            <input type="text" name="title" className="form-control" required onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label className="required">Описание</label>
                            <textarea name="description" className="form-control" required onChange={handleChange} />
                        </div>
                    </div>

                    <div className="form-section">
                        <h3 className="section-title">📂 Категория и Локация</h3>
                        <div className="form-group">
                            <label className="required">Категория</label>
                            <select name="category" className="form-control" required onChange={handleChange}>
                                <option value="">Выберите категорию</option>
                                <option value="ELECTRONICS">Электроника</option>
                                <option value="CLOTHING">Одежда и обувь</option>
                                <option value="HOME">Дом и сад</option>
                                <option value="AUTO">Автотовары</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="required">Местоположение</label>
                            <input type="text" name="location" className="form-control" required onChange={handleChange} />
                        </div>
                    </div>

                    <div className="form-section">
                        <h3 className="section-title">💰 Цена и Состояние</h3>
                        <div className="form-group">
                            <label className="required">Состояние товара</label>
                            <div className="radio-group">
                                <label className="radio-item"><input type="radio" name="condition" value="NEW" onChange={handleChange} /><span>Новое</span></label>
                                <label className="radio-item"><input type="radio" name="condition" value="USED" defaultChecked onChange={handleChange} /><span>Б/У</span></label>
                                <label className="radio-item"><input type="radio" name="condition" value="BROKEN" onChange={handleChange} /><span>На запчасти</span></label>
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="required">Тип цены</label>
                            <div className="radio-group">
                                <label className="radio-item"><input type="radio" name="priceType" value="fixed" defaultChecked onChange={handleChange} /><span>Точная цена</span></label>
                                <label className="radio-item"><input type="radio" name="priceType" value="negotiable" onChange={handleChange} /><span>Договорная</span></label>
                                <label className="radio-item"><input type="radio" name="priceType" value="free" onChange={handleChange} /><span>Бесплатно</span></label>
                            </div>
                        </div>

                        {formData.priceType === 'fixed' && (
                            <div className="form-group">
                                <label>Цена (руб.)</label>
                                <input type="number" name="price" className="form-control" onChange={handleChange} required />
                            </div>
                        )}
                    </div>

                    <div className="form-actions">
                        <a href="/" className="btn btn-outline">Отмена</a>
                        <button type="submit" className="btn btn-primary">✓ Создать объявление</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CreateAd;