export const BASE_URL = window.location.origin;


export const BACKEND_API_URL = BASE_URL.includes('localhost') ? 'http://localhost:8080/api' : BASE_URL + '/api';
export const PROXY_API_URL = BASE_URL + '/api';
