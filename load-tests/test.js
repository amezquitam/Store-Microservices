// load-tests/test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 20, // usuarios virtuales simultáneos
    duration: '30s',
};

const USERNAME = 'admin';
const PASSWORD = 'admin';

async function auth(username, password) {
    let res = http.post('http://gateway:8080/auth/login', JSON.stringify({
        username: username,
        password: password
    }), {
        headers: { 'Content-Type': 'application/json' }
    });
    check(res, { 'status was 200': (r) => r.status === 200 });
    return res.json('access_token');
}

async function getAll(path) {
    const token = await auth(USERNAME, PASSWORD);
    let params = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    };
    let res = http.get('http://gateway:8080/auth/' + path, params);
    check(res, { 'status was 200': (r) => r.status === 200 });
}

export default function () {
    const paths = [
        'products',
        'orders',
        'inventory',
        'payments'
    ]

    Promise.all(paths.map(path => getAll(path)))
        .then(results => {
            results.forEach((result, index) => {
                check(result, { 'status was 200': (r) => r.status === 200 });
            });
        })
        .catch(error => {
            console.error('Error fetching paths:', error);
        });

    sleep(1);
}
