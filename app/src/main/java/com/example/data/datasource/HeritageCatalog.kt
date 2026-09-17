package com.example.data.datasource

import com.example.data.model.HeritageCategory
import com.example.data.model.HeritageItem

object HeritageCatalog {
    val items: List<HeritageItem> = listOf(
        HeritageItem(
            id = "konark-sun-temple",
            name = "Konark Sun Temple",
            hindiName = "कोणार्क सूर्य मंदिर",
            category = HeritageCategory.TEMPLES,
            state = "Odisha",
            city = "Puri / Konark",
            era = "13th Century CE (Eastern Ganga Dynasty)",
            constructedBy = "King Narasimhadeva I",
            yearBuilt = "1250 CE",
            shortDescription = "Architectural marvel shaped as a colossal 24-wheeled chariot of Surya with intricately carved stone wheels.",
            fullHistory = "The Sun Temple at Konark was built in the 13th century by King Narasimhadeva I of the Eastern Ganga dynasty. Conceived as a colossal celestial chariot for the Sun God Surya, the temple stands on a high platform with 24 carved stone wheels, each nearly 10 feet in diameter, pulled by seven stone horses. The wheels function as sundials capable of calculating exact solar time down to minutes.",
            culturalSignificance = "One of India's most venerated sun temples, celebrating cosmic solar worship and ancient Indian mathematics, astronomy, and classical Odissi iconography.",
            architectureStyle = "Kalinga Architecture (Rekha Deula and Pidha Deula styles)",
            quickFacts = listOf(
                "Sundial wheels predict precise time by reading shadow positions.",
                "Seven horses represent the seven days of the solar week.",
                "UNESCO World Heritage Site designated in 1984."
            ),
            imageUrl = "https://images.unsplash.com/photo-1600100397608-f010f443b749?w=800&q=80",
            latitude = 19.8876,
            longitude = 86.0945,
            unescoWorldHeritage = true
        ),
        HeritageItem(
            id = "hampi-vijayanagara",
            name = "Hampi Vijayanagara",
            hindiName = "हम्पी विजयनगर",
            category = HeritageCategory.MONUMENTS,
            state = "Karnataka",
            city = "Vijayanagara / Hospet",
            era = "14th - 16th Century CE",
            constructedBy = "Harihara I, Bukka Raya & Krishnadevaraya",
            yearBuilt = "1336 CE",
            shortDescription = "The legendary capital of the Vijayanagara Empire along the Tungabhadra River, famed for the Stone Chariot and Vittala Temple.",
            fullHistory = "Hampi was the grand jewel of the Vijayanagara Empire from 1336 to 1565. At its zenith, it was described by Persian and European travelers like Domingo Paes and Abdur Razzaq as one of the richest and second-largest cities in the medieval world, trading in diamonds, silks, and Arabian horses. The complex boasts the monolithic Stone Chariot, musical pillars of Vittala Temple, and Virupaksha Temple.",
            culturalSignificance = "Spiritual epicenter of southern India and eternal symbol of South Indian Renaissance in literature, sculpture, and civil architecture.",
            architectureStyle = "Vijayanagara Dravidian Temple Architecture",
            quickFacts = listOf(
                "Vittala Temple pillars produce distinct musical notes when tapped.",
                "Virupaksha Temple has an uninterrupted living worship heritage of over 700 years.",
                "Spread across over 4,100 hectares with over 1,600 surviving monuments."
            ),
            imageUrl = "https://images.unsplash.com/photo-1609137144822-482d8c3664c3?w=800&q=80",
            latitude = 15.3350,
            longitude = 76.4600,
            unescoWorldHeritage = true
        ),
        HeritageItem(
            id = "brihadisvara-temple",
            name = "Brihadisvara Temple",
            hindiName = "बृहदीश्वर मन्दिर (तंजावुर)",
            category = HeritageCategory.TEMPLES,
            state = "Tamil Nadu",
            city = "Thanjavur",
            era = "11th Century CE (Chola Dynasty)",
            constructedBy = "Rajaraja Chola I",
            yearBuilt = "1010 CE",
            shortDescription = "Peruvudaiyar Kovil, towering 216-foot granite vimana crowned by an 80-tonne monolithic stone dome.",
            fullHistory = "Commissioned by the Great Emperor Rajaraja Chola I, Brihadisvara Temple (Big Temple) in Thanjavur is one of the grandest granite stone structures in human history. Completed in 1010 CE, its central Vimana tower rises 66 meters into the sky without the use of binding mortar, utilizing interlocking granite blocks. The temple is dedicated to Lord Shiva as Nataraja and Adavallan.",
            culturalSignificance = "The pinnacle of Chola imperial art and South Indian bronze casting, classical music, and Natyasastra dance tradition.",
            architectureStyle = "Chola Dravidian Architecture",
            quickFacts = listOf(
                "The Kumbam apex capstone weighs approximately 80 metric tonnes.",
                "Built completely with granite brought from quarries over 50 kilometers away.",
                "Houses the largest monolithic Nandi statue in Tamil Nadu."
            ),
            imageUrl = "https://images.unsplash.com/photo-1582510003544-4d00b7f74220?w=800&q=80",
            latitude = 10.7828,
            longitude = 79.1318,
            unescoWorldHeritage = true
        ),
        HeritageItem(
            id = "ajanta-ellora-caves",
            name = "Ajanta & Ellora Caves",
            hindiName = "अजन्ता और एलोरा गुफाएँ",
            category = HeritageCategory.CAVES,
            state = "Maharashtra",
            city = "Chhatrapati Sambhajinagar (Aurangabad)",
            era = "2nd Century BCE - 10th Century CE",
            constructedBy = "Satavahana, Vakataka, Rashtrakuta & Chalukya dynasties",
            yearBuilt = "200 BCE - 1000 CE",
            shortDescription = "Ancient rock-cut cave sanctuaries featuring timeless Jataka murals and the breathtaking Kailasa monolith.",
            fullHistory = "Carved directly into volcanic basalt horseshoe cliffs along the Waghur River, Ajanta Caves preserve classical Indian mural painting depicting scenes from the life of Buddha. Ellora features 34 rock-cut temples representing Buddhism, Hinduism, and Jainism in harmony. Ellora's Cave 16 houses the Kailasa Temple, the world's largest single monolithic rock-cut structure carved top-down out of a sheer mountain.",
            culturalSignificance = "Represents classical Indian fresco mastery and the synthesis of Buddhist, Hindu, and Jain spiritual traditions.",
            architectureStyle = "Ancient Indian Rock-Cut Architecture",
            quickFacts = listOf(
                "Kailasa temple was carved top-down from a single cliff face with over 200,000 tonnes of rock excavated.",
                "Ajanta frescoes retain mineral pigment vibrancy after two millennia.",
                "Hidden in dense jungle until rediscovered by John Smith in 1819."
            ),
            imageUrl = "https://images.unsplash.com/photo-1620766182966-c6eb5ed2b788?w=800&q=80",
            latitude = 20.5519,
            longitude = 75.7033,
            unescoWorldHeritage = true
        ),
        HeritageItem(
            id = "amber-fort-jaipur",
            name = "Amber Fort & Palace",
            hindiName = "आमेर किला (जयपुर)",
            category = HeritageCategory.FORTS,
            state = "Rajasthan",
            city = "Jaipur",
            era = "16th - 17th Century CE",
            constructedBy = "Raja Man Singh I & Mirza Raja Jai Singh",
            yearBuilt = "1592 CE",
            shortDescription = "Hilltop Rajput fortress of red sandstone and marble overlooking Maota Lake, renowned for Sheesh Mahal.",
            fullHistory = "Perched atop the rugged Aravalli hills overlooking Maota Lake, Amber Fort was the royal seat of the Kachwaha Rajputs before Jaipur was founded. Constructed with yellow and red sandstone and marble, it comprises four distinct courtyards, the Diwan-e-Aam, the Summer Palace (Sukh Niwas) cooled by water cascades, and the ethereal Sheesh Mahal (Mirror Palace) inlaid with Belgian concave glass.",
            culturalSignificance = "Exemplifies the Rajput chivalric valor, royal artistry, and hydraulic cooling innovations in arid climates.",
            architectureStyle = "Rajput - Indo-Islamic Fusion Architecture",
            quickFacts = listOf(
                "Sheesh Mahal can be illuminated by a single candle flame reflecting through convex mirrors.",
                "Connected by underground subterranean tunnels to the defensive Jaigarh Fort.",
                "Part of the UNESCO Hill Forts of Rajasthan cluster."
            ),
            imageUrl = "https://images.unsplash.com/photo-1599661046289-e31897846e41?w=800&q=80",
            latitude = 26.9855,
            longitude = 75.8513,
            unescoWorldHeritage = true
        ),
        HeritageItem(
            id = "qutub-minar",
            name = "Qutub Minar Complex",
            hindiName = "क़ुतुब मीनार",
            category = HeritageCategory.MONUMENTS,
            state = "Delhi",
            city = "New Delhi",
            era = "12th - 14th Century CE",
            constructedBy = "Qutb-ud-din Aibak, Iltutmish & Alauddin Khilji",
            yearBuilt = "1199 CE",
            shortDescription = "72.5-meter fluted red sandstone minaret alongside the 1,600-year-old rust-resistant Gupta Iron Pillar.",
            fullHistory = "The Qutub Minar is the world's tallest brick minaret, soaring 72.5 meters high with five tapering storeys adorned with intricate calligraphy and projecting balconies. Within its serene courtyard stands the famed 4th-century Gupta Iron Pillar, renowned for its metallurgical mystery of remaining completely rust-free despite exposure to atmospheric weathering for over 1,600 years.",
            culturalSignificance = "Marks the transition into medieval Delhi Sultanate architecture alongside ancient Indian metallurgical brilliance.",
            architectureStyle = "Indo-Islamic & Fluted Red Sandstone Masonry",
            quickFacts = listOf(
                "The Iron Pillar contains high phosphorus content that formed a protective iron hydrogen phosphate layer.",
                "Features 379 spiral stone steps inside the tower.",
                "Alai Darwaza within the complex exhibits true arch and dome engineering."
            ),
            imageUrl = "https://images.unsplash.com/photo-1587474260584-136574528ed5?w=800&q=80",
            latitude = 28.5245,
            longitude = 77.1855,
            unescoWorldHeritage = true
        ),
        HeritageItem(
            id = "khajuraho-temples",
            name = "Khajuraho Group of Monuments",
            hindiName = "खजुराहो स्मारक समूह",
            category = HeritageCategory.TEMPLES,
            state = "Madhya Pradesh",
            city = "Chhatarpur / Khajuraho",
            era = "10th - 11th Century CE (Chandela Dynasty)",
            constructedBy = "Chandela Kings (Yashovarman, Dhanga)",
            yearBuilt = "950 - 1050 CE",
            shortDescription = "Nagara-style stone temples celebrated for exquisite sculptural harmony, spirituality, and celebration of life.",
            fullHistory = "The Khajuraho temples were built between 950 and 1050 CE by the Chandela dynasty. Of the original 85 temples, 25 survive today across western, eastern, and southern groups. The Kandariya Mahadeva Temple is the largest and most ornate, designed to represent Mount Kailash with intricate vertical shikhara spires ascending harmoniously.",
            culturalSignificance = "Celebration of the four Purusharthas: Dharma (duty), Artha (prosperity), Kama (desire & aesthetics), and Moksha (spiritual liberation).",
            architectureStyle = "Nagara Style (Panchayatana Temple Layout)",
            quickFacts = listOf(
                "Erotic carvings constitute less than 10% of the sculptures, while 90% depict daily life, music, and devotion.",
                "Built using sandstone blocks fitted together with mortise and tenon joints.",
                "Rediscovered by British surveyor T.S. Burt in the 1830s."
            ),
            imageUrl = "https://images.unsplash.com/photo-1608958435020-e8a7109ba809?w=800&q=80",
            latitude = 24.8318,
            longitude = 79.9199,
            unescoWorldHeritage = true
        ),
        HeritageItem(
            id = "varanasi-ghats",
            name = "Varanasi & Sacred Ganga Ghats",
            hindiName = "काशी / वाराणसी के घाट",
            category = HeritageCategory.CITIES,
            state = "Uttar Pradesh",
            city = "Varanasi (Kashi)",
            era = "Antiquity (Over 3,000 years continuous history)",
            constructedBy = "Maratha, Scindia, Holkar rulers & Ancient Kashi kings",
            yearBuilt = "Continuous Living Heritage",
            shortDescription = "The eternal city of Lord Shiva, lined with 84 sacred riverfront stone ghats and mesmerizing Ganga Aarti.",
            fullHistory = "Varanasi, also known as Kashi and Benares, is universally revered as one of the world's oldest continuously inhabited sacred cities. Marking the crescent bend of the holy Ganges, its 84 stone ghats—including Dashashwamedh, Assi, and Manikarnika—were rebuilt by Maratha patrons like Ahilyabai Holkar in the 18th century. It is the seat of sacred scholarship, classical Hindustani music, Banarasi silk weaving, and spiritual liberation.",
            culturalSignificance = "Center of Sanatana spiritual philosophy, Sanskrit learning, Kabir, Tulsidas, and Indian classical music gharanas.",
            architectureStyle = "Sacred Riverfront Stone Stepped Architecture",
            quickFacts = listOf(
                "Mark Twain remarked: 'Benares is older than history, older than tradition, older even than legend.'",
                "Evening Maha Aarti at Dashashwamedh Ghat has been performed continuously for centuries.",
                "Birthplace of the Banarasi brocade silk handloom tradition."
            ),
            imageUrl = "https://images.unsplash.com/photo-1561361513-2d000a50f0dc?w=800&q=80",
            latitude = 25.3176,
            longitude = 82.9739,
            unescoWorldHeritage = false
        ),
        HeritageItem(
            id = "bharatanatyam-tradition",
            name = "Bharatanatyam & Classical Natya",
            hindiName = "भरतनाट्यम शास्त्रीय परंपरा",
            category = HeritageCategory.TRADITIONS,
            state = "Tamil Nadu & Pan-India",
            city = "Thanjavur / Chennai",
            era = "Ancient Vedic era (Codified in Natyashastra ~200 BCE)",
            constructedBy = "Sage Bharata Muni & Devadasi lineages",
            yearBuilt = "Ancient Antiquity",
            shortDescription = "India's oldest classical dance tradition synthesizing Bhava (expression), Raga (melody), and Tala (rhythm).",
            fullHistory = "Bharatanatyam originated in the sacred temples of Tamil Nadu, formerly known as Sadir or Dasi Attam. Codified in Bharata Muni's treatise 'Natyashastra' and Nandikeshavara's 'Abhinaya Darpana', it was preserved by temple artists and court composers like the Thanjavur Quartet. The form combines pure geometric rhythmic footwork (Nritta) with expressive narrative mime (Abhinaya).",
            culturalSignificance = "Living embodiment of ancient Indian theatrical philosophy, Carnatic rhythm cycles, and poetic storytelling.",
            architectureStyle = "Classical Performing Arts & Natyashastra Tradition",
            quickFacts = listOf(
                "The name Bha-Ra-Ta is an acronym for Bhava (emotion), Raga (melody), and Tala (rhythm).",
                "Chidambaram Temple stone carvings depict all 108 Karanas of Bharatanatyam.",
                "Revitalized in the early 20th century by Rukmini Devi Arundale."
            ),
            imageUrl = "https://images.unsplash.com/photo-1547153760-18fc86324498?w=800&q=80",
            latitude = 11.3992,
            longitude = 79.6934,
            unescoWorldHeritage = false
        ),
        HeritageItem(
            id = "nalanda-mahavihara",
            name = "Ancient Nalanda Mahavihara",
            hindiName = "नालन्दा महाविहार",
            category = HeritageCategory.MONUMENTS,
            state = "Bihar",
            city = "Rajgir / Nalanda",
            era = "5th - 12th Century CE (Gupta to Pala Empires)",
            constructedBy = "Kumargupta I & King Harshavardhana",
            yearBuilt = "427 CE",
            shortDescription = "Ancient residential university of international learning that housed 10,000 scholars and 9 million manuscripts.",
            fullHistory = "Nalanda was one of the earliest residential universities in recorded history. Established during the golden age of the Gupta Empire, it attracted scholars and pilgrims from China, Korea, Japan, Tibet, Persia, and Sumatra. Chinese pilgrim Xuanzang (Hiuen Tsang) spent years studying here. Its legendary nine-storey library, Dharmaganja, housed priceless scriptures on astronomy, mathematics, medicine, and Buddhist philosophy.",
            culturalSignificance = "Symbol of ancient India's global intellectual leadership, free residential pedagogy, and universal knowledge exchange.",
            architectureStyle = "Ancient Monastic Brick Architecture (Viharas & Stupas)",
            quickFacts = listOf(
                "Housed 10,000 students and 2,000 faculty entirely supported through royal endowments.",
                "Entrance examinations conducted by gatekeeper professors had a 20% admission rate.",
                "Aryabhata, father of Indian mathematics, was an illustrious chancellor/alumnus."
            ),
            imageUrl = "https://images.unsplash.com/photo-1628155930542-3c7a64e2c833?w=800&q=80",
            latitude = 25.1357,
            longitude = 85.4439,
            unescoWorldHeritage = true
        )
    )

    fun findById(id: String): HeritageItem? {
        return items.firstOrNull { it.id == id }
    }

    fun search(query: String, category: HeritageCategory = HeritageCategory.ALL): List<HeritageItem> {
        val q = query.trim().lowercase()
        return items.filter { item ->
            val matchesCategory = category == HeritageCategory.ALL || item.category == category
            val matchesQuery = q.isEmpty() ||
                item.name.lowercase().contains(q) ||
                item.hindiName.lowercase().contains(q) ||
                item.state.lowercase().contains(q) ||
                item.city.lowercase().contains(q) ||
                item.era.lowercase().contains(q) ||
                item.category.title.lowercase().contains(q) ||
                item.shortDescription.lowercase().contains(q) ||
                item.architectureStyle.lowercase().contains(q)
            matchesCategory && matchesQuery
        }
    }
}
