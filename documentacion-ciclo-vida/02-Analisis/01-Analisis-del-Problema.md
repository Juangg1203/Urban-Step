# 1. Analisis del problema

Una tienda de ropa que crece necesita mas que un catalogo en linea: necesita controlar
que se vende, quien lo aprueba, quien lo despacha, y quien responde por la seguridad de
los datos de sus clientes. Ademas, el negocio quiere poder pagarle una comision a sus
vendedores sin que eso dependa de llevar la cuenta a mano en una hoja de calculo aparte.

El problema se resume en tres tensiones que el sistema tiene que resolver:

1. **Rapidez para el cliente vs. control para la empresa.** El cliente quiere comprar sin
   fricción; la empresa necesita verificar el pago antes de comprometer mercancia.
2. **Autonomia del vendedor vs. trazabilidad.** El vendedor necesita poder vender
   (incluso por un cliente presencial) sin que eso abra una puerta para inflar sus
   propias comisiones sin control.
3. **Separacion de responsabilidades.** Quien administra el sistema no deberia poder
   tocar el catalogo ni aprobar ventas; quien vende no deberia ver lo que le corresponde
   a bodega, y viceversa.
