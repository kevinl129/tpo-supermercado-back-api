package com.uade.ecommerce.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.uade.ecommerce.entity.Imagen;
import com.uade.ecommerce.entity.Producto;
import com.uade.ecommerce.repository.ImagenRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import com.uade.ecommerce.exception.ProductoNotFoundException;

@Service
public class ImagenService {

    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private ImagenRepository imagenRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Imagen guardarImagen(int productoId, MultipartFile archivo) throws IOException, ProductoNotFoundException {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con id: " + productoId));

        File carpeta = new File(UPLOAD_DIR);
        if (!carpeta.exists()) carpeta.mkdirs();

        String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename().replaceAll("\\s+","_");
        Path rutaArchivo = Paths.get(UPLOAD_DIR, nombreArchivo);
        Files.write(rutaArchivo, archivo.getBytes());

        Imagen imagen = new Imagen();
        imagen.setImagen(UPLOAD_DIR + nombreArchivo);
        imagen.setProducto(producto);

        return imagenRepository.save(imagen);
    }

    public List<Imagen> obtenerImagenesPorProducto(int productoId) {
        return imagenRepository.findByProductoId(productoId);
    }

    public void eliminarImagenPorId(int imagenId) throws IOException {
        Optional<Imagen> opt = imagenRepository.findById(imagenId);
        if (opt.isPresent()) {
            Imagen imagen = opt.get();
            // borrar archivo fisico si existe
            try {
                Files.deleteIfExists(Paths.get(imagen.getImagen()));
            } catch (IOException e) {
                // loggear si querés, continuar con el borrado en BD
            }
            imagenRepository.deleteById(imagenId);
        } else {
            throw new RuntimeException("Imagen no encontrada con id: " + imagenId);
        }
    }

    public void eliminarImagenesPorProducto(int productoId) {
        // opcional: borrar archivos fisicos
        List<Imagen> imgs = imagenRepository.findByProductoId(productoId);
        for (Imagen img : imgs) {
            try { Files.deleteIfExists(Paths.get(img.getImagen())); } catch (IOException ignored) {}
        }
        imagenRepository.deleteByProductoId(productoId);
    }
}
