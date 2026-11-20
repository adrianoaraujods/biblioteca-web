import { render, screen } from '@testing-library/react';
import App from './App';

// Mock do serviço para não chamar o backend de verdade (e não carregar o axios no Jest)
jest.mock('./services/api', () => ({
  autorService: { listar: jest.fn(() => Promise.resolve({ data: [] })) }
}));

test('mostra o título e o menu da Biblioteca Web', async () => {
  render(<App />);
  expect(screen.getByText(/Biblioteca Web/i, { selector: 'h1' })).toBeInTheDocument();
  expect(screen.getByRole('link', { name: 'Autores' })).toBeInTheDocument();
  expect(screen.getByRole('link', { name: 'Livros' })).toBeInTheDocument();
  expect(await screen.findByText('Nenhum autor cadastrado.')).toBeInTheDocument();
});
