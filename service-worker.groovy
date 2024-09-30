const CACHE_NAME = 'tetris-cache-v1';
const FILES_TO_CACHE = [
    '/',
    '/index.html',
    '/style.css',
    '/script.js',
    '/manifest.json',
    '/icon-192x192.png',
    '/icon-512x512.png'
];

// Установка сервисного рабочего процесса
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                return cache.addAll(FILES_TO_CACHE);
            })
    );
});

// Обработка запросов
self.addEventListener('fetch', event => {
    event.respondWith(
        caches.match(event.request)
            .then(response => {
                return response || fetch(event.request);
            })
    );
});
