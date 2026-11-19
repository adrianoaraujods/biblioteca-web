import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { autorService } from '../services/api';

function AutorForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [autor, setAutor] = useState({
    nome: '', nacionalidade: '', dataNascimento: ''
  });
  const [erro, setErro] = useState('');

  useEffect(() => {
    if (id) {
      autorService.buscar(id).then(res => setAutor(res.data));
    }
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    // campos opcionais vazios vão como null
    const dados = {
      ...autor,
      nacionalidade: autor.nacionalidade || null,
      dataNascimento: autor.dataNascimento || null
    };
    try {
      if (id) {
        await autorService.atualizar(id, dados);
      } else {
        await autorService.criar(dados);
      }
      navigate('/autores');
    } catch (error) {
      console.error('Erro ao salvar autor:', error);
      setErro('Erro ao salvar autor. Verifique os dados.');
    }
  };

  return (
    <div>
      <h2>{id ? 'Editar Autor' : 'Novo Autor'}</h2>
      {erro && <p className="erro">{erro}</p>}
      <form onSubmit={handleSubmit} className="form">
        <div className="form-group">
          <label htmlFor="nome">Nome *</label>
          <input id="nome" type="text" value={autor.nome || ''} required maxLength={100}
            onChange={e => setAutor({...autor, nome: e.target.value})} />
        </div>
        <div className="form-group">
          <label htmlFor="nacionalidade">Nacionalidade</label>
          <input id="nacionalidade" type="text" value={autor.nacionalidade || ''} maxLength={50}
            onChange={e => setAutor({...autor, nacionalidade: e.target.value})} />
        </div>
        <div className="form-group">
          <label htmlFor="dataNascimento">Data de nascimento</label>
          <input id="dataNascimento" type="date" value={autor.dataNascimento || ''}
            onChange={e => setAutor({...autor, dataNascimento: e.target.value})} />
        </div>
        <button type="submit" className="btn btn-primary">Salvar</button>
        <button type="button" className="btn" onClick={() => navigate('/autores')}>Cancelar</button>
      </form>
    </div>
  );
}

export default AutorForm;
