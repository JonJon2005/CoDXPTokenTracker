import { useState } from 'react'

export default function Register({ onAuth, switchToLogin }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)

  const handleSubmit = (e) => {
    e.preventDefault()
    fetch('/api/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })
      .then((res) => {
        if (!res.ok) throw new Error('Registration failed')
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
      <h2>Register</h2>
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
      <button type="submit">Register</button>
      <p>
        Already have an account?{' '}
        <button type="button" onClick={switchToLogin}>
          Login
        </button>
      </p>
    </form>
  )
}

