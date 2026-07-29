import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Skeleton from '@mui/material/Skeleton';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import Inventory2Icon from '@mui/icons-material/Inventory2';

import StatusBadge from '../../components/common/StatusBadge';
import EmptyState from '../../components/common/EmptyState';
import { getMyItems } from '../../api/items';
import { getMatchesForLostItem, getMatchesForFoundItem } from '../../api/matches';

export default function MatchesList() {
  const location = useLocation();
  const navigate = useNavigate();

  const [myItems, setMyItems] = useState([]);
  const [selectedItemId, setSelectedItemId] = useState('');
  const [matches, setMatches] = useState([]);
  const [loadingItems, setLoadingItems] = useState(true);
  const [loadingMatches, setLoadingMatches] = useState(false);
  const [toast, setToast] = useState('');

  const openItems = useMemo(() => myItems.filter((i) => i.status === 'OPEN'), [myItems]);
  const selectedItem = useMemo(
    () => openItems.find((i) => i.id === selectedItemId),
    [openItems, selectedItemId]
  );

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoadingItems(true);
      try {
        const items = await getMyItems();
        if (cancelled) return;
        setMyItems(items);

        const preselect = location.state?.itemId;
        const openOnes = items.filter((i) => i.status === 'OPEN');
        if (preselect && openOnes.some((i) => i.id === preselect)) {
          setSelectedItemId(preselect);
        } else if (openOnes.length) {
          setSelectedItemId(openOnes[0].id);
        }
      } catch (err) {
        if (!cancelled) setToast(err.message || 'Failed to load your items.');
      } finally {
        if (!cancelled) setLoadingItems(false);
      }
    })();
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadMatches = useCallback(async () => {
    if (!selectedItem) {
      setMatches([]);
      return;
    }
    setLoadingMatches(true);
    try {
      const data =
        selectedItem.itemType === 'LOST'
          ? await getMatchesForLostItem(selectedItem.id)
          : await getMatchesForFoundItem(selectedItem.id);
      setMatches(data);
    } catch (err) {
      setToast(err.message || 'Failed to load matches.');
    } finally {
      setLoadingMatches(false);
    }
  }, [selectedItem]);

  useEffect(() => {
    loadMatches();
  }, [loadMatches]);

  return (
    <Box sx={{ pt: 1 }}>
      <Typography variant="h5" fontWeight={800}>
        Matches
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Potential matches found for your items.
      </Typography>

      {loadingItems ? (
        <Skeleton variant="rounded" height={56} sx={{ mb: 3, maxWidth: 420 }} />
      ) : openItems.length === 0 ? (
        <Paper elevation={0} sx={{ p: 3, borderRadius: 3 }}>
          <EmptyState
            title="No open items to match"
            description="Report a lost or found item first — we'll automatically look for matches."
          />
        </Paper>
      ) : (
        <>
          <FormControl sx={{ mb: 3, minWidth: 320 }} size="small">
            <InputLabel>Select one of your items</InputLabel>
            <Select
              value={selectedItemId}
              label="Select one of your items"
              onChange={(e) => setSelectedItemId(e.target.value)}
            >
              {openItems.map((item) => (
                <MenuItem key={item.id} value={item.id}>
                  [{item.itemType}] {item.title}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <Paper elevation={0} sx={{ borderRadius: 3, overflow: 'hidden' }}>
            {loadingMatches ? (
              <Box sx={{ p: 2 }}>
                {[1, 2, 3].map((i) => (
                  <Skeleton key={i} variant="rounded" height={72} sx={{ mb: 1.5 }} />
                ))}
              </Box>
            ) : matches.length === 0 ? (
              <Box sx={{ p: 2 }}>
                <EmptyState
                  title="No matches yet"
                  description="We'll notify you as soon as a potential match is found for this item."
                />
              </Box>
            ) : (
              matches.map((m) => (
                <Stack
                  key={m.itemMatchId}
                  direction="row"
                  alignItems="center"
                  spacing={2}
                  sx={{
                    p: 2,
                    borderBottom: '1px solid rgba(255,255,255,0.06)',
                    '&:last-of-type': { borderBottom: 'none' },
                  }}
                >
                  <Avatar variant="rounded" sx={{ width: 48, height: 48, bgcolor: 'grey.800' }}>
                    <Inventory2Icon fontSize="small" />
                  </Avatar>
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="body1" fontWeight={600}>
                      {m.itemTitle}
                    </Typography>
                    <Stack direction="row" spacing={1} alignItems="center" sx={{ mt: 0.3 }}>
                      <StatusBadge status={m.matchStatus} />
                      <Typography variant="caption" color="text.secondary">
                        Matched on this item
                      </Typography>
                    </Stack>
                  </Box>
                  <Typography variant="body2" fontWeight={700} color="success.main" sx={{ minWidth: 80, textAlign: 'right' }}>
                    {Math.round(m.matchScore)}% Match
                  </Typography>
                  <Stack direction="row" spacing={1}>
                    <Button size="small" variant="outlined" onClick={() => navigate(`/matches/${m.itemMatchId}`)}>
                      View Match
                    </Button>
                  </Stack>
                </Stack>
              ))
            )}
          </Paper>
        </>
      )}

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast('')}>
        <Alert severity="info" onClose={() => setToast('')} sx={{ width: '100%' }}>
          {toast}
        </Alert>
      </Snackbar>
    </Box>
  );
}