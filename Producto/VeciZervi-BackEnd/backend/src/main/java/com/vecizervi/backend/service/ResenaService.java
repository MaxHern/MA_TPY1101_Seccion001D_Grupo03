package com.vecizervi.backend.service;

import com.vecizervi.backend.model.Resena;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.repository.ResenaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    public Resena save(Resena resena) {
        return resenaRepository.save(resena);
    }

    public List<Resena> findAll() {
        return resenaRepository.findAll();
    }

    public List<Resena> findByTrabajo(Trabajo trabajo) {
        return resenaRepository.findByTrabajo(trabajo);
    }
}