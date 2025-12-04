package com.unam.integrador.model;

// === IMPORTS DE JAVA ESTÁNDAR ===
import java.math.BigDecimal;  // Clase para manejar números decimales con precisión (ej: montos de dinero)
import java.time.LocalDate;    // Clase para representar fechas sin hora (ej: 2025-12-03)
import java.util.ArrayList;    // Implementación de lista dinámica (arreglo que crece automáticamente)
import java.util.List;         // Interfaz que representa una colección ordenada de elementos

// === IMPORTS DEL PROYECTO ===
import com.unam.integrador.model.enums.MetodoPago;  // Enum que define los métodos de pago disponibles

// === IMPORTS DE JPA (Java Persistence API) - MAPEO OBJETO-RELACIONAL ===
import jakarta.persistence.CascadeType;      // Define operaciones en cascada (ej: al eliminar un Pago, eliminar sus DetallePago)
import jakarta.persistence.Column;           // Anota un atributo como columna de la tabla en BD
import jakarta.persistence.Entity;           // Indica que esta clase es una entidad JPA (tabla en la BD)
import jakarta.persistence.EnumType;         // Define cómo se almacena un enum en la BD
import jakarta.persistence.Enumerated;       // Anota un atributo enum para persistencia
import jakarta.persistence.GeneratedValue;   // Indica que el valor del ID se genera automáticamente
import jakarta.persistence.GenerationType;   // Estrategia de generación del ID (IDENTITY, SEQUENCE, etc.)
import jakarta.persistence.Id;               // Marca el atributo como clave primaria (Primary Key)
import jakarta.persistence.OneToMany;        // Define relación uno-a-muchos (un Pago tiene muchos DetallePago)

// === IMPORTS DE LOMBOK - GENERACIÓN AUTOMÁTICA DE CÓDIGO ===
import lombok.AccessLevel;       // Define niveles de acceso (PUBLIC, PROTECTED, PRIVATE)
import lombok.Getter;            // Genera automáticamente los métodos getter (getAtributo())
import lombok.NoArgsConstructor; // Genera constructor sin parámetros
import lombok.ToString;          // Genera método toString() automáticamente

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * ENTIDAD DE DOMINIO: PAGO
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Representa un PAGO realizado por un cliente sobre una o más facturas.
 * Implementa el patrón MODELO RICO (Rich Domain Model) donde la entidad contiene
 * tanto datos como comportamiento (lógica de negocio).
 * 
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │ RESPONSABILIDADES DE ESTA CLASE:                                             │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 1. Validar que los datos del pago sean correctos (monto > 0, método válido)  │
 * │ 2. Mantener la relación con DetallePago (aplicación a facturas específicas)  │
 * │ 3. Almacenar el número de recibo para agrupar pagos combinados               │
 * │ 4. Proporcionar un Factory Method (crearPago) para construcción segura       │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │ RELACIONES CON OTRAS CLASES:                                                 │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ • DetallePago (1 Pago → N DetallePago): Un pago se aplica a varias facturas  │
 * │ • MetodoPago (enum): Define el medio usado (EFECTIVO, TRANSFERENCIA, etc.)   │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * EJEMPLO DE USO:
 * <pre>
 *   // Crear un pago de $10000 en efectivo
 *   Pago pago = Pago.crearPago(
 *       new BigDecimal("10000.00"),
 *       MetodoPago.EFECTIVO,
 *       "Recibo N° 12345"
 *   );
 * </pre>
 * 
 * @author Sistema ERP Facturación UNAM
 * @version 2.0
 * @since 2025-01-01
 * @see DetallePago
 * @see MetodoPago
 */
