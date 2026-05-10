const mongoose = require('mongoose');

const animalSchema = new mongoose.Schema({
    nombre: { type: String, required: true },
    descripcion: { type: String }, // ej: Biología y Comportamiento
    gradoAmenaza: { type: String },
    dieta: { type: String },
    curiosidades: { type: String },
    vida: { type: String },
    ecosistema: { type: String }, // ej: 'África ecuatorial'
    tipoAnimal: { type: String }, // ej: mamífero
    imagen: { type: String }, //URL de ImageKit
    zonaId: { type: mongoose.Schema.Types.ObjectId, ref: 'Zona', required: true }
});

module.exports = mongoose.model('Animal', animalSchema);