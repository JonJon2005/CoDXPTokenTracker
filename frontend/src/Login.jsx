import { useState } from 'react'

export default function Login({ onAuth, switchToRegister }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)

  const handleSubmit = (e) => {
    e.preventDefault()
    fetch('/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })
      .then((res) => {
        if (!res.ok) throw new Error('Login failed')
        return res.json()
      })
      .then((data) => {
        if (data.token) {
          onAuth(data.token)
        } else {
          throw new Error('No token returned')
        }
      })
      .catch((err) => setError(err.message))
  }

  return (
    <form onSubmit={handleSubmit}>
      <h2>Login</h2>
      {error && <p className="error">{error}</p>}
      <div>
        <label>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} />
        </label>
      </div>
      <div>
        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>
      </div>
      <button type="submit">Login</button>
      <p>
        Need an account?{' '}
        <button type="button" onClick={switchToRegister}>
          Register
        </button>
      </p>
    </form>
  )
}

