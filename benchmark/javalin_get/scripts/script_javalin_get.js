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

export default function () {
    const res = http.get('http://javalin_get:7000/get');
    responseTime.add(res.timings.duration); 

    sleep(1);
}



