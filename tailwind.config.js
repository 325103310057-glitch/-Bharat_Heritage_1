/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        heritage: {
          saffron: '#FF6F00',
          gold: '#FFB300',
          deepGold: '#D4AF37',
          maroon: '#800020',
          sandstone: '#F4ECE1',
          templeRed: '#C84B31',
          darkBg: '#121212',
          cardDark: '#1E1E24'
        }
      }
    },
  },
  plugins: [],
}
