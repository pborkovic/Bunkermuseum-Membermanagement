/**
 * @fileoverview Dienstgrad (rank) abbreviations and their written-out form.
 *
 * Ranks are stored as free text, mostly abbreviated and optionally followed by
 * an addition such as "i.R." or "d.Res." (e.g. "Vzlt i.R.", "ObstdhmfD").
 * {@link formatRank} shows the stored value together with the written-out form,
 * e.g. "Gfr – Gefreiter" or "Vzlt i.R. – Vizeleutnant im Ruhestand".
 *
 * @module ranks
 * @author Philipp Borkovic
 */

const RANKS: Record<string, string> = {
  Rekr: 'Rekrut',
  Gfr: 'Gefreiter',
  Kpl: 'Korporal',
  Zgf: 'Zugsführer',
  Wm: 'Wachtmeister',
  OWm: 'Oberwachtmeister',
  StWm: 'Stabswachtmeister',
  OStWm: 'Oberstabswachtmeister',
  OStv: 'Offiziersstellvertreter',
  Vzlt: 'Vizeleutnant',
  Fhr: 'Fähnrich',
  Lt: 'Leutnant',
  Olt: 'Oberleutnant',
  Hptm: 'Hauptmann',
  Mjr: 'Major',
  Obstlt: 'Oberstleutnant',
  Obst: 'Oberst',
  Bgdr: 'Brigadier',
  GenMjr: 'Generalmajor',
  GenLt: 'Generalleutnant',
  Gen: 'General',
  OGfr: 'Obergefreiter',
  HptGfr: 'Hauptgefreiter',
  StGfr: 'Stabsgefreiter',
  Uffz: 'Unteroffizier',
  StUffz: 'Stabsunteroffizier',
  Fw: 'Feldwebel',
  OFw: 'Oberfeldwebel',
  HptFw: 'Hauptfeldwebel',
  StFw: 'Stabsfeldwebel',
  OStFw: 'Oberstabsfeldwebel',
  Insp: 'Inspektor',
  RevInsp: 'Revierinspektor',
  GrInsp: 'Gruppeninspektor',
  BezInsp: 'Bezirksinspektor',
  AbtInsp: 'Abteilungsinspektor',
  KontrInsp: 'Kontrollinspektor',
  ChefInsp: 'Chefinspektor',
  PK: 'Polizeikommissar',
  POK: 'Polizeioberkommissar',
  PHK: 'Polizeihauptkommissar',
  EPHK: 'Erster Polizeihauptkommissar',
};

/**
 * Additions after the rank, keyed by their normalized form
 * (lower case, without spaces and dots).
 */
const ADDITIONS: Record<string, string> = {
  ir: 'im Ruhestand',
  dres: 'der Reserve',
  dr: 'der Reserve',
  ad: 'außer Dienst',
  dg: 'des Generalstabsdienstes',
  dhmfd: 'des höheren militärfachlichen Dienstes',
  dhtd: 'des höheren technischen Dienstes',
  dintd: 'des Intendanzdienstes',
};

const RANK_KEYS = Object.keys(RANKS).sort((a, b) => b.length - a.length);
const ADDITION_KEYS = Object.keys(ADDITIONS).sort((a, b) => b.length - a.length);

/**
 * Splits the text after the rank abbreviation into known additions.
 *
 * @param {string} rest - Text following the abbreviation
 * @returns {string[] | null} The written-out additions, or null if anything is unknown
 */
const parseAdditions = (rest: string): string[] | null => {
  let normalized = rest.replace(/[\s.]/g, '').toLowerCase();
  const additions: string[] = [];

  while (normalized) {
    const key = ADDITION_KEYS.find((k) => normalized.startsWith(k));
    if (!key) return null;
    additions.push(ADDITIONS[key]);
    normalized = normalized.slice(key.length);
  }

  return additions;
};

/**
 * Returns the written-out form of a rank, e.g. "Vzlt i.R." → "Vizeleutnant im Ruhestand".
 *
 * @param {string | null | undefined} rank - The stored rank
 * @returns {string | null} The written-out form, or null if the rank is not recognized
 */
export function expandRank(rank: string | null | undefined): string | null {
  const value = rank?.trim();
  if (!value) return null;

  for (const key of RANK_KEYS) {
    if (!value.toLowerCase().startsWith(key.toLowerCase())) continue;
    const additions = parseAdditions(value.slice(key.length));
    if (additions) {
      return [RANKS[key], ...additions].join(' ');
    }
  }

  return null;
}

/**
 * Formats a rank as "short form – written-out form".
 *
 * Abbreviations are expanded ("Gfr" → "Gfr – Gefreiter"); a stored written-out
 * rank gets its abbreviation ("Gefreiter" → "Gfr – Gefreiter"). Anything not
 * recognized is returned unchanged.
 *
 * @param {string | null | undefined} rank - The stored rank
 * @returns {string} The formatted rank, or an empty string if none is set
 */
export function formatRank(rank: string | null | undefined): string {
  const value = rank?.trim() ?? '';
  if (!value) return '';

  const expanded = expandRank(value);
  if (expanded) return `${value} – ${expanded}`;

  const abbreviation = RANK_KEYS.find((key) => RANKS[key].toLowerCase() === value.toLowerCase());
  if (abbreviation) return `${abbreviation} – ${RANKS[abbreviation]}`;

  return value;
}
