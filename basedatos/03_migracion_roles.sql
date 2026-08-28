-- ============================================================
--  Migracion de roles viejos a los nuevos
-- ============================================================
--  Usa este script SOLO si ya tenias la base creada con los roles
--  anteriores (AGENTE, MARKETING, OFICIAL_DATOS) y no quieres perder
--  los datos. Si te da igual empezar de cero, es mas rapido:
--
--      DROP DATABASE tienda_ropa;
--
--  y volver a ejecutar 01_esquema.sql.
--
--  Sintoma que resuelve este script:
--      No enum constant com.tiendaropa.model.Rol.AGENTE
--  Hibernate lee una fila con un rol que ya no existe en el codigo
--  y no sabe convertirla.
-- ============================================================

USE tienda_ropa;

-- 1. La columna del subtipo puede no existir todavia.
--    Si MySQL responde "Duplicate column name", ya la tenias: ignora el error.
ALTER TABLE usuario ADD COLUMN subtipo VARCHAR(20) NULL AFTER rol;

-- 2. Equivalencias entre el modelo viejo y el nuevo
--    AGENTE          -> EMPLEADO (vendedor): atiende y verifica pagos
--    MARKETING       -> JEFE: consulta reportes
--    OFICIAL_DATOS   -> JEFE: audita accesos y ve datos privados
UPDATE usuario SET rol = 'EMPLEADO', subtipo = 'VENDEDOR' WHERE rol = 'AGENTE';
UPDATE usuario SET rol = 'JEFE',     subtipo = NULL       WHERE rol IN ('MARKETING', 'OFICIAL_DATOS');

-- 3. Comprobacion: despues de esto no debe quedar ningun rol desconocido
SELECT rol, subtipo, COUNT(*) AS cuantos
FROM usuario
GROUP BY rol, subtipo;

-- Si el SELECT anterior muestra algo distinto de CLIENTE, EMPLEADO,
-- JEFE o ADMIN, corrige esas filas a mano antes de arrancar la aplicacion.
