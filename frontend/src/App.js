import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import AutorList from './components/AutorList';
import AutorForm from './components/AutorForm';
import './App.css';

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
            <Route path="/" element={<AutorList />} />
            <Route path="/autores" element={<AutorList />} />
            <Route path="/autores/novo" element={<AutorForm />} />
            <Route path="/autores/editar/:id" element={<AutorForm />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
