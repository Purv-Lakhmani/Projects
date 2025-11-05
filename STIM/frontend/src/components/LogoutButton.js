import React from 'react';
import { useNavigate } from 'react-router-dom';
import { FaSignOutAlt } from 'react-icons/fa';
import './css/LogoutButton.css';

function LogoutButton() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <button className="logout-btn" onClick={handleLogout}>
      <FaSignOutAlt className="logout-icon" />
      <span className="logout-text">Logout</span>
    </button>
  );
}

export default LogoutButton;