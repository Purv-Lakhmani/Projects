import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import './App.css';
import LogoutButton from './components/LogoutButton';

const API_BASE_URL = 'http://localhost:8000';

function WordGuessGame() {
  const [gameId, setGameId] = useState(null);
  const [currentGuess, setCurrentGuess] = useState('');
  const [attempts, setAttempts] = useState([]);
  const [gameOver, setGameOver] = useState(false);
  const [won, setWon] = useState(false);
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [wordLength, setWordLength] = useState(5);
  const inputRef = useRef(null);

  useEffect(() => {
    if (!gameOver) {
      inputRef.current?.focus();
    }
  }, [attempts, gameOver]);

  const startNewGame = async () => {
    try {
      setIsLoading(true);
      const token = localStorage.getItem('token');
      
      console.log('Starting new game with token:', token); // Debug log
      
      const response = await axios.post(
        `${API_BASE_URL}/start-game`,
        {},
        {
          headers: { 
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      
      console.log('Start game response:', response.data); // Debug log
      
      setGameId(response.data.game_id);
      setAttempts([]);
      setWordLength(response.data.word_length);
      setGameOver(false);
      setWon(false);
      setCurrentGuess('');
      setMessage('');
    } catch (error) {
      console.error('Start game error:', error);
      console.error('Error details:', {
        status: error.response?.status,
        data: error.response?.data,
        headers: error.response?.headers
      });
      
      setMessage(error.response?.data?.detail || 'Failed to start new game. Please try again.');
    } finally {
      setIsLoading(false);
      setTimeout(() => {
        inputRef.current?.focus();
      }, 100);
    }
  };

  const handleGuess = async () => {
    if (!gameId || !currentGuess || currentGuess.length !== wordLength || isLoading) return;

    try {
      setIsLoading(true);
      setMessage('');
      const token = localStorage.getItem('token');
      const response = await axios.post(
        `${API_BASE_URL}/make-guess/${gameId}`,
        { guess: currentGuess },
        { 
          headers: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          } 
        }
      );
      
      setAttempts(response.data.attempts);
      setGameOver(response.data.game_over);
      setWon(response.data.won);

      if (response.data.game_over) {
        setMessage(won 
          ? 'Congratulations! You guessed the word!' 
          : `Game over! The word was: ${response.data.secret_word}`
        );
      }
      setCurrentGuess('');
    } catch (error) {
      console.error('Error making guess:', error);
      setMessage(error.response?.data?.detail || 'An error occurred. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleGuess();
    }
  };

  useEffect(() => {
    startNewGame();
  }, []);

  const getLetterStatus = (row, col) => {
    if (attempts.length > row && attempts[row].length > col) {
      return attempts[row][col].status;
    }
    return 'empty';
  };

  return (
    <div className="game-container">
      <div className="header">
        <h1>Word Guess Game</h1>
        <LogoutButton />
      </div>
      
      <div className="game-board">
        {Array.from({ length: 6 }).map((_, row) => (
          <div key={row} className="word-row">
            {Array.from({ length: wordLength }).map((_, col) => {
              const status = getLetterStatus(row, col);
              const isCurrentRow = row === attempts.length && !gameOver;
              const letter = isCurrentRow && col < currentGuess.length 
                ? currentGuess[col] 
                : status !== 'empty' ? attempts[row][col].letter : '';
              
              return (
                <div 
                  key={col} 
                  className={`letter-box ${status}`}
                >
                  {letter.toUpperCase()}
                </div>
              );
            })}
          </div>
        ))}
      </div>

      {!gameOver ? (
        <div className="input-area">
          <input
            ref={inputRef}
            type="text"
            value={currentGuess}
            onChange={(e) => setCurrentGuess(e.target.value.toLowerCase().replace(/[^a-z]/g, ''))}
            onKeyDown={handleKeyDown}
            maxLength={wordLength}
            disabled={gameOver || isLoading}
          />
          <button 
            onClick={handleGuess} 
            disabled={currentGuess.length !== wordLength || gameOver || isLoading}
          >
            {isLoading ? 'Processing...' : 'Guess'}
          </button>
        </div>
      ) : (
        <button 
          onClick={startNewGame} 
          className="new-game-btn"
          disabled={isLoading}
        >
          {isLoading ? 'Loading...' : 'Play Again'}
        </button>
      )}

      {message && <div className="message">{message}</div>}
    </div>
  );
}

export default WordGuessGame;