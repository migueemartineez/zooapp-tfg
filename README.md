# 🐾 SmartZoo TFG

Aplicación Android para un zoológico inteligente basada en beacons BLE, 
desarrollada como Trabajo de Fin de Grado (Ingeniería Informática, UMA).

## Estructura del repositorio

- `Frontend-ZooApp/` — Aplicación Android (Kotlin + Jetpack Compose)
- `Backend-zoo-app/` — Servidor API REST (Node.js + Express + MongoDB)
- `apk/` — APK compilado para pruebas directas

## Tecnologías

Kotlin, Jetpack Compose, AltBeacon, Node.js, Express, MongoDB Atlas, 
ImageKit.io, Render.

## Cómo probar la aplicación

La forma más rápida es instalar el APK incluido en `apk/app-debug.apk` 
en un dispositivo Android 8.1 (API 27) o superior. La aplicación se 
conecta automáticamente al backend ya desplegado en Render.

## Cómo ejecutar el backend en local (opcional)

1. Entrar en `Backend-zoo-app/`
2. Instalar dependencias: `npm install`
3. El archivo `.env` ya incluye la configuración necesaria
4. Ejecutar: `node server.js`

## Cómo ejecutar la aplicación Android desde el código fuente

1. Abrir `Frontend-ZooApp/` con Android Studio
2. Sincronizar Gradle
3. Ejecutar en un emulador o dispositivo con Android 8.1 (API 27) o superior

## Pruebas

Para simular beacons sin hardware físico se utilizó la aplicación 
"Beacon Simulator" (Agile 4.0 Cluster), disponible en Google Play.

## Autor

Miguel Martínez López — TFG Ingeniería Informática, Universidad de Málaga, 2026  
Tutora: Mónica Pinto Alarcón
