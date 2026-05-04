const mongoose = require('mongoose');

const zonaSchema = new mongoose.Schema({
    nombre: { type: String, required: true },
    descripcion: { type: String },
    coordenadas: {
        latitud: { type: Number, required: true },
        longitud: { type: Number, required: true },
        radio: { type: Number, required: true } // en metros, para el geofencing
    }
});

module.exports = mongoose.model('Zona', zonaSchema);