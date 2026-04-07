import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import './EditAd.css';
import ProfanityWarningModal from './ProfanityWarningModal';

const API_BASE = 'http://localhost:8080';

const categoryOptions = [
  { value: 'ELECTRONICS', label: 'Электроника' },
  { value: 'CLOTHING', label: 'Одежда и обувь' },
  { value: 'HOME', label: 'Дом и сад' },
  { value: 'BEAUTY', label: 'Красота и здоровье' },
  { value: 'SPORTS', label: 'Спорт и отдых' },
  { value: 'CHILDREN', label: 'Детские товары' },
  { value: 'AUTO', label: 'Автотовары' },
  { value: 'BOOKS', label: 'Книги и канцелярия' },
  { value: 'HOBBY', label: 'Хобби и творчество' },
  { value: 'PETS', label: 'Животные' },
  { value: 'TUTORING', label: 'Репетиторство' },
  { value: 'EDUCATION_SERVICES', label: 'Образовательные услуги' },
  { value: 'HOUSEHOLD_SERVICES', label: 'Бытовые услуги' },
  { value: 'REPAIR', label: 'Ремонт и строительство' },
  { value: 'BEAUTY_SERVICES', label: 'Красота и уход' },
  { value: 'TRANSPORT_SERVICES', label: 'Транспортные услуги' },
  { value: 'IT_SERVICES', label: 'IT и компьютерные услуги' },
  { value: 'EVENTS', label: 'Мероприятия и развлечения' },
  { value: 'MEDICAL', label: 'Медицинские услуги' },
  { value: 'LEGAL', label: 'Юридические услуги' },
  { value: 'OTHER', label: 'Другое' }
];

const EditAd = () => {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const adId = useMemo(() => params.get('adId') || params.get('id'), [params]);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [showProfanityWarning, setShowProfanityWarning] = useState(false);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: 'OTHER',
    subcategory: '',
    location: '',
    condition: 'USED',
    priceType: 'fixed',
    price: '',
    action: 'draft'
  });

  const hasProfanity = async (text) => {
    const response = await fetch(`${API_BASE}/api/profanity/check`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ text })
    });
    if (!response.ok) {
      return false;
    }
    const data = await response.json();
    return Boolean(data.hasProfanity);
  };

  useEffect(() => {
    if (!adId) {
      setMessage({ type: 'error', text: 'Не указан id объявления' });
      setLoading(false);
      return;
    }

    const loadAd = async () => {
      try {
        const response = await fetch(`${API_BASE}/api/announcements/${adId}`, {
          credentials: 'include'
        });

        if (!response.ok) {
          throw new Error('Не удалось загрузить объявление');
        }

        const ad = await response.json();
        let priceType = 'fixed';
        let price = ad.price;
        if (ad.price === -1) {
          priceType = 'negotiable';
          price = '';
        } else if (ad.price === 0) {
          priceType = 'free';
          price = '';
        }

        setFormData({
          title: ad.title || '',
          description: ad.description || '',
          category: ad.category || 'OTHER',
          subcategory: ad.subcategory || '',
          location: ad.location || '',
          condition: ad.condition || 'USED',
          priceType,
          price,
          action: 'draft'
        });
      } catch (e) {
        setMessage({ type: 'error', text: e.message || 'Ошибка загрузки объявления' });
      } finally {
        setLoading(false);
      }
    };

    loadAd();
  }, [adId]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage({ type: '', text: '' });

    const textToCheck = `${formData.title} ${formData.description}`.trim();
    if (textToCheck && await hasProfanity(textToCheck)) {
      setShowProfanityWarning(true);
      setSaving(false);
      return;
    }

    let finalPrice = Number(formData.price || 0);
    if (formData.priceType === 'free') {
      finalPrice = 0;
    } else if (formData.priceType === 'negotiable') {
      finalPrice = -1;
    }

    try {
      const response = await fetch(`${API_BASE}/api/announcements/${adId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          title: formData.title,
          description: formData.description,
          category: formData.category,
          subcategory: formData.subcategory,
          location: formData.location,
          condition: formData.condition,
          price: finalPrice,
          action: formData.action
        })
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || 'Ошибка при сохранении');
      }

      const savedAd = await response.json();
      navigate('/successful-edit-ad', {
        state: {
          announcement: savedAd,
          action: formData.action
        }
      });
    } catch (e) {
      setMessage({ type: 'error', text: e.message || 'Ошибка при сохранении' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="editAdPage"><div className="editAdCard">Загрузка...</div></div>;
  }

  return (
    <div className="editAdPage">
      <ProfanityWarningModal open={showProfanityWarning} onClose={() => setShowProfanityWarning(false)} />
      <div className="editAdCard">
        <h1>Редактировать объявление</h1>

        {message.text && (
          <div className={`editAdAlert ${message.type === 'success' ? 'success' : 'error'}`}>
            {message.text}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <label>Заголовок</label>
          <input name="title" value={formData.title} onChange={handleChange} required />

          <label>Описание</label>
          <textarea name="description" value={formData.description} onChange={handleChange} required />

          <label>Категория</label>
          <select name="category" value={formData.category} onChange={handleChange} required>
            {categoryOptions.map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>

          <label>Подкатегория</label>
          <input name="subcategory" value={formData.subcategory} onChange={handleChange} required />

          <label>Местоположение</label>
          <input name="location" value={formData.location} onChange={handleChange} required />

          <label>Состояние</label>
          <select name="condition" value={formData.condition} onChange={handleChange}>
            <option value="USED">б/у</option>
            <option value="NEW">Новое</option>
            <option value="BROKEN">Не работает</option>
          </select>

          <label>Тип цены</label>
          <select name="priceType" value={formData.priceType} onChange={handleChange}>
            <option value="fixed">Указать цену</option>
            <option value="negotiable">Договорная</option>
            <option value="free">Бесплатно</option>
          </select>

          {formData.priceType === 'fixed' && (
            <>
              <label>Цена</label>
              <input
                type="number"
                name="price"
                min="1"
                max="1000000000"
                value={formData.price}
                onChange={handleChange}
                required
              />
            </>
          )}

          <label>После сохранения</label>
          <select name="action" value={formData.action} onChange={handleChange}>
            <option value="draft">Сохранить как черновик</option>
            <option value="publish">Отправить на модерацию</option>
          </select>

          <div className="editAdActions">
            <button type="button" className="secondary" onClick={() => navigate('/dashboard')}>Отмена</button>
            <button type="submit" disabled={saving}>{saving ? 'Сохранение...' : 'Сохранить'}</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditAd;

