import express, { Request, Response } from 'express';
import cors from 'cors';
import path from 'path';
import http from 'http';
import { WebSocketServer, WebSocket } from 'ws';
import * as admin from 'firebase-admin';
import dotenv from 'dotenv';

dotenv.config();

const app = express();
const server = http.createServer(app);
const wss = new WebSocketServer({ server, path: '/ws' });

const PORT = process.env.PORT || 3000;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || '';

// Initialize Firebase Admin if credentials are provided
if (!admin.apps.length) {
  try {
    if (process.env.FIREBASE_PROJECT_ID && process.env.FIREBASE_CLIENT_EMAIL && process.env.FIREBASE_PRIVATE_KEY) {
      admin.initializeApp({
        credential: admin.credential.cert({
          projectId: process.env.FIREBASE_PROJECT_ID,
          clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
          privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n'),
        }),
      });
      console.log('Firebase Admin SDK initialized with environment credentials.');
    } else {
      admin.initializeApp();
      console.log('Firebase Admin SDK initialized with default credentials.');
    }
  } catch (error) {
    console.warn('Firebase Admin initialization deferred/fallback mode:', (error as Error).message);
  }
}

app.use(cors());
app.use(express.json());

// Built-in Curated Heritage Knowledge Base
export const HERITAGE_ITEMS = [
  {
    id: "konark-sun-temple",
    name: "Konark Sun Temple",
    hindiName: "कोणार्क सूर्य मंदिर",
    category: "Sacred Temples",
    state: "Odisha",
    city: "Puri / Konark",
    era: "13th Century CE (Eastern Ganga Dynasty)",
    constructedBy: "King Narasimhadeva I",
    yearBuilt: "1250 CE",
    shortDescription: "Architectural marvel shaped as a colossal 24-wheeled chariot of Surya with intricately carved stone sundial wheels.",
    fullHistory: "The Sun Temple at Konark was built in the 13th century by King Narasimhadeva I of the Eastern Ganga dynasty. Conceived as a colossal celestial chariot for the Sun God Surya, the temple stands on a high platform with 24 carved stone wheels, each nearly 10 feet in diameter, pulled by seven stone horses. The wheels function as sundials capable of calculating exact solar time down to minutes.",
    culturalSignificance: "One of India's most venerated sun temples, celebrating cosmic solar worship and ancient Indian mathematics, astronomy, and classical Odissi iconography.",
    architectureStyle: "Kalinga Architecture (Rekha Deula and Pidha Deula styles)",
    quickFacts: [
      "Sundial wheels predict precise time by reading shadow positions.",
      "Seven horses represent the seven days of the solar week.",
      "UNESCO World Heritage Site designated in 1984."
    ],
    imageUrl: "https://images.unsplash.com/photo-1600100397608-f010f443b749?w=800&q=80",
    latitude: 19.8876,
    longitude: 86.0945,
    unescoWorldHeritage: true
  },
  {
    id: "hampi-vijayanagara",
    name: "Hampi Vijayanagara",
    hindiName: "हम्पी विजयनगर",
    category: "Monuments",
    state: "Karnataka",
    city: "Vijayanagara / Hospet",
    era: "14th - 16th Century CE",
    constructedBy: "Harihara I, Bukka Raya & Krishnadevaraya",
    yearBuilt: "1336 CE",
    shortDescription: "The legendary capital of the Vijayanagara Empire along the Tungabhadra River, famed for the Stone Chariot and Vittala Temple.",
    fullHistory: "Hampi was the grand jewel of the Vijayanagara Empire from 1336 to 1565. At its zenith, it was described by Persian and European travelers like Domingo Paes and Abdur Razzaq as one of the richest and second-largest cities in the medieval world, trading in diamonds, silks, and Arabian horses. The complex boasts the monolithic Stone Chariot, musical pillars of Vittala Temple, and Virupaksha Temple.",
    culturalSignificance: "Spiritual epicenter of southern India and eternal symbol of South Indian Renaissance in literature, sculpture, and civil architecture.",
    architectureStyle: "Vijayanagara Dravidian Temple Architecture",
    quickFacts: [
      "Vittala Temple pillars produce distinct musical notes when tapped.",
      "Virupaksha Temple has an uninterrupted living worship heritage of over 700 years.",
      "Spread across over 4,100 hectares with over 1,600 surviving monuments."
    ],
    imageUrl: "https://images.unsplash.com/photo-1609137144822-482d8c3664c3?w=800&q=80",
    latitude: 15.3350,
    longitude: 76.4600,
    unescoWorldHeritage: true
  },
  {
    id: "brihadisvara-temple",
    name: "Brihadisvara Temple",
    hindiName: "बृहदीश्वर मन्दिर (तंजावुर)",
    category: "Sacred Temples",
    state: "Tamil Nadu",
    city: "Thanjavur",
    era: "11th Century CE (Chola Dynasty)",
    constructedBy: "Rajaraja Chola I",
    yearBuilt: "1010 CE",
    shortDescription: "Peruvudaiyar Kovil, towering 216-foot granite vimana crowned by an 80-tonne monolithic stone dome.",
    fullHistory: "Commissioned by the Great Emperor Rajaraja Chola I, Brihadisvara Temple (Big Temple) in Thanjavur is one of the grandest granite stone structures in human history. Completed in 1010 CE, its central Vimana tower rises 66 meters into the sky without the use of binding mortar, utilizing interlocking granite blocks. The temple is dedicated to Lord Shiva as Nataraja and Adavallan.",
    culturalSignificance: "The pinnacle of Chola imperial art and South Indian bronze casting, classical music, and Natyasastra dance tradition.",
    architectureStyle: "Chola Dravidian Architecture",
    quickFacts: [
      "The Kumbam apex capstone weighs approximately 80 metric tonnes.",
      "Built completely with granite brought from quarries over 50 kilometers away.",
      "Houses the largest monolithic Nandi statue in Tamil Nadu."
    ],
    imageUrl: "https://images.unsplash.com/photo-1582510003544-4d00b7f74220?w=800&q=80",
    latitude: 10.7828,
    longitude: 79.1318,
    unescoWorldHeritage: true
  },
  {
    id: "ajanta-ellora-caves",
    name: "Ajanta & Ellora Caves",
    hindiName: "अजन्ता और एलोरा गुफाएँ",
    category: "Ancient Caves",
    state: "Maharashtra",
    city: "Chhatrapati Sambhajinagar",
    era: "2nd Century BCE - 10th Century CE",
    constructedBy: "Satavahana, Vakataka, Rashtrakuta & Chalukya dynasties",
    yearBuilt: "200 BCE - 1000 CE",
    shortDescription: "Ancient rock-cut cave sanctuaries featuring timeless Jataka murals and the breathtaking Kailasa monolith.",
    fullHistory: "Carved directly into volcanic basalt horseshoe cliffs along the Waghur River, Ajanta Caves preserve classical Indian mural painting depicting scenes from the life of Buddha. Ellora features 34 rock-cut temples representing Buddhism, Hinduism, and Jainism in harmony. Ellora's Cave 16 houses the Kailasa Temple, the world's largest single monolithic rock-cut structure carved top-down out of a sheer mountain.",
    culturalSignificance: "Represents classical Indian fresco mastery and the synthesis of Buddhist, Hindu, and Jain spiritual traditions.",
    architectureStyle: "Ancient Indian Rock-Cut Architecture",
    quickFacts: [
      "Kailasa temple was carved top-down from a single cliff face with over 200,000 tonnes of rock excavated.",
      "Ajanta frescoes retain mineral pigment vibrancy after two millennia.",
      "Hidden in dense jungle until rediscovered by John Smith in 1819."
    ],
    imageUrl: "https://images.unsplash.com/photo-1620766182966-c6eb5ed2b788?w=800&q=80",
    latitude: 20.5519,
    longitude: 75.7033,
    unescoWorldHeritage: true
  },
  {
    id: "amber-fort-jaipur",
    name: "Amber Fort & Palace",
    hindiName: "आमेर किला (जयपुर)",
    category: "Forts & Palaces",
    state: "Rajasthan",
    city: "Jaipur",
    era: "16th - 17th Century CE",
    constructedBy: "Raja Man Singh I & Mirza Raja Jai Singh",
    yearBuilt: "1592 CE",
    shortDescription: "Hilltop Rajput fortress of red sandstone and marble overlooking Maota Lake, renowned for Sheesh Mahal.",
    fullHistory: "Perched atop the rugged Aravalli hills overlooking Maota Lake, Amber Fort was the royal seat of the Kachwaha Rajputs before Jaipur was founded. Constructed with yellow and red sandstone and marble, it comprises four distinct courtyards, the Diwan-e-Aam, the Summer Palace (Sukh Niwas) cooled by water cascades, and the ethereal Sheesh Mahal (Mirror Palace) inlaid with Belgian concave glass.",
    culturalSignificance: "Exemplifies the Rajput chivalric valor, royal artistry, and hydraulic cooling innovations in arid climates.",
    architectureStyle: "Rajput - Indo-Islamic Fusion Architecture",
    quickFacts: [
      "Sheesh Mahal can be illuminated by a single candle flame reflecting through convex mirrors.",
      "Connected by underground subterranean tunnels to the defensive Jaigarh Fort.",
      "Part of the UNESCO Hill Forts of Rajasthan cluster."
    ],
    imageUrl: "https://images.unsplash.com/photo-1599661046289-e31897846e41?w=800&q=80",
    latitude: 26.9855,
    longitude: 75.8513,
    unescoWorldHeritage: true
  },
  {
    id: "qutub-minar",
    name: "Qutub Minar Complex",
    hindiName: "क़ुतुब मीनार",
    category: "Monuments",
    state: "Delhi",
    city: "New Delhi",
    era: "12th - 14th Century CE",
    constructedBy: "Qutb-ud-din Aibak, Iltutmish & Alauddin Khilji",
    yearBuilt: "1199 CE",
    shortDescription: "72.5-meter fluted red sandstone minaret alongside the 1,600-year-old rust-resistant Gupta Iron Pillar.",
    fullHistory: "The Qutub Minar is the world's tallest brick minaret, soaring 72.5 meters high with five tapering storeys adorned with intricate calligraphy and projecting balconies. Within its serene courtyard stands the famed 4th-century Gupta Iron Pillar, renowned for its metallurgical mystery of remaining completely rust-free despite exposure to atmospheric weathering for over 1,600 years.",
    culturalSignificance: "Marks the transition into medieval Delhi Sultanate architecture alongside ancient Indian metallurgical brilliance.",
    architectureStyle: "Indo-Islamic & Fluted Red Sandstone Masonry",
    quickFacts: [
      "The Iron Pillar contains high phosphorus content that formed a protective iron hydrogen phosphate layer.",
      "Features 379 spiral stone steps inside the tower.",
      "Alai Darwaza within the complex exhibits true arch and dome engineering."
    ],
    imageUrl: "https://images.unsplash.com/photo-1587474260584-136574528ed5?w=800&q=80",
    latitude: 28.5245,
    longitude: 77.1855,
    unescoWorldHeritage: true
  },
  {
    id: "khajuraho-temples",
    name: "Khajuraho Group of Monuments",
    hindiName: "खजुराहो स्मारक समूह",
    category: "Sacred Temples",
    state: "Madhya Pradesh",
    city: "Chhatarpur / Khajuraho",
    era: "10th - 11th Century CE (Chandela Dynasty)",
    constructedBy: "Chandela Kings (Yashovarman, Dhanga)",
    yearBuilt: "950 - 1050 CE",
    shortDescription: "Nagara-style stone temples celebrated for exquisite sculptural harmony, spirituality, and celebration of life.",
    fullHistory: "The Khajuraho temples were built between 950 and 1050 CE by the Chandela dynasty. Of the original 85 temples, 25 survive today across western, eastern, and southern groups. The Kandariya Mahadeva Temple is the largest and most ornate, designed to represent Mount Kailash with intricate vertical shikhara spires ascending harmoniously.",
    culturalSignificance: "Celebration of the four Purusharthas: Dharma (duty), Artha (prosperity), Kama (desire & aesthetics), and Moksha (spiritual liberation).",
    architectureStyle: "Nagara Style (Panchayatana Temple Layout)",
    quickFacts: [
      "Erotic carvings constitute less than 10% of the sculptures, while 90% depict daily life, music, and devotion.",
      "Built using sandstone blocks fitted together with mortise and tenon joints.",
      "Rediscovered by British surveyor T.S. Burt in the 1830s."
    ],
    imageUrl: "https://images.unsplash.com/photo-1608958435020-e8a7109ba809?w=800&q=80",
    latitude: 24.8318,
    longitude: 79.9199,
    unescoWorldHeritage: true
  },
  {
    id: "varanasi-ghats",
    name: "Varanasi & Sacred Ganga Ghats",
    hindiName: "काशी / वाराणसी के घाट",
    category: "Heritage Cities",
    state: "Uttar Pradesh",
    city: "Varanasi (Kashi)",
    era: "Antiquity (Over 3,000 years continuous history)",
    constructedBy: "Maratha, Scindia, Holkar rulers & Ancient Kashi kings",
    yearBuilt: "Continuous Living Heritage",
    shortDescription: "The eternal city of Lord Shiva, lined with 84 sacred riverfront stone ghats and mesmerizing Ganga Aarti.",
    fullHistory: "Varanasi, also known as Kashi and Benares, is universally revered as one of the world's oldest continuously inhabited sacred cities. Marking the crescent bend of the holy Ganges, its 84 stone ghats—including Dashashwamedh, Assi, and Manikarnika—were rebuilt by Maratha patrons like Ahilyabai Holkar in the 18th century. It is the seat of sacred scholarship, classical Hindustani music, Banarasi silk weaving, and spiritual liberation.",
    culturalSignificance: "Center of Sanatana spiritual philosophy, Sanskrit learning, Kabir, Tulsidas, and Indian classical music gharanas.",
    architectureStyle: "Sacred Riverfront Stone Stepped Architecture",
    quickFacts: [
      "Mark Twain remarked: 'Benares is older than history, older than tradition, older even than legend.'",
      "Evening Maha Aarti at Dashashwamedh Ghat has been performed continuously for centuries.",
      "Birthplace of the Banarasi brocade silk handloom tradition."
    ],
    imageUrl: "https://images.unsplash.com/photo-1561361513-2d000a50f0dc?w=800&q=80",
    latitude: 25.3176,
    longitude: 82.9739,
    unescoWorldHeritage: false
  },
  {
    id: "nalanda-mahavihara",
    name: "Ancient Nalanda Mahavihara",
    hindiName: "नालन्दा महाविहार",
    category: "Monuments",
    state: "Bihar",
    city: "Rajgir / Nalanda",
    era: "5th - 12th Century CE (Gupta to Pala Empires)",
    constructedBy: "Kumargupta I & King Harshavardhana",
    yearBuilt: "427 CE",
    shortDescription: "Ancient residential university of international learning that housed 10,000 scholars and 9 million manuscripts.",
    fullHistory: "Nalanda was one of the earliest residential universities in recorded history. Established during the golden age of the Gupta Empire, it attracted scholars and pilgrims from China, Korea, Japan, Tibet, Persia, and Sumatra. Chinese pilgrim Xuanzang (Hiuen Tsang) spent years studying here. Its legendary nine-storey library, Dharmaganja, housed priceless scriptures on astronomy, mathematics, medicine, and Buddhist philosophy.",
    culturalSignificance: "Symbol of ancient India's global intellectual leadership, free residential pedagogy, and universal knowledge exchange.",
    architectureStyle: "Ancient Monastic Brick Architecture (Viharas & Stupas)",
    quickFacts: [
      "Housed 10,000 students and 2,000 faculty entirely supported through royal endowments.",
      "Entrance examinations conducted by gatekeeper professors had a 20% admission rate.",
      "Aryabhata, father of Indian mathematics, was an illustrious chancellor/alumnus."
    ],
    imageUrl: "https://images.unsplash.com/photo-1628155930542-3c7a64e2c833?w=800&q=80",
    latitude: 25.1357,
    longitude: 85.4439,
    unescoWorldHeritage: true
  },
  {
    id: "bharatanatyam-tradition",
    name: "Bharatanatyam & Classical Natya",
    hindiName: "भरतनाट्यम शास्त्रीय परंपरा",
    category: "Culture & Arts",
    state: "Tamil Nadu & Pan-India",
    city: "Thanjavur / Chennai",
    era: "Ancient Vedic era (Codified in Natyashastra ~200 BCE)",
    constructedBy: "Sage Bharata Muni & Devadasi lineages",
    yearBuilt: "Ancient Antiquity",
    shortDescription: "India's oldest classical dance tradition synthesizing Bhava (expression), Raga (melody), and Tala (rhythm).",
    fullHistory: "Bharatanatyam originated in the sacred temples of Tamil Nadu, formerly known as Sadir or Dasi Attam. Codified in Bharata Muni's treatise 'Natyashastra' and Nandikeshavara's 'Abhinaya Darpana', it was preserved by temple artists and court composers like the Thanjavur Quartet. The form combines pure geometric rhythmic footwork (Nritta) with expressive narrative mime (Abhinaya).",
    culturalSignificance: "Living embodiment of ancient Indian theatrical philosophy, Carnatic rhythm cycles, and poetic storytelling.",
    architectureStyle: "Classical Performing Arts & Natyashastra Tradition",
    quickFacts: [
      "The name Bha-Ra-Ta is an acronym for Bhava (emotion), Raga (melody), and Tala (rhythm).",
      "Chidambaram Temple stone carvings depict all 108 Karanas of Bharatanatyam.",
      "Revitalized in the early 20th century by Rukmini Devi Arundale."
    ],
    imageUrl: "https://images.unsplash.com/photo-1547153760-18fc86324498?w=800&q=80",
    latitude: 11.3992,
    longitude: 79.6934,
    unescoWorldHeritage: false
  }
];

