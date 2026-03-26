const express = require('express');
const router = express.Router();
const User = require('../models/User');

// Crear usuario
router.post('/', async (req, res) => {
  try {
    const nuevoUsuario = new User(req.body);
    await nuevoUsuario.save();

    const usuarioResponse = {
      id: nuevoUsuario._id,
      nombre: nuevoUsuario.nombre,
      edad: nuevoUsuario.edad,
      preferencias: nuevoUsuario.preferencias
    };

    res.status(201).json(usuarioResponse);
  } catch (error) {
    res.status(500).json({ error: 'Error al crear usuario' });
  }
});

// Obtener todos los usuarios
router.get('/', async (req, res) => {
  try {
    const users = await User.find();
    const listaUsuarios = users.map(u => ({
      id: u._id,
      nombre: u.nombre,
      edad: u.edad,
      preferencias: u.preferencias
    }));
    res.json(listaUsuarios);
  } catch (err) {
    res.status(500).send('Error al obtener usuarios');
  }
});

// Eliminar usuario por ID
router.delete('/:id', async (req, res) => {
  try {
    await User.findByIdAndDelete(req.params.id);
    res.status(200).send('Usuario eliminado');
  } catch (err) {
    res.status(500).send('Error al eliminar usuario');
  }
});

module.exports = router;