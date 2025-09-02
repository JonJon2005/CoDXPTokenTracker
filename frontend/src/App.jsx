import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [tokens, setTokens] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetch('/api/tokens')
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((data) => setTokens(data))
      .catch((err) => setError(err.message))
  }, [])

  if (error) {
    return <p>Failed to load: {error}</p>
  }

  if (!tokens) {
    return <p>Loading...</p>
  }

  const minutes = [15, 30, 45, 60]

  return (
    <>
      <h1>2XP Tokens</h1>
      {Object.entries(tokens).map(([category, counts]) => (
        <div key={category}>
          <h2>{category}</h2>
          <ul>
            {counts.map((count, idx) => (
              <li key={idx}>{minutes[idx]} min: {count}</li>
            ))}
          </ul>
        </div>
      ))}
    </>
  )
}

export default App
