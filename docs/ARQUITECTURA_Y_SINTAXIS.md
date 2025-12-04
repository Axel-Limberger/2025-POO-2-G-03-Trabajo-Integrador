# 📚 GUÍA DE ARQUITECTURA Y SINTAXIS DEL PROYECTO

## 🏗️ ARQUITECTURA GENERAL DEL SISTEMA

Este proyecto implementa un **Sistema ERP de Facturación** usando el patrón arquitectónico **Modelo-Vista-Controlador (MVC)** con Spring Boot.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        ARQUITECTURA EN CAPAS                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  CAPA DE PRESENTACIÓN (Vista)                                 │  │
│  │  • Templates Thymeleaf (HTML)                                 │  │
│  │  • CSS (styles.css)                                           │  │
│  │  • Formularios web                                            │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↕                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  CAPA DE CONTROLADORES (Controllers)                          │  │
│  │  • CuentaClienteController                                    │  │
│  │  • FacturaViewController                                      │  │
│  │  • PagoController                                             │  │
│  │  • ServicioController                                         │  │
│  │  • FacturacionMasivaController                                │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↕                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  CAPA DE SERVICIOS (Services)                                 │  │
│  │  • CuentaClienteService                                       │  │
│  │  • FacturaService                                             │  │
│  │  • PagoService                                                │  │
│  │  • ServicioService                                            │  │
│  │  • ReciboService                                              │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↕                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  CAPA DE DOMINIO (Model - MODELO RICO)                        │  │
│  │  • CuentaCliente                                              │  │
│  │  • Factura                                                    │  │
│  │  • Pago                                                       │  │
│  │  • DetallePago                                                │  │
│  │  • Servicio                                                   │  │
│  │  • ServicioContratado                                         │  │
│  │  • ItemFactura                                                │  │
│  │  • LoteFacturacion                                            │  │
│  │  • NotaCredito                                                │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↕                                        │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  CAPA DE REPOSITORIOS (Repositories - Spring Data JPA)       │  │
│  │  • CuentaClienteRepositorie                                   │  │
│  │  • FacturaRepository                                          │  │
│  │  • PagoRepository                                             │  │
│  │  • DetallePagoRepository                                      │  │
│  │  • ServicioRepository                                         │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↕                                        │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  BASE DE DATOS (H2 / MySQL / PostgreSQL)                     │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 FLUJO DE DATOS EN EL SISTEMA

### Ejemplo: Registrar un Pago Combinado

```
1. Usuario completa formulario web
   ↓
2. PagoController.registrarPagoCombinado()
   ↓
3. PagoService.registrarPagoCombinado()
   ├─ Obtiene facturas del FacturaRepository
   ├─ Valida con métodos de CuentaCliente
   ├─ Crea Pago usando Pago.crearPago() [Factory Method]
   ├─ Crea DetallePago usando DetallePago.crear()
   ├─ Llama a Factura.registrarPago()
   └─ Persiste con PagoRepository
   ↓
4. Retorna número de recibo
   ↓
5. Controller redirige a vista de confirmación
```

---

## 📦 PRINCIPALES PAQUETES Y SU FUNCIÓN

### `com.unam.integrador.model` - Entidades de Dominio

**Patrón: Modelo Rico (Rich Domain Model)**

Las entidades contienen tanto **datos** como **comportamiento** (lógica de negocio).

| Clase | Responsabilidad | Métodos Clave |
|-------|----------------|---------------|
| `Pago` | Representa un pago | `crearPago()`, `agregarDetallePago()` |
| `Factura` | Representa una factura | `agregarItem()`, `calcularTotales()`, `registrarPago()` |
| `DetallePago` | Relaciona Pago con Factura | `crear()` (Factory Method) |
| `CuentaCliente` | Cuenta de cliente | `contratarServicio()`, `cambiarEstado()` |
| `Servicio` | Servicio facturable | `calcularIva()`, `puedeFacturarse()` |
| `ItemFactura` | Línea de factura | `calcular()`, `crearProporcional()` |
| `LoteFacturacion` | Lote de facturación masiva | `agregarFactura()`, `anular()` |

### `com.unam.integrador.services` - Servicios de Aplicación

**Patrón: Orquestador Delgado (Thin Orchestrator)**

Los servicios coordinan las operaciones pero **NO** contienen lógica de negocio compleja.

