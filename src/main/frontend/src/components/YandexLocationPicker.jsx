import React, { useEffect, useRef, useState } from 'react';
import './YandexLocationPicker.css';
import { loadYandexMapsApi, reverseGeocode } from '../utils/yandexMaps';

const DEFAULT_CENTER = [55.751244, 37.618423]; // Москва

const formatCoords = (coords) => {
  if (!Array.isArray(coords) || coords.length < 2) {
    return '';
  }

  return `${coords[0].toFixed(6)}, ${coords[1].toFixed(6)}`;
};

const YandexLocationPicker = ({ onAddressChange }) => {
  const mapContainerRef = useRef(null);
  const mapRef = useRef(null);
  const markerRef = useRef(null);

  const [coords, setCoords] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let isMounted = true;

    const initMap = async () => {
      try {
        const ymaps = await loadYandexMapsApi();
        if (!isMounted || !mapContainerRef.current) {
          return;
        }

        mapRef.current = new ymaps.Map(mapContainerRef.current, {
          center: DEFAULT_CENTER,
          zoom: 10,
          controls: ['zoomControl']
        });

        mapRef.current.events.add('click', async (event) => {
          const clickedCoords = event.get('coords');
          setCoords(clickedCoords);

          if (!markerRef.current) {
            markerRef.current = new ymaps.Placemark(clickedCoords);
            mapRef.current.geoObjects.add(markerRef.current);
          } else {
            markerRef.current.geometry.setCoordinates(clickedCoords);
          }

          try {
            const address = await reverseGeocode(clickedCoords);
            if (address && onAddressChange) {
              onAddressChange(address);
            }
          } catch (geocodeError) {
            console.error('Ошибка обратного геокодирования', geocodeError);
          }
        });
      } catch (mapError) {
        if (isMounted) {
          setError(mapError.message || 'Карта временно недоступна');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    initMap();

    return () => {
      isMounted = false;
      if (mapRef.current) {
        mapRef.current.destroy();
      }
    };
  }, [onAddressChange]);

  if (error) {
    return <p className="yandex-map-error">{error}</p>;
  }

  return (
    <div className="yandex-map-picker">
      <div ref={mapContainerRef} className="yandex-map-container" />
      {loading && <p className="yandex-map-hint">Загрузка карты...</p>}
      {!loading && (
        <p className="yandex-map-hint">
          Нажмите на карту, чтобы выбрать адрес.
          {coords ? ` Координаты: ${formatCoords(coords)}` : ''}
        </p>
      )}
    </div>
  );
};

export default YandexLocationPicker;

