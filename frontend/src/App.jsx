import { useCallback, useEffect, useRef, useState } from 'react'
import './App.css'

function App() {
  const [tokens, setTokens] = useState(null)
  const [error, setError] = useState(null)
  const [editing, setEditing] = useState(false)
  const editingRef = useRef(false)

  useEffect(() => {
    editingRef.current = editing
  }, [editing])

  const fetchTokens = useCallback(() => {
    if (editingRef.current) return
    fetch('/api/tokens')
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((data) => setTokens(data))
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    fetchTokens()
    const id = setInterval(fetchTokens, 5000)
    return () => clearInterval(id)
  }, [fetchTokens])

  if (error) {
    return <p>Failed to load: {error}</p>
  }

  if (!tokens) {
    return <p>Loading...</p>
  }

  const minutes = [15, 30, 45, 60]

  const updateToken = (category, idx, delta) => {
    setTokens((prev) => {
      const next = { ...prev }
      const arr = [...next[category]]
      arr[idx] = Math.max(0, arr[idx] + delta)
      next[category] = arr
      return next
    })
    setEditing(true)
  }

  const handleSave = () => {
    fetch('/api/tokens', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(tokens),
    })
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        setEditing(false)
      })
      .catch((err) => setError(err.message))
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
                {minutes[idx]} min:
                <button onClick={() => updateToken(category, idx, -1)}>-</button>
                {count}
                <button onClick={() => updateToken(category, idx, 1)}>+</button>
              </li>
            ))}
          </ul>
        </div>
      ))}
      {editing && <button onClick={handleSave}>Save</button>}
    </>
  )
}

export default App
