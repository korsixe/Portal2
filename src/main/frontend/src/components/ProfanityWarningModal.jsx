import React from 'react';
import './ProfanityWarningModal.css';

const ProfanityWarningModal = ({ open, onClose }) => {
  if (!open) return null;

  return (
    <div className="profanityOverlay" onClick={onClose}>
      <div className="profanityModal" onClick={(e) => e.stopPropagation()}>
        <div className="profanityIcon">⚠️</div>
        <h3>Обнаружена ненормативная лексика</h3>
        <p>Пожалуйста, измените текст и попробуйте снова.</p>
        <button onClick={onClose}>Понятно</button>
      </div>
    </div>
  );
};

export default ProfanityWarningModal;

