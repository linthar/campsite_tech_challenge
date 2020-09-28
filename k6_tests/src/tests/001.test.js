/**
 * Campsite availability report
 * (GET /availability endpoint)
 */

import http from 'k6/http';
import { check } from 'k6';
import { runTest } from '../utils/run.js';

export const testDescription = {
  name: '001 - GET Campsite dates availability',
  fn: test,
}

export default function() {
  runTest(testDescription);
}
function test() {
  // GET request
  const response = http.get(`http://localhost:8080/availability`);
  const jsonResponse = response.json();
  console.log(JSON.stringify(jsonResponse));

//  const body = JSON.stringify(reservation);
  return check(response, {
    '[POST reservation] status is 200': r => r.status === 200,
  });
}
