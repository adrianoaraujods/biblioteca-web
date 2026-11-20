import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { livroService } from '../services/api';

function LivroList() {
  const [livros, setLivros] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    carregarLivros();
  }, []);

  const carregarLivros = async () => {
    try {
      const response = await livroService.listar();
      setLivros(response.data);
    } catch (error) {
      console.error('Erro ao carregar livros:', error);
      setErro('Não foi possível carregar os livros. O backend está rodando?');
    } finally {
      setLoading(false);
    }
  };

  const deletarLivro = async (id) => {
    if (window.confirm('Tem certeza que deseja excluir este livro?')) {
      try {
        await livroService.deletar(id);
        carregarLivros();
      } catch (error) {
        console.error('Erro ao deletar livro:', error);
        setErro('Erro ao excluir livro.');
      }
    }
  };

  if (loading) return <p>Carregando...</p>;

  return (
    <div>
      <div className="header">
        <h2>📖 Livros</h2>
        <Link to="/livros/novo" className="btn btn-primary">+ Novo Livro</Link>
      </div>

      {erro && <p className="erro">{erro}</p>}

      <table className="table">
        <thead>
          <tr>
            <th>Título</th>
            <th>Autor</th>
            <th>Ano</th>
            <th>Gênero</th>
            <th>ISBN</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {livros.map(livro => (
            <tr key={livro.id}>
              <td>{livro.titulo}</td>
              <td>{livro.autor?.nome || '-'}</td>
              <td>{livro.anoPublicacao || '-'}</td>
              <td>{livro.genero || '-'}</td>
              <td>{livro.isbn || '-'}</td>
              <td>
                <Link to={`/livros/editar/${livro.id}`} className="btn btn-sm">Editar</Link>
                <button onClick={() => deletarLivro(livro.id)} className="btn btn-danger btn-sm">
                  Excluir
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {livros.length === 0 && <p className="empty">Nenhum livro cadastrado.</p>}
    </div>
  );
}

export default LivroList;
