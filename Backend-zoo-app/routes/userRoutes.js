const express = require('express');
const router = express.Router();
const User = require('../models/User');

// Registro de usuario
router.post('/registro', async (req, res) => {
    try {
        const { nombre, email, contraseña, preferencias } = req.body;

        const existeUsuario = await User.findOne({ email });
        if (existeUsuario) {
            return res.status(400).json({ error: 'El email ya está registrado' });
        }

        const nuevoUsuario = new User({ nombre, email, contraseña, preferencias });
        await nuevoUsuario.save();
        res.status(201).json({ mensaje: 'Usuario registrado correctamente', id: nuevoUsuario._id });
    } catch (error) {
        res.status(500).json({ error: 'Error al registrar usuario' });
    }
});

// Login
router.post('/login', async (req, res) => {
    try {
        const { email, contraseña } = req.body;
        const usuario = await User.findOne({ email, contraseña });
        if (!usuario) {
            return res.status(401).json({ error: 'Email o contraseña incorrectos' });
        }
        res.json({ mensaje: 'Login correcto', usuario });
    } catch (error) {
        res.status(500).json({ error: 'Error al iniciar sesión' });
    }
});

// Obtener usuario por ID
router.get('/:id', async (req, res) => {
    try {
        const usuario = await User.findById(req.params.id);
        if (!usuario) return res.status(404).json({ error: 'Usuario no encontrado' });
        res.json(usuario);
    } catch (error) {
        res.status(500).json({ error: 'Error al obtener usuario' });
    }
});

// Actualizar preferencias
router.put('/:id/preferencias', async (req, res) => {
    try {
        const usuario = await User.findByIdAndUpdate(
            req.params.id,
            { preferencias: req.body.preferencias },
            { new: true }
        );
        res.json(usuario);
    } catch (error) {
        res.status(500).json({ error: 'Error al actualizar preferencias' });
    }
});

// Añadir visita al historial
router.post('/:id/visita', async (req, res) => {
    try {
        const { zonasVisitadas, animalesVistos } = req.body;
        const usuario = await User.findByIdAndUpdate(
            req.params.id,
            { $push: { historialVisitas: { zonasVisitadas, animalesVistos } } },
            { new: true }
        );
        res.json(usuario);
    } catch (error) {
        res.status(500).json({ error: 'Error al guardar visita' });
    }
});

// Añadir logro
router.post('/:id/logro', async (req, res) => {
    try {
        const usuario = await User.findByIdAndUpdate(
            req.params.id,
            { $addToSet: { logros: req.body.logro } },
            { new: true }
        );
        res.json(usuario);
    } catch (error) {
        res.status(500).json({ error: 'Error al añadir logro' });
    }
});

module.exports = router;