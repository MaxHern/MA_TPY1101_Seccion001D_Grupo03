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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private TrabajoRepository trabajoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // FIX-29/30: al cargar la conversación, marcar como leídos los mensajes del receptor
    @GetMapping("/{idTrabajo}")
    public ResponseEntity<?> getMensajes(
            @PathVariable Long idTrabajo,
            @RequestParam(required = false) Long idUsuario1,
            @RequestParam(required = false) Long idUsuario2) {

        if (idUsuario1 != null && idUsuario2 != null) {
            List<Mensaje> mensajes = mensajeRepository.findConversacionPrivada(
                idTrabajo, idUsuario1, idUsuario2
            );

            // Marcar como leídos los mensajes que recibió idUsuario1
            // (idUsuario1 es quien está abriendo el chat, es decir el receptor actual)
            mensajes.stream()
                .filter(m -> m.getReceptor() != null
                          && m.getReceptor().getId().equals(idUsuario1)
                          && !m.isLeido())
                .forEach(m -> {
                    m.setLeido(true);
                    mensajeRepository.save(m);
                });

            return ResponseEntity.ok(mensajes);
        }
        return ResponseEntity.ok(List.of());
    }

    // FIX-29/30: endpoint para contar no leídos de UNA conversación específica
    @GetMapping("/no-leidos/{idTrabajo}")
    public ResponseEntity<?> contarNoLeidosPorConversacion(
            @PathVariable Long idTrabajo,
            @RequestParam Long idReceptor) {
        long count = mensajeRepository
            .countByTrabajo_IdAndReceptor_IdAndLeidoFalse(idTrabajo, idReceptor);
        return ResponseEntity.ok(Map.of("noLeidos", count));
    }

    @GetMapping("/participantes/{idTrabajo}")
    public ResponseEntity<?> getParticipantes(
            @PathVariable Long idTrabajo,
            @RequestParam Long idDueno) {
        List<Usuario> participantes = mensajeRepository.findParticipantesByTrabajo(idTrabajo, idDueno);
        return ResponseEntity.ok(participantes);
    }

    @GetMapping("/inbox/{idUsuario}")
    public ResponseEntity<?> getInbox(@PathVariable Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) return ResponseEntity.notFound().build();

        List<Trabajo> conMensajes = mensajeRepository.findTrabajosConMensajesByUsuario(usuario);
        List<Trabajo> comoCliente = trabajoRepository.findByCliente(usuario);

        List<Trabajo> resultado = new ArrayList<>(conMensajes);
        for (Trabajo t : comoCliente) {
            if (resultado.stream().noneMatch(tm -> tm.getId().equals(t.getId()))) {
                resultado.add(t);
            }
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity<?> postMensaje(@RequestBody Map<String, Object> body) {
        try {
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
            // leido = false por defecto (el receptor aún no lo ha visto)

            return ResponseEntity.ok(mensajeRepository.save(m));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}