# 📖 GLOSARIO DE TÉRMINOS Y CONCEPTOS DEL PROYECTO

## 📚 ÍNDICE

1. [Términos de Dominio (Negocio)](#términos-de-dominio-negocio)
2. [Términos Técnicos de Java](#términos-técnicos-de-java)
3. [Términos de Spring Framework](#términos-de-spring-framework)
4. [Términos de JPA/Hibernate](#términos-de-jpahibernate)
5. [Patrones de Diseño](#patrones-de-diseño)
6. [Acrónimos](#acrónimos)

---

## 🏢 TÉRMINOS DE DOMINIO (NEGOCIO)

### Factura
**Definición:** Documento comercial que detalla los servicios prestados a un cliente, su precio, impuestos y total a pagar.

**En el sistema:** Representada por la clase `Factura`, contiene items, tiene un estado (PENDIENTE, PAGADA, etc.), y se puede pagar parcialmente.

**Tipos:**
- **Factura A:** Entre responsables inscriptos (discrimina IVA)
- **Factura B:** A consumidores finales (IVA incluido)
- **Factura C:** Monotributistas y exentos (sin IVA)

---

### Nota de Crédito
**Definición:** Documento que anula total o parcialmente una factura emitida. Se genera cuando se anula una factura.

**En el sistema:** Clase `NotaCredito`, se crea automáticamente al anular una factura.

---

### Pago
**Definición:** Dinero recibido de un cliente para cancelar una o más facturas.

**En el sistema:** Clase `Pago`, puede aplicarse a múltiples facturas mediante `DetallePago`.

---

### Detalle de Pago
**Definición:** Relación que indica cuánto de un pago se aplicó a una factura específica.

**En el sistema:** Clase `DetallePago`, conecta `Pago` con `Factura` almacenando el monto aplicado.

---

### Servicio
**Definición:** Producto o servicio que la empresa ofrece a sus clientes (ej: "Internet 100MB", "Telefonía").

**En el sistema:** Clase `Servicio`, tiene precio, alícuota de IVA, y puede estar activo o inactivo.

---

### Servicio Contratado
**Definición:** Registro de que un cliente contrató un servicio específico, con su precio y fecha de alta.

**En el sistema:** Clase `ServicioContratado`, relaciona `CuentaCliente` con `Servicio`, guarda el precio histórico.

---

### Item de Factura
**Definición:** Línea de detalle en una factura que representa un servicio facturado.

**En el sistema:** Clase `ItemFactura`, tiene descripción, cantidad, precio unitario, IVA y total.

---

### Lote de Facturación
**Definición:** Conjunto de facturas generadas en una facturación masiva para un período determinado.

**En el sistema:** Clase `LoteFacturacion`, agrupa facturas, permite anularlas todas juntas.

---

### Saldo a Favor
**Definición:** Crédito que tiene el cliente por haber pagado de más. Se representa como saldo negativo en la cuenta.

**En el sistema:** Atributo `saldo` en `CuentaCliente`, si es negativo indica saldo a favor.

---

### Facturación Proporcional
**Definición:** Facturar solo los días efectivos de uso de un servicio (ej: alta a mitad de mes).

**En el sistema:** `ItemFactura.crearProporcional()` calcula el precio según días efectivos.

---

### Período de Facturación
**Definición:** Mes y año al que corresponde una factura (ej: "Noviembre 2025").

**En el sistema:** Clase `PeriodoFacturacion`, calcula días efectivos para facturación proporcional.

---

### Estado de Cuenta
**Definición:** Situación actual de la cuenta del cliente.

**Valores:**
- **ACTIVA:** Puede operar normalmente
- **SUSPENDIDA:** Bloqueada temporalmente
- **BAJA:** Cliente dado de baja

**En el sistema:** Enum `EstadoCuenta`

---

### Estado de Factura
**Definición:** Situación de pago de una factura.

**Valores:**
- **PENDIENTE:** Sin pagos
- **PAGADA_PARCIALMENTE:** Pagada parcialmente
- **PAGADA_TOTALMENTE:** Pagada completamente
- **VENCIDA:** Pasó la fecha de vencimiento sin pagar
- **ANULADA:** Anulada con nota de crédito

**En el sistema:** Enum `EstadoFactura`

---

## ⚙️ TÉRMINOS TÉCNICOS DE JAVA

### BigDecimal
**Definición:** Clase de Java para representar números decimales con precisión exacta.

**Por qué se usa:** `double` y `float` tienen errores de redondeo. Para dinero siempre usar `BigDecimal`.

**Ejemplo:**
```java
BigDecimal precio = new BigDecimal("10.50");
BigDecimal total = precio.multiply(new BigDecimal("2")); // 21.00
```

---

### LocalDate
**Definición:** Clase de Java 8+ para representar fechas sin hora ni zona horaria.

**Formato:** ISO-8601: `yyyy-MM-dd` (ej: 2025-12-03)

**Ejemplo:**
```java
LocalDate hoy = LocalDate.now();
LocalDate fecha = LocalDate.of(2025, 12, 25);
```

---

### LocalDateTime
**Definición:** Clase para representar fecha y hora sin zona horaria.

**Formato:** ISO-8601: `yyyy-MM-ddTHH:mm:ss` (ej: 2025-12-03T14:30:00)

---

### Stream
**Definición:** API de Java 8+ para procesamiento funcional de colecciones.

**Operaciones comunes:**
- `filter()`: Filtrar elementos
- `map()`: Transformar elementos
- `reduce()`: Combinar elementos
- `toList()`: Convertir a lista

**Ejemplo:**
```java
List<Factura> vencidas = facturas.stream()
    .filter(f -> f.getEstado() == EstadoFactura.VENCIDA)
    .toList();
```

---

### Optional
**Definición:** Contenedor que puede o no tener un valor (evita `null`).

**Métodos:**
- `Optional.of(valor)`: Crea Optional con valor (falla si es null)
- `Optional.empty()`: Crea Optional vacío
- `isPresent()`: Verifica si tiene valor
- `orElse(valor)`: Retorna valor o un default
- `orElseThrow()`: Retorna valor o lanza excepción

**Ejemplo:**
```java
Optional<Cliente> cliente = clienteRepository.findById(1L);
Cliente c = cliente.orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
```

---

### Enum (Enumeración)
**Definición:** Tipo de dato que define un conjunto fijo de constantes.

**Ejemplo:**
```java
public enum MetodoPago {
    EFECTIVO,
    TRANSFERENCIA,
    TARJETA,
    SALDO_A_FAVOR
}

// Uso:
MetodoPago metodo = MetodoPago.EFECTIVO;
```

---

### Exception (Excepción)
**Definición:** Objeto que representa un error o situación anormal.

**Tipos principales:**
- `IllegalArgumentException`: Parámetro inválido
- `IllegalStateException`: Operación inválida en el estado actual
- `RuntimeException`: Excepción no chequeada (no requiere try-catch)

---

### Generic (Genérico)
**Definición:** Permite que clases y métodos operen con tipos parametrizados.

**Ejemplo:**
```java
List<Factura> facturas = new ArrayList<>();  // List de Factura
Optional<Cliente> cliente = repository.findById(1L);  // Optional de Cliente
```

---

### Lambda Expression
**Definición:** Función anónima (sin nombre) para código más conciso.

**Sintaxis:**
```java
// Lambda sin parámetros
() -> System.out.println("Hola")

// Lambda con un parámetro
x -> x * 2

// Lambda con múltiples parámetros
(a, b) -> a + b

// Lambda con bloque
(x) -> {
    int resultado = x * 2;
    return resultado;
}
```

---

### Method Reference (Referencia a Método)
**Definición:** Forma abreviada de llamar a un método en lugar de usar lambda.

**Sintaxis:**
```java
// Lambda:
facturas.stream().map(f -> f.getTotal())

// Method Reference:
facturas.stream().map(Factura::getTotal)
```

---

## 🍃 TÉRMINOS DE SPRING FRAMEWORK

### Spring Boot
**Definición:** Framework que simplifica la creación de aplicaciones Java empresariales.

**Características:**
- Auto-configuración
- Servidor embebido (Tomcat)
- Gestión de dependencias
- Facilita desarrollo rápido

---

### @Autowired
**Definición:** Anotación para inyección de dependencias automática.

**Ejemplo:**
```java
@Service
public class PagoService {
    @Autowired  // Spring inyecta automáticamente una instancia
    private PagoRepository pagoRepository;
}
```

---

### @Service
**Definición:** Marca una clase como servicio de Spring (componente de lógica de negocio).

**Por qué se usa:** Spring detecta automáticamente estas clases y las gestiona.

---

### @Controller
**Definición:** Marca una clase como controlador web MVC.

**Función:** Manejar peticiones HTTP y retornar vistas.

---

### @GetMapping / @PostMapping
**Definición:** Mapea un método a una petición HTTP GET o POST.

**Ejemplo:**
```java
@GetMapping("/clientes")  // GET http://localhost:8080/clientes
public String listarClientes(Model model) {
    // ...
}

@PostMapping("/clientes")  // POST http://localhost:8080/clientes
public String crearCliente(@ModelAttribute Cliente cliente) {
    // ...
}
```

---

### @Transactional
**Definición:** Ejecuta un método dentro de una transacción de base de datos.

**Comportamiento:**
- Si el método termina sin error → COMMIT (guardar cambios)
- Si lanza excepción → ROLLBACK (deshacer cambios)

**Ejemplo:**
```java
@Transactional
public void registrarPago(...) {
    // Si algo falla aquí, se deshacen TODOS los cambios
    pagoRepository.save(pago);
    facturaRepository.save(factura);
}
```

---

### Model (Modelo)
**Definición:** Objeto que transporta datos del controlador a la vista.

**Ejemplo:**
```java
@GetMapping("/facturas")
public String listar(Model model) {
    List<Factura> facturas = facturaService.listarTodas();
    model.addAttribute("facturas", facturas);  // Pasa datos a la vista
    return "facturas/lista";  // Retorna nombre de la vista
}
```

---

### RedirectAttributes
**Definición:** Permite pasar mensajes entre redirecciones.

**Ejemplo:**
```java
@PostMapping("/facturas")
public String crear(..., RedirectAttributes redirect) {
    // ... guardar factura ...
    redirect.addFlashAttribute("mensaje", "Factura creada con éxito");
    return "redirect:/facturas";  // Redirige con mensaje
}
```

---

## 🗄️ TÉRMINOS DE JPA/HIBERNATE

### JPA (Java Persistence API)
**Definición:** Especificación estándar de Java para mapeo objeto-relacional (ORM).

**Función:** Convertir objetos Java en registros de BD y viceversa.

---

### Hibernate
**Definición:** Implementación de JPA más popular.

**Función:** Motor que ejecuta las operaciones JPA (genera SQL, gestiona conexiones, etc.).

---

### @Entity
**Definición:** Marca una clase como entidad JPA (tabla en BD).

**Ejemplo:**
```java
@Entity
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ...
}
```

---

### @Id
**Definición:** Marca un atributo como clave primaria (Primary Key).

---

### @GeneratedValue
**Definición:** Indica cómo se genera el valor del ID.

**Estrategias:**
- `IDENTITY`: BD genera el ID (auto_increment en MySQL)
- `SEQUENCE`: Usa secuencia de BD
- `AUTO`: JPA decide automáticamente

---

### @Column
**Definición:** Configura cómo se mapea un atributo a una columna de BD.

**Atributos:**
- `nullable = false`: Columna NOT NULL
- `length = 100`: Tamaño máximo (VARCHAR(100))
- `precision = 10, scale = 2`: Para DECIMAL(10,2)
- `unique = true`: Columna UNIQUE

---

### @OneToMany
**Definición:** Define relación uno-a-muchos.

**Ejemplo:**
```java
@Entity
public class Pago {
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)
    private List<DetallePago> detallesPago;
}
```

**Atributos:**
- `mappedBy`: Indica el atributo en el otro lado
- `cascade`: Propaga operaciones
- `orphanRemoval`: Elimina registros huérfanos

---

### @ManyToOne
**Definición:** Define relación muchos-a-uno.

**Ejemplo:**
```java
@Entity
public class DetallePago {
    @ManyToOne
    @JoinColumn(name = "pago_id")
    private Pago pago;
}
```

---

### @JoinColumn
**Definición:** Especifica la columna de foreign key.

**Atributos:**
- `name`: Nombre de la columna en BD
- `nullable`: Si puede ser NULL

---

### CascadeType
**Definición:** Define qué operaciones se propagan a entidades relacionadas.

**Valores:**
- `ALL`: Todas las operaciones
- `PERSIST`: Solo al guardar
- `REMOVE`: Solo al eliminar
- `MERGE`: Solo al actualizar

---

### FetchType
**Definición:** Define cuándo se cargan las entidades relacionadas.

**Valores:**
- `LAZY`: Carga bajo demanda (al acceder)
- `EAGER`: Carga inmediata (en el SELECT inicial)

---

### Repository
**Definición:** Interfaz de Spring Data JPA para acceso a datos.

**Métodos automáticos:**
- `save(entity)`: Guardar/actualizar
- `findById(id)`: Buscar por ID
- `findAll()`: Listar todos
- `deleteById(id)`: Eliminar por ID
- `existsById(id)`: Verificar si existe

**Ejemplo:**
```java
public interface PagoRepository extends JpaRepository<Pago, Long> {
    // Spring genera implementación automáticamente
}
```

---

## 🎨 PATRONES DE DISEÑO

### Factory Method
**Definición:** Patrón que encapsula la creación de objetos.

**Ventajas:**
- Controla cómo se crean los objetos
- Permite validaciones antes de construir
- Facilita cambios en la lógica de creación

**En el proyecto:**
- `Pago.crearPago()`
- `DetallePago.crear()`

---

### Repository Pattern
**Definición:** Abstrae el acceso a datos, ocultando detalles de persistencia.

**Ventajas:**
- Separa lógica de negocio de acceso a datos
- Facilita cambiar la BD
- Permite testing con mocks

**En el proyecto:**
- `PagoRepository`
- `FacturaRepository`
- `CuentaClienteRepository`

---

### MVC (Model-View-Controller)
**Definición:** Patrón arquitectónico que separa datos, lógica y presentación.

**Componentes:**
- **Model (Modelo):** Datos y lógica de negocio
- **View (Vista):** Interfaz de usuario (HTML)
- **Controller (Controlador):** Maneja peticiones HTTP

---

### Rich Domain Model (Modelo Rico)
**Definición:** El modelo contiene tanto datos como comportamiento (lógica de negocio).

**Ventajas:**
- Lógica centralizada en las entidades
- Facilita mantenimiento
- Más expresivo

**Contrario:**
- **Anemic Model (Modelo Anémico):** Solo datos, lógica en servicios

**En el proyecto:**
- `Factura` tiene métodos como `calcularTotales()`, `registrarPago()`
- `Pago` tiene métodos como `crearPago()`, validaciones

---

### Dependency Injection (Inyección de Dependencias)
**Definición:** Patrón donde los objetos no crean sus dependencias, se las pasan desde afuera.

**Ventajas:**
- Desacoplamiento
- Facilita testing
- Configuración centralizada

**En el proyecto:**
- Spring inyecta repositorios en servicios con `@Autowired`

---

## 🔤 ACRÓNIMOS

| Acrónimo | Significado | Descripción |
|----------|-------------|-------------|
| **API** | Application Programming Interface | Conjunto de funciones y procedimientos para interactuar con un sistema |
| **CRUD** | Create, Read, Update, Delete | Operaciones básicas sobre datos |
| **DTO** | Data Transfer Object | Objeto para transferir datos entre capas |
| **ERP** | Enterprise Resource Planning | Sistema de planificación de recursos empresariales |
| **HTTP** | HyperText Transfer Protocol | Protocolo de transferencia de hipertexto |
| **IVA** | Impuesto al Valor Agregado | Impuesto sobre el consumo |
| **JDBC** | Java Database Connectivity | API de Java para conectar con bases de datos |
| **JPA** | Java Persistence API | API de Java para persistencia de objetos |
| **JSON** | JavaScript Object Notation | Formato de intercambio de datos |
| **MVC** | Model-View-Controller | Patrón arquitectónico de separación de capas |
| **ORM** | Object-Relational Mapping | Mapeo objeto-relacional |
| **POJO** | Plain Old Java Object | Objeto Java simple sin dependencias de frameworks |
| **REST** | Representational State Transfer | Estilo arquitectónico para servicios web |
| **SQL** | Structured Query Language | Lenguaje de consulta estructurado |
| **URI** | Uniform Resource Identifier | Identificador uniforme de recursos |
| **URL** | Uniform Resource Locator | Localizador uniforme de recursos |

---

## 🧪 CONCEPTOS DE TESTING

### Test Unitario
**Definición:** Prueba de un método o clase aislada.

**Herramientas:** JUnit, Mockito

---

### Test de Integración
**Definición:** Prueba de múltiples componentes trabajando juntos.

**Ejemplo:** Probar que un servicio funciona con el repositorio real.

---

### Mock
**Definición:** Objeto falso que simula el comportamiento de un objeto real.

**Uso:** Para aislar la clase bajo prueba.

---

## 🔐 CONCEPTOS DE SEGURIDAD

### Transacción
**Definición:** Conjunto de operaciones que se ejecutan como una unidad atómica.

**Propiedades ACID:**
- **Atomicity (Atomicidad):** Todo o nada
- **Consistency (Consistencia):** Mantiene integridad
- **Isolation (Aislamiento):** Transacciones no interfieren
- **Durability (Durabilidad):** Los cambios son permanentes

---

### Validación
**Definición:** Verificación de que los datos cumplan con las reglas de negocio.

**Niveles:**
- **Cliente:** En el navegador (HTML5, JavaScript)
- **Controlador:** En Spring con Bean Validation
- **Modelo:** En las entidades (métodos privados)

---

## 📊 CONCEPTOS DE BASE DE DATOS

### Primary Key (Clave Primaria)
**Definición:** Columna o conjunto de columnas que identifica únicamente cada registro.

**Características:**
- Única
- No nula
- Inmutable

---

### Foreign Key (Clave Foránea)
**Definición:** Columna que referencia la clave primaria de otra tabla.

**Función:** Mantener integridad referencial.

---

### Index (Índice)
**Definición:** Estructura que acelera búsquedas en una tabla.

**Costo:** Ocupa espacio y ralentiza INSERT/UPDATE.

---

### Join
**Definición:** Operación que combina registros de dos o más tablas.

**Tipos:**
- **INNER JOIN:** Solo registros que coinciden
- **LEFT JOIN:** Todos de la izquierda + coincidencias
- **RIGHT JOIN:** Todos de la derecha + coincidencias

---

📌 **Este glosario cubre los términos más importantes del proyecto**
📅 **Última actualización:** Diciembre 2025
