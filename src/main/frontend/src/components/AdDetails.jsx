import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import './AdDetails.css';

const API_BASE = 'http://localhost:8080';

const formatPrice = (price) => {
  if (price === -1) return 'Договорная';
  if (price === 0) return 'Бесплатно';
  return `${Number(price || 0).toLocaleString('ru-RU')} руб.`;
};

const formatDate = (value) => {
  if (!value) return 'Не указано';
  return new Date(value).toLocaleString('ru-RU');
};

const AdDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [announcement, setAnnouncement] = useState(null);
  const [details, setDetails] = useState({ authorName: 'Неизвестный пользователь', photoCount: 0 });
  const [comments, setComments] = useState([]);
  const [commentText, setCommentText] = useState('');
  const [commentFeedback, setCommentFeedback] = useState({ type: '', text: '' });
  const [photoIndex, setPhotoIndex] = useState(0);

  const photoCount = useMemo(() => Number(details.photoCount || 0), [details.photoCount]);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [adResp, detailsResp, commentsResp] = await Promise.all([
        fetch(`${API_BASE}/api/announcements/${id}`, { credentials: 'include' }),
        fetch(`${API_BASE}/api/announcements/${id}/details`, { credentials: 'include' }),
        fetch(`${API_BASE}/api/announcements/${id}/comments`, { credentials: 'include' })
      ]);

      if (!adResp.ok) {
        if (adResp.status === 404) {
          navigate('/error?code=404&message=Объявление не найдено');
          return;
        }
        throw new Error('Ошибка загрузки объявления');
      }

      const adData = await adResp.json();
      const detailsData = detailsResp.ok ? await detailsResp.json() : { authorName: 'Неизвестный пользователь', photoCount: 0 };
      const commentsData = commentsResp.ok ? await commentsResp.json() : [];

      setAnnouncement(adData);
      setDetails(detailsData);
      setComments(Array.isArray(commentsData) ? commentsData : []);
      setPhotoIndex(0);
    } catch (e) {
      setError(e.message || 'Ошибка загрузки');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      loadData();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const addComment = async (e) => {
    e.preventDefault();
    setCommentFeedback({ type: '', text: '' });
    const text = commentText.trim();
    if (!text) return;

    const response = await fetch(`${API_BASE}/api/announcements/${id}/comments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ content: text })
    });

    if (response.status === 401) {
      navigate('/login');
      return;
    }

    if (!response.ok) {
      const message = await response.text();
      setCommentFeedback({
        type: 'error',
        text: message || 'Не удалось добавить комментарий'
      });
      return;
    }

    setCommentText('');
    setCommentFeedback({ type: 'success', text: 'Комментарий добавлен' });
    const commentsResp = await fetch(`${API_BASE}/api/announcements/${id}/comments`, { credentials: 'include' });
    if (commentsResp.ok) {
      setComments(await commentsResp.json());
    }
  };

  const showPhoto = (index) => {
    if (index >= 0 && index < photoCount) {
      setPhotoIndex(index);
    }
  };

  if (loading) {
    return <div className="adDetailsPage"><div className="adDetailsCard">Загрузка...</div></div>;
  }

  if (!announcement) {
    return <div className="adDetailsPage"><div className="adDetailsCard">Объявление не найдено</div></div>;
  }

  return (
    <div className="adDetailsPage">
      <div className="adDetailsLayout">
        <div className="adDetailsCard">
          <h1>{announcement.title}</h1>
          <div className="price">{formatPrice(announcement.price)}</div>

          <div className="metaRow">
            <span>Категория: {announcement.category}</span>
            <span>Состояние: {announcement.condition}</span>
            <span>Автор: {details.authorName}</span>
          </div>

          <div className="photoSection">
            {photoCount > 0 ? (
              <>
                <img
                  className="mainPhoto"
                  src={`${API_BASE}/ad-photo?adId=${announcement.id}&photoIndex=${photoIndex}`}
                  alt="Фото объявления"
                />
                <div className="photoControls">
                  <button onClick={() => showPhoto(photoIndex - 1)} disabled={photoIndex === 0}>←</button>
                  <span>{photoIndex + 1} / {photoCount}</span>
                  <button onClick={() => showPhoto(photoIndex + 1)} disabled={photoIndex >= photoCount - 1}>→</button>
                </div>
              </>
            ) : (
              <div className="emptyPhoto">Фотографии отсутствуют</div>
            )}
          </div>

          <h3>Описание</h3>
          <p className="description">{announcement.description || 'Описание отсутствует'}</p>

          <div className="infoGrid">
            <div>Локация: {announcement.location || 'Не указано'}</div>
            <div>Подкатегория: {announcement.subcategory || 'Не указано'}</div>
            <div>Просмотры: {announcement.viewCount || 0}</div>
            <div>Создано: {formatDate(announcement.createdAt)}</div>
          </div>

          {error && <div className="errorText">{error}</div>}

          <div className="actions">
            <button onClick={() => navigate(-1)} className="secondary">Назад</button>
            <button onClick={() => navigate('/dashboard')}>В кабинет</button>
          </div>
        </div>

        <div className="commentsCard">
          <h3>Комментарии ({comments.length})</h3>
          <form onSubmit={addComment}>
            <textarea
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              placeholder="Напишите комментарий"
              required
            />
            {commentFeedback.text && (
              <div className={`commentFeedback ${commentFeedback.type}`}>
                {commentFeedback.text}
              </div>
            )}
            <button type="submit">Добавить</button>
          </form>

          <div className="commentsList">
            {comments.length === 0 ? (
              <div className="emptyComments">Пока нет комментариев</div>
            ) : (
              comments.map((comment) => (
                <div key={comment.id} className="commentItem">
                  <div className="commentHeader">
                    <strong>{comment.userName}</strong>
                    <span>{formatDate(comment.createdAt)}</span>
                  </div>
                  <p>{comment.content}</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdDetails;

