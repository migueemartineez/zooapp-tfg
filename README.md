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

### Configuración de beacons por zona

Para probar la detección automática de zonas, instala "Beacon Simulator" 
en un segundo dispositivo Android y configura un perfil de beacon iBeacon 
para cada zona con los siguientes valores:

**UUID común para todas las zonas:** `b9407f30-f5f8-466e-aff9-25556b57fe6d`

- **Isla de Madagascar** — Major: 1, Minor: 1
- **África Ecuatorial** — Major: 1, Minor: 2
- **Sudeste Asiático** — Major: 2, Minor: 1
- **Indo Pacífico** — Major: 2, Minor: 2
- **Centro y Sudamérica** — Major: 3, Minor: 1

### Pasos para probar

1. Instala "Beacon Simulator" en un dispositivo Android distinto al que 
   tiene SmartZoo.
2. Crea un perfil iBeacon con el UUID indicado y los valores Major/Minor 
   de la zona que quieras simular.
3. Activa la emisión del beacon.
4. En el dispositivo con SmartZoo, asegúrate de tener Bluetooth y 
   ubicación activados.
5. Acércate los dos dispositivos (a menos de 1-2 metros) e inicia sesión 
   en SmartZoo.
6. La zona correspondiente se resaltará en el mapa con una aureola verde, 
   y se desbloqueará el contenido de los animales de esa zona.
7. Para cambiar de zona, detén la emisión actual y activa el perfil de 
   otra zona.

## Autor

Miguel Martínez López — TFG Ingeniería Informática, Universidad de Málaga, 2026  
Tutora: Mónica Pinto Alarcón