// Interactive Quiz Questions
export const QUIZ_QUESTIONS = [
  {
    id: 1,
    question: "Which Indian temple features 24 stone wheels functioning as precision solar sundials?",
    options: ["Brihadisvara Temple", "Konark Sun Temple", "Kailasa Temple", "Virupaksha Temple"],
    answerIndex: 1,
    explanation: "The Konark Sun Temple in Odisha features 24 carved stone wheels that read solar time accurately down to minutes."
  },
  {
    id: 2,
    question: "What ancient residential university in Bihar once housed 10,000 students and over 9 million manuscripts?",
    options: ["Takshashila", "Nalanda Mahavihara", "Vikramashila", "Vallabhi"],
    answerIndex: 1,
    explanation: "Ancient Nalanda Mahavihara, founded in 427 CE, was the world's premier residential center for higher learning."
  },
  {
    id: 3,
    question: "The Kailasa Temple at Ellora (Cave 16) is historically unique because it was:",
    options: ["Built from wood and later petrified", "Carved top-down from a single monolithic basalt cliff", "Assembled from 500,000 separate granite blocks", "Submerged beneath a sacred lake"],
    answerIndex: 1,
    explanation: "Kailasa Temple is the largest monolithic rock-cut structure on Earth, carved top-down out of a single mountain cliff."
  },
  {
    id: 4,
    question: "Why does the 4th-century Gupta Iron Pillar at Qutub Minar resist corrosion for over 1,600 years?",
    options: ["Coated with modern synthetic lacquer", "High phosphorus iron forming a protective passive film", "Made of pure gold alloy", "Stored in vacuum chamber"],
    answerIndex: 1,
    explanation: "Ancient Indian metallurgists created an iron alloy high in phosphorus, creating an unbroken passive protective layer of misawite."
  },
  {
    id: 5,
    question: "The word 'Bharatanatyam' derives its name as an acronym of which three aesthetic elements?",
    options: ["Brahma, Rama, Tatva", "Bhava (emotion), Raga (melody), Tala (rhythm)", "Bharat, Rashtra, Tejas", "Bhakti, Riti, Tandav"],
    answerIndex: 1,
    explanation: "Bha-Ra-Ta stands for Bhava (emotion/expression), Raga (melody), and Tala (rhythm) from the Natyashastra."
  }
];

