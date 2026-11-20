import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import LivroForm from './LivroForm';
import { autorService, livroService } from '../services/api';

jest.mock('../services/api', () => ({
  autorService: { listar: jest.fn() },
  livroService: { buscar: jest.fn(), criar: jest.fn(), atualizar: jest.fn() }
}));

beforeEach(() => {
  // o CRA reseta os mocks antes de cada teste, por isso definimos aqui
  autorService.listar.mockResolvedValue({
    data: [{ id: 1, nome: 'Machado de Assis' }, { id: 2, nome: 'Clarice Lispector' }]
  });
  livroService.criar.mockResolvedValue({ data: {} });
});

test('select de autores e envio do livro com "autor": {"id": ...}', async () => {
  render(
    <MemoryRouter initialEntries={['/livros/novo']}>
      <Routes>
        <Route path="/livros/novo" element={<LivroForm />} />
        <Route path="/livros" element={<p>lista</p>} />
      </Routes>
    </MemoryRouter>
  );

  // o select é preenchido com os autores vindos da API
  expect(await screen.findByRole('option', { name: 'Machado de Assis' })).toBeInTheDocument();

  fireEvent.change(screen.getByLabelText('Título *'), { target: { value: 'Dom Casmurro' } });
  fireEvent.change(screen.getByLabelText('Autor *'), { target: { value: '1' } });
  fireEvent.change(screen.getByLabelText('Ano de publicação'), { target: { value: '1899' } });
  fireEvent.click(screen.getByRole('button', { name: 'Salvar' }));

  await waitFor(() => expect(livroService.criar).toHaveBeenCalledWith({
    titulo: 'Dom Casmurro', isbn: null, anoPublicacao: 1899, genero: null, autor: { id: 1 }
  }));
});
