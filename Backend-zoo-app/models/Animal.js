const mongoose = require('mongoose');

const animalSchema = new mongoose.Schema({
    nombre: { type: String, required: true },
    descripcion: { type: String, required: true },
    ecosistema: { type: String, required: true }, // ej: 'África subsahariana'
    tipoAnimal: { type: String, required: true },  // ej: 'mamífero'
    interesTematico: [String], // ej: ['conservación', 'comportamiento']
    imagen: { type: String },  // URL de ImageKit
    zonaId: { type: mongoose.Schema.Types.ObjectId, ref: 'Zona', required: true }
});

module.exports = mongoose.model('Animal', animalSchema);