// === ANOTACIONES DE LOMBOK ===
@Getter  // Genera getters para todos los atributos (getFechaPago(), getMonto(), etc.)
// === ANOTACIONES DE JPA ===
@Entity  // Indica que esta clase se mapeará a una tabla "pago" en la base de datos
// Genera constructor vacío con acceso PROTECTED (solo accesible desde el paquete y subclases)
// Lombok protege el constructor para forzar el uso del Factory Method crearPago()
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pago {
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ATRIBUTOS DE LA ENTIDAD (MAPEAN A COLUMNAS EN LA TABLA "pago")
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Identificador único del pago (Primary Key).
     * 
     * SINTAXIS:
     * - @Id: Marca este campo como clave primaria de la tabla
     * - @GeneratedValue: Indica que el valor se genera automáticamente
     * - GenerationType.IDENTITY: La BD genera el ID (auto-increment en MySQL/PostgreSQL)
     * - Long: Tipo de dato numérico de 64 bits (admite valores grandes)
     * 
     * SEMÁNTICA:
     * - Este ID se asigna automáticamente cuando se persiste el Pago en la BD
     * - No debe ser modificado manualmente (por eso no tiene setter público)
     * 
     * LLAMADAS:
     * - Se obtiene con getPago.getIdPago() desde PagoService, controladores y vistas
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;
    
    /**
     * Fecha en que se realizó el pago.
     * 
     * SINTAXIS:
     * - @Column(nullable = false): La columna NO puede ser NULL en la BD
     * - LocalDate: Clase de Java para fechas sin hora (ISO-8601: yyyy-MM-dd)
     * 
     * SEMÁNTICA:
     * - Se establece automáticamente a la fecha actual en el constructor
     * - Representa cuándo el cliente efectuó el pago (no cuándo se registró en el sistema)
     * 
     * LLAMADAS:
     * - Se utiliza en PagoService.listarFiltrados() para filtrar pagos por fecha
     * - Se muestra en las vistas de lista de pagos y recibos
     */
    @Column(nullable = false)
    private LocalDate fechaPago;
    
    /**
     * Monto o importe del pago en pesos.
     * 
     * SINTAXIS:
     * - @Column(precision = 10, scale = 2): Define 10 dígitos totales, 2 decimales
     *   Ejemplo válido: 99,999,999.99
     * - BigDecimal: Clase para números decimales con precisión exacta (evita errores de float)
     * 
     * SEMÁNTICA:
     * - Debe ser mayor a cero (validado en validarMonto())
     * - Representa el dinero recibido del cliente
     * - Se puede distribuir entre múltiples facturas (via DetallePago)
     * 
     * LLAMADAS:
     * - Validado en el Factory Method crearPago()
     * - Usado en PagoService.registrarPagoCombinado() para distribuir el pago
     * - Mostrado en recibos y listados de pagos
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;
    
    /**
     * Método o medio de pago utilizado.
     * 
     * SINTAXIS:
     * - @Enumerated(EnumType.STRING): Guarda el nombre del enum como String en la BD
     *   Alternativa: EnumType.ORDINAL guarda el índice numérico (no recomendado)
     * - MetodoPago: Enum definido en com.unam.integrador.model.enums.MetodoPago
     * 
     * SEMÁNTICA:
     * - Define cómo pagó el cliente: EFECTIVO, TRANSFERENCIA, TARJETA, SALDO_A_FAVOR
     * - No puede ser nulo (validado en validarMetodoPago())
     * 
     * LLAMADAS:
     * - Se pasa como parámetro en PagoService.registrarPagoCombinado()
     * - Se usa en PagoController para mostrar el método en la vista
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodoPago;
    
    /**
     * Referencia o comprobante del pago (opcional).
     * 
     * SINTAXIS:
     * - @Column(length = 500): Máximo 500 caracteres en la BD
     * - String: Cadena de texto (puede ser null)
     * 
     * SEMÁNTICA:
     * - Almacena información adicional: número de cheque, número de transferencia, etc.
     * - Es opcional (puede ser null o vacío)
     * - Tiene longitud máxima de 500 caracteres (validado en validarReferencia())
     * 
     * LLAMADAS:
     * - Se pasa como parámetro en PagoService.registrarPagoCombinado()
     * - Se muestra en la vista de detalle del pago
     */
    @Column(length = 500)
    private String referencia;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // RELACIONES JPA CON OTRAS ENTIDADES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Lista de detalles de pago que relacionan este Pago con las Facturas pagadas.
     * 
     * SINTAXIS Y SEMÁNTICA DE LA RELACIÓN:
     * - @OneToMany: Un Pago tiene muchos DetallePago (relación 1:N)
     * - mappedBy = "pago": Indica que DetallePago tiene un atributo "pago" que mapea esta relación
     *   (DetallePago es el lado "owner" de la relación bidireccional)
     * - cascade = CascadeType.ALL: Operaciones en Pago se propagan a DetallePago
     *   (al guardar un Pago, se guardan sus DetallePago automáticamente)
     * - orphanRemoval = true: Si se elimina un DetallePago de la lista, se borra de la BD
     * - List<DetallePago>: Colección ordenada de DetallePago
     * - final: La lista no puede ser reasignada (pero sí modificada con add/remove)
     * - new ArrayList<>(): Se inicializa vacía al crear el objeto
     * 
     * EJEMPLO DE USO:
     * <pre>
     *   Pago pago = Pago.crearPago(...);
     *   DetallePago detalle = DetallePago.crear(pago, factura, monto);
     *   // detalle se agrega automáticamente a pago.detallesPago
     * </pre>
     * 
     * LLAMADAS:
     * - agregadoDetallePago() desde DetallePago.crear() para mantener bidireccionalidad
     * - Accedido en PagoService para validar y mostrar facturas pagadas
     * - Usado en vistas para mostrar el detalle del recibo
     */
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude  // Excluye este campo del método toString() para evitar ciclos infinitos
    private final List<DetallePago> detallesPago = new ArrayList<>();

    /**
     * Número de recibo asociado a este pago.
     * 
     * SINTAXIS:
     * - @Column(length = 50): Máximo 50 caracteres
     * - String: Puede ser null si aún no se asignó recibo
     * 
     * SEMÁNTICA:
     * - Permite agrupar múltiples pagos bajo un mismo recibo
     * - Útil para pagos combinados: un cliente paga varias facturas con un solo pago
     * - El recibo se genera en PagoService.generarNumeroReciboSecuencial()
     * 
     * EJEMPLO:
     * - Cliente paga 3 facturas con un solo pago → todos tienen el mismo numeroRecibo
     * - El recibo agrupa todos los pagos con el mismo número
     * 
     * LLAMADAS:
     * - Asignado en PagoService.registrarPagoCombinado() via setNumeroRecibo()
     * - Usado en ReciboService para generar el recibo imprimible
     */
    @Column(length = 50)
    private String numeroRecibo;
    
    
    // ═══════════════════════════════════════════════════════════════════════════
    // CONSTRUCTORES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Constructor privado que inicializa un Pago con sus atributos básicos.
     * 
     * SINTAXIS:
     * - private: Solo accesible desde dentro de esta clase
     *   Esto fuerza el uso del Factory Method crearPago() para crear instancias
     * - this.atributo = parámetro: Asigna el parámetro al atributo de la instancia
     * 
     * SEMÁNTICA Y FLUJO DE EJECUCIÓN:
     * 1. Valida que el monto sea mayor a cero (validarMonto)
     * 2. Valida que el método de pago no sea nulo (validarMetodoPago)
     * 3. Valida que la referencia no exceda 500 caracteres (validarReferencia)
     * 4. Si todas las validaciones pasan, inicializa los atributos
     * 5. Establece fechaPago a la fecha actual del sistema
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: crearPago() (Factory Method público)
     * - LLAMA A: validarMonto(), validarMetodoPago(), validarReferencia()
     * 
     * @param monto Monto del pago (debe ser > 0)
     * @param metodoPago Método de pago utilizado (no puede ser null)
     * @param referencia Referencia o comprobante (puede ser null)
     * @throws IllegalArgumentException si alguna validación falla
     */
    private Pago(BigDecimal monto, MetodoPago metodoPago, String referencia) {
        // Validar parámetros antes de asignarlos
        validarMonto(monto);
        validarMetodoPago(metodoPago);
        validarReferencia(referencia);
        
        // Asignar atributos
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.referencia = referencia;
        this.fechaPago = LocalDate.now();  // Obtiene la fecha actual del sistema
    }
    
    
    // ═══════════════════════════════════════════════════════════════════════════
    // FACTORY METHOD (PATRÓN DE DISEÑO)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * FACTORY METHOD: Crea un nuevo Pago validando todas las reglas de negocio.
     * 
     * PATRÓN DE DISEÑO - FACTORY METHOD:
     * - Encapsula la lógica de creación de objetos complejos
     * - Permite agregar validaciones antes de construir el objeto
     * - Proporciona un punto único de creación (más fácil de mantener)
     * - El constructor privado evita creación directa con 'new Pago(...)'
     * 
     * SINTAXIS:
     * - public static: Método de clase (se llama con Pago.crearPago(), no necesita instancia)
     * - Pago: Retorna una nueva instancia de Pago
     * - return new Pago(...): Llama al constructor privado
     * 
     * SEMÁNTICA:
     * - Este es el ÚNICO método público para crear instancias de Pago
     * - Garantiza que todos los Pagos creados estén validados
     * - Las validaciones fallan rápido (fail-fast) lanzando excepciones
     * 
     * FLUJO DE EJECUCIÓN:
     * 1. Se llama Pago.crearPago(monto, método, referencia)
     * 2. Se invoca el constructor privado Pago(...)
     * 3. El constructor valida los parámetros
     * 4. Si las validaciones pasan, se crea y retorna la instancia
     * 5. Si alguna validación falla, se lanza IllegalArgumentException
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: 
     *   • PagoService.registrarPagoCombinado() - líneas que crean pagos
     *   • PagoService.aplicarSaldoAFavor() - línea que crea pago con saldo
     * - LLAMA A: Constructor privado Pago(monto, metodoPago, referencia)
     * 
     * EJEMPLO DE USO:
     * <pre>
     *   // Correcto: usando Factory Method
     *   Pago pago = Pago.crearPago(
     *       new BigDecimal("5000.00"),
     *       MetodoPago.EFECTIVO,
     *       "Recibo 001"
     *   );
     *   
     *   // Incorrecto: no compila porque el constructor es privado
     *   // Pago pago = new Pago(...);  ← ERROR DE COMPILACIÓN
     * </pre>
     * 
     * @param monto Monto del pago (debe ser mayor a cero)
     * @param metodoPago Método de pago utilizado (no puede ser nulo)
     * @param referencia Referencia del pago, como número de comprobante (puede ser null)
     * @return Nueva instancia de Pago validada y lista para persistir
     * @throws IllegalArgumentException si alguna validación falla (monto <= 0, método null, etc.)
     */
    public static Pago crearPago(BigDecimal monto, MetodoPago metodoPago, String referencia) {
        return new Pago(monto, metodoPago, referencia);
    }
    
    
    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE LÓGICA DE NEGOCIO (MODELO RICO)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Agrega un detalle de pago manteniendo la coherencia bidireccional.
     * 
     * SINTAXIS:
     * - void: No retorna ningún valor
     * - package-private (sin modificador): Solo accesible desde clases del mismo paquete
     * - !this.detallesPago.contains(detalle): Operador lógico NOT
     *   Retorna true si el detalle NO está en la lista
     * 
     * SEMÁNTICA - RELACIÓN BIDIRECCIONAL:
     * - Una relación bidireccional tiene dos referencias:
     *   1. Pago → List<DetallePago> (este lado)
     *   2. DetallePago → Pago (el otro lado)
     * - Ambos lados deben estar sincronizados para mantener consistencia
     * 
     * PATRÓN DE DISEÑO - GESTIÓN DE RELACIONES BIDIRECCIONALES:
     * - Este método es package-private (no público) para controlarel acceso
     * - Solo DetallePago.crear() debe llamar a este método
     * - Evita duplicados verificando con contains()
     * 
     * FLUJO DE EJECUCIÓN:
     * 1. DetallePago.crear() crea un nuevo detalle
     * 2. DetallePago.crear() llama a pago.agregarDetallePago(detalle)
     * 3. Este método verifica si el detalle ya está en la lista
     * 4. Si no está, lo agrega a this.detallesPago
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: DetallePago.crear() (único punto de entrada)
     * - LLAMA A: List.contains() y List.add() (métodos de ArrayList)
     * 
     * @param detalle El DetallePago a agregar a la lista
     */
    void agregarDetallePago(DetallePago detalle) {
        // Verificar si el detalle ya existe en la lista (evitar duplicados)
        if (!this.detallesPago.contains(detalle)) {
            this.detallesPago.add(detalle);  // Agregar a la lista
        }
    }
    
    
    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE VALIDACIÓN (LÓGICA DE NEGOCIO)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Valida que el monto del pago sea mayor a cero.
     * 
     * SINTAXIS:
     * - private: Solo accesible desde esta clase
     * - void: No retorna valor (solo valida y lanza excepción si falla)
     * - monto == null: Operador de igualdad para verificar si es nulo
     * - monto.compareTo(BigDecimal.ZERO): Compara dos BigDecimal
     *   Retorna: -1 si monto < 0, 0 si monto == 0, 1 si monto > 0
     * - <= 0: Operador menor o igual (verifica si es negativo o cero)
     * - throw new IllegalArgumentException: Lanza excepción (detiene el flujo)
     * 
     * SEMÁNTICA - VALIDACIÓN FAIL-FAST:
     * - Principio: "Fallar rápido y ruidosamente"
     * - Si el monto es inválido, lanza excepción inmediatamente
     * - No permite crear objetos en estado inválido
     * - El código que llama debe manejar la excepción
     * 
     * REGLAS DE NEGOCIO:
     * 1. El monto no puede ser nulo (null)
     * 2. El monto debe ser mayor a cero (positivo)
     * 
     * FLUJO DE EJECUCIÓN:
     * 1. Se verifica si monto es null
     * 2. Si es null, lanza excepción con mensaje descriptivo
     * 3. Se compara monto con BigDecimal.ZERO
     * 4. Si es <= 0, lanza excepción con mensaje descriptivo
     * 5. Si pasa ambas validaciones, el método termina sin error
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: Constructor privado Pago(...)
     * - LLAMA A: BigDecimal.compareTo() (método de la clase BigDecimal)
     * 
     * @param monto El monto a validar
     * @throws IllegalArgumentException si el monto es nulo o menor/igual a cero
     */
    private void validarMonto(BigDecimal monto) {
        // Validación 1: Verificar si el monto es nulo
        if (monto == null) {
            throw new IllegalArgumentException("El monto del pago no puede ser nulo");
        }
        // Validación 2: Verificar si el monto es positivo
        // compareTo retorna -1, 0, o 1 según si monto es menor, igual o mayor que ZERO
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
    }
    
    /**
     * Valida que el método de pago no sea nulo.
     * 
     * SINTAXIS:
     * - metodoPago == null: Verifica si la referencia es nula
     * 
     * SEMÁNTICA:
     * - El método de pago es obligatorio para registrar un pago
     * - Debe ser uno de los valores del enum MetodoPago
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: Constructor privado Pago(...)
     * 
     * @param metodoPago El método de pago a validar
     * @throws IllegalArgumentException si el método de pago es nulo
     */
    private void validarMetodoPago(MetodoPago metodoPago) {
        if (metodoPago == null) {
            throw new IllegalArgumentException("El método de pago no puede ser nulo");
        }
    }
    
    /**
     * Valida que la referencia no exceda el tamaño máximo permitido.
     * 
     * SINTAXIS:
     * - referencia != null: Verifica que no sea nulo antes de acceder a métodos
     * - &&: Operador lógico AND (ambas condiciones deben ser true)
     * - referencia.length(): Retorna el número de caracteres del String
     * - > 500: Operador mayor que
     * 
     * SEMÁNTICA:
     * - La referencia es OPCIONAL (puede ser null)
     * - Si se proporciona, debe tener máximo 500 caracteres
     * - Esta restricción viene de la anotación @Column(length = 500)
     * 
     * FLUJO DE EJECUCIÓN:
     * 1. Si referencia es null → no se valida (es opcional)
     * 2. Si referencia no es null → se valida su longitud
     * 3. Si tiene más de 500 caracteres → lanza excepción
     * 4. Si tiene 500 o menos → pasa la validación
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: Constructor privado Pago(...)
     * - LLAMA A: String.length() (método de la clase String)
     * 
     * @param referencia La referencia a validar (puede ser null)
     * @throws IllegalArgumentException si la referencia excede 500 caracteres
     */
    private void validarReferencia(String referencia) {
        // Solo validar si la referencia no es nula
        if (referencia != null && referencia.length() > 500) {
            throw new IllegalArgumentException("La referencia no puede exceder 500 caracteres");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SETTERS (MÉTODOS MODIFICADORES)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Establece el número de recibo para este pago.
     * 
     * SINTAXIS:
     * - public: Accesible desde cualquier clase
     * - void: No retorna valor
     * - this.numeroRecibo = numeroRecibo: Asigna el parámetro al atributo
     * 
     * SEMÁNTICA:
     * - El número de recibo se asigna DESPUÉS de crear el pago
     * - Se genera en PagoService para agrupar pagos combinados
     * - Múltiples pagos pueden tener el mismo número de recibo
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: 
     *   • PagoService.registrarPagoCombinado() - después de crear cada pago
     *   • PagoService.aplicarSaldoAFavor() - después de crear el pago
     * 
     * @param numeroRecibo El número de recibo a asignar (formato: 8 dígitos)
     */
    public void setNumeroRecibo(String numeroRecibo) {
        this.numeroRecibo = numeroRecibo;
    }
}