// --- API ROUTES ---

// 1. Health check (Used by Android app and Render uptime monitors)
app.get('/api/health', (req: Request, res: Response) => {
  res.json({
    status: 'online',
    service: 'Bharat Heritage Backend & AI Service',
    uptimeSeconds: Math.floor(process.uptime()),
    timestamp: new Date().toISOString()
  });
});

// 2. Heritage items catalog
app.get('/api/heritage', (req: Request, res: Response) => {
  const { category, search } = req.query;
  let results = HERITAGE_ITEMS;

  if (category && category !== 'All Heritage') {
    results = results.filter(item => item.category === category);
  }

  if (search && typeof search === 'string') {
    const q = search.toLowerCase().trim();
    results = results.filter(item =>
      item.name.toLowerCase().includes(q) ||
      item.hindiName.toLowerCase().includes(q) ||
      item.state.toLowerCase().includes(q) ||
      item.city.toLowerCase().includes(q) ||
      item.shortDescription.toLowerCase().includes(q) ||
      item.architectureStyle.toLowerCase().includes(q)
    );
  }

  res.json({ success: true, count: results.length, data: results });
});

// 3. Single heritage item by ID
app.get('/api/heritage/:id', (req: Request, res: Response) => {
  const item = HERITAGE_ITEMS.find(h => h.id === req.params.id);
  if (!item) {
    return res.status(404).json({ success: false, error: 'Monument or heritage item not found' });
  }
  res.json({ success: true, data: item });
});

