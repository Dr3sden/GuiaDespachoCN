package cl.duoc.guias_despacho.controller;

import cl.duoc.guias_despacho.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/guia")
public class GuiaController {

    private final S3Service s3Service;

    @Value("${efs.guias.path}")
    private String efsPath;

    public GuiaController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    // Endpoint 1: Crear guía (se guarda en EFS)
    @PostMapping("/crear")
    public ResponseEntity<String> crearGuia(
            @RequestParam String transportista,
            @RequestParam String idGuia,
            @RequestParam String contenido) throws Exception {

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String rutaLocal = Paths.get(efsPath, fecha, transportista, idGuia + ".txt").toString();
        File file = new File(rutaLocal);
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), contenido);

        return ResponseEntity.ok("Guía guardada en EFS: " + rutaLocal);
    }

    // Endpoint 2: Subir guía de EFS a S3
    @PostMapping("/subir")
    public ResponseEntity<String> subirAS3(
            @RequestParam String transportista,
            @RequestParam String idGuia) throws Exception {

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = Paths.get(fecha, transportista, idGuia + ".txt").toString();
        String rutaLocal = Paths.get(efsPath, fecha, transportista, idGuia + ".txt").toString();
        File file = new File(rutaLocal);

        if (!file.exists()) {
            return ResponseEntity.badRequest().body("La guía no existe en EFS");
        }

        s3Service.uploadFile(key, file);
        return ResponseEntity.ok("Guía subida a S3: " + key);
    }

    // Endpoint 3: Descargar guía desde S3
    @GetMapping("/descargar")
    public ResponseEntity<String> descargarGuia(
            @RequestParam String transportista,
            @RequestParam String idGuia) throws Exception {

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = Paths.get(fecha, transportista, idGuia + ".txt").toString();
        File temp = new File("/tmp/" + idGuia + ".txt");
        s3Service.downloadFile(key, temp);

        String contenido = Files.readString(temp.toPath());
        return ResponseEntity.ok(contenido);
    }

    // Endpoint 4: Modificar guía en S3
    @PutMapping("/modificar")
    public ResponseEntity<String> modificarGuia(
            @RequestParam String transportista,
            @RequestParam String idGuia,
            @RequestParam String nuevoContenido) throws Exception {

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = Paths.get(fecha, transportista, idGuia + ".txt").toString();
        File temp = new File("/tmp/" + idGuia + ".txt");
        Files.writeString(temp.toPath(), nuevoContenido);

        s3Service.uploadFile(key, temp);
        return ResponseEntity.ok("Guía actualizada en S3");
    }

    // Endpoint 5: Eliminar guía de S3
    @DeleteMapping("/eliminar")
    public ResponseEntity<String> eliminarGuia(
            @RequestParam String transportista,
            @RequestParam String idGuia) {

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = Paths.get(fecha, transportista, idGuia + ".txt").toString();
        s3Service.deleteFile(key);
        return ResponseEntity.ok("Guía eliminada de S3");
    }
}
