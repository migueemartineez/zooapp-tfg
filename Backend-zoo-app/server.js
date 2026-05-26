const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config(); // Carga las variables del archivo .env

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Conexión a MongoDB
mongoose.connect(process.env.MONGO_URI)
  .then(() => console.log('✅ Conectado a MongoDB'))
  .catch((err) => console.error('❌ Error al conectar a MongoDB:', err));

// Ruta básica de prueba
app.get('/', (req, res) => {
    res.send('SmartZoo API está funcionando correctamente');
});

// Rutas
const userRoutes = require('./routes/userRoutes');
const zonaRoutes = require('./routes/zonaRoutes');
const animalRoutes = require('./routes/animalRoutes');

app.use('/api/users', userRoutes);
app.use('/api/zonas', zonaRoutes);
app.use('/api/animales', animalRoutes);

// Puerto del servidor
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`🚀 Servidor corriendo en http://localhost:${PORT}`);
});