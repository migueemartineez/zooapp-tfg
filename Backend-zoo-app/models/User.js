const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    nombre: { type: String, required: true },
    email: { type: String, required: true, unique: true },
    contraseña: { type: String, required: true },
    preferencias: {
        ecosistema: [String],
        tipoAnimal: [String],
        interesTematico: [String]
    },
    logros: [String],
    historialVisitas: [{
        fecha: { type: Date, default: Date.now },
        zonasVisitadas: [String],
        animalesVistos: [String]
    }],
    fechaRegistro: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', userSchema);