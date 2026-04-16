import React, { useState, useEffect } from 'react';
import './Dashboard.css';
import ChangePasswordModal from './ChangePasswordModal';
import NotificationBell from './NotificationBell';
import Icon from './Icon';

const Dashboard = () => {
  const [user, setUser] = useState(null);
  const [ads, setAds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [activeModal, setActiveModal] = useState(null);
  const [deletePassword, setDeletePassword] = useState('');

  useEffect(() => {
    loadUserData();
  }, []);

  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => setSuccessMessage(''), 3000);
      return () => clearTimeout(timer);
    }
    if (errorMessage) {
      const timer = setTimeout(() => setErrorMessage(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [successMessage, errorMessage]);

  const loadUserData = async () => {
    setLoading(true);
    try {
      const userResponse = await fetch('http://localhost:8080/api/users/me', {
        method: 'GET',
        credentials: 'include'
      });

      if (!userResponse.ok) {
        if (userResponse.status === 401) {
          window.location.href = '/login';
          return;
        }
        setErrorMessage('Error loading user data');
        return;
      }
      const userData = await userResponse.json();
      setUser(userData);

      const adsResponse = await fetch('http://localhost:8080/api/announcements/my', {
        method: 'GET',
        credentials: 'include'
      });

      if (adsResponse.ok) {
        const adsData = await adsResponse.json();
        const activeAds = adsData.filter(ad => ad.status !== 'DELETED');
        setAds(activeAds);
      }
    } catch (error) {
      console.error('Error loading data:', error);
      setErrorMessage('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChanged = async (currentPassword, newPassword, confirmPassword) => {
    if (newPassword !== confirmPassword) {
      setErrorMessage('Passwords do not match!');
      return false;
    }
    if (newPassword.length < 8) {
      setErrorMessage('Password must be at least 8 characters!');
      return false;
    }

    try {
      const response = await fetch('http://localhost:8080/api/users/change-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          currentPassword: currentPassword,
          newPassword: newPassword,
          confirmPassword: confirmPassword
        }),
        credentials: 'include'
      });

      const data = await response.json();

      if (response.ok && data.success) {
        setSuccessMessage(data.message || 'Password changed successfully!');
        if (data.user) {
          setUser(data.user);
        }
        return true;
      } else {
        setErrorMessage(data.message || 'Error changing password');
        return false;
      }
    } catch (error) {
      console.error('Error:', error);
      setErrorMessage('Error changing password');
      return false;
    }
  };

  const handleDeleteAccount = async (e) => {
    e.preventDefault();
    const confirm = window.confirm('Are you sure you want to delete your account? This action cannot be undone!');
    if (!confirm) return;

    try {
      const response = await fetch('http://localhost:8080/api/users/delete-account', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ password: deletePassword }),
        credentials: 'include'
      });

      if (response.ok) {
        setSuccessMessage('Account deleted successfully');
        setTimeout(() => {
          window.location.href = '/login';
        }, 2000);
      } else {
        const error = await response.text();
        setErrorMessage('Error: ' + error);
      }
    } catch (error) {
      setErrorMessage('Error deleting account');
    }
  };

  const handleLogout = async () => {
    try {
      await fetch('http://localhost:8080/api/users/logout', {
        method: 'POST',
        credentials: 'include'
      });
      window.location.href = '/login';
    } catch (error) {
      console.error('Logout error:', error);
      window.location.href = '/login';
    }
  };

  const handleDeleteAd = async (adId) => {
    if (!window.confirm('Are you sure you want to delete this ad?')) return;

    try {
      const response = await fetch(`http://localhost:8080/api/announcements/${adId}`, {
        method: 'DELETE',
        credentials: 'include'
      });

      if (response.ok) {
        setSuccessMessage('Ad deleted');
        loadUserData();
      } else {
        setErrorMessage('Error deleting ad');
      }
    } catch (error) {
      setErrorMessage('Error deleting ad');
    }
  };

  const getStatusClass = (status) => {
    const statusMap = {
      'ACTIVE': 'statusActive',
      'DRAFT': 'statusDraft',
      'UNDER_MODERATION': 'statusModeration',
      'ARCHIVED': 'statusArchived',
      'DELETED': 'statusDeleted'
    };
    return statusMap[status] || 'statusDraft';
  };

  const formatPrice = (price) => {
    if (price === -1) return 'Negotiable';
    if (price === 0) return 'Free';
    return `${price.toLocaleString()} RUB`;
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Not specified';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US');
  };

  const formatAddress = (address) => {
    if (!address) return 'Not specified';
    if (typeof address === 'string') return address;
    if (address.fullAddress && String(address.fullAddress).trim()) return address.fullAddress;

    const parts = [address.city, address.street, address.houseNumber, address.building]
      .filter(Boolean)
      .map((v) => String(v).trim())
      .filter(Boolean);
    return parts.length ? parts.join(', ') : 'Not specified';
  };

  const getCategoryDisplayName = (category) => {
    const categoryMap = {
      'ELECTRONICS': 'Electronics',
      'CLOTHING': 'Clothing',
      'BOOKS': 'Books',
      'FURNITURE': 'Furniture',
      'SPORTS': 'Sports',
      'OTHER': 'Other'
    };
    return categoryMap[category] || category;
  };

  const getConditionDisplayName = (condition) => {
    const conditionMap = {
      'NEW': 'New',
      'LIKE_NEW': 'Like New',
      'GOOD': 'Good',
      'FAIR': 'Fair',
      'POOR': 'Poor'
    };
    return conditionMap[condition] || condition;
  };

  const getStatusDisplayName = (status) => {
    const statusMap = {
      'ACTIVE': 'Active',
      'DRAFT': 'Draft',
      'UNDER_MODERATION': 'Under Moderation',
      'ARCHIVED': 'Archived',
      'DELETED': 'Deleted'
    };
    return statusMap[status] || status;
  };

  const openModal = (modalName) => setActiveModal(modalName);
  const closeModals = () => setActiveModal(null);

  if (loading) {
    return (
        <div className="loadingContainer">
          <div className="loader"></div>
          <p>Loading...</p>
        </div>
    );
  }

  if (!user) {
    return (
        <div className="errorContainer">
          <p>Failed to load user data</p>
          <button onClick={() => window.location.href = '/login'} className="btnPrimary">
            Sign In Again
          </button>
        </div>
    );
  }

  return (
      <div className="dashboardContainer">
        {successMessage && (
            <div className="successMessage">
              <Icon name="success" size={24} className="successIcon" />
              <span>{successMessage}</span>
            </div>
        )}

        {errorMessage && (
            <div className="errorMessage">
              <Icon name="error" size={24} className="errorIcon" />
              <span>{errorMessage}</span>
            </div>
        )}

        <div className="header">
          <div className="headerTop">
            <div className="portalLogo">PORTAL</div>
          </div>
        </div>

        <div className="headerBell">
          <div className="headerTopBell">
            <div className="notificationLeft">
              <NotificationBell adIds={ads.map((ad) => ad.id)} />
            </div>

            <div className="avatarCenter">
              <div className="avatarCircle">
                <Icon name="user" size={40} className="avatarIcon" />
                <div className="onlineStatus"></div>
              </div>
            </div>

            <div className="buttonsVertical">
              <button onClick={() => window.location.href = '/edit-profile'} className="btnPrimary">
                <Icon name="edit" size={18} className="btnIcon" />
                Edit Profile
              </button>
              <button onClick={() => openModal('account')} className="btnPrimary">
                <Icon name="settings" size={18} className="btnIcon" />
                Account Settings
              </button>
            </div>
          </div>
        </div>

        <div className="profileActions">
          {user.moderator && (
              <button onClick={() => window.location.href = '/moderator/dashboard'} className="btnModerator">
                Moderator Panel
              </button>
          )}
          {user.admin && (
              <button onClick={() => window.location.href = '/admin/dashboard'} className="btnAdmin">
                Admin Panel
              </button>
          )}
        </div>

        <div className="stats">
          <div className="statCard">
            <div className="statNumber">{ads.length}</div>
            <div className="statLabel">Ads</div>
          </div>
          <div className="statCard">
            <div className="statNumber">{user.rating?.toFixed(1) || '0.0'}</div>
            <div className="statLabel">Rating</div>
          </div>
          <div className="statCard">
            <div className="statNumber">{user.coins || 0}</div>
            <div className="statLabel">Coins</div>
          </div>
          <div className="statCard">
            <div className="statNumber">{user.course || 1}</div>
            <div className="statLabel">Course</div>
          </div>
        </div>

        <div className="userInfo">
          <div className="infoCard">
            <h3>
              <Icon name="user" size={24} />
              Basic Information
            </h3>
            <div className="infoItem">
              <span className="infoLabel">Name:</span>
              <span className="infoValue">{user.name || 'Not specified'}</span>
            </div>
            <div className="infoItem">
              <span className="infoLabel">Email:</span>
              <span className="infoValue">{user.email}</span>
            </div>
            <div className="infoItem">
              <span className="infoLabel">Address:</span>
              <span className="infoValue">{formatAddress(user.address)}</span>
            </div>
          </div>

          <div className="infoCard">
            <h3>
              <Icon name="graduation" size={24} />
              Academic Information
            </h3>
            <div className="infoItem">
              <span className="infoLabel">Study Program:</span>
              <span className="infoValue">{user.studyProgram || 'Not specified'}</span>
            </div>
            <div className="infoItem">
              <span className="infoLabel">Course:</span>
              <span className="infoValue">{user.course || 1} year</span>
            </div>
          </div>

          <div className="infoCard">
            <h3>
              <Icon name="star" size={24} />
              Rating & Coins
            </h3>
            <div className="infoItem">
              <span className="infoLabel">Rating:</span>
              <span className="infoValue">
              <span className="ratingStars">
                {[...Array(5)].map((_, i) => (
                    <span key={i}>{i < Math.round(user.rating || 0) ? '\u2605' : '\u2606'}</span>
                ))}
              </span>
              ({(user.rating || 0).toFixed(1)})
            </span>
            </div>
            <div className="infoItem">
              <span className="infoLabel">Coins:</span>
              <span className="infoValue coins">
                {user.coins || 0}
                <Icon name="coin" size={18} />
              </span>
            </div>
          </div>
        </div>

        <div className="adsSection">
          <h3>
            <Icon name="list" size={24} />
            My Ads
            <button onClick={() => window.location.href = '/create-ad'} className="btnSuccess">
              + Create Ad
            </button>
          </h3>

          <div className="adList">
            {ads.length === 0 ? (
                <div className="noAds">
                  <h4>You have no ads yet</h4>
                  <p>Create your first ad to start selling or trading items!</p>
                </div>
            ) : (
                ads.map(ad => (
                    <div key={ad.id} className="adItem">
                      <div className="adTitle">{ad.title}</div>
                      <div className="adMeta">
                        <span className="adCategory">{getCategoryDisplayName(ad.category)}</span>
                        <span className="adCondition">{getConditionDisplayName(ad.condition)}</span>
                        <span className={`adStatus ${getStatusClass(ad.status)}`}>
                    {getStatusDisplayName(ad.status)}
                  </span>
                      </div>
                      <div className="adPrice">{formatPrice(ad.price)}</div>
                      <div className="adLocation">
                        <Icon name="location" size={16} />
                        {ad.location || 'Not specified'}
                      </div>
                      <div className="adDescription">{ad.description}</div>
                      <div className="adViews">
                        <Icon name="view" size={14} />
                        {ad.viewCount || 0} views
                      </div>
                      <div className="adDate">
                        <Icon name="calendar" size={14} />
                        {formatDate(ad.createdAt)}
                      </div>
                      <div className="adActions">
                        <button onClick={() => window.location.href = `/edit-ad?adId=${ad.id}`} className="btnEdit">
                          Edit
                        </button>
                        <button onClick={() => handleDeleteAd(ad.id)} className="btnDanger">
                          Delete
                        </button>
                      </div>
                    </div>
                ))
            )}
          </div>
        </div>

        <div className="actionButtons">
          <button onClick={() => window.location.href = '/support'} className="btnPrimary">
            Support
          </button>
          <button onClick={() => window.location.href = '/'} className="btnPrimary">
            Home
          </button>
          <button onClick={handleLogout} className="btnSecondary">
            Sign Out
          </button>
        </div>

        {/* Account Management Modal */}
        {activeModal === 'account' && (
            <div className="modal" onClick={(e) => e.target === e.currentTarget && closeModals()}>
              <div className="modalContent">
                <span className="close" onClick={closeModals}>&times;</span>
                <h3>
                  <Icon name="settings" size={28} />
                  Account Management
                </h3>
                <div className="buttonGroup">
                  <button onClick={() => openModal('password')} className="btnPrimary">
                    Change Password
                  </button>
                  <button onClick={() => openModal('delete')} className="btnDanger">
                    Delete Account
                  </button>
                </div>
              </div>
            </div>
        )}

        {/* Change Password Modal */}
        {activeModal === 'password' && (
            <ChangePasswordModal
                onClose={closeModals}
                onChangePassword={handlePasswordChanged}
            />
        )}

        {/* Delete Account Modal */}
        {activeModal === 'delete' && (
            <div className="modal" onClick={(e) => e.target === e.currentTarget && closeModals()}>
              <div className="modalContent">
                <span className="close" onClick={closeModals}>&times;</span>
                <h3>
                  <Icon name="delete" size={28} />
                  Delete Account
                </h3>
                <div className="warningBox">
                  <h4>
                    <Icon name="warning" size={20} />
                    Warning!
                  </h4>
                  <p>This action is irreversible. All your data, including ads, will be permanently deleted.</p>
                </div>
                <p>Enter your password to confirm:</p>
                <form onSubmit={handleDeleteAccount}>
                  <div className="formGroup">
                    <label>Current Password</label>
                    <input
                        type="password"
                        required
                        value={deletePassword}
                        onChange={(e) => setDeletePassword(e.target.value)}
                    />
                  </div>
                  <div className="buttonGroup">
                    <button type="submit" className="btnDanger">Delete Account</button>
                    <button type="button" onClick={closeModals} className="btnSecondary">Cancel</button>
                  </div>
                </form>
              </div>
            </div>
        )}
      </div>
  );
};

export default Dashboard;
