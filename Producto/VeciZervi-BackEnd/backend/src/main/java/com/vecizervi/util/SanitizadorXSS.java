package com.vecizervi.util;

/**
 
S05 — Sanitización XSS
Limpia cualquier campo de texto antes de guardarlo en la BD.
Elimina tags HTML/script y escapa caracteres peligrosos.*/
public class SanitizadorXSS {

    private SanitizadorXSS() {}

    public static String limpiar(String input) {
        if (input == null) return null;
        return input
            // Eliminar tags <script> con contenido
            .replaceAll("(?i)<script[^>]*>.*?</script>", "")
            // Eliminar tags <iframe>, <object>, <embed>
            .replaceAll("(?i)<(iframe|object|embed|form|input|link|meta)[^>]*>.*?</\\1>", "")
            .replaceAll("(?i)<(iframe|object|embed|form|input|link|meta)[^>]*/?>", "")
            // Eliminar atributos on (onclick, onload, etc.)
            .replaceAll("(?i)\\son\\w+\\s=\\s\"[^\"]*\"", "")
            .replaceAll("(?i)\\son\\w+\\s=\\s'[^']*'", "")
            // Eliminar javascript: en href/src
            .replaceAll("(?i)javascript\\s*:", "")
            // Eliminar cualquier otro tag HTML restante
            .replaceAll("<[^>]+>", "")
            // Escapar caracteres peligrosos
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .trim();
    }

    public static String limpiarPermitirVacio(String input) {
        if (input == null || input.isBlank()) return "";
        return limpiar(input);
    }
}
