import React, { useState, useRef } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './css/AuthForm.css';
import { FaUser, FaLock, FaSignInAlt, FaUserPlus } from 'react-icons/fa';

function AuthForm({ type }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const buttonsRef = useRef([]);

  const createRipple = (e, index) => {
    
    const button = buttonsRef.current[index];
    const ripple = document.createElement('span');
    ripple.classList.add('ripple');
    
    const rect = button.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = e.clientX - rect.left - size / 2;
    const y = e.clientY - rect.top - size / 2;
    
    ripple.style.width = ripple.style.height = `${size}px`;
    ripple.style.left = `${x}px`;
    ripple.style.top = `${y}px`;
    
    button.appendChild(ripple);
    
    setTimeout(() => {
      ripple.remove();
    }, 600);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const endpoint = type === 'login' ? '/login' : '/signup';
      const payload = type === 'login' 
        ? new URLSearchParams({ username, password }) 
        : { username, password };

      const response = await axios.post(
        `http://localhost:8000${endpoint}`,
        payload,
        {
          headers: {
            'Content-Type': type === 'login' 
              ? 'application/x-www-form-urlencoded' 
              : 'application/json'
          }
        }
      );

      localStorage.setItem('token', response.data.access_token);
      navigate('/game');
    } catch (err) {
      if (err.response) {
        if (err.response.data && err.response.data.detail) {
          setError(err.response.data.detail);
        } else {
          setError('Invalid username or password');
        }
      } else if (err.request) {
        setError('No response from server');
      } else {
        setError('An unexpected error occurred');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2 className="auth-title">
        {type === 'login' ? 'Welcome' : 'Create Account'}
      </h2>
      
      {error && <div className="error-message">{error}</div>}
      
      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="input-group">
          <span className="input-icon"><FaUser /></span>
          <input
            type="text"
            className="auth-input"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        
        <div className="input-group">
          <span className="input-icon"><FaLock /></span>
          <input
            type="password"
            className="auth-input"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        
        <button
          type="submit"
          className="auth-btn"
          disabled={isLoading}
          ref={el => buttonsRef.current[0] = el}
          onClick={(e) => createRipple(e, 0)}
        >
          {isLoading ? (
            'Processing...'
          ) : (
            <>
              {type === 'login' ? (
                <>
                  <FaSignInAlt style={{ marginRight: '8px' }} />
                  Login
                </>
              ) : (
                <>
                  <FaUserPlus style={{ marginRight: '8px' }} />
                  Sign Up
                </>
              )}
            </>
          )}
        </button>
      </form>
      
      <div className="toggle-auth">
        {type === 'login' ? (
          <>
            Don't have an account?{' '}
            <span className="toggle-link" onClick={() => navigate('/signup')}>
              Sign up
            </span>
          </>
        ) : (
          <>
            Already have an account?{' '}
            <span className="toggle-link" onClick={() => navigate('/login')}>
              Login
            </span>
          </>
        )}
      </div>
    </div>
  );
}

export default AuthForm;