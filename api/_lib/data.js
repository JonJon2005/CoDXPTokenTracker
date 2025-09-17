const fs = require('fs/promises')
const path = require('path')
const bcrypt = require('bcryptjs')

const CATEGORIES = ['regular', 'weapon', 'battlepass']
const MINUTE_BUCKETS = [15, 30, 45, 60]
const DATA_DIR = path.join(process.cwd(), 'data', 'users')
const LEGACY_FILES = [
  path.join(process.cwd(), 'tokens.txt'),
  path.join(process.cwd(), '..', 'tokens.txt'),
]

const memoryStore = global.__CODXP_USERS || (global.__CODXP_USERS = new Map())

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

async function fileExists(filePath) {
  try {
    await fs.access(filePath)
    return true
  } catch (err) {
    if (err && err.code === 'ENOENT') return false
    throw err
  }
}

function zeroTokens() {
  return CATEGORIES.reduce((acc, cat) => {
    acc[cat] = MINUTE_BUCKETS.map(() => 0)
    return acc
  }, {})
}

function ensureTokenArray(value) {
  const arr = Array.isArray(value) ? value : []
  return MINUTE_BUCKETS.map((_, idx) => {
    const raw = arr[idx]
    const num = typeof raw === 'number' ? raw : parseInt(raw, 10)
    if (!Number.isFinite(num) || isNaN(num) || num < 0) {
      return 0
    }
    return Math.floor(num)
  })
}

function normalizeTokens(tokens = {}) {
  const out = {}
  for (const cat of CATEGORIES) {
    out[cat] = ensureTokenArray(tokens[cat])
  }
  return out
}

function clampLevel(value) {
  const num = typeof value === 'number' ? value : parseInt(value, 10)
  if (!Number.isFinite(num) || isNaN(num)) {
    return 1
  }
  return Math.min(1000, Math.max(1, Math.round(num)))
}

function normalizeUser(raw = {}) {
  const tokens = normalizeTokens(raw.tokens)
  return {
    password_hash: typeof raw.password_hash === 'string' ? raw.password_hash : '',
    tokens,
    cod_username: typeof raw.cod_username === 'string' ? raw.cod_username : '',
    prestige: typeof raw.prestige === 'string' ? raw.prestige : '',
    level: clampLevel(raw.level ?? 1),
  }
}

async function readLegacyTokens() {
  for (const candidate of LEGACY_FILES) {
    try {
      const contents = await fs.readFile(candidate, 'utf8')
      const lines = contents.split(/\r?\n/).filter((line) => line.length > 0)
      const values = []
      for (const line of lines) {
        const num = parseInt(line.trim(), 10)
        values.push(Number.isFinite(num) && !isNaN(num) && num >= 0 ? Math.floor(num) : 0)
      }
      while (values.length < 12) {
        values.push(0)
      }
      const tokens = {
        regular: ensureTokenArray(values.slice(0, 4)),
        weapon: ensureTokenArray(values.slice(4, 8)),
        battlepass: ensureTokenArray(values.slice(8, 12)),
      }
      return tokens
    } catch (err) {
      if (!err || err.code !== 'ENOENT') {
        throw err
      }
    }
  }
  return zeroTokens()
}

async function writeLegacyTokens(tokens) {
  const payload = []
  for (const cat of CATEGORIES) {
    for (const val of ensureTokenArray(tokens[cat])) {
      payload.push(String(val))
    }
  }
  for (const candidate of LEGACY_FILES) {
    try {
      await fs.writeFile(candidate, payload.join('\n') + '\n', 'utf8')
      return true
    } catch (err) {
      if (err && (err.code === 'ENOENT' || err.code === 'EROFS' || err.code === 'EACCES')) {
        continue
      }
      throw err
    }
  }
  return false
}

async function readUserFile(username) {
  const userPath = path.join(DATA_DIR, `${username}.json`)
  try {
    const contents = await fs.readFile(userPath, 'utf8')
    return JSON.parse(contents)
  } catch (err) {
    if (err && err.code === 'ENOENT') {
      return null
    }
    throw err
  }
}

