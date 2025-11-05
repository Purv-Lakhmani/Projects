from datetime import datetime, timedelta
from typing import Optional
from jose import JWTError, jwt
from passlib.context import CryptContext
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from pydantic import BaseModel
from motor.motor_asyncio import AsyncIOMotorClient # Async MongoDB driver
import os

# MongoDB setup
MONGODB_URI = os.getenv("MONGODB_URI", "mongodb://localhost:27017")
DATABASE_NAME = os.getenv("DATABASE_NAME", "stim_word_guess_game")

client = AsyncIOMotorClient(MONGODB_URI) # Create an async MongoDB client
db = client[DATABASE_NAME] # Access the database
users_collection = db["users"] # Collection for users

# User model
class User(BaseModel):
    """Model representing a user in the system."""
    username: str
    id: Optional[str] = None 

    class Config:
        """Pydantic configuration."""
        arbitrary_types_allowed = True

# JWT Configuration
SECRET_KEY = os.getenv("SECRET_KEY", "fallback-secret-key") # Use an environment variable for the secret key
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

# Password hashing
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# OAuth2 scheme
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verify a plain password against a hashed password and returns boolean."""
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password: str) -> str:
    """Hash a password using bcrypt."""
    return pwd_context.hash(password)

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    """Create a JWT access token with an expiration time."""
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

async def get_current_user(token: str = Depends(oauth2_scheme)) -> User:
    """Get the current user from the JWT token."""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub") # Extract username from the token payload
        if username is None:
            raise credentials_exception
        
        # Get user from MongoDB
        user_data = await users_collection.find_one({"username": username})
        if not user_data:
            raise credentials_exception
        
        return User(username=user_data["username"], id=str(user_data["_id"]))
        
    except JWTError as e:
        print(f"JWT Error: {e}")
        raise credentials_exception