import { createTheme } from '@mui/material/styles';

// Palette derived from the Reclaim dark-theme mockups:
// deep near-black backgrounds, indigo/violet primary accent,
// green/amber/red status colors for open/pending/rejected states.
const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#6C5CE7',
      light: '#8B7CF6',
      dark: '#5A4BD1',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#22C55E',
    },
    success: { main: '#22C55E' },
    warning: { main: '#F5A524' },
    error: { main: '#EF4444' },
    info: { main: '#3B82F6' },
    background: {
      default: '#0A0B10',
      paper: '#12141C',
    },
    divider: 'rgba(255,255,255,0.08)',
    text: {
      primary: '#F1F2F6',
      secondary: '#9096A8',
    },
  },
  shape: {
    borderRadius: 12,
  },
  typography: {
    fontFamily: '"Inter", "Plus Jakarta Sans", system-ui, sans-serif',
    h1: { fontFamily: '"Plus Jakarta Sans", sans-serif', fontWeight: 700 },
    h2: { fontFamily: '"Plus Jakarta Sans", sans-serif', fontWeight: 700 },
    h3: { fontFamily: '"Plus Jakarta Sans", sans-serif', fontWeight: 700 },
    h4: { fontFamily: '"Plus Jakarta Sans", sans-serif', fontWeight: 700 },
    h5: { fontFamily: '"Plus Jakarta Sans", sans-serif', fontWeight: 700 },
    h6: { fontFamily: '"Plus Jakarta Sans", sans-serif', fontWeight: 700 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  components: {
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          border: '1px solid rgba(255,255,255,0.06)',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 10, paddingInline: 16 },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600, fontSize: '0.72rem' },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: { borderColor: 'rgba(255,255,255,0.06)' },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: '#0B0D14',
          borderRight: '1px solid rgba(255,255,255,0.06)',
        },
      },
    },
  },
});

export default theme;
