const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    nombre: { type: String, required: true },
    edad: { type: Number, required: true },
    preferencias: [String]
});

module.exports = mongoose.model('User', userSchema);
