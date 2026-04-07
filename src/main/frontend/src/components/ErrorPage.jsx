import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import './ErrorPage.css';

const ErrorPage = ({ defaultCode = '500', defaultMessage = 'Произошла непредвиденная ошибка' }) => {
  const navigate = useNavigate();
  const [params] = useSearchParams();

  const code = params.get('code') || defaultCode;
  const message = params.get('message') || defaultMessage;

  return (
    <div className="errorPage">
      <div className="errorCard">
        <div className="errorCode">{code}</div>
        <h1>Ошибка</h1>
        <p>{message}</p>
        <div className="errorActions">
          <button onClick={() => navigate('/')}>На главную</button>
          <button className="secondary" onClick={() => navigate(-1)}>Назад</button>
        </div>
      </div>
    </div>
  );
};

export default ErrorPage;

