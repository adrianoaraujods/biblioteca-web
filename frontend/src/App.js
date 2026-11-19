import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './App.css';

function Inicio() {
  return <p>Bem-vindo à Biblioteca Web! Use o menu acima.</p>;
}

function App() {
  return (
    <Router>
      <div className="App">
        <nav className="navbar">
          <h1>📚 Biblioteca Web</h1>
          <div className="nav-links">
            <Link to="/autores">Autores</Link>
            <Link to="/livros">Livros</Link>
          </div>
        </nav>

        <main className="container">
          <Routes>
            <Route path="/" element={<Inicio />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