// 4. Interactive Heritage Quiz
app.get('/api/quiz', (req: Request, res: Response) => {
  res.json({ success: true, questions: QUIZ_QUESTIONS });
});

// 5. Firebase Token Verification (Endpoint called by Android app & Web client)
app.post('/api/auth/verify-token', async (req: Request, res: Response) => {
  try {
    const authHeader = req.headers.authorization;
    const tokenFromBody = req.body?.idToken;
    const idToken = authHeader?.startsWith('Bearer ') ? authHeader.split('Bearer ')[1] : tokenFromBody;

    if (!idToken) {
      return res.status(401).json({
        success: false,
        error: 'Missing Firebase ID token in Authorization header or body'
      });
    }

    if (admin.apps.length && admin.auth()) {
      try {
        const decodedToken = await admin.auth().verifyIdToken(idToken, true);
        return res.status(200).json({
          success: true,
          uid: decodedToken.uid,
          phoneNumber: decodedToken.phone_number || null,
          sessionToken: idToken,
          message: 'Firebase token verified successfully via Admin SDK'
        });
      } catch (err: any) {
        console.warn('Firebase Admin verification failed:', err.message);
        // If Firebase admin keys aren't set up yet on Render, gracefully decode payload or return helpful message
        return res.status(401).json({
          success: false,
          error: err.code === 'auth/id-token-expired' ? 'Token expired. Please request a new OTP.' : 'Invalid Firebase ID token signature'
        });
      }
    } else {
      // In development fallback if Admin SDK is unconfigured
      return res.status(200).json({
        success: true,
        uid: 'dev-user-' + Date.now(),
        phoneNumber: '+919876543210',
        sessionToken: idToken,
        message: 'Dev mode: Token accepted (Configure FIREBASE_PROJECT_ID on Render for strict check)'
      });
    }
  } catch (error: any) {
    res.status(500).json({ success: false, error: error.message || 'Internal server error during verification' });
  }
});

