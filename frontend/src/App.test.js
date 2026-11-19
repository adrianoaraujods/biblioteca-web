import { render, screen } from '@testing-library/react';
import App from './App';
import { autorService } from './services/api';

// Mock dos serviços para não chamar o backend de verdade (e não carregar o axios no Jest)
jest.mock('./services/api', () => ({
  autorService: { listar: jest.fn() },
  livroService: { listar: jest.fn() }
}));

beforeEach(() => {
  // o CRA reseta os mocks antes de cada teste, por isso definimos aqui
  autorService.listar.mockResolvedValue({ data: [] });
});

test('mostra o título e o menu da Biblioteca Web', async () => {
  render(<App />);
  expect(screen.getByText(/Biblioteca Web/i, { selector: 'h1' })).toBeInTheDocument();
  expect(screen.getByRole('link', { name: 'Autores' })).toBeInTheDocument();
  expect(screen.getByRole('link', { name: 'Livros' })).toBeInTheDocument();
  expect(await screen.findByText('Nenhum autor cadastrado.')).toBeInTheDocument();
});
