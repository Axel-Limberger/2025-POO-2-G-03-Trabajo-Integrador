# 🗺️ MAPA COMPLETO DE LLAMADAS ENTRE MÉTODOS

## 📋 ÍNDICE

1. [Módulo de Pagos](#módulo-de-pagos)
2. [Módulo de Facturas](#módulo-de-facturas)
3. [Módulo de Clientes](#módulo-de-clientes)
4. [Módulo de Servicios](#módulo-de-servicios)
5. [Facturación Masiva](#facturación-masiva)

---

## 💰 MÓDULO DE PAGOS

### `Pago.crearPago()` - Factory Method

**Ubicación:** `com.unam.integrador.model.Pago`

**ES LLAMADO POR:**
- `PagoService.registrarPagoCombinado()` (línea donde crea pagos)
- `PagoService.aplicarSaldoAFavor()` (línea donde crea el pago)

**LLAMA A:**
- Constructor privado `Pago(BigDecimal, MetodoPago, String)`
  - `validarMonto(BigDecimal)`
  - `validarMetodoPago(MetodoPago)`
  - `validarReferencia(String)`

**Sintaxis:**
```java
public static Pago crearPago(BigDecimal monto, MetodoPago metodoPago, String referencia)
```

**Propósito:** Crear instancias de Pago asegurando que todos los datos sean válidos.

---

### `Pago.agregarDetallePago()`

**Ubicación:** `com.unam.integrador.model.Pago`

**ES LLAMADO POR:**
- `DetallePago.crear()` (para mantener relación bidireccional)

**LLAMA A:**
- `List.contains(Object)` (método de ArrayList)
- `List.add(Object)` (método de ArrayList)

**Sintaxis:**
```java
void agregarDetallePago(DetallePago detalle)
```

**Propósito:** Mantener sincronizada la relación bidireccional Pago ↔ DetallePago.

---

### `DetallePago.crear()` - Factory Method

**Ubicación:** `com.unam.integrador.model.DetallePago`

**ES LLAMADO POR:**
- `PagoService.registrarPagoCombinado()` (al distribuir el pago entre facturas)
- `Factura.registrarPago()` (retorna el DetallePago creado)

**LLAMA A:**
- Constructor privado `DetallePago(Pago, Factura, BigDecimal)`
  - `validarPago(Pago)`
  - `validarFactura(Factura)`
  - `validarMontoAplicado(BigDecimal)`
- `Pago.agregarDetallePago(this)`
- `Factura.agregarDetallePago(this)`

**Sintaxis:**
```java
public static DetallePago crear(Pago pago, Factura factura, BigDecimal montoAplicado)
```

**Propósito:** Crear la relación entre un Pago y una Factura con un monto específico.

---

### `PagoService.registrarPagoCombinado()`

**Ubicación:** `com.unam.integrador.services.PagoService`

**ES LLAMADO POR:**
- `PagoController.registrarPagoCombinado()` (método POST del formulario)

**LLAMA A:**
1. `FacturaRepository.findAllById(List<Long>)` - Obtener facturas a pagar
2. `CuentaCliente.aplicarSaldoAFavor(BigDecimal)` - Si hay saldo a favor
3. `Pago.crearPago()` - Crea uno o dos pagos (saldo + método de pago)
4. `generarNumeroReciboSecuencial()` - Genera número de recibo
5. `Pago.setNumeroRecibo(String)` - Asigna recibo a los pagos
6. `PagoRepository.save(Pago)` - Persiste pagos
7. Para cada factura:
   - `Factura.registrarPago(Pago, BigDecimal)` - Aplica el pago
   - `DetallePagoRepository.save(DetallePago)` - Persiste detalle
   - `FacturaRepository.save(Factura)` - Actualiza factura
8. `CuentaCliente.registrarSaldoAFavor(BigDecimal)` - Si sobra dinero
9. `CuentaClienteRepository.save(CuentaCliente)` - Actualiza cliente

**Sintaxis:**
```java
@Transactional
public String registrarPagoCombinado(
    List<Long> facturasIds, 
    BigDecimal montoTotal, 
    BigDecimal saldoAFavorAplicar,
    MetodoPago metodoPago, 
    String referencia)
```

**Retorna:** Número de recibo generado

**Propósito:** Coordinar el registro de un pago que puede aplicarse a múltiples facturas.

---

### `PagoService.listarFiltrados()`

**Ubicación:** `com.unam.integrador.services.PagoService`

**ES LLAMADO POR:**
- `PagoController.listarPagos()` (para mostrar lista con filtros)

**LLAMA A:**
1. `PagoRepository.findAll()` - Obtiene todos los pagos
2. `DetallePagoRepository.findByPagoIdPago(Long)` - Para filtrar por cliente
3. `Pago.getFechaPago()` - Para filtro de fechas
4. `Factura.getCliente()` - Para filtro de cliente
5. `CuentaCliente.getNombre()` - Para comparar nombres

**Sintaxis:**
```java
@Transactional(readOnly = true)
public List<Pago> listarFiltrados(String clienteNombre, LocalDate desde, LocalDate hasta)
```

**Propósito:** Buscar pagos aplicando filtros opcionales.

---

## 🧾 MÓDULO DE FACTURAS

### `Factura.agregarItem()`

**Ubicación:** `com.unam.integrador.model.Factura`

**ES LLAMADO POR:**
- `FacturaService.emitirFacturaDesdeServiciosContratados()` (al crear factura individual)
- `FacturaService.emitirFacturaProporcional()` (al crear factura proporcional)
- `FacturaService.ejecutarFacturacionMasiva()` (al crear facturas del lote)

**LLAMA A:**
1. `ItemFactura.calcular()` - Calcula subtotal, IVA y total del item
2. `ItemFactura.setFactura(this)` - Establece relación bidireccional
3. `List.add(ItemFactura)` - Agrega item a la lista
4. `calcularTotales()` - Recalcula totales de la factura

**Sintaxis:**
```java
public void agregarItem(ItemFactura item)
```

**Propósito:** Agregar una línea de detalle a la factura y actualizar totales.

---

### `Factura.calcularTotales()`

**Ubicación:** `com.unam.integrador.model.Factura`

**ES LLAMADO POR:**
- `Factura.agregarItem()` (después de agregar cada item)

**LLAMA A:**
1. `calcularSubtotal()` - Suma subtotales de items
2. `calcularTotalIva()` - Suma IVA de items
3. `calcularTotal()` - Calcula total con descuento
4. `calcularSaldoPendiente()` - Inicializa saldo

**Sintaxis:**
```java
public void calcularTotales()
```

**Propósito:** Ejecutar todos los cálculos en el orden correcto.

---

### `Factura.registrarPago()`

**Ubicación:** `com.unam.integrador.model.Factura`

**ES LLAMADO POR:**
- `PagoService.registrarPagoCombinado()` (al distribuir el pago)

**LLAMA A:**
1. `validarPuedeRecibirPago(BigDecimal)` - Verifica que puede recibir el pago
2. `DetallePago.crear(Pago, Factura, BigDecimal)` - Crea el detalle
3. `agregarDetallePago(DetallePago)` - Agrega a la lista
4. `BigDecimal.subtract()` - Actualiza saldo pendiente
5. `actualizarEstadoSegunSaldo()` - Cambia estado si corresponde

**Sintaxis:**
```java
public DetallePago registrarPago(Pago pago, BigDecimal montoAplicado)
```

**Retorna:** DetallePago creado

**Propósito:** Registrar un pago en la factura y actualizar su estado.

---

### `Factura.anular()`

**Ubicación:** `com.unam.integrador.model.Factura`

**ES LLAMADO POR:**
- `FacturaService.anularFactura()` (después de validar)
- `FacturaService.anularLoteFacturacion()` (para cada factura del lote)

**LLAMA A:**
1. `puedeSerAnulada()` - Valida que se puede anular
2. Establece `this.estado = EstadoFactura.ANULADA`

**Sintaxis:**
```java
public void anular()
```

**Propósito:** Cambiar el estado de la factura a ANULADA.

---

### `Factura.determinarTipoFactura()` - Método Estático

**Ubicación:** `com.unam.integrador.model.Factura`

**ES LLAMADO POR:**
- `FacturaService.emitirFacturaDesdeServiciosContratados()`
- `FacturaService.emitirFacturaProporcional()`
- `FacturaService.ejecutarFacturacionMasiva()`

**LLAMA A:** Ninguno (lógica condicional pura)

**Sintaxis:**
```java
public static TipoFactura determinarTipoFactura(
    TipoCondicionIVA condicionEmisor,
    TipoCondicionIVA condicionCliente)
```

**Retorna:** TipoFactura (A, B o C)

**Propósito:** Determinar qué tipo de factura emitir según las condiciones fiscales.

---

### `FacturaService.emitirFacturaDesdeServiciosContratados()`

**Ubicación:** `com.unam.integrador.services.FacturaService`

**ES LLAMADO POR:**
- `FacturaViewController.emitirFacturaIndividual()` (formulario de factura individual)

**LLAMA A:**
1. `CuentaClienteRepository.findById(Long)` - Obtiene el cliente
2. `CuentaCliente.getServiciosContratadosActivos()` - Servicios a facturar
3. `FacturaRepository.existsByClienteIdAndPeriodoAndEstadoNot()` - Valida duplicados
4. `Factura.determinarTipoFactura()` - Determina tipo A/B/C
5. `obtenerSerie(TipoFactura)` - Obtiene serie según tipo
6. `obtenerSiguienteNumeroFactura(int)` - Obtiene número secuencial
7. Constructor `new Factura(...)` - Crea la factura
8. `Factura.validarFechas()` - Valida fechas
9. `Factura.validarClienteActivo()` - Valida que cliente esté activo
10. Para cada servicio:
    - `new ItemFactura(...)` - Crea item
    - `Factura.agregarItem()` - Agrega a factura
11. `Factura.aplicarDescuento()` - Si hay descuento
12. `FacturaRepository.save(Factura)` - Persiste factura

**Sintaxis:**
```java
@Transactional
public Factura emitirFacturaDesdeServiciosContratados(
    Long clienteId, 
    LocalDate periodo, 
    LocalDate fechaEmision,
    LocalDate fechaVencimiento,
    Double porcentajeDescuento,
    String motivoDescuento)
```

**Retorna:** Factura generada

**Propósito:** Emitir una factura individual completa para un cliente.

---

### `FacturaService.anularFactura()`

**Ubicación:** `com.unam.integrador.services.FacturaService`

**ES LLAMADO POR:**
- `FacturaViewController.confirmarAnulacion()` (formulario de anulación)

**LLAMA A:**
1. `FacturaRepository.findById(Long)` - Obtiene la factura
2. `Factura.puedeSerAnulada()` - Valida que se puede anular
3. `obtenerSiguienteNumeroNotaCredito(int)` - Número de nota de crédito
4. Constructor `new NotaCredito(...)` - Crea nota de crédito
5. `Factura.agregarNotaCredito(NotaCredito)` - Asocia nota a factura
6. `Factura.anular()` - Cambia estado
7. `NotaCreditoRepository.save(NotaCredito)` - Persiste nota
8. `FacturaRepository.save(Factura)` - Actualiza factura

**Sintaxis:**
```java
@Transactional
public Factura anularFactura(Long facturaId, String motivo)
```

**Retorna:** Factura anulada

**Propósito:** Anular una factura generando la nota de crédito correspondiente.

---

### `FacturaService.ejecutarFacturacionMasiva()`

**Ubicación:** `com.unam.integrador.services.FacturaService`

**ES LLAMADO POR:**
- `FacturacionMasivaController.ejecutarFacturacion()` (formulario de facturación masiva)

**LLAMA A:**
1. `convertirPeriodoALocalDate(String)` - Convierte periodo a fecha
2. `LoteFacturacionRepository.existsByPeriodoFechaAndAnuladoFalse()` - Valida duplicados
3. Constructor `new LoteFacturacion(...)` - Crea el lote
4. `CuentaClienteRepository.findAll()` - Obtiene todos los clientes
5. `Stream.filter()` - Filtra clientes activos con servicios
6. `obtenerSiguienteNumeroFactura()` - Para cada serie A, B, C
7. Para cada cliente:
   - `Factura.determinarTipoFactura()` - Determina tipo
   - `obtenerSerie()` - Obtiene serie
   - Constructor `new Factura(...)` - Crea factura
   - Para cada servicio:
     - `new ItemFactura(...)` - Crea item
     - `Factura.agregarItem()` - Agrega item
   - `LoteFacturacion.agregarFactura()` - Agrega al lote
8. `LoteFacturacionRepository.save(LoteFacturacion)` - Persiste todo

**Sintaxis:**
```java
@Transactional
public LoteFacturacion ejecutarFacturacionMasiva(
    String periodoStr,
    LocalDate fechaVencimiento)
```

**Retorna:** LoteFacturacion con todas las facturas generadas

**Propósito:** Generar facturas para todos los clientes activos en un período.

---

## 👥 MÓDULO DE CLIENTES

### `CuentaCliente.contratarServicio()`

**Ubicación:** `com.unam.integrador.model.CuentaCliente`

**ES LLAMADO POR:**
- `CuentaClienteService.asignarServicio()` (al asignar servicio a cliente)

**LLAMA A:**
1. `tieneServicioContratadoActivo(Servicio)` - Valida duplicados
2. Constructor `new ServicioContratado()` - Crea el contrato
3. `ServicioContratado.setCliente(this)` - Establece relación
4. `ServicioContratado.setServicio(Servicio)` - Establece relación
5. `ServicioContratado.setFechaAlta(LocalDate.now())`
6. `ServicioContratado.setPrecioContratado()` - Precio actual
7. `List.add(ServicioContratado)` - Agrega a la lista

**Sintaxis:**
```java
public void contratarServicio(Servicio servicio)
```

**Propósito:** Asociar un servicio a un cliente registrando el precio actual.

---

### `CuentaCliente.cambiarEstado()`

**Ubicación:** `com.unam.integrador.model.CuentaCliente`

**ES LLAMADO POR:**
- `CuentaClienteService.cambiarEstado()` (al cambiar estado de cuenta)

**LLAMA A:**
1. Validaciones de parámetros (throw si falla)
2. Constructor `new CambioEstadoCuenta()` - Crea registro de cambio
3. `CambioEstadoCuenta.setCliente(this)`
4. `CambioEstadoCuenta.setEstadoAnterior()`
5. `CambioEstadoCuenta.setEstadoNuevo()`
6. `CambioEstadoCuenta.setMotivo()`
7. `List.add(CambioEstadoCuenta)` - Agrega al historial
8. `this.estado = nuevoEstado` - Actualiza estado

**Sintaxis:**
```java
public void cambiarEstado(EstadoCuenta nuevoEstado, String motivo)
```

**Propósito:** Cambiar el estado de la cuenta registrando el cambio en el historial.

---

### `CuentaCliente.aplicarSaldoAFavor()`

**Ubicación:** `com.unam.integrador.model.CuentaCliente`

**ES LLAMADO POR:**
- `PagoService.registrarPagoCombinado()` (al usar saldo a favor)
- `PagoService.aplicarSaldoAFavor()` (aplicación exclusiva de saldo)

**LLAMA A:**
1. `getSaldoAFavor()` - Obtiene saldo disponible
2. `BigDecimal.min()` - Calcula monto aplicable
3. `BigDecimal.add()` - Actualiza saldo (lo acerca a cero)

**Sintaxis:**
```java
public BigDecimal aplicarSaldoAFavor(BigDecimal montoSolicitado)
```

**Retorna:** Monto efectivamente aplicado

**Propósito:** Usar el saldo a favor del cliente para pagar facturas.

---

### `CuentaClienteService.asignarServicio()`

**Ubicación:** `com.unam.integrador.services.CuentaClienteService`

**ES LLAMADO POR:**
- `CuentaClienteController.confirmarAsignarServicio()` (formulario)

**LLAMA A:**
1. `obtenerClientePorId(Long)` - Obtiene el cliente
2. `ServicioRepository.findById(Long)` - Obtiene el servicio
3. `CuentaCliente.contratarServicio(Servicio)` - Crea el contrato
4. `CuentaClienteRepository.save(CuentaCliente)` - Persiste

**Sintaxis:**
```java
@Transactional
public CuentaCliente asignarServicio(Long clienteId, Long servicioId)
```

**Retorna:** Cliente actualizado

**Propósito:** Coordinar la asignación de un servicio a un cliente.

---

## 🛠️ MÓDULO DE SERVICIOS

### `Servicio.calcularIva()`

**Ubicación:** `com.unam.integrador.model.Servicio`

**ES LLAMADO POR:**
- `Servicio.calcularPrecioConIva()` (al calcular precio total)

**LLAMA A:**
1. `TipoAlicuotaIVA.getPorcentaje()` - Obtiene porcentaje de IVA
2. `BigDecimal.multiply()` - Multiplica precio × porcentaje
3. `BigDecimal.divide()` - Divide entre 100

**Sintaxis:**
```java
public BigDecimal calcularIva()
```

**Retorna:** Monto del IVA

**Propósito:** Calcular el IVA del servicio según su alícuota.

---

### `ServicioService.modificarServicio()`

**Ubicación:** `com.unam.integrador.services.ServicioService`

**ES LLAMADO POR:**
- `ServicioController.modificarServicio()` (formulario de edición)

**LLAMA A:**
1. `buscarPorId(Long)` - Obtiene el servicio
2. `ServicioRepository.findByNombre(String)` - Valida nombre único
3. `Servicio.modificar()` - Actualiza datos
4. Para cada contrato activo:
   - `ServicioContratado.setPrecioContratado()` - Actualiza precio
5. `ServicioRepository.save(Servicio)` - Persiste

**Sintaxis:**
```java
@Transactional
public Servicio modificarServicio(Long id, Servicio servicioActualizado)
```

**Retorna:** Servicio modificado

**Propósito:** Actualizar un servicio y propagar cambios de precio a contratos activos.

---

## 📊 FACTURACIÓN MASIVA

### `LoteFacturacion.agregarFactura()`

**Ubicación:** `com.unam.integrador.model.LoteFacturacion`

**ES LLAMADO POR:**
- `FacturaService.ejecutarFacturacionMasiva()` (al agregar cada factura al lote)

**LLAMA A:**
1. `Factura.setLoteFacturacion(this)` - Establece relación bidireccional
2. `List.add(Factura)` - Agrega a la lista
3. `this.cantidadFacturas++` - Incrementa contador
4. `BigDecimal.add()` - Suma al monto total

**Sintaxis:**
```java
public void agregarFactura(Factura factura)
```

**Propósito:** Agregar una factura al lote y actualizar totales.

---

### `LoteFacturacion.anular()`

**Ubicación:** `com.unam.integrador.model.LoteFacturacion`

**ES LLAMADO POR:**
- `FacturaService.anularLoteFacturacion()` (después de anular todas las facturas)

**LLAMA A:**
1. `puedeSerAnulado()` - Valida que se puede anular
2. Establece `this.anulado = true`
3. Establece `this.fechaAnulacion = LocalDateTime.now()`
4. Establece `this.motivoAnulacion = motivo`

**Sintaxis:**
```java
public void anular(String motivo)
```

**Propósito:** Marcar el lote como anulado.

---

## 🎯 TABLA RESUMEN: MÉTODOS MÁS IMPORTANTES

| Método | Clase | Llamado Por | Propósito |
|--------|-------|-------------|-----------|
| `crearPago()` | Pago | PagoService | Factory para crear pagos |
| `crear()` | DetallePago | PagoService, Factura | Factory para crear detalles |
| `registrarPago()` | Factura | PagoService | Aplicar pago a factura |
| `agregarItem()` | Factura | FacturaService | Agregar línea a factura |
| `calcularTotales()` | Factura | agregarItem() | Calcular subtotal, IVA, total |
| `determinarTipoFactura()` | Factura | FacturaService | Determinar tipo A/B/C |
| `contratarServicio()` | CuentaCliente | CuentaClienteService | Asignar servicio a cliente |
| `cambiarEstado()` | CuentaCliente | CuentaClienteService | Cambiar estado de cuenta |
| `aplicarSaldoAFavor()` | CuentaCliente | PagoService | Usar saldo a favor |
| `calcularIva()` | Servicio | ItemFactura, Servicio | Calcular IVA del servicio |
| `agregarFactura()` | LoteFacturacion | FacturaService | Agregar factura a lote |
| `ejecutarFacturacionMasiva()` | FacturaService | Controller | Generar facturas masivas |

---

## 🔍 CÓMO BUSCAR LLAMADAS EN EL CÓDIGO

### En Visual Studio Code:

1. **Buscar definición:**
   - Click derecho → "Go to Definition" (F12)

2. **Buscar todas las referencias:**
   - Click derecho → "Find All References" (Shift+F12)

3. **Buscar uso de un método:**
   - Ctrl+Shift+F → Buscar `nombreMetodo(`

### Convención de nombres:

- `Clase.metodo()` → Método de instancia
- `Clase.metodoStatic()` → Método estático (se llama con el nombre de la clase)
- `objeto.metodo()` → Llamada a método de instancia

---

📌 **Este documento mapea todas las llamadas importantes entre métodos del proyecto**
📅 **Última actualización:** Diciembre 2025
