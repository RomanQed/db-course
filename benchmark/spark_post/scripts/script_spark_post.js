import http from 'k6/http';
import { sleep } from 'k6';
import { Trend } from 'k6/metrics';

const responseTime = new Trend('response_time', true);

export const options = {
    stages: [
        { duration: '10s', target: 1000 },
        { duration: '1m', target: 5000 },
        { duration: '10s', target: 5000 },
    ],
};

function generateLargePayload(sizeInKB) {
    const data = {
        field1: 'x'.repeat(sizeInKB * 1024 / 2),
        field2: 'y'.repeat(sizeInKB * 1024 / 2),
    };
    return JSON.stringify(data);
}

export default function () {
    const url = 'http://spark_post:7000/post';

    const payload = generateLargePayload(128);
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    responseTime.add(res.timings.duration);

    sleep(1);
}
