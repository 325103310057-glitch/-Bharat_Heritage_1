import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './App';
import { HeritageProvider } from './context/HeritageContext';
import './index.css';

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <HeritageProvider>
      <App />
    </HeritageProvider>
  </React.StrictMode>
);
