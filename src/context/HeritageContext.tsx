import React, { createContext, useContext, useState, useEffect } from 'react';

export interface HeritageItem {
  id: string;
  name: string;
  hindiName: string;
  category: string;
  state: string;
  city: string;
  era: string;
  constructedBy: string;
  yearBuilt: string;
  shortDescription: string;
  fullHistory: string;
  culturalSignificance: string;
  architectureStyle: string;
  quickFacts: string[];
  imageUrl: string;
  latitude: number;
  longitude: number;
  unescoWorldHeritage: boolean;
}

export interface UserSession {
  uid: string;
  phoneNumber?: string;
  sessionToken?: string;
}

interface HeritageContextType {
  items: HeritageItem[];
  allItems: HeritageItem[];
  selectedCategory: string;
  setSelectedCategory: (cat: string) => void;
  searchQuery: string;
  setSearchQuery: (query: string) => void;
  favorites: Set<string>;
  toggleFavorite: (id: string) => void;
  user: UserSession | null;
  setUser: (user: UserSession | null) => void;
  logout: () => void;
  activeVisitors: number;
  sendMessageToAI: (message: string) => Promise<string>;
  isSpeaking: boolean;
  currentSpeechText: string;
  toggleSpeech: (text: string) => void;
}

const HeritageContext = createContext<HeritageContextType | undefined>(undefined);

export const HeritageProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [allItems, setAllItems] = useState<HeritageItem[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('All Heritage');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [favorites, setFavorites] = useState<Set<string>>(() => {
    try {
      const saved = localStorage.getItem('bharat_heritage_favorites');
      return saved ? new Set(JSON.parse(saved)) : new Set();
    } catch (_) {
      return new Set();
    }
  });

  const [user, setUser] = useState<UserSession | null>(() => {
    try {
      const savedUser = localStorage.getItem('bharat_heritage_user');
      return savedUser ? JSON.parse(savedUser) : null;
    } catch (_) {
      return null;
    }
  });

  const [activeVisitors, setActiveVisitors] = useState<number>(1);
  const [isSpeaking, setIsSpeaking] = useState<boolean>(false);
  const [currentSpeechText, setCurrentSpeechText] = useState<string>('');

  // Fetch initial catalog from backend
  useEffect(() => {
    fetch('/api/heritage')
      .then(res => res.json())
      .then(data => {
        if (data.success && Array.isArray(data.data)) {
          setAllItems(data.data);
        }
      })
      .catch(err => {
        console.warn('Could not load heritage catalog from server:', err);
      });
  }, []);

  // Connect to WebSocket for real-time live visitors stream
  useEffect(() => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws`;

    let socket: WebSocket | null = null;
    try {
      socket = new WebSocket(wsUrl);

      socket.onmessage = (event) => {
        try {
          const payload = JSON.parse(event.data);
          if (payload.type === 'EXPLORERS_UPDATE' && typeof payload.count === 'number') {
            setActiveVisitors(payload.count);
          } else if (payload.type === 'WELCOME' && typeof payload.activeExplorers === 'number') {
            setActiveVisitors(payload.activeExplorers);
          }
        } catch (_) {}
      };
    } catch (_) {}

    return () => {
      if (socket && socket.readyState === WebSocket.OPEN) {
        socket.close();
      }
    };
  }, []);

  const toggleFavorite = (id: string) => {
    setFavorites(prev => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      try {
        localStorage.setItem('bharat_heritage_favorites', JSON.stringify(Array.from(next)));
      } catch (_) {}
      return next;
    });
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('bharat_heritage_user');
  };

  const toggleSpeech = (text: string) => {
    if ('speechSynthesis' in window) {
      if (isSpeaking) {
        window.speechSynthesis.cancel();
        setIsSpeaking(false);
        setCurrentSpeechText('');
      } else {
        window.speechSynthesis.cancel();
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.rate = 0.95;
        utterance.pitch = 1.0;
        utterance.onend = () => {
          setIsSpeaking(false);
          setCurrentSpeechText('');
        };
        utterance.onerror = () => {
          setIsSpeaking(false);
          setCurrentSpeechText('');
        };
        window.speechSynthesis.speak(utterance);
        setIsSpeaking(true);
        setCurrentSpeechText(text);
      }
    }
  };

  const sendMessageToAI = async (message: string): Promise<string> => {
    const res = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message, context: 'Bharat Heritage Web Exploration' })
    });
    const data = await res.json();
    return data.reply || data.text || 'Unable to retrieve answer.';
  };

  // Filter items based on category and search query
  const filteredItems = allItems.filter(item => {
    const matchesCat = selectedCategory === 'All Heritage' || item.category === selectedCategory;
    const q = searchQuery.toLowerCase().trim();
    const matchesQuery = !q ||
      item.name.toLowerCase().includes(q) ||
      item.hindiName.toLowerCase().includes(q) ||
      item.state.toLowerCase().includes(q) ||
      item.city.toLowerCase().includes(q) ||
      item.shortDescription.toLowerCase().includes(q) ||
      item.architectureStyle.toLowerCase().includes(q) ||
      item.constructedBy.toLowerCase().includes(q);

    return matchesCat && matchesQuery;
  });

  return (
    <HeritageContext.Provider
      value={{
        items: filteredItems,
        allItems,
        selectedCategory,
        setSelectedCategory,
        searchQuery,
        setSearchQuery,
        favorites,
        toggleFavorite,
        user,
        setUser,
        logout,
        activeVisitors,
        sendMessageToAI,
        isSpeaking,
        currentSpeechText,
        toggleSpeech
      }}
    >
      {children}
    </HeritageContext.Provider>
  );
};

export const useHeritage = () => {
  const context = useContext(HeritageContext);
  if (!context) {
    throw new Error('useHeritage must be used within a HeritageProvider');
  }
  return context;
};