// 6. AI Chatbot & Heritage Guide powered by Google Gemini API
app.post('/api/ai/chat', async (req: Request, res: Response) => {
  const { message, context } = req.body;

  if (!message || typeof message !== 'string') {
    return res.status(400).json({ error: 'Field "message" is required' });
  }

  // If Gemini API key is configured, use Google GenAI
  if (GEMINI_API_KEY && GEMINI_API_KEY !== 'MY_GEMINI_API_KEY') {
    try {
      const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{ parts: [{ text: message }] }],
          systemInstruction: {
            parts: [{
              text: "You are the Bharat Heritage AI Guide, an expert scholar on Indian history, monuments, temples, caves, philosophy, culture, and arts. Answer gracefully with accurate historical facts, cultural nuance, and engaging explanations. Keep answers clear, authentic, and inspiring. Context: " + (context || "Bharat Heritage Web & Mobile Platform")
            }]
          }
        })
      });

      if (response.ok) {
        const data: any = await response.json();
        const reply = data.candidates?.[0]?.content?.parts?.[0]?.text;
        if (reply) {
          return res.json({ reply, source: 'gemini-2.5-flash' });
        }
      }
    } catch (geminiError: any) {
      console.warn('Gemini API call failed, falling back to curated knowledge engine:', geminiError.message);
    }
  }

  // Built-in Curated Fallback
  const q = message.toLowerCase();
  let reply = "Bharat Heritage represents five millennia of unbroken civilization, profound architectural wisdom, sacred philosophy, and artistic traditions. Explore the monuments in the catalog to learn about their history, architecture, and timeless cultural significance.";

  if (q.includes('konark') || q.includes('sun temple')) {
    reply = "The Konark Sun Temple in Odisha was built in 1250 CE by King Narasimhadeva I. Shaped as Surya's chariot with 24 giant stone wheels that function as accurate solar sundials, it is a magnificent pinnacle of Kalinga architecture.";
  } else if (q.includes('hampi') || q.includes('vijayanagara')) {
    reply = "Hampi was the grand capital of the Vijayanagara Empire from 1336 to 1565. Famous for the Stone Chariot, musical pillars of Vittala Temple, and 700-year continuous living worship at Virupaksha Temple, it was one of the medieval world's richest cities.";
  } else if (q.includes('brihadisvara') || q.includes('thanjavur') || q.includes('chola')) {
    reply = "Brihadisvara Temple (Peruvudaiyar Kovil) was completed in 1010 CE by Rajaraja Chola I. Its 216-foot granite Vimana is crowned with an 80-tonne monolithic stone capstone built without binding mortar.";
  } else if (q.includes('ajanta') || q.includes('ellora') || q.includes('kailasa')) {
    reply = "Ajanta preserves 2,000-year-old Buddhist mural frescoes, while Ellora unites Buddhist, Hindu, and Jain cave temples. Cave 16 (Kailasa Temple) is the world's largest monolithic structure carved top-down from a single mountain cliff.";
  } else if (q.includes('amber') || q.includes('jaipur')) {
    reply = "Amber Fort in Jaipur, built by Raja Man Singh I in 1592 CE, is renowned for the Sheesh Mahal (Palace of Mirrors) where a single candle flame illuminates the entire hall through thousands of concave mirrors.";
  } else if (q.includes('qutub') || q.includes('iron pillar')) {
    reply = "The Qutub Minar complex features a 72.5m fluted red sandstone minaret alongside the 4th-century Gupta Iron Pillar, which has remained completely rust-free for over 1,600 years due to ancient metallurgical ingenuity.";
  } else if (q.includes('varanasi') || q.includes('kashi') || q.includes('ghat')) {
    reply = "Varanasi (Kashi) is one of the world's oldest living cities, with over 3,000 years of unbroken spiritual culture across its 84 sacred stone riverfront ghats along Mother Ganga.";
  } else if (q.includes('nalanda')) {
    reply = "Nalanda Mahavihara was the ancient world's preeminent residential university, housing 10,000 students and 2,000 scholars from across Asia with a 9-million manuscript library before its destruction.";
  }

  res.json({ reply, source: 'curated-knowledge-engine' });
});