| Servicio | Responsabilidad |
|----------|----------------|
| `PagoService` | Coordina registro de pagos |
| `FacturaService` | Coordina emisión de facturas |
| `CuentaClienteService` | Coordina gestión de clientes |
| `ServicioService` | Coordina gestión de servicios |
| `ReciboService` | Genera DTOs de recibos |

### `com.unam.integrador.repositories` - Repositorios

**Patrón: Repository (Spring Data JPA)**

Interfaces que extienden `JpaRepository` para acceso a datos.

```java
// Ejemplo: Spring genera automáticamente la implementación
public interface PagoRepository extends JpaRepository<Pago, Long> {
    // Métodos automáticos: save(), findById(), findAll(), etc.
    
    // Métodos personalizados por convención de nombres
    List<Pago> findByFechaPagoBetween(LocalDate inicio, LocalDate fin);
}
```

### `com.unam.integrador.controllers` - Controladores Web

**Patrón: MVC Controller**

Manejan las peticiones HTTP y retornan vistas.

```java
@Controller
public class PagoController {
    
    @GetMapping("/pagos/lista")  // GET http://localhost:8080/pagos/lista
    public String listarPagos(Model model) {
        // Obtener datos del servicio
        List<Pago> pagos = pagoService.listarTodos();
        
        // Agregar al modelo (para la vista)
        model.addAttribute("pagos", pagos);
        
        // Retornar nombre de la vista (templates/pagos/lista.html)
        return "pagos/lista";
    }
}
```

---

## 🔗 DIAGRAMA DE LLAMADAS: CASO DE USO "REGISTRAR PAGO COMBINADO"

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        FLUJO COMPLETO DE LLAMADAS                        │
└──────────────────────────────────────────────────────────────────────────┘

[Usuario en navegador]
    ↓ HTTP POST
┌─────────────────────────────────────────────────────────────────────┐
│ PagoController.registrarPagoCombinado()                             │
│   • Recibe parámetros del formulario                                │
│   • Convierte Strings a tipos correctos (Long, BigDecimal, Enum)   │
└─────────────────────────────────────────────────────────────────────┘
    ↓ llama
┌─────────────────────────────────────────────────────────────────────┐
│ PagoService.registrarPagoCombinado()                                │
│   ├─ facturaRepository.findAllById(facturasIds)                     │
│   │     └─ SQL: SELECT * FROM factura WHERE id IN (...)            │
│   │                                                                 │
│   ├─ cliente.aplicarSaldoAFavor(monto)                              │
│   │     └─ CuentaCliente.aplicarSaldoAFavor()                       │
│   │           ├─ Valida que tenga saldo                             │
│   │           └─ Actualiza this.saldo                               │
│   │                                                                 │
│   ├─ Pago.crearPago(monto, metodoPago, referencia) [FACTORY]       │
│   │     └─ new Pago(...)  [constructor privado]                    │
│   │           ├─ validarMonto()                                     │
│   │           ├─ validarMetodoPago()                                │
│   │           └─ validarReferencia()                                │
│   │                                                                 │
│   ├─ pago.setNumeroRecibo(numeroRecibo)                             │
│   │                                                                 │
│   ├─ pagoRepository.save(pago)                                      │
│   │     └─ SQL: INSERT INTO pago VALUES (...)                      │
│   │                                                                 │
│   ├─ Para cada factura:                                             │
│   │   ├─ DetallePago.crear(pago, factura, montoAplicado) [FACTORY] │
│   │   │     └─ new DetallePago(...)  [constructor privado]         │
│   │   │           ├─ validarPago()                                  │
│   │   │           ├─ validarFactura()                               │
│   │   │           ├─ validarMontoAplicado()                         │
│   │   │           ├─ pago.agregarDetallePago(this)                  │
│   │   │           └─ factura.agregarDetallePago(this)               │
│   │   │                                                             │
│   │   ├─ factura.registrarPago(pago, montoAplicado)                 │
│   │   │     ├─ validarPuedeRecibirPago()                            │
│   │   │     ├─ this.saldoPendiente -= montoAplicado                 │
│   │   │     └─ actualizarEstadoSegunSaldo()                         │
│   │   │           └─ Cambia estado a PAGADA_TOTALMENTE si saldo=0  │
│   │   │                                                             │
│   │   ├─ detallePagoRepository.save(detalle)                        │
│   │   │     └─ SQL: INSERT INTO detalle_pago VALUES (...)          │
│   │   │                                                             │
│   │   └─ facturaRepository.save(factura)                            │
│   │         └─ SQL: UPDATE factura SET saldo_pendiente=?, estado=? │
│   │                                                                 │
│   └─ return numeroRecibo                                            │
└─────────────────────────────────────────────────────────────────────┘
    ↓ retorna String
