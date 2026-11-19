import { render, screen } from '@testing-library/react';
import App from './App';

test('mostra o título e o menu da Biblioteca Web', () => {
  render(<App />);
  expect(screen.getByText(/Biblioteca Web/i, { selector: 'h1' })).toBeInTheDocument();
  expect(screen.getByRole('link', { name: 'Autores' })).toBeInTheDocument();
  expect(screen.getByRole('link', { name: 'Livros' })).toBeInTheDocument();
});
