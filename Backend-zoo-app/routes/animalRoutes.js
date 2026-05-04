const express = require('express');
const router = express.Router();
const Animal = require('../models/Animal');

// Obtener todos los animales
router.get('/', async (req, res) => {
    try {
        const animales = await Animal.find();
        res.json(animales);
    } catch (error) {
        res.status(500).json({ error: 'Error al obtener animales' });
    }
});

// Obtener animal por ID
router.get('/:id', async (req, res) => {
    try {
        const animal = await Animal.findById(req.params.id);
        if (!animal) return res.status(404).json({ error: 'Animal no encontrado' });
        res.json(animal);
    } catch (error) {
        res.status(500).json({ error: 'Error al obtener animal' });
    }
});

// Obtener animales por zona
router.get('/zona/:zonaId', async (req, res) => {
    try {
        const animales = await Animal.find({ zonaId: req.params.zonaId });
        res.json(animales);
    } catch (error) {
        res.status(500).json({ error: 'Error al obtener animales por zona' });
    }
});

// Crear animal
router.post('/', async (req, res) => {
    try {
        const nuevoAnimal = new Animal(req.body);
        await nuevoAnimal.save();
        res.status(201).json(nuevoAnimal);
    } catch (error) {
        res.status(500).json({ error: 'Error al crear animal' });
    }
});

module.exports = router;