package com.uade.ecommerce.service;

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

    private final Path rootUbicacion; // Absolute path for uploads

    @Autowired
    private ImagenRepository imagenRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // Constructor to create the uploads directory using an absolute path
    public ImagenService() {
        this.rootUbicacion = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootUbicacion);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public Imagen guardarImagen(int productoId, MultipartFile archivo) throws IOException, ProductoNotFoundException {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con id: " + productoId));

        String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename().replaceAll("\\s+","_");
        Path rutaArchivoDestino = this.rootUbicacion.resolve(nombreArchivo); // Absolute path for saving

        Files.write(rutaArchivoDestino, archivo.getBytes()); // Save the file

        Imagen imagen = new Imagen();
        // Store the RELATIVE path in the database (e.g., "uploads/...")
        String rutaParaDB = Paths.get("uploads").resolve(nombreArchivo).toString().replace("\\", "/");
        imagen.setImagen(rutaParaDB);
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
            try {
                // Resolve absolute path to delete the physical file
                Path nombreArchivo = Paths.get(imagen.getImagen()).getFileName();
                Path archivoABorrar = this.rootUbicacion.resolve(nombreArchivo);
                Files.deleteIfExists(archivoABorrar);
            } catch (IOException e) {
                System.err.println("Failed to delete physical file: " + imagen.getImagen() + " - " + e.getMessage());
            }
            imagenRepository.deleteById(imagenId);
        } else {
            System.err.println("Imagen no encontrada con id: " + imagenId);
        }
    }

    public void eliminarImagenesPorProducto(int productoId) {
        List<Imagen> imgs = imagenRepository.findByProductoId(productoId);
        for (Imagen img : imgs) {
            try {
                // Resolve absolute path to delete physical files
                Path nombreArchivo = Paths.get(img.getImagen()).getFileName();
                Path archivoABorrar = this.rootUbicacion.resolve(nombreArchivo);
                Files.deleteIfExists(archivoABorrar);
            } catch (IOException e) {
                 System.err.println("Failed to delete physical file during product image cleanup: " + img.getImagen() + " - " + e.getMessage());
            }
        }
        imagenRepository.deleteByProductoId(productoId);
    }
}