import http from "k6/http";
import {check} from "k6";

export const options = {
    stages: [
        {duration: '10s', target: 50},
        {duration: '30s', target: 50},
    ],
    maxRedirects: 0,
};

export function setup() {
    const res = http.post(
        "http://localhost:8080/shorten",
        JSON.stringify({url: "https://example.com/"}),
        {headers: {"Content-Type": "application/json"}},
    );

    if (res.status !== 201) {
        throw new Error(`setup failed: ${res.status} ${res.body}`);
    }
    return {shortCode: res.json().shortCode};
}

export default function (data) {
    const res = http.get(`http://localhost:8080/${data.shortCode}`);

    check(res, {
        "status is 302": (r) => r.status === 302,
        "has redirect location": (r) => r.headers["Location"] !== undefined,
    });
}
