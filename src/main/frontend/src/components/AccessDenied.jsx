import React from 'react';
import PropTypes from 'prop-types';
import './AccessDenied.css';

function AccessDenied({ title, message, actionLabel = 'На главную', actionHref = '/' }) {
  return (
    <div className="access-denied">
      <div className="access-denied-card">
        <h1>{title || 'Доступ запрещен'}</h1>
        <p>{message || 'У вас нет прав для просмотра этой страницы.'}</p>
        <a className="access-denied-button" href={actionHref}>
          {actionLabel}
        </a>
      </div>
    </div>
  );
}

AccessDenied.propTypes = {
  title: PropTypes.string,
  message: PropTypes.string,
  actionLabel: PropTypes.string,
  actionHref: PropTypes.string
};

export default AccessDenied;
