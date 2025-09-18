# token_lib.py
# Shared helpers for manipulating XP token data.

import os
from typing import Any, Dict, List, Tuple, Optional

import bcrypt
from pymongo import MongoClient
from pymongo.collection import Collection
from pymongo.errors import DuplicateKeyError

MINUTE_BUCKETS = [15, 30, 45, 60]
CATEGORIES = ["regular", "weapon", "battlepass"]

MONGO_URI = os.environ.get("MONGODB_URI", "mongodb://localhost:27017")
MONGO_DB = os.environ.get("MONGODB_DATABASE", "codxp_tokens")
MONGO_COLLECTION = os.environ.get("MONGODB_USERS_COLLECTION", "users")

_USERS_COLLECTION: Optional[Collection] = None


def _get_users_collection() -> Collection:
    """Return the MongoDB collection storing user documents."""
    global _USERS_COLLECTION
    if _USERS_COLLECTION is None:
        client = MongoClient(MONGO_URI)
        collection = client[MONGO_DB][MONGO_COLLECTION]
        try:
            collection.create_index("username", unique=True)
        except Exception as exc:  # pragma: no cover - configuration error
            raise RuntimeError("Unable to initialise MongoDB user collection") from exc
        _USERS_COLLECTION = collection
    return _USERS_COLLECTION


def _default_tokens() -> Dict[str, List[int]]:
    return {cat: [0, 0, 0, 0] for cat in CATEGORIES}


def _default_profile() -> Dict[str, object]:
    return {"cod_username": "", "prestige": "", "level": 1}


def _to_int(value: Any) -> int:
    if isinstance(value, bool):
        return int(value)
    if isinstance(value, int):
        return value
    if isinstance(value, float):
        return int(value)
    if isinstance(value, str):
        try:
            return int(value.strip())
        except ValueError:
            return 0
    return 0


def _ensure_size4(values: Any) -> List[int]:
    out = [0, 0, 0, 0]
    if isinstance(values, (list, tuple)):
        limit = min(4, len(values))
        for i in range(limit):
            out[i] = max(0, _to_int(values[i]))
    return out


def register_user(filename: str, username: str, password: str) -> bool:
    """Create a new user with a bcrypt-hashed password in MongoDB."""
    del filename  # Legacy argument retained for backwards compatibility
    collection = _get_users_collection()
    if collection.find_one({"username": username}):
        return False
    pw_hash = bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()
    doc: Dict[str, object] = {"username": username, "password_hash": pw_hash}
    doc.update(_default_profile())
    doc["tokens"] = _default_tokens()
    try:
        collection.insert_one(doc)
        return True
    except DuplicateKeyError:
        return False


def authenticate_user(filename: str, username: str, password: str) -> bool:
    """Validate supplied credentials against stored bcrypt hash."""
    del filename
    collection = _get_users_collection()
    doc = collection.find_one({"username": username})
    if not doc:
        return False
    hash_val = doc.get("password_hash")
    if not isinstance(hash_val, str) or not hash_val:
        return False
    return bcrypt.checkpw(password.encode(), hash_val.encode())


def read_all_tokens(filename: str, username: str) -> Dict[str, List[int]]:
    """Read tokens for the given user from MongoDB."""
    del filename
    collection = _get_users_collection()
    doc = collection.find_one({"username": username})
    if not doc:
        tokens = _default_tokens()
        collection.update_one(
            {"username": username},
            {
                "$set": {"tokens": tokens},
                "$setOnInsert": {"password_hash": "", **_default_profile()},
            },
            upsert=True,
        )
        return tokens
    tokens_obj = doc.get("tokens")
    data: Dict[str, List[int]] = {}
    for cat in CATEGORIES:
        raw_vals = []
        if isinstance(tokens_obj, dict):
            raw_vals = tokens_obj.get(cat, [])  # type: ignore[assignment]
        data[cat] = _ensure_size4(raw_vals)
    return data


def write_all_tokens(filename: str, username: str, data: Dict[str, List[int]]) -> None:
    """Write tokens for the given user back to MongoDB."""
    del filename
    collection = _get_users_collection()
    tokens: Dict[str, List[int]] = {}
    for cat in CATEGORIES:
        tokens[cat] = _ensure_size4(data.get(cat, []))
    collection.update_one(
        {"username": username},
        {
            "$set": {"tokens": tokens},
            "$setOnInsert": {"password_hash": "", **_default_profile()},
        },
        upsert=True,
    )


def compute_totals(tokens: List[int]) -> Tuple[int, float]:
    total_minutes = 0
    for count, minutes in zip(tokens, MINUTE_BUCKETS):
        total_minutes += count * minutes
    total_hours = total_minutes / 60.0
    return total_minutes, total_hours


def build_totals_report(data: Dict[str, List[int]]) -> str:
    lines: List[str] = []
    lines.append("=== 2XP Totals Report ===")
    grand_minutes = 0
    for cat in CATEGORIES:
        tokens = data[cat]
        cat_minutes, cat_hours = compute_totals(tokens)
        label = cat.capitalize()
        lines.append(f"{label}: {cat_minutes} minutes ({cat_hours:.2f} hours)")
        grand_minutes += cat_minutes
    lines.append("")
    lines.append(f"Grand Total: {grand_minutes} minutes ({grand_minutes/60.0:.2f} hours)")
    return "\n".join(lines)


def read_tokens_for_user(username: str) -> Dict[str, List[int]]:
    """Convenience wrapper mirroring the Java helper signature."""
    return read_all_tokens("", username)


def write_tokens_for_user(username: str, data: Dict[str, List[int]]) -> None:
    """Convenience wrapper mirroring the Java helper signature."""
    write_all_tokens("", username, data)
