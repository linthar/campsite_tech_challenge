import { Counter } from 'k6/metrics';
import { group } from 'k6';
import { testDescription as test001 } from './001.test.js';

const CounterErrors = new Counter("Errors");

export let options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    "Errors": ["count<1"],
  }
}

const testToRun = [
  test001,
];

export default function () {
  for (const test of testToRun) {
    try {
      group(test.name, () => {
        if (!test.fn(test.args)) {
          console.error(`FAILURE: "${test.name}" failed in checks`);
          CounterErrors.add(1, { tag: test.name });
        }
      });
    } catch (e) {
      CounterErrors.add(1);
      console.error(`FAILURE: "${test.name}" throwed an exception\n${e}\n`);
    }
  }
}
