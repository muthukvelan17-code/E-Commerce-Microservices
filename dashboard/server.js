const express = require('express');
const http = require('http');
const { exec } = require('child_process');
const path = require('path');

const app = express();
const PORT = 3000;

app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

const SERVICES = [
  { name: 'Eureka Server', port: 8761, path: '/actuator/health' },
  { name: 'Config Server', port: 8888, path: '/actuator/health' },
  { name: 'API Gateway', port: 8080, path: '/actuator/health' },
  { name: 'User Service', port: 8081, path: '/actuator/health' },
  { name: 'Product Service', port: 8082, path: '/actuator/health' },
  { name: 'Inventory Service', port: 8083, path: '/actuator/health' },
  { name: 'Order Service', port: 8084, path: '/actuator/health' },
  { name: 'Payment Service', port: 8085, path: '/actuator/health' },
  { name: 'Notification Service', port: 8086, path: '/actuator/health' }
];

// Helper to check a service health status
function checkService(service) {
  return new Promise((resolve) => {
    const options = {
      hostname: 'localhost',
      port: service.port,
      path: service.path,
      method: 'GET',
      timeout: 2000
    };

    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        if (res.statusCode === 200) {
          try {
            const parsed = JSON.parse(body);
            resolve({
              name: service.name,
              port: service.port,
              status: parsed.status || 'UP',
              details: parsed
            });
          } catch (e) {
            resolve({ name: service.name, port: service.port, status: 'UP', raw: body.substring(0, 100) });
          }
        } else {
          resolve({ name: service.name, port: service.port, status: 'DOWN', code: res.statusCode });
        }
      });
    });

    req.on('error', () => {
      resolve({ name: service.name, port: service.port, status: 'DOWN' });
    });

    req.on('timeout', () => {
      req.destroy();
      resolve({ name: service.name, port: service.port, status: 'TIMEOUT' });
    });

    req.end();
  });
}

// Endpoint to query health of all microservices
app.get('/api/status', async (req, res) => {
  try {
    const checks = SERVICES.map(svc => checkService(svc));
    const results = await Promise.all(checks);
    res.json(results);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Endpoint to run start-all.ps1
app.post('/api/start', (req, res) => {
  console.log('Received request to start services...');
  exec('powershell -ExecutionPolicy Bypass -File "..\\start-all.ps1"', { cwd: path.join(__dirname, '..') }, (error, stdout, stderr) => {
    if (error) {
      console.error(`exec error: ${error}`);
    }
    console.log(`stdout: ${stdout}`);
    console.error(`stderr: ${stderr}`);
  });
  res.json({ message: 'Startup script initiated in background.' });
});

// Endpoint to run stop-all.ps1
app.post('/api/stop', (req, res) => {
  console.log('Received request to stop services...');
  exec('powershell -ExecutionPolicy Bypass -File "..\\stop-all.ps1"', { cwd: path.join(__dirname, '..') }, (error, stdout, stderr) => {
    if (error) {
      console.error(`exec error: ${error}`);
    }
    console.log(`stdout: ${stdout}`);
    console.error(`stderr: ${stderr}`);
  });
  res.json({ message: 'Shutdown script initiated in background.' });
});

app.listen(PORT, () => {
  console.log(`Dashboard server running at http://localhost:${PORT}`);
});