async function loadUser(username) {
  if (!username) return null
  if (memoryStore.has(username)) {
    return clone(memoryStore.get(username))
  }
  const fileData = await readUserFile(username)
  if (fileData) {
    const normalized = normalizeUser(fileData)
    memoryStore.set(username, normalized)
    return clone(normalized)
  }
  if (username === 'default') {
    const tokens = await readLegacyTokens()
    const normalized = {
      password_hash: '',
      tokens,
      cod_username: '',
      prestige: '',
      level: 1,
    }
    memoryStore.set(username, normalized)
    return clone(normalized)
  }
  return null
}

async function userExists(username) {
  if (!username) return false
  if (memoryStore.has(username)) return true
  const userPath = path.join(DATA_DIR, `${username}.json`)
  if (await fileExists(userPath)) return true
  return false
}

async function saveUser(username, data) {
  if (!username) return false
  const normalized = normalizeUser(data)
  memoryStore.set(username, normalized)
  const userPath = path.join(DATA_DIR, `${username}.json`)
  try {
    await fs.mkdir(path.dirname(userPath), { recursive: true })
  } catch (err) {
    // ignore mkdir errors; writing will surface them
  }
  if (username === 'default') {
    await writeLegacyTokens(normalized.tokens)
  }
  const payload = {
    password_hash: normalized.password_hash,
    tokens: normalized.tokens,
    cod_username: normalized.cod_username,
    prestige: normalized.prestige,
    level: normalized.level,
  }
  try {
    await fs.writeFile(userPath, JSON.stringify(payload, null, 2))
    return true
  } catch (err) {
    if (err && (err.code === 'EROFS' || err.code === 'EACCES' || err.code === 'ENOSYS')) {
      console.warn(`Non-persistent environment, user '${username}' changes kept in memory only.`)
      return false
    }
    throw err
  }
}

async function registerUser(username, password) {
  const trimmed = typeof username === 'string' ? username.trim() : ''
  if (!trimmed) {
    const err = new Error('Username is required')
    err.statusCode = 400
    throw err
  }
  if (typeof password !== 'string' || password.length === 0) {
    const err = new Error('Password is required')
    err.statusCode = 400
    throw err
  }
  if (await userExists(trimmed)) {
    return false
  }
  const hash = await bcrypt.hash(password, 10)
  const user = normalizeUser({
    password_hash: hash,
    tokens: zeroTokens(),
    cod_username: '',
    prestige: '',
    level: 1,
  })
  await saveUser(trimmed, user)
  return true
}

async function validateUser(username, password) {
  const name = typeof username === 'string' ? username.trim() : ''
  if (!name || typeof password !== 'string') {
    return false
  }
  const user = await loadUser(name)
  if (!user || !user.password_hash) {
    return false
  }
  try {
    return await bcrypt.compare(password, user.password_hash)
  } catch (err) {
    console.error('Failed to compare password for user', name, err)
    return false
  }
}

async function getTokens(username) {
  const user = await loadUser(username)
  if (!user) return null
  return clone(user.tokens)
}

async function setTokens(username, tokens) {
  const user = await loadUser(username)
  if (!user) return false
  user.tokens = normalizeTokens(tokens)
  await saveUser(username, user)
  return true
}

async function getProfile(username) {
  const user = await loadUser(username)
  if (!user) return null
  return {
    cod_username: user.cod_username,
    prestige: user.prestige,
    level: user.level,
  }
}

async function updateProfile(username, profile) {
  const user = await loadUser(username)
  if (!user) return false
  user.cod_username = typeof profile.cod_username === 'string' ? profile.cod_username : ''
  user.prestige = typeof profile.prestige === 'string' ? profile.prestige : ''
  user.level = clampLevel(profile.level ?? user.level)
  await saveUser(username, user)
  return true
}

module.exports = {
  CATEGORIES,
  MINUTE_BUCKETS,
  loadUser,
  userExists,
  saveUser,
  registerUser,
  validateUser,
  getTokens,
  setTokens,
  getProfile,
  updateProfile,
}
