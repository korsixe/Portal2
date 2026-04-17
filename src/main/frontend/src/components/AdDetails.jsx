import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import './AdDetails.css';
import { useI18n } from '../i18n/I18nProvider';

const API_BASE = 'http://localhost:8080';

const AdDetails = () => {
  const { t, language } = useI18n();
  const { id } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [announcement, setAnnouncement] = useState(null);
  const [details, setDetails] = useState({ authorName: '', photoCount: 0 });
  const [comments, setComments] = useState([]);
  const [commentText, setCommentText] = useState('');
  const [commentFeedback, setCommentFeedback] = useState({ type: '', text: '' });
  const [hasPhoto, setHasPhoto] = useState(false);

  const formatPrice = (price) => {
    if (price === -1) return t('home.negotiable');
    if (price === 0) return t('home.free');
    return `${Number(price || 0).toLocaleString(language === 'ru' ? 'ru-RU' : 'en-US')} ₽`;
  };

  const formatDate = (value) => {
    if (!value) return t('adDetails.notSpecified', 'Not specified');
    return new Date(value).toLocaleString(language === 'ru' ? 'ru-RU' : 'en-US');
  };

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
          navigate(`/error?code=404&message=${encodeURIComponent(t('adDetails.notFound', 'Listing not found'))}`);
          return;
        }
        setError(t('adDetails.loadError', 'Failed to load listing'));
        return;
      }

      const adData = await adResp.json();
      const detailsData = detailsResp.ok ? await detailsResp.json() : { authorName: t('adDetails.unknownUser', 'Unknown user'), photoCount: 0 };
      const commentsData = commentsResp.ok ? await commentsResp.json() : [];

      setAnnouncement(adData);
      setDetails(detailsData);
      setComments(Array.isArray(commentsData) ? commentsData : []);
      setHasPhoto(Number(detailsData.photoCount || 0) > 0);
    } catch (e) {
      setError(e.message || t('adDetails.loadError', 'Failed to load listing'));
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
        text: message || t('adDetails.commentAddError', 'Failed to add comment')
      });
      return;
    }

    setCommentText('');
    setCommentFeedback({ type: 'success', text: t('adDetails.commentAdded', 'Comment added') });
    const commentsResp = await fetch(`${API_BASE}/api/announcements/${id}/comments`, { credentials: 'include' });
    if (commentsResp.ok) {
      setComments(await commentsResp.json());
    }
  };

  if (loading) {
    return <div className="adDetailsPage"><div className="adDetailsCard">{t('common.loading')}</div></div>;
  }

  if (!announcement) {
    return <div className="adDetailsPage"><div className="adDetailsCard">{t('adDetails.notFound', 'Listing not found')}</div></div>;
  }

  return (
    <div className="adDetailsPage">
      <div className="adDetailsLayout">
        <div className="adDetailsCard">
          <h1>{announcement.title}</h1>
          <div className="price">{formatPrice(announcement.price)}</div>

          <div className="metaRow">
            <span>{t('adDetails.category', 'Category')}: {announcement.category}</span>
            <span>{t('adDetails.condition', 'Condition')}: {announcement.condition}</span>
            <span>{t('adDetails.author', 'Author')}: {details.authorName || t('adDetails.unknownUser', 'Unknown user')}</span>
          </div>

          <div className="photoSection">
            {hasPhoto ? (
              <img
                className="mainPhoto"
                src={`${API_BASE}/ad-photo?adId=${announcement.id}&photoIndex=0`}
                alt={t('home.adPhotoAlt')}
                onError={() => setHasPhoto(false)}
              />
            ) : (
              <div className="emptyPhoto">{t('adDetails.noPhotos', 'No photos available')}</div>
            )}
          </div>

          <h3>{t('adDetails.description', 'Description')}</h3>
          <p className="description">{announcement.description || t('adDetails.noDescription', 'Description is missing')}</p>

          <div className="infoGrid">
            <div>{t('adDetails.location', 'Location')}: {announcement.location || t('adDetails.notSpecified', 'Not specified')}</div>
            <div>{t('adDetails.subcategory', 'Subcategory')}: {announcement.subcategory || t('adDetails.notSpecified', 'Not specified')}</div>
            <div>{t('adDetails.views', 'Views')}: {announcement.viewCount || 0}</div>
            <div>{t('adDetails.createdAt', 'Created')}: {formatDate(announcement.createdAt)}</div>
          </div>

          {error && <div className="errorText">{error}</div>}

          <div className="actions">
            <button onClick={() => navigate(-1)} className="secondary">{t('common.back')}</button>
            <button onClick={() => navigate('/dashboard')}>{t('login.goToDashboard')}</button>
          </div>
        </div>

        <div className="commentsCard">
          <h3>{t('adDetails.comments', 'Comments')} ({comments.length})</h3>
          <form onSubmit={addComment}>
            <textarea
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              placeholder={t('adDetails.commentPlaceholder', 'Write a comment')}
              required
            />
            {commentFeedback.text && (
              <div className={`commentFeedback ${commentFeedback.type}`}>
                {commentFeedback.text}
              </div>
            )}
            <button type="submit">{t('adDetails.addComment', 'Add')}</button>
          </form>

          <div className="commentsList">
            {comments.length === 0 ? (
              <div className="emptyComments">{t('adDetails.noComments', 'No comments yet')}</div>
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
