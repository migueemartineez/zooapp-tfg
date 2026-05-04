const express = require('express');
const router = express.Router();
const Zona = require('../models/Zona');

// Obtener todas las zonas
router.get('/', async (req, res) => {
    try {
        const zonas = await Zona.find();
        res.json(zonas);
    } catch (error) {
        res.status(500).json({ error: 'Error al obtener zonas' });
    }
});

// Obtener zona por ID
router.get('/:id', async (req, res) => {
    try {
        const zona = await Zona.findById(req.params.id);
        if (!zona) return res.status(404).json({ error: 'Zona no encontrada' });
        res.json(zona);
    } catch (error) {
        res.status(500).json({ error: 'Error al obtener zona' });
    }
});

// Crear zona
router.post('/', async (req, res) => {
    try {
        const nuevaZona = new Zona(req.body);
        await nuevaZona.save();
        res.status(201).json(nuevaZona);
    } catch (error) {
        res.status(500).json({ error: 'Error al crear zona' });
    }
});

module.exports = router;