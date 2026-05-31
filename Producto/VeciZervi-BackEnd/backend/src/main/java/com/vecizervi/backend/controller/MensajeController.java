package com.vecizervi.backend.controller;

import com.vecizervi.backend.model.Mensaje;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import com.vecizervi.backend.repository.MensajeRepository;
import com.vecizervi.backend.repository.TrabajoRepository;
import com.vecizervi.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private TrabajoRepository trabajoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // GET historial de mensajes de un trabajo
    @GetMapping("/{idTrabajo}")
    public ResponseEntity<?> getMensajes(@PathVariable Long idTrabajo) {
        Trabajo trabajo = trabajoRepository.findById(idTrabajo).orElse(null);
        if (trabajo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(mensajeRepository.findByTrabajoOrderByFechaEnvioAsc(trabajo));
    }

    // GET todos los trabajos donde el usuario participó en el chat (para el inbox)
    @GetMapping("/inbox/{idUsuario}")
    public ResponseEntity<?> getInbox(@PathVariable Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) return ResponseEntity.notFound().build();

        // Trabajos donde el usuario envió o recibió mensajes
        List<Trabajo> trabajosConMensajes = mensajeRepository.findTrabajosConMensajesByUsuario(usuario);

        // También incluir trabajos donde es el cliente (aunque no haya escrito aún)
        List<Trabajo> trabajosComoCliente = trabajoRepository.findByCliente(usuario);

        // Unir ambas listas sin duplicados
        for (Trabajo t : trabajosComoCliente) {
            if (trabajosConMensajes.stream().noneMatch(tm -> tm.getId().equals(t.getId()))) {
                trabajosConMensajes.add(t);
            }
        }

        return ResponseEntity.ok(trabajosConMensajes);
    }

    // POST enviar mensaje
    @PostMapping
    public ResponseEntity<?> postMensaje(@RequestBody Map<String, Object> body) {
        if (!body.containsKey("id_trabajo") || !body.containsKey("id_emisor") || !body.containsKey("contenido"))
            return ResponseEntity.badRequest().body("Faltan campos: id_trabajo, id_emisor, contenido.");

        Long idTrabajo   = Long.valueOf(body.get("id_trabajo").toString());
        Long idEmisor    = Long.valueOf(body.get("id_emisor").toString());
        String contenido = body.get("contenido").toString().trim();

        if (contenido.isEmpty())
            return ResponseEntity.badRequest().body("El contenido no puede estar vacío.");

        Trabajo trabajo = trabajoRepository.findById(idTrabajo).orElse(null);
        Usuario emisor  = usuarioRepository.findById(idEmisor).orElse(null);

        if (trabajo == null) return ResponseEntity.badRequest().body("Trabajo no encontrado.");
        if (emisor  == null) return ResponseEntity.badRequest().body("Emisor no encontrado.");

        Usuario receptor = null;
        if (body.containsKey("id_receptor")) {
            try {
                Long idReceptor = Long.valueOf(body.get("id_receptor").toString());
                if (idReceptor > 0) receptor = usuarioRepository.findById(idReceptor).orElse(null);
            } catch (Exception ignored) {}
        }

        Mensaje m = new Mensaje();
        m.setTrabajo(trabajo);
        m.setEmisor(emisor);
        m.setReceptor(receptor);
        m.setContenido(contenido);

        return ResponseEntity.ok(mensajeRepository.save(m));
    }
}