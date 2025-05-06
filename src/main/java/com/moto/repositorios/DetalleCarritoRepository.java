package com.moto.repositorios;

import com.moto.modelos.DetalleCarrito;
import com.moto.modelos.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {
    List<DetalleCarrito> findByCarritoId(Long carritoId);
    void deleteByCarritoId(Long carritoId);
}
