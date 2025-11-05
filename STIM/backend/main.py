from fastapi import FastAPI, HTTPException, Body, Depends, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import OAuth2PasswordRequestForm
from .auth import (
    get_password_hash, 
    verify_password, 
    create_access_token, 
    get_current_user,
    ACCESS_TOKEN_EXPIRE_MINUTES,
    users_collection,
    db
)
from datetime import datetime, timedelta
from pydantic import BaseModel
from typing import List, Optional
from bson import ObjectId

app = FastAPI() # Initialize FastAPI app

# Database Collections
games_collection = db["games"]
word_bank_collection = db["word_bank"]
stats_collection = db["user_stats"]

# Enable CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Sample word bank
WORD_BANK = [
    "fig", "kiwi", "plum", "pear", "lime", "date",
    "apple", "grape", "melon", "mango", "berry", "lemon", "olive", "guava",
    "banana", "papaya", "durian", "lychee", "tomato", "orange", "apricot",
    "pineapple", "blueberry", "strawberry", "blackberry", "cranberry", "raspberry"
]

# Models
class User(BaseModel):
    """Model representing a user in the system."""
    username: str
    id: Optional[str] = None

class Token(BaseModel):
    """Model for JWT token response."""
    access_token: str
    token_type: str

class UserCreate(BaseModel):
    """Model for user creation."""
    username: str
    password: str

class GameStats(BaseModel):
    """Model for user game statistics."""
    total_games: int = 0
    games_won: int = 0
    games_lost: int = 0
    current_streak: int = 0
    max_streak: int = 0
    guess_distribution: List[int] = [0, 0, 0, 0, 0, 0] # Distribution of guesses (1-6)

# Startup event to initialize database
@app.on_event("startup")
async def startup_db_client():
    """Initialize MongoDB connection and word bank."""
    # Initialize word bank if empty
    word_count = await word_bank_collection.count_documents({})
    if word_count == 0:
        await word_bank_collection.insert_many([{"word": word, "length": len(word)} for word in WORD_BANK])
    print("✅ MongoDB connected and word bank initialized")

@app.on_event("shutdown")
async def shutdown_db_client():
    """Close MongoDB connection on shutdown."""
    pass  # Motor handles connection closing automatically

# Auth endpoints
@app.post("/signup", response_model=Token)
async def signup(user: UserCreate):
    """Create a new user and return an access token."""
    # Check if user already exists
    existing_user = await users_collection.find_one({"username": user.username})
    if existing_user:
        raise HTTPException(status_code=400, detail="Username already registered")
    
    hashed_password = get_password_hash(user.password)
    user_data = {
        "username": user.username,
        "hashed_password": hashed_password,
        "created_at": datetime.utcnow(),
        "last_login": datetime.utcnow()
    }
    
    result = await users_collection.insert_one(user_data)
    
    # Initialize user stats
    await stats_collection.insert_one({
        "user_id": result.inserted_id,
        "username": user.username,
        "stats": {
            "total_games": 0,
            "games_won": 0,
            "games_lost": 0,
            "current_streak": 0,
            "max_streak": 0,
            "guess_distribution": [0, 0, 0, 0, 0, 0]
        },
        "created_at": datetime.utcnow(),
        "updated_at": datetime.utcnow()
    })
    
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": user.username}, expires_delta=access_token_expires
    )
    return {"access_token": access_token, "token_type": "bearer"}

@app.post("/login", response_model=Token)
async def login(form_data: OAuth2PasswordRequestForm = Depends()):
    """Authenticate user and return an access token."""
    # Check if user exists
    user_data = await users_collection.find_one({"username": form_data.username})
    if not user_data:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    if not verify_password(form_data.password, user_data["hashed_password"]):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    # Update last login
    await users_collection.update_one(
        {"_id": user_data["_id"]},
        {"$set": {"last_login": datetime.utcnow()}}
    )
    
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": form_data.username}, expires_delta=access_token_expires
    )
    return {"access_token": access_token, "token_type": "bearer"}

