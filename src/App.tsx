import React, { useState } from 'react';
import { 
  Landmark, 
  MapPin, 
  Calendar, 
  Sparkles, 
  Volume2, 
  VolumeX, 
  Heart, 
  Search, 
  HelpCircle, 
  MessageSquare, 
  Award, 
  ShieldCheck, 
  ArrowRight,
  LogOut,
  Send,
  Users,
  Compass,
  BookOpen
} from 'lucide-react';
import { HeritageItem, useHeritage } from './context/HeritageContext';

export const App: React.FC = () => {
  const {
    items,
    selectedCategory,
    setSelectedCategory,
    searchQuery,
    setSearchQuery,
    favorites,
    toggleFavorite,
    user,
    logout,
    activeVisitors,
    sendMessageToAI,
    isSpeaking,
    toggleSpeech,
    currentSpeechText
  } = useHeritage();

  const [activeTab, setActiveTab] = useState<'catalog' | 'detail' | 'quiz' | 'chat'>('catalog');
  const [selectedItem, setSelectedItem] = useState<HeritageItem | null>(null);
  const [chatInput, setChatInput] = useState('');
  const [chatMessages, setChatMessages] = useState<Array<{ sender: 'user' | 'ai'; text: string }>>([
    {
      sender: 'ai',
      text: 'Namaste! I am your Bharat Heritage AI Guide. Ask me anything about ancient Indian architecture, historical dynasties, UNESCO monuments, or spiritual traditions.'
    }
  ]);
  const [isAiLoading, setIsAiLoading] = useState(false);

  // Quiz state
  const [quizQuestions, setQuizQuestions] = useState<any[]>([]);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedOption, setSelectedOption] = useState<number | null>(null);
  const [score, setScore] = useState(0);
  const [quizCompleted, setQuizCompleted] = useState(false);

  const categories = [
    'All Heritage',
    'Sacred Temples',
    'Monuments',
    'Forts & Palaces',
    'Ancient Caves',
    'Heritage Cities',
    'Culture & Arts'
  ];

  const handleOpenDetail = (item: HeritageItem) => {
    setSelectedItem(item);
    setActiveTab('detail');
  };

  const handleSendChat = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!chatInput.trim() || isAiLoading) return;

    const userMsg = chatInput.trim();
    setChatMessages(prev => [...prev, { sender: 'user', text: userMsg }]);
    setChatInput('');
    setIsAiLoading(true);

    try {
      const reply = await sendMessageToAI(userMsg);
      setChatMessages(prev => [...prev, { sender: 'ai', text: reply }]);
    } catch (_) {
      setChatMessages(prev => [...prev, { 
        sender: 'ai', 
        text: 'Sorry, I encountered a brief connection error. Please try asking again.' 
      }]);
    } finally {
      setIsAiLoading(false);
    }
  };

  const startQuiz = async () => {
    try {
      const res = await fetch('/api/quiz');
      const data = await res.json();
      if (data.success && data.questions) {
        setQuizQuestions(data.questions);
        setCurrentQuestionIndex(0);
        setSelectedOption(null);
        setScore(0);
        setQuizCompleted(false);
        setActiveTab('quiz');
      }
    } catch (_) {
      setActiveTab('quiz');
    }
  };

  const handleOptionSelect = (optionIndex: number) => {
    if (selectedOption !== null) return;
    setSelectedOption(optionIndex);
    if (optionIndex === quizQuestions[currentQuestionIndex]?.answerIndex) {
      setScore(prev => prev + 1);
    }
  };

  const handleNextQuestion = () => {
    if (currentQuestionIndex + 1 < quizQuestions.length) {
      setCurrentQuestionIndex(prev => prev + 1);
      setSelectedOption(null);
    } else {
      setQuizCompleted(true);
    }
  };

  return (
    <div className="min-h-screen bg-[#FAF7F2] text-[#2C241B] flex flex-col font-['Plus_Jakarta_Sans',sans-serif]">
      {/* Top Navbar */}
      <header className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-[#E8DFD5] shadow-xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <div 
            onClick={() => setActiveTab('catalog')} 
            className="flex items-center gap-3 cursor-pointer group"
          >
            <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-[#FF6F00] via-[#D4AF37] to-[#800020] flex items-center justify-center text-white shadow-md group-hover:scale-105 transition-transform">
              <Landmark className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-['Rozha_One',serif] text-2xl tracking-wide text-[#800020]">भारत</span>
                <span className="font-['Cinzel',serif] text-xl font-bold tracking-wider text-[#FF6F00]">HERITAGE</span>
              </div>
              <p className="text-[11px] text-[#7A6A58] uppercase tracking-widest font-semibold">National Cultural Portal</p>
            </div>
          </div>

          {/* Navigation Items */}
          <nav className="hidden md:flex items-center gap-1 bg-[#F5EDE2] p-1.5 rounded-full border border-[#E4D7C8]">
            <button
              onClick={() => setActiveTab('catalog')}
              className={`px-5 py-2 rounded-full text-sm font-semibold transition-all flex items-center gap-2 ${
                activeTab === 'catalog'
                  ? 'bg-white text-[#800020] shadow-sm'
                  : 'text-[#6B5A49] hover:text-[#2C241B]'
              }`}
            >
              <Compass className="w-4 h-4" />
              Explore Catalog
            </button>
            <button
              onClick={startQuiz}
              className={`px-5 py-2 rounded-full text-sm font-semibold transition-all flex items-center gap-2 ${
                activeTab === 'quiz'
                  ? 'bg-white text-[#800020] shadow-sm'
                  : 'text-[#6B5A49] hover:text-[#2C241B]'
              }`}
            >
              <Award className="w-4 h-4" />
              Heritage Quiz
            </button>
            <button
              onClick={() => setActiveTab('chat')}
              className={`px-5 py-2 rounded-full text-sm font-semibold transition-all flex items-center gap-2 ${
                activeTab === 'chat'
                  ? 'bg-white text-[#800020] shadow-sm'
                  : 'text-[#6B5A49] hover:text-[#2C241B]'
              }`}
            >
              <Sparkles className="w-4 h-4 text-[#FF6F00]" />
              AI Guide
            </button>
          </nav>

          {/* Right Header Status & Actions */}
          <div className="flex items-center gap-3">
            <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#FAF3EB] border border-[#E9DC CE] text-xs font-semibold text-[#800020]">
              <Users className="w-3.5 h-3.5 text-[#FF6F00] animate-pulse" />
              <span>{activeVisitors} Active Explorers</span>
            </div>

            {user ? (
              <div className="flex items-center gap-2">
                <div className="hidden md:block text-right">
                  <p className="text-xs font-bold text-[#800020]">Verified Member</p>
                  <p className="text-[11px] text-[#7A6A58]">{user.phoneNumber || user.uid.substring(0, 10)}</p>
                </div>
                <button
                  onClick={logout}
                  title="Sign Out"
                  className="p-2.5 rounded-xl border border-[#E4D7C8] hover:bg-[#F2EAE0] transition-colors text-[#800020]"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-1.5 text-xs text-[#2E7D32] bg-[#E8F5E9] px-3 py-1.5 rounded-full font-bold">
                <ShieldCheck className="w-4 h-4" />
                <span>Backend Connected</span>
              </div>
            )}
          </div>
        </div>

        {/* Global Speech Banner if active */}
        {isSpeaking && (
          <div className="bg-[#800020] text-white px-4 py-2 flex items-center justify-between text-xs sm:text-sm animate-fadeIn">
            <div className="flex items-center gap-2 truncate max-w-4xl">
              <Volume2 className="w-4 h-4 text-[#FFB300] shrink-0 animate-bounce" />
              <span className="font-semibold text-[#FFB300]">Voice Narrator:</span>
              <span className="truncate opacity-95">{currentSpeechText}</span>
            </div>
            <button
              onClick={() => toggleSpeech('')}
              className="text-white hover:text-[#FFB300] px-2 py-0.5 rounded text-xs border border-white/30"
            >
              Stop Audio
            </button>
          </div>
        )}
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* TAB 1: CATALOG VIEW */}
        {activeTab === 'catalog' && (
          <div className="space-y-8">
            {/* Hero Section */}
            <div className="relative rounded-3xl overflow-hidden shadow-xl bg-gradient-to-r from-[#2A1810] via-[#4A2016] to-[#7B241C] text-white p-8 md:p-12">
              <div className="absolute inset-0 opacity-15 bg-[radial-gradient(#FFB300_1px,transparent_1px)] [background-size:16px_16px]"></div>
              <div className="relative z-10 max-w-2xl space-y-4">
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#FF6F00]/30 border border-[#FFB300]/40 text-xs font-bold text-[#FFD54F] tracking-wide uppercase">
                  <Sparkles className="w-3.5 h-3.5" />
                  Preserving 5,000 Years of Heritage
                </div>
                <h1 className="font-['Cinzel',serif] text-3xl sm:text-4xl md:text-5xl font-extrabold leading-tight">
                  Discover India's Timeless Monuments & Culture
                </h1>
                <p className="text-white/80 text-sm sm:text-base leading-relaxed">
                  Immerse yourself in sacred temples, rock-cut marvels, hill forts, and living classical arts with intelligent AI storytelling and authentic historical scholarship.
                </p>
                <div className="flex flex-wrap gap-4 pt-2">
                  <button
                    onClick={() => setActiveTab('chat')}
                    className="px-6 py-3 rounded-xl bg-gradient-to-r from-[#FF6F00] to-[#E65100] text-white font-bold text-sm shadow-lg hover:shadow-xl hover:scale-102 transition-all flex items-center gap-2"
                  >
                    <Sparkles className="w-4 h-4" />
                    Ask AI Historian
                  </button>
                  <button
                    onClick={startQuiz}
                    className="px-6 py-3 rounded-xl bg-white/10 hover:bg-white/20 backdrop-blur-md text-white font-bold text-sm border border-white/20 transition-all flex items-center gap-2"
                  >
                    <Award className="w-4 h-4 text-[#FFB300]" />
                    Test Heritage Knowledge
                  </button>
                </div>
              </div>
            </div>

            {/* Search & Category Filter Toolbar */}
            <div className="space-y-4">
              <div className="flex flex-col sm:flex-row items-center gap-4">
                <div className="relative flex-1 w-full">
                  <Search className="w-5 h-5 text-[#8D7B68] absolute left-4 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    placeholder="Search monuments, states, dynasties, architectural styles..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-12 pr-4 py-3.5 rounded-2xl bg-white border border-[#E2D5C4] focus:outline-none focus:ring-2 focus:ring-[#FF6F00] text-sm shadow-xs"
                  />
                  {searchQuery && (
                    <button
                      onClick={() => setSearchQuery('')}
                      className="absolute right-4 top-1/2 -translate-y-1/2 text-xs text-[#8D7B68] hover:text-[#2C241B]"
                    >
                      Clear
                    </button>
                  )}
                </div>
              </div>

              {/* Category Pills */}
              <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
                {categories.map(cat => (
                  <button
                    key={cat}
                    onClick={() => setSelectedCategory(cat)}
                    className={`px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold whitespace-nowrap transition-all ${
                      selectedCategory === cat
                        ? 'bg-[#800020] text-white shadow-md'
                        : 'bg-white text-[#6B5A49] border border-[#E2D5C4] hover:bg-[#F7EFE5]'
                    }`}
                  >
                    {cat}
                  </button>
                ))}
              </div>
            </div>

            {/* Monuments Cards Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {items.map(item => (
                <div
                  key={item.id}
                  onClick={() => handleOpenDetail(item)}
                  className="group bg-white rounded-3xl overflow-hidden border border-[#E6DBCE] shadow-xs hover:shadow-xl hover:-translate-y-1 transition-all cursor-pointer flex flex-col"
                >
                  {/* Card Image */}
                  <div className="relative h-56 overflow-hidden bg-[#EAE2D8]">
                    <img
                      src={item.imageUrl}
                      alt={item.name}
                      className="w-full h-full object-cover group-hover:scale-108 transition-transform duration-500"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/75 via-black/20 to-transparent"></div>

                    {/* Category & UNESCO Badges */}
                    <div className="absolute top-4 left-4 flex flex-wrap gap-2">
                      <span className="px-3 py-1 rounded-full bg-black/60 backdrop-blur-md text-[11px] font-bold text-white uppercase tracking-wider">
                        {item.category}
                      </span>
                      {item.unescoWorldHeritage && (
                        <span className="px-2.5 py-1 rounded-full bg-[#FF6F00]/90 backdrop-blur-md text-[11px] font-bold text-white shadow-xs">
                          UNESCO
                        </span>
                      )}
                    </div>

                    {/* Favorite Button */}
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        toggleFavorite(item.id);
                      }}
                      className="absolute top-4 right-4 p-2 rounded-full bg-white/80 backdrop-blur-md hover:bg-white text-[#800020] transition-colors"
                    >
                      <Heart 
                        className={`w-4 h-4 ${favorites.has(item.id) ? 'fill-[#800020]' : ''}`} 
                      />
                    </button>

                    {/* Location and Hindi Title */}
                    <div className="absolute bottom-3 left-4 right-4 text-white">
                      <p className="text-xs text-[#FFD54F] font-['Rozha_One',serif]">{item.hindiName}</p>
                      <div className="flex items-center gap-1 text-[12px] text-white/90">
                        <MapPin className="w-3.5 h-3.5 text-[#FFB300]" />
                        <span>{item.city}, {item.state}</span>
                      </div>
                    </div>
                  </div>

                  {/* Card Body */}
                  <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
                    <div>
                      <h3 className="font-['Cinzel',serif] text-lg font-bold text-[#800020] group-hover:text-[#FF6F00] transition-colors line-clamp-1">
                        {item.name}
                      </h3>
                      <div className="flex items-center gap-1.5 text-xs text-[#7A6A58] mt-1">
                        <Calendar className="w-3.5 h-3.5" />
                        <span>{item.era} ({item.yearBuilt})</span>
                      </div>
                      <p className="text-xs text-[#5D5043] mt-2 line-clamp-2 leading-relaxed">
                        {item.shortDescription}
                      </p>
                    </div>

                    <div className="pt-3 border-t border-[#F0E6D8] flex items-center justify-between text-xs">
                      <span className="font-semibold text-[#800020]">
                        By {item.constructedBy.split('&')[0]}
                      </span>
                      <span className="text-[#FF6F00] font-bold flex items-center gap-1 group-hover:translate-x-1 transition-transform">
                        Explore <ArrowRight className="w-3.5 h-3.5" />
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {items.length === 0 && (
              <div className="text-center py-16 bg-white rounded-3xl border border-[#E6DBCE] p-8 space-y-3">
                <Landmark className="w-12 h-12 text-[#B3A08D] mx-auto" />
                <h3 className="font-bold text-lg text-[#800020]">No monuments match your query</h3>
                <p className="text-xs text-[#7A6A58]">Try adjusting your search terms or selecting a different category above.</p>
                <button
                  onClick={() => { setSearchQuery(''); setSelectedCategory('All Heritage'); }}
                  className="px-5 py-2 rounded-xl bg-[#800020] text-white text-xs font-semibold"
                >
                  Reset Filters
                </button>
              </div>
            )}
          </div>
        )}

        {/* TAB 2: DETAILED MONUMENT VIEW */}
        {activeTab === 'detail' && selectedItem && (
          <div className="space-y-8 animate-fadeIn">
            <button
              onClick={() => setActiveTab('catalog')}
              className="inline-flex items-center gap-2 text-sm font-bold text-[#800020] hover:text-[#FF6F00] transition-colors"
            >
              ← Back to Catalog
            </button>

            {/* Monument Hero Image & Overlay */}
            <div className="relative rounded-3xl overflow-hidden h-80 sm:h-96 shadow-2xl">
              <img
                src={selectedItem.imageUrl}
                alt={selectedItem.name}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent"></div>

              <div className="absolute bottom-6 left-6 right-6 text-white space-y-2">
                <div className="flex flex-wrap gap-2">
                  <span className="px-3 py-1 rounded-full bg-[#FF6F00] text-xs font-bold uppercase tracking-wider">
                    {selectedItem.category}
                  </span>
                  {selectedItem.unescoWorldHeritage && (
                    <span className="px-3 py-1 rounded-full bg-white/20 backdrop-blur-md text-xs font-bold">
                      UNESCO World Heritage Site
                    </span>
                  )}
                </div>
                <h1 className="font-['Cinzel',serif] text-2xl sm:text-4xl font-extrabold">{selectedItem.name}</h1>
                <p className="font-['Rozha_One',serif] text-[#FFD54F] text-lg sm:text-xl">{selectedItem.hindiName}</p>
                <p className="text-sm text-white/80 flex items-center gap-1.5">
                  <MapPin className="w-4 h-4 text-[#FFB300]" />
                  {selectedItem.city}, {selectedItem.state} • {selectedItem.era}
                </p>
              </div>
            </div>

            {/* Action Bar */}
            <div className="flex flex-wrap items-center justify-between gap-4 p-4 rounded-2xl bg-white border border-[#E6DBCE] shadow-xs">
              <div className="flex items-center gap-3">
                <button
                  onClick={() => toggleSpeech(`${selectedItem.name}. Built in ${selectedItem.yearBuilt} by ${selectedItem.constructedBy}. ${selectedItem.fullHistory}`)}
                  className={`px-5 py-2.5 rounded-xl font-bold text-sm transition-all flex items-center gap-2 ${
                    isSpeaking 
                      ? 'bg-[#800020] text-white' 
                      : 'bg-[#FF6F00] hover:bg-[#E65100] text-white shadow-md'
                  }`}
                >
                  {isSpeaking ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4" />}
                  {isSpeaking ? 'Pause Audio Guide' : 'Listen with Audio Guide'}
                </button>
                <button
                  onClick={() => toggleFavorite(selectedItem.id)}
                  className="px-4 py-2.5 rounded-xl border border-[#E6DBCE] hover:bg-[#F5ECE0] text-sm font-semibold flex items-center gap-2 text-[#800020]"
                >
                  <Heart className={`w-4 h-4 ${favorites.has(selectedItem.id) ? 'fill-[#800020]' : ''}`} />
                  {favorites.has(selectedItem.id) ? 'Saved in Favorites' : 'Add to Favorites'}
                </button>
              </div>

              <button
                onClick={() => {
                  setChatMessages(prev => [
                    ...prev,
                    { sender: 'user', text: `Tell me detailed architectural facts about ${selectedItem.name}.` }
                  ]);
                  setActiveTab('chat');
                  sendMessageToAI(`Provide a comprehensive architectural and historical briefing on ${selectedItem.name} in ${selectedItem.state}, built in ${selectedItem.yearBuilt}.`)
                    .then(reply => {
                      setChatMessages(prev => [...prev, { sender: 'ai', text: reply }]);
                    });
                }}
                className="px-5 py-2.5 rounded-xl bg-[#FAF0E6] hover:bg-[#F2E4D5] text-[#800020] font-bold text-sm flex items-center gap-2"
              >
                <Sparkles className="w-4 h-4 text-[#FF6F00]" />
                Ask AI about this Monument
              </button>
            </div>

            {/* Detailed Content Columns */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              <div className="lg:col-span-2 space-y-6">
                <div className="bg-white p-6 sm:p-8 rounded-3xl border border-[#E6DBCE] shadow-xs space-y-4">
                  <h2 className="font-['Cinzel',serif] text-xl font-bold text-[#800020] flex items-center gap-2">
                    <BookOpen className="w-5 h-5 text-[#FF6F00]" />
                    Historical Chronicle & Significance
                  </h2>
                  <p className="text-[#3E342B] text-sm sm:text-base leading-relaxed">
                    {selectedItem.fullHistory}
                  </p>
                  <div className="pt-4 border-t border-[#F0E6D8]">
                    <h4 className="font-semibold text-xs uppercase tracking-wider text-[#800020] mb-2">Cultural & Spiritual Essence</h4>
                    <p className="text-xs text-[#5D5043] leading-relaxed">
                      {selectedItem.culturalSignificance}
                    </p>
                  </div>
                </div>

                <div className="bg-white p-6 sm:p-8 rounded-3xl border border-[#E6DBCE] shadow-xs space-y-4">
                  <h2 className="font-['Cinzel',serif] text-xl font-bold text-[#800020] flex items-center gap-2">
                    <Sparkles className="w-5 h-5 text-[#FF6F00]" />
                    Key Architectural & Archaeological Facts
                  </h2>
                  <ul className="space-y-3">
                    {selectedItem.quickFacts.map((fact, index) => (
                      <li key={index} className="flex items-start gap-3 text-sm text-[#3E342B]">
                        <span className="w-6 h-6 rounded-full bg-[#FAF0E6] text-[#FF6F00] font-bold flex items-center justify-center shrink-0 text-xs">
                          {index + 1}
                        </span>
                        <span className="pt-0.5">{fact}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>

              {/* Sidebar Info Card */}
              <div className="space-y-6">
                <div className="bg-white p-6 rounded-3xl border border-[#E6DBCE] shadow-xs space-y-4">
                  <h3 className="font-['Cinzel',serif] text-base font-bold text-[#800020]">Monument Metadata</h3>
                  <div className="space-y-3 text-xs">
                    <div>
                      <p className="text-[#7A6A58] uppercase font-bold">Constructed By</p>
                      <p className="font-semibold text-[#2C241B]">{selectedItem.constructedBy}</p>
                    </div>
                    <div>
                      <p className="text-[#7A6A58] uppercase font-bold">Architecture Style</p>
                      <p className="font-semibold text-[#2C241B]">{selectedItem.architectureStyle}</p>
                    </div>
                    <div>
                      <p className="text-[#7A6A58] uppercase font-bold">Historical Era</p>
                      <p className="font-semibold text-[#2C241B]">{selectedItem.era}</p>
                    </div>
                    <div>
                      <p className="text-[#7A6A58] uppercase font-bold">Coordinates</p>
                      <p className="font-semibold text-[#2C241B]">{selectedItem.latitude.toFixed(4)}° N, {selectedItem.longitude.toFixed(4)}° E</p>
                    </div>
                  </div>
                </div>

                <div className="bg-gradient-to-br from-[#800020] to-[#5C0017] text-white p-6 rounded-3xl shadow-lg space-y-3">
                  <h3 className="font-['Cinzel',serif] text-lg font-bold">Plan Your Visit</h3>
                  <p className="text-xs text-white/80 leading-relaxed">
                    Always respect temple guidelines, photography regulations, and archaeological boundaries preserved by the Archaeological Survey of India (ASI).
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: QUIZ VIEW */}
        {activeTab === 'quiz' && (
          <div className="max-w-2xl mx-auto space-y-6 animate-fadeIn">
            <div className="text-center space-y-2">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#FAF0E6] text-xs font-bold text-[#800020]">
                <Award className="w-4 h-4 text-[#FF6F00]" />
                Interactive Archaeological Challenge
              </div>
              <h1 className="font-['Cinzel',serif] text-3xl font-extrabold text-[#800020]">Bharat Heritage Quiz</h1>
              <p className="text-xs sm:text-sm text-[#7A6A58]">Test your understanding of India's architectural wonders, science, and civilizations.</p>
            </div>

            {quizQuestions.length > 0 && !quizCompleted && (
              <div className="bg-white p-6 sm:p-8 rounded-3xl border border-[#E6DBCE] shadow-lg space-y-6">
                <div className="flex items-center justify-between text-xs font-bold text-[#7A6A58]">
                  <span>Question {currentQuestionIndex + 1} of {quizQuestions.length}</span>
                  <span className="text-[#FF6F00]">Current Score: {score}</span>
                </div>

                <div className="w-full bg-[#EAE2D8] h-2 rounded-full overflow-hidden">
                  <div 
                    className="bg-[#FF6F00] h-full transition-all duration-300"
                    style={{ width: `${((currentQuestionIndex + 1) / quizQuestions.length) * 100}%` }}
                  ></div>
                </div>

                <h3 className="font-semibold text-lg text-[#2C241B]">
                  {quizQuestions[currentQuestionIndex].question}
                </h3>

                <div className="space-y-3">
                  {quizQuestions[currentQuestionIndex].options.map((opt: string, idx: number) => {
                    let btnStyle = "bg-white border-[#E6DBCE] hover:bg-[#FAF3EB] text-[#2C241B]";
                    if (selectedOption !== null) {
                      if (idx === quizQuestions[currentQuestionIndex].answerIndex) {
                        btnStyle = "bg-[#E8F5E9] border-[#2E7D32] text-[#2E7D32] font-bold";
                      } else if (idx === selectedOption) {
                        btnStyle = "bg-[#FFEBEE] border-[#C62828] text-[#C62828]";
                      } else {
                        btnStyle = "opacity-50 border-[#E6DBCE]";
                      }
                    }

                    return (
                      <button
                        key={idx}
                        onClick={() => handleOptionSelect(idx)}
                        disabled={selectedOption !== null}
                        className={`w-full text-left p-4 rounded-2xl border text-sm transition-all flex items-center justify-between ${btnStyle}`}
                      >
                        <span>{opt}</span>
                        {selectedOption !== null && idx === quizQuestions[currentQuestionIndex].answerIndex && (
                          <ShieldCheck className="w-4 h-4 text-[#2E7D32]" />
                        )}
                      </button>
                    );
                  })}
                </div>

                {selectedOption !== null && (
                  <div className="p-4 rounded-2xl bg-[#FAF0E6] text-xs text-[#800020] space-y-2">
                    <p className="font-bold">Explanation:</p>
                    <p>{quizQuestions[currentQuestionIndex].explanation}</p>
                    <button
                      onClick={handleNextQuestion}
                      className="mt-3 px-6 py-2.5 rounded-xl bg-[#800020] text-white font-bold text-xs shadow-md hover:bg-[#68001A]"
                    >
                      {currentQuestionIndex + 1 < quizQuestions.length ? 'Next Question →' : 'Finish Quiz'}
                    </button>
                  </div>
                )}
              </div>
            )}

            {quizCompleted && (
              <div className="bg-white p-8 rounded-3xl border border-[#E6DBCE] shadow-xl text-center space-y-4">
                <div className="w-16 h-16 rounded-full bg-[#FAF0E6] text-[#FF6F00] mx-auto flex items-center justify-center">
                  <Award className="w-8 h-8" />
                </div>
                <h2 className="font-['Cinzel',serif] text-2xl font-bold text-[#800020]">Quiz Completed!</h2>
                <p className="text-sm text-[#7A6A58]">
                  You achieved a score of <span className="font-bold text-[#FF6F00]">{score}</span> out of <span className="font-bold text-[#800020]">{quizQuestions.length}</span>!
                </p>
                <div className="pt-4 flex justify-center gap-3">
                  <button
                    onClick={startQuiz}
                    className="px-6 py-2.5 rounded-xl bg-[#800020] text-white font-bold text-xs"
                  >
                    Play Again
                  </button>
                  <button
                    onClick={() => setActiveTab('catalog')}
                    className="px-6 py-2.5 rounded-xl border border-[#E6DBCE] text-[#800020] font-bold text-xs hover:bg-[#FAF3EB]"
                  >
                    Back to Catalog
                  </button>
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 4: AI GUIDE CHAT */}
        {activeTab === 'chat' && (
          <div className="max-w-3xl mx-auto space-y-4 animate-fadeIn">
            <div className="bg-gradient-to-r from-[#800020] to-[#A93226] text-white p-6 rounded-3xl shadow-lg flex items-center justify-between">
              <div>
                <div className="inline-flex items-center gap-1.5 text-xs text-[#FFD54F] font-bold uppercase tracking-wider mb-1">
                  <Sparkles className="w-3.5 h-3.5" />
                  Gemini AI Powered Assistant
                </div>
                <h2 className="font-['Cinzel',serif] text-2xl font-bold">Bharat Heritage AI Scholar</h2>
                <p className="text-xs text-white/80">Ask questions about architecture, rituals, history, dynasty reigns, or Sanskrit terminology.</p>
              </div>
            </div>

            {/* Chat Messages Log */}
            <div className="bg-white rounded-3xl border border-[#E6DBCE] p-6 h-[460px] overflow-y-auto space-y-4 shadow-xs">
              {chatMessages.map((msg, idx) => (
                <div
                  key={idx}
                  className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  <div
                    className={`max-w-[80%] p-4 rounded-2xl text-xs sm:text-sm leading-relaxed ${
                      msg.sender === 'user'
                        ? 'bg-[#800020] text-white rounded-tr-none'
                        : 'bg-[#FAF3EB] text-[#2C241B] border border-[#EFE4D6] rounded-tl-none'
                    }`}
                  >
                    {msg.sender === 'ai' && (
                      <div className="flex items-center justify-between gap-4 mb-2 pb-1 border-b border-[#E6DACB] text-[10px] font-bold text-[#FF6F00]">
                        <span>AI GUIDE RESPONSE</span>
                        <button
                          onClick={() => toggleSpeech(msg.text)}
                          className="hover:underline flex items-center gap-1 text-[#800020]"
                        >
                          <Volume2 className="w-3 h-3" /> Listen
                        </button>
                      </div>
                    )}
                    <p className="whitespace-pre-wrap">{msg.text}</p>
                  </div>
                </div>
              ))}

              {isAiLoading && (
                <div className="flex justify-start">
                  <div className="bg-[#FAF3EB] p-4 rounded-2xl rounded-tl-none border border-[#EFE4D6] text-xs flex items-center gap-2 text-[#7A6A58]">
                    <Sparkles className="w-4 h-4 text-[#FF6F00] animate-spin" />
                    <span>Consulting archaeological records & Gemini AI...</span>
                  </div>
                </div>
              )}
            </div>

            {/* Chat Input Form */}
            <form onSubmit={handleSendChat} className="flex gap-2">
              <input
                type="text"
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                placeholder="Ask about Konark sundials, Kailasa temple engineering, Chola bronze casting..."
                className="flex-1 px-5 py-3.5 rounded-2xl bg-white border border-[#E2D5C4] focus:outline-none focus:ring-2 focus:ring-[#800020] text-sm shadow-xs"
              />
              <button
                type="submit"
                disabled={!chatInput.trim() || isAiLoading}
                className="px-6 py-3.5 rounded-2xl bg-[#800020] hover:bg-[#68001A] disabled:opacity-50 text-white font-bold text-sm shadow-md transition-all flex items-center gap-2"
              >
                <span>Send</span>
                <Send className="w-4 h-4" />
              </button>
            </form>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-[#E8DFD5] py-8 text-center text-xs text-[#7A6A58] space-y-2 mt-auto">
        <p className="font-['Cinzel',serif] font-bold text-[#800020] text-sm">
          BHARAT HERITAGE DIGITAL INITIATIVE
        </p>
        <p>Preserving Indian Art, Architecture & Culture for Generations • Real Firebase Authentication & Gemini Intelligence</p>
      </footer>
    </div>
  );
};
