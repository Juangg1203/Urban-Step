-- =====================================================================
--  Datos minimos. Si dejas app.datos-demo=true la aplicacion carga
--  ademas usuarios, clientes y 3 meses de atenciones de prueba.
--  Claves de los usuarios demo: ver README.
-- =====================================================================
USE tienda_ropa;

INSERT INTO categoria (nombre, linea) VALUES
 ('Camisetas','ROPA'), ('Pantalones','ROPA'), ('Chaquetas','ROPA'),
 ('Tenis','CALZADO'), ('Botas','CALZADO'), ('Sandalias','CALZADO');