┌─────────────────────────────────────────────────────────────────────┐
│ PagoController recibe numeroRecibo                                  │
│   └─ redirectAttributes.addFlashAttribute("mensaje", "...")         │
│         └─ Mensaje de éxito mostrado en la vista                    │
└─────────────────────────────────────────────────────────────────────┘
    ↓ redirect
[Vista de confirmación con mensaje de éxito]
```

---

## 📝 SINTAXIS JAVA EXPLICADA

### 1. **Anotaciones (Annotations)**

Las anotaciones son metadatos que modifican el comportamiento de clases, métodos o atributos.

```java
// === ANOTACIONES DE JPA ===
@Entity  // Indica que la clase se mapea a una tabla en la BD
@Id      // Marca el atributo como clave primaria
@GeneratedValue(strategy = GenerationType.IDENTITY)  // ID auto-incrementado
@Column(nullable = false, precision = 10, scale = 2)  // Configuración de columna
@OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)  // Relación 1:N
@Enumerated(EnumType.STRING)  // Guarda enum como String en BD

// === ANOTACIONES DE LOMBOK ===
@Getter  // Genera automáticamente getAtributo() para todos los atributos
@Data    // Genera getters, setters, toString, equals, hashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // Constructor sin parámetros

// === ANOTACIONES DE SPRING ===
@Service          // Marca la clase como servicio de Spring
@Transactional    // Ejecuta el método dentro de una transacción
@Autowired        // Inyección de dependencias automática
@Controller       // Controlador web MVC
@GetMapping("/path")  // Mapea petición HTTP GET
@PostMapping("/path") // Mapea petición HTTP POST
```

### 2. **BigDecimal para Dinero**

```java
// NUNCA usar double o float para dinero (pierden precisión)
// double precio = 10.10;  ❌ INCORRECTO
// float precio = 10.10f;  ❌ INCORRECTO

// ✅ CORRECTO: usar BigDecimal
BigDecimal monto = new BigDecimal("10000.50");

// Comparaciones
if (monto.compareTo(BigDecimal.ZERO) > 0) {  // monto > 0
    // monto es positivo
}

// Operaciones aritméticas (inmutables, retornan nuevo objeto)
BigDecimal suma = monto.add(new BigDecimal("500"));        // +
BigDecimal resta = monto.subtract(new BigDecimal("200"));  // -
BigDecimal multiplicacion = monto.multiply(new BigDecimal("2"));  // *
BigDecimal division = monto.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);  // /
```

### 3. **LocalDate para Fechas**

```java
// Clase moderna de Java 8+ para fechas sin hora
LocalDate hoy = LocalDate.now();  // Fecha actual: 2025-12-03
LocalDate fecha = LocalDate.of(2025, 12, 25);  // 25 de diciembre de 2025

// Comparaciones
if (fecha.isAfter(hoy)) {  // fecha > hoy
    // fecha es futura
}
if (fecha.isBefore(hoy)) {  // fecha < hoy
    // fecha es pasada
}

// Operaciones
LocalDate maniana = hoy.plusDays(1);  // Sumar 1 día
LocalDate mesProximo = hoy.plusMonths(1);  // Sumar 1 mes
int dia = fecha.getDayOfMonth();  // Obtener día (1-31)
int mes = fecha.getMonthValue();  // Obtener mes (1-12)
```

### 4. **Factory Method Pattern**

```java
// Constructor PRIVADO → no se puede hacer: new Pago(...)
private Pago(BigDecimal monto, MetodoPago metodoPago, String referencia) {
    // Validaciones y construcción
}

// Método PÚBLICO ESTÁTICO para crear instancias
public static Pago crearPago(BigDecimal monto, MetodoPago metodoPago, String referencia) {
    return new Pago(monto, metodoPago, referencia);
}