# Protected game endpoints
@app.post("/start-game")
async def start_game(current_user: User = Depends(get_current_user)):
    """Start a new game for the current user."""
    # Get random word from MongoDB
    random_word_doc = await word_bank_collection.aggregate([{"$sample": {"size": 1}}]).to_list(1)
    if not random_word_doc:
        raise HTTPException(status_code=500, detail="No words available in database")
    
    secret_word = random_word_doc[0]["word"]
    game_id = str(ObjectId())
    
    game_data = {
        "game_id": game_id,
        "secret_word": secret_word,
        "attempts": [],
        "max_attempts": 6,
        "game_over": False,
        "won": False,
        "user_id": current_user.id,
        "username": current_user.username,
        "created_at": datetime.utcnow(),
        "updated_at": datetime.utcnow()
    }
    
    await games_collection.insert_one(game_data)
    
    return {
        "game_id": game_id, 
        "max_attempts": 6, 
        "word_length": len(secret_word)
    }

@app.post("/make-guess/{game_id}")
async def make_guess(game_id: str, guess: str = Body(..., embed=True), current_user: User = Depends(get_current_user)):
    """Make a guess in the game and return the result."""
    # Find the game
    game = await games_collection.find_one({"game_id": game_id})
    if not game:
        raise HTTPException(status_code=404, detail="Game not found")
    
    # Check if game belongs to user
    if game["user_id"] != current_user.id:
        raise HTTPException(status_code=403, detail="Not your game")
    
    if game["game_over"]:
        raise HTTPException(status_code=400, detail="Game is already over")
    
    expected_length = len(game["secret_word"])
    if len(guess) != expected_length:
        raise HTTPException(status_code=400, detail=f"Guess must be {expected_length} letters")
    
    guess = guess.lower()
    secret_word = game["secret_word"]
    
    # Check each letter
    result = []
    for i in range(len(secret_word)):
        if guess[i] == secret_word[i]:
            result.append({"letter": guess[i], "status": "correct"})
        elif guess[i] in secret_word:
            result.append({"letter": guess[i], "status": "present"})
        else:
            result.append({"letter": guess[i], "status": "absent"})
    
    # Update game in database
    new_attempts = game["attempts"] + [result]
    game_over = False
    won = False
    
    if guess == secret_word:
        game_over = True
        won = True
    elif len(new_attempts) >= game["max_attempts"]:
        game_over = True
    
    await games_collection.update_one(
        {"game_id": game_id},
        {
            "$set": {
                "attempts": new_attempts,
                "game_over": game_over,
                "won": won,
                "updated_at": datetime.utcnow()
            }
        }
    )
    
    # Update user stats if game is over
    if game_over:
        await update_user_stats(current_user.id, won, len(new_attempts))
    
    return {
        "attempts": new_attempts,
        "game_over": game_over,
        "won": won,
        "secret_word": secret_word if game_over else None
    }

async def update_user_stats(user_id: str, won: bool, attempts_count: int):
    """Update user statistics after a game."""
    stats_data = await stats_collection.find_one({"user_id": ObjectId(user_id)})
    
    if not stats_data:
        return
    
    stats = stats_data["stats"]
    stats["total_games"] += 1
    
    if won:
        stats["games_won"] += 1
        stats["current_streak"] += 1
        stats["max_streak"] = max(stats["max_streak"], stats["current_streak"])
        if 1 <= attempts_count <= 6:
            stats["guess_distribution"][attempts_count - 1] += 1
    else:
        stats["games_lost"] += 1
        stats["current_streak"] = 0
    
    await stats_collection.update_one(
        {"user_id": ObjectId(user_id)},
        {
            "$set": {
                "stats": stats,
                "updated_at": datetime.utcnow()
            }
        }
    )

