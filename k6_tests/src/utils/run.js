import { group } from 'k6';

export function runTest(g, ...args) {
  group(g.name, () => { g.fn(...args) });
}