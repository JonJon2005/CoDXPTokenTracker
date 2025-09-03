import { useEffect, useState } from 'react'
import './App.css'

const minutes = [15, 30, 45, 60]

function App() {
  const [tokens, setTokens] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchTokens = () => {
      fetch('/api/tokens')
        .then((res) => {
          if (!res.ok) throw new Error(`HTTP ${res.status}`)
          return res.json()
        })
        .then((data) => setTokens(data))
        .catch((err) => setError(err.message))
    }

    fetchTokens()
    const id = setInterval(fetchTokens, 5000)
    return () => clearInterval(id)
  }, [])

  const adjustToken = (category, idx, delta) => {
    const current = tokens[category][idx]
    const updatedCount = Math.max(0, current + delta)
    const updatedTokens = {
      ...tokens,
      [category]: tokens[category].map((c, i) => (i === idx ? updatedCount : c)),
    }
    setTokens(updatedTokens)
    fetch('/api/tokens', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updatedTokens),
    }).catch(() => {})
  }

  if (error) {
    return <p>Failed to load: {error}</p>
  }

  if (!tokens) {
    return <p>Loading...</p>
  }

  return (
    <>
      <h1>2XP Tokens</h1>
      {Object.entries(tokens).map(([category, counts]) => (
        <div key={category}>
          <h2>{category}</h2>
          <ul>
            {counts.map((count, idx) => (
              <li key={idx}>
                {minutes[idx]} min: {count}{' '}
                <button onClick={() => adjustToken(category, idx, -1)}>-</button>
                <button onClick={() => adjustToken(category, idx, 1)}>+</button>
              </li>
            ))}
          </ul>
        </div>
      ))}
    </>
  )
}

export default App

