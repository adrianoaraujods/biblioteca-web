import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { autorService } from '../services/api';

function AutorList() {
  const [autores, setAutores] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    carregarAutores();
  }, []);

  const carregarAutores = async () => {
    try {
      const response = await autorService.listar();
      setAutores(response.data);
    } catch (error) {
      console.error('Erro ao carregar autores:', error);
      setErro('Não foi possível carregar os autores. O backend está rodando?');
    } finally {
      setLoading(false);
    }
  };

  const deletarAutor = async (id) => {
    if (window.confirm('Tem certeza que deseja excluir este autor?')) {
      try {
        await autorService.deletar(id);
        setErro('');
        carregarAutores();
      } catch (error) {
        console.error('Erro ao deletar autor:', error);
        setErro(error.response?.data?.mensagem || 'Erro ao excluir autor.');
      }
    }
  };

  if (loading) return <p>Carregando...</p>;

  return (
    <div>
      <div className="header">
        <h2>✍️ Autores</h2>
        <Link to="/autores/novo" className="btn btn-primary">+ Novo Autor</Link>
      </div>

      {erro && <p className="erro">{erro}</p>}

      <table className="table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Nacionalidade</th>
            <th>Data de nascimento</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {autores.map(autor => (
            <tr key={autor.id}>
              <td>{autor.nome}</td>
              <td>{autor.nacionalidade || '-'}</td>
              <td>{autor.dataNascimento
                ? autor.dataNascimento.split('-').reverse().join('/')
                : '-'}</td>
              <td>
                <Link to={`/autores/editar/${autor.id}`} className="btn btn-sm">Editar</Link>
                <button onClick={() => deletarAutor(autor.id)} className="btn btn-danger btn-sm">
                  Excluir
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {autores.length === 0 && <p className="empty">Nenhum autor cadastrado.</p>}
    </div>
  );
}

export default AutorList;
