import http from 'k6/http';
import { check, group } from 'k6';

const BASE_URL = 'http://gateway:8080/api';
const AUTH_URL = "http://gateway:8080/auth/login";

const credentials = {
  username: 'admin',
  password: 'admin',
};

function getAuthToken() {
  const headers = { 'Content-Type': 'application/json' };
  const response = http.post(AUTH_URL, JSON.stringify(credentials), { headers });
  check(response, { 'Login exitoso': (r) => r.status === 200 });
  return response.json().access_token;
}

export default function () {
  const authToken = getAuthToken();
  const authHeaders = { Authorization: `Bearer ${authToken}` };

  group('Order Service', () => {
    const orderUrl = `${BASE_URL}/orders`;

    const getOrdersResponse = http.get(orderUrl, { headers: authHeaders });
    check(getOrdersResponse, { 'Obtener órdenes exitoso': (r) => r.status === 200 });

    const newOrder = {
      orderDate: new Date().toISOString(),
      orderProducts: [],
    };
    const createOrderResponse = http.post(orderUrl, JSON.stringify(newOrder), {
      headers: { ...authHeaders, 'Content-Type': 'application/json' },
    });
    check(createOrderResponse, { 'Crear orden exitoso': (r) => r.status === 200 });
  });

  group('Product Service', () => {
    const productUrl = `${BASE_URL}/products`;

    const getProductsResponse = http.get(productUrl, { headers: authHeaders });
    check(getProductsResponse, { 'Obtener productos exitoso': (r) => r.status === 200 });

    const newProduct = {
      name: 'Test Product',
      description: 'Test Description',
      price: 100.0,
      category: 'Test Category',
    };
    const createProductResponse = http.post(productUrl, JSON.stringify(newProduct), {
      headers: { ...authHeaders, 'Content-Type': 'application/json' },
    });
    check(createProductResponse, { 'Crear producto exitoso': (r) => r.status === 200 });
  });

  group('Inventory Service', () => {
    const inventoryUrl = `${BASE_URL}/inventories`;

    const getInventoriesResponse = http.get(inventoryUrl, { headers: authHeaders });
    check(getInventoriesResponse, { 'Obtener inventarios exitoso': (r) => r.status === 200 });

    const newInventory = {
      productId: '123e4567-e89b-12d3-a456-426614174000',
      stock: 100,
      minimumQuantity: 10,
      maximumQuantity: 200,
    };
    const createInventoryResponse = http.post(inventoryUrl, JSON.stringify(newInventory), {
      headers: { ...authHeaders, 'Content-Type': 'application/json' },
    });
    check(createInventoryResponse, { 'Crear inventario exitoso': (r) => r.status === 200 });
  });

  group('Payment Service', () => {
    const paymentUrl = `${BASE_URL}/payments`;

    const getPaymentsResponse = http.get(paymentUrl, { headers: authHeaders });
    check(getPaymentsResponse, { 'Obtener pagos exitoso': (r) => r.status === 200 });

    const newPayment = {
      amount: 100.0,
      currency: 'USD',
      status: 'PENDING',
      paymentMethod: 'Credit Card',
    };
    const createPaymentResponse = http.post(paymentUrl, JSON.stringify(newPayment), {
      headers: { ...authHeaders, 'Content-Type': 'application/json' },
    });
    check(createPaymentResponse, { 'Crear pago exitoso': (r) => r.status === 200 });
  });
}

export const options = {
    stages: [
      { duration: '5s', target: 10 },   // subida suave a 10 usuarios
      { duration: '10s', target: 50 },   // carga moderada
      { duration: '10s', target: 100 },  // carga pesada
      { duration: '10s', target: 200 },  // carga muy pesada
      { duration: '5s', target: 0 },    // fase de bajada (recuperación)
    ],
    thresholds: {
      http_req_duration: ['p(95)<1000'], // el 95% de las peticiones deben tardar menos de 1s
      http_req_failed: ['rate<0.05'],    // menos del 5% de errores
    },
  };