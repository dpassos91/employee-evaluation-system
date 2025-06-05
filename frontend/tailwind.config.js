/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      colors: {
        primary: "#D41C1C",
        secondary: "#F4F4F4",
        dark: "#1F1F1F",
        light: "#FFFFFF",
        accent: "#FF9F1C",
      },
      fontFamily: {
        hand: ['"Just Another Hand"', 'cursive'],
    },
  },
  },
  plugins: [],
};
