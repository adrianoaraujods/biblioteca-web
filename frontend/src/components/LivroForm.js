import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { livroService, autorService } from '../services/api';

function LivroForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [livro, setLivro] = useState({
    titulo: '', isbn: '', anoPublicacao: '', genero: '', autor: null
  });
  const [autores, setAutores] = useState([]);
  const [erro, setErro] = useState('');

  useEffect(() => {
    autorService.listar().then(res => setAutores(res.data));
    if (id) {
      livroService.buscar(id).then(res => setLivro(res.data));
    }
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    // O autor vai só com o id: "autor": {"id": 1}
    const dados = {
      titulo: livro.titulo,
      isbn: livro.isbn || null,
      anoPublicacao: livro.anoPublicacao ? parseInt(livro.anoPublicacao, 10) : null,
      genero: livro.genero || null,
      autor: livro.autor?.id ? { id: livro.autor.id } : null
    };
    try {
      if (id) {
        await livroService.atualizar(id, dados);
      } else {
        await livroService.criar(dados);
      }
      navigate('/livros');
    } catch (error) {
      console.error('Erro ao salvar livro:', error);
      setErro(error.response?.data?.mensagem || 'Erro ao salvar livro. Verifique os dados.');
    }
  };

  return (
    <div>
      <h2>{id ? 'Editar Livro' : 'Novo Livro'}</h2>
      {erro && <p className="erro">{erro}</p>}
      <form onSubmit={handleSubmit} className="form">
        <div className="form-group">
          <label htmlFor="titulo">Título *</label>
          <input id="titulo" type="text" value={livro.titulo || ''} required maxLength={200}
            onChange={e => setLivro({...livro, titulo: e.target.value})} />
        </div>
        <div className="form-group">
          <label htmlFor="autor">Autor *</label>
          <select id="autor" value={livro.autor?.id || ''} required
            onChange={e => setLivro({...livro,
              autor: e.target.value ? {id: parseInt(e.target.value, 10)} : null})}>
            <option value="">Selecione um autor</option>
            {autores.map(a => (
              <option key={a.id} value={a.id}>{a.nome}</option>
            ))}
          </select>
        </div>
        <div className="form-group">
          <label htmlFor="isbn">ISBN</label>
          <input id="isbn" type="text" value={livro.isbn || ''} maxLength={20}
            onChange={e => setLivro({...livro, isbn: e.target.value})} />
        </div>
        <div className="form-group">
          <label htmlFor="anoPublicacao">Ano de publicação</label>
          <input id="anoPublicacao" type="number" value={livro.anoPublicacao ?? ''}
            onChange={e => setLivro({...livro, anoPublicacao: e.target.value})} />
        </div>
        <div className="form-group">
          <label htmlFor="genero">Gênero</label>
          <input id="genero" type="text" value={livro.genero || ''} maxLength={50}
            onChange={e => setLivro({...livro, genero: e.target.value})} />
        </div>
        <button type="submit" className="btn btn-primary">Salvar</button>
        <button type="button" className="btn" onClick={() => navigate('/livros')}>Cancelar</button>
      </form>
    </div>
  );
}

export default LivroForm;