// Uso:
Pago pago = Pago.crearPago(monto, metodoPago, ref);  // ✅ CORRECTO
// Pago pago = new Pago(monto, metodoPago, ref);     // ❌ ERROR DE COMPILACIÓN
```

### 5. **Relaciones Bidireccionales**

```java
// Lado "padre" (Pago)
@OneToMany(mappedBy = "pago")  // "pago" es el atributo en DetallePago
private List<DetallePago> detallesPago = new ArrayList<>();

void agregarDetallePago(DetallePago detalle) {
    if (!this.detallesPago.contains(detalle)) {
        this.detallesPago.add(detalle);
    }
}

// Lado "hijo" (DetallePago)
@ManyToOne
@JoinColumn(name = "pago_id")
private Pago pago;

// Mantener sincronización
public static DetallePago crear(Pago pago, Factura factura, BigDecimal monto) {
    DetallePago detalle = new DetallePago(pago, factura, monto);
    pago.agregarDetallePago(detalle);  // ← Sincroniza el otro lado
    factura.agregarDetallePago(detalle);
    return detalle;
}
```

### 6. **Streams y Programación Funcional**

```java
// Filtrar y transformar colecciones
List<Factura> facturasVencidas = facturas.stream()
    .filter(f -> f.getEstado() == EstadoFactura.VENCIDA)  // Filtrar
    .filter(f -> f.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0)
    .toList();  // Convertir a lista

// Sumar con reduce
BigDecimal total = facturas.stream()
    .map(Factura::getTotal)  // Transformar: Factura → BigDecimal
    .reduce(BigDecimal.ZERO, BigDecimal::add);  // Sumar todos
```

---

## 🎯 CONVENCIONES Y BUENAS PRÁCTICAS

### Nombres de Clases

```java
// PascalCase (cada palabra empieza con mayúscula)
public class CuentaCliente { }
public class DetallePago { }
```

### Nombres de Métodos y Variables

```java
// camelCase (primera palabra minúscula, resto mayúsculas)
private BigDecimal saldoPendiente;
public void calcularTotales() { }
```

### Nombres de Constantes

```java
// UPPER_SNAKE_CASE (todo mayúsculas, palabras separadas por _)
private static final int SERIE_FACTURA_A = 1;
private static final String MENSAJE_ERROR = "Error";
```

### Validaciones Fail-Fast

```java
// Validar parámetros al principio del método
public void metodo(String param) {
    if (param == null || param.isEmpty()) {
        throw new IllegalArgumentException("El parámetro es obligatorio");
    }
    // ... resto del código
}
```

### Excepciones

```java
// IllegalArgumentException: parámetro inválido
throw new IllegalArgumentException("El monto debe ser positivo");

// IllegalStateException: operación inválida en el estado actual
throw new IllegalStateException("La factura ya está anulada");
```

---

## 🧩 PATRONES DE DISEÑO UTILIZADOS

| Patrón | Dónde | Para Qué |
|--------|-------|----------|
| **Factory Method** | `Pago.crearPago()`, `DetallePago.crear()` | Construcción controlada de objetos |
| **Repository** | `PagoRepository`, `FacturaRepository` | Abstracción del acceso a datos |
| **MVC** | Controladores, Servicios, Modelos | Separación de responsabilidades |
| **Modelo Rico** | Entidades de dominio | Lógica de negocio en el modelo |
| **Inyección de Dependencias** | `@Autowired` en servicios | Desacoplamiento de componentes |
| **DTO** | `ReciboDTO`, `FacturacionMasivaDTO` | Transferencia de datos entre capas |

---

## 📚 REFERENCIAS Y RECURSOS

- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **Lombok**: https://projectlombok.org/
- **Thymeleaf**: https://www.thymeleaf.org/
- **Java 17**: https://docs.oracle.com/en/java/javase/17/

---

## ✅ RESUMEN DE RESPONSABILIDADES POR CAPA

| Capa | Responsabilidad | NO Debe Hacer |
|------|----------------|---------------|
| **Controller** | Recibir peticiones HTTP, validar formato, llamar servicios | Lógica de negocio, acceso directo a BD |
| **Service** | Coordinar operaciones, gestionar transacciones | Lógica de negocio compleja (va en el modelo) |
| **Model** | Contener datos y lógica de negocio, validar reglas de dominio | Acceso a BD, manejo de HTTP |
| **Repository** | Acceso a base de datos, queries | Lógica de negocio, transformaciones |

---

📌 **Archivo creado por el análisis del proyecto ERP Facturación UNAM**
📅 **Fecha:** Diciembre 2025
