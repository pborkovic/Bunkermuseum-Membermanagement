/**
 * @fileoverview Client-side fuzzy text search.
 *
 * Mirrors the server-side member search: the query is split into words and every
 * word must match the searchable text, either as a substring or with a small number
 * of typos. Matching ignores case, accents and umlauts (ä→a, ß→ss).
 *
 * @module search
 * @author Philipp Borkovic
 */

/**
 * Normalizes text for searching: lower case, accents/umlauts folded, ß → ss.
 *
 * @param {string} text - The raw text
 * @returns {string} The normalized text
 */
export function normalizeSearchText(text: string): string {
  return text
    .toLowerCase()
    .replace(/ß/g, 'ss')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '');
}

const allowedTypos = (length: number): number => (length <= 3 ? 0 : length <= 6 ? 1 : 2);

/**
 * Levenshtein edit distance between two strings, stopping early above a limit.
 */
const editDistance = (a: string, b: string, limit: number): number => {
  if (Math.abs(a.length - b.length) > limit) return limit + 1;
  let previous = Array.from({ length: b.length + 1 }, (_, i) => i);
  for (let i = 1; i <= a.length; i++) {
    const current = [i];
    let rowMin = i;
    for (let j = 1; j <= b.length; j++) {
      const cost = a[i - 1] === b[j - 1] ? 0 : 1;
      current[j] = Math.min(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost);
      rowMin = Math.min(rowMin, current[j]);
    }
    if (rowMin > limit) return limit + 1;
    previous = current;
  }
  return previous[b.length];
};

/**
 * Checks whether one normalized search word matches the normalized text.
 */
const termMatches = (term: string, text: string, words: string[]): boolean => {
  if (text.includes(term)) return true;
  const limit = allowedTypos(term.length);
  if (limit === 0) return false;
  return words.some(
    (word) =>
      editDistance(term, word, limit) <= limit ||
      (word.length > term.length && editDistance(term, word.slice(0, term.length), limit) <= limit)
  );
};

/**
 * Returns true if every word of the query matches the given fields.
 *
 * @param {string} query - The search query; an empty query matches everything
 * @param {Array<string | number | null | undefined>} fields - The searchable values
 * @returns {boolean} Whether the fields match the query
 */
export function fuzzyMatches(query: string, fields: Array<string | number | null | undefined>): boolean {
  const terms = normalizeSearchText(query).split(/\s+/).filter(Boolean);
  if (terms.length === 0) return true;

  const text = normalizeSearchText(
    fields.filter((field) => field !== null && field !== undefined && field !== '').join(' ')
  );
  const words = text.split(/[\s,.;:()/\-]+/).filter(Boolean);

  return terms.every((term) => termMatches(term, text, words));
}
