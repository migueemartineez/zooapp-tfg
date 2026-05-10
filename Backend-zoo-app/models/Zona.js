const mongoose = require('mongoose');

const zonaSchema = new mongoose.Schema({
    nombre: { type: String, required: true },
    descripcion: { type: String },
    orden: { type: Number, required: true }
});

module.exports = mongoose.model('Zona', zonaSchema);