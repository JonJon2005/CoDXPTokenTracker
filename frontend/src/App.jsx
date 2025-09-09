import { useCallback, useEffect, useState } from 'react'
import './App.css'
import codLogoLight from './assets/cod-logo.png'
import codLogoDark from './assets/cod-logo2.png'
import xpRegular from './assets/xp-regular.png'
import xpWeapon from './assets/xp-weapon.png'
import xpBattlepass from './assets/xp-battlepass.png'
import accountIcon from './assets/account.svg'
import Login from './Login.jsx'
import Register from './Register.jsx'
import ChangePassword from './ChangePassword.jsx'

const minutes = [15, 30, 45, 60]
const categoryIcons = {
  regular: xpRegular,
  weapon: xpWeapon,
  battlepass: xpBattlepass,
}

function App() {
  const [tokens, setTokens] = useState(null)
  const [error, setError] = useState(null)
  const [dirty, setDirty] = useState(false)
  const [theme, setTheme] = useState('dark')
  const [authToken, setAuthToken] = useState(() => localStorage.getItem('token'))
  const [showRegister, setShowRegister] = useState(false)
  const [showAccountMenu, setShowAccountMenu] = useState(false)
  const [showChangePassword, setShowChangePassword] = useState(false)

  const handleAuth = (t) => {
    localStorage.setItem('token', t)
    setAuthToken(t)
  }

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    setAuthToken(null)
    setTokens(null)
    setDirty(false)
    setError(null)
  }, [])

  useEffect(() => {
    if (dirty || !authToken) return

    const fetchTokens = () => {
      fetch('/api/tokens', {
        headers: { Authorization: `Bearer ${authToken}` },
      })
        .then((res) => {
          if (res.status === 401) {
            logout()
            throw new Error('Unauthorized')
          }
          if (!res.ok) throw new Error(`HTTP ${res.status}`)
          return res.json()
        })
        .then((data) => setTokens(data))
        .catch((err) => setError(err.message))
    }

    fetchTokens()
    const id = setInterval(fetchTokens, 5000)
    return () => clearInterval(id)
  }, [dirty, authToken, logout])

  useEffect(() => {
    document.documentElement.className = theme
  }, [theme])

  const adjustToken = (category, idx, delta) => {
    const current = tokens[category][idx]
    const updatedCount = Math.max(0, current + delta)
    const updatedTokens = {
      ...tokens,
      [category]: tokens[category].map((c, i) => (i === idx ? updatedCount : c)),
    }
    setTokens(updatedTokens)
    setDirty(true)
  }

  const setToken = (category, idx) => {
    const input = prompt('Enter number:', tokens[category][idx])
    if (input === null) return
    const parsed = Math.max(0, parseInt(input, 10))
    if (isNaN(parsed)) return
    const updatedTokens = {
      ...tokens,
      [category]: tokens[category].map((c, i) => (i === idx ? parsed : c)),
    }
    setTokens(updatedTokens)
    setDirty(true)
  }

  const resetTokens = () => {
    const reset = Object.fromEntries(
      Object.keys(tokens).map((k) => [k, tokens[k].map(() => 0)])
    )
    setTokens(reset)
    setDirty(true)
  }

  const saveTokens = () => {
    fetch('/api/tokens', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authToken}`,
      },
      body: JSON.stringify(tokens),
    })
      .then((res) => {
        if (res.status === 401) {
          logout()
          throw new Error('Unauthorized')
        }
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        setDirty(false)
      })
      .catch((err) => setError(err.message))
  }

  const minutesForCategory = (counts) =>
    counts.reduce((sum, count, idx) => sum + count * minutes[idx], 0)

  const formatMinutes = (m) => `${Math.floor(m / 60)} Hours (${m} Minutes)`

  if (!authToken) {
    return showRegister ? (
      <Register
        onAuth={handleAuth}
        switchToLogin={() => setShowRegister(false)}
      />
    ) : (
      <Login
        onAuth={handleAuth}
        switchToRegister={() => setShowRegister(true)}
      />
    )
  }

  if (error) {
    return <p>Failed to load: {error}</p>
  }

  if (!tokens) {
    return <p>Loading...</p>
  }

  const grandTotal = Object.values(tokens).reduce(
    (sum, counts) => sum + minutesForCategory(counts),
    0
  )

  return (
    <>
      <img
        src={accountIcon}
        alt="Account"
        className="account-icon"
        onClick={() => setShowAccountMenu((m) => !m)}
      />
      {showAccountMenu && (
        <div className="account-menu">
          <button
            onClick={() => {
              setShowChangePassword(true)
              setShowAccountMenu(false)
            }}
          >
            Change Password
          </button>
          <button onClick={logout}>Logout</button>
        </div>
      )}
      {showChangePassword && (
        <ChangePassword
          authToken={authToken}
          onClose={() => setShowChangePassword(false)}
        />
      )}
      <img
        src={theme === 'dark' ? codLogoDark : codLogoLight}
        alt="Call of Duty logo"
        className="cod-logo"
      />
      <h1 className="app-title">2XP Tokens</h1>
      <p className="grand-total">{formatMinutes(grandTotal)}</p>
      <div>
        <button onClick={saveTokens} disabled={!dirty}>
          Save
        </button>
        <button onClick={resetTokens}>Reset All</button>
        <label>
          Theme:
          <select value={theme} onChange={(e) => setTheme(e.target.value)}>
            <option value="light">Light</option>
            <option value="dark">Dark</option>
          </select>
        </label>
      </div>
      <div className="categories">
        {['regular', 'weapon', 'battlepass'].map((category) => {
          const counts = tokens[category]
          const total = minutesForCategory(counts)
          return (
            <section key={category} className="category">
              <h2 className={`category-title title-${category}`}>
                <img
                  src={categoryIcons[category]}
                  alt={`${category} icon`}
                  className="category-icon"
                />
                {category.charAt(0).toUpperCase() + category.slice(1)}
              </h2>
              <p className="category-total">{formatMinutes(total)}</p>
              <ul>
                {counts.map((count, idx) => (
                  <li key={idx}>
                    {minutes[idx]} min: <span className="count">{count}</span>{' '}
                    <button onClick={() => adjustToken(category, idx, -1)}>-</button>
                    <button onClick={() => adjustToken(category, idx, 1)}>+</button>
                    <button onClick={() => setToken(category, idx)}>Enter Number</button>
                  </li>
                ))}
              </ul>
            </section>
          )
        })}
      </div>
    </>
  )
}

export default App

