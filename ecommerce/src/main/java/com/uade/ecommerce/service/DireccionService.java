package com.uade.ecommerce.service;

import com.uade.ecommerce.entity.Direccion;
import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.repository.DireccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DireccionService {

    @Autowired
    private DireccionRepository direccionRepository;

    public List<Direccion> getDireccionesByUsuario(Usuario usuario) {
        return direccionRepository.findByUsuario(usuario);
    }

    public Optional<Direccion> getDireccionById(int id) {
        return direccionRepository.findById(id);
    }

    public Direccion saveDireccion(Direccion direccion) {
        return direccionRepository.save(direccion);
    }

    public void deleteDireccion(int id) {
        direccionRepository.deleteById(id);
    }
}