// WebSocket for real-time live visitor counts & interactive guide session
wss.on('connection', (ws: WebSocket) => {
  ws.send(JSON.stringify({
    type: 'WELCOME',
    message: 'Connected to Bharat Heritage Live Stream',
    activeExplorers: wss.clients.size,
    timestamp: new Date().toISOString()
  }));

  // Broadcast visitor count updates
  wss.clients.forEach(client => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify({
        type: 'EXPLORERS_UPDATE',
        count: wss.clients.size
      }));
    }
  });

  ws.on('message', (data: string) => {
    try {
      const parsed = JSON.parse(data.toString());
      if (parsed.type === 'PING') {
        ws.send(JSON.stringify({ type: 'PONG', timestamp: Date.now() }));
      }
    } catch (_) {}
  });

  ws.on('close', () => {
    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          type: 'EXPLORERS_UPDATE',
          count: wss.clients.size
        }));
      }
    });
  });
});

// Serve frontend static build on Render production deployment
const distPath = path.join(process.cwd(), 'dist');
app.use(express.static(distPath));

// SPA Fallback: send index.html for non-API routes
app.get('*', (req: Request, res: Response) => {
  if (req.path.startsWith('/api')) {
    return res.status(404).json({ error: 'API route not found' });
  }
  const indexPath = path.join(distPath, 'index.html');
  res.sendFile(indexPath, (err) => {
    if (err) {
      res.status(200).send('Bharat Heritage Web Backend is Running. Frontend will appear after running npm run build.');
    }
  });
});

server.listen(PORT, () => {
  console.log(`Bharat Heritage Web & API Server listening on port ${PORT}`);
});
