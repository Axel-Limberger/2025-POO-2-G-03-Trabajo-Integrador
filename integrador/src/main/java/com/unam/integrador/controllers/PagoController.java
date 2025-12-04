package com.unam.integrador.controllers;

// === IMPORTS DE JAVA ESTÁNDAR ===
import java.math.BigDecimal;                     // Clase para manejar números decimales con precisión (montos de dinero)
import java.time.LocalDate;                       // Clase para representar fechas sin hora (ISO-8601: yyyy-MM-dd)
import java.time.format.DateTimeParseException;   // Excepción lanzada al parsear una fecha con formato inválido
import java.util.List;                            // Interfaz que representa una colección ordenada de elementos
import java.util.Map;                             // Interfaz que representa un mapeo clave-valor

// === IMPORTS DE SPRING FRAMEWORK - INYECCIÓN DE DEPENDENCIAS ===
import org.springframework.beans.factory.annotation.Autowired;  // Anotación para inyección automática de dependencias

// === IMPORTS DE SPRING MVC - CONTROLADOR WEB ===
import org.springframework.stereotype.Controller;              // Marca la clase como controlador web MVC
import org.springframework.ui.Model;                            // Contenedor para pasar datos a las vistas Thymeleaf
import org.springframework.web.bind.annotation.GetMapping;      // Mapea peticiones HTTP GET a métodos del controlador
import org.springframework.web.bind.annotation.PathVariable;    // Extrae variables de la ruta URL (ej: /pagos/{id})
import org.springframework.web.bind.annotation.PostMapping;     // Mapea peticiones HTTP POST a métodos del controlador
import org.springframework.web.bind.annotation.RequestMapping;  // Define la ruta base para todos los endpoints del controlador
import org.springframework.web.bind.annotation.RequestParam;    // Extrae parámetros del query string (ej: ?clienteId=1)
import org.springframework.web.servlet.mvc.support.RedirectAttributes;  // Permite pasar atributos entre redirecciones

// === IMPORTS DEL PROYECTO - DTOs (Data Transfer Objects) ===
import com.unam.integrador.dto.ReciboDTO;  // DTO para transferir datos del recibo a la vista

// === IMPORTS DEL PROYECTO - ENTIDADES DE DOMINIO ===
import com.unam.integrador.model.CuentaCliente;       // Entidad que representa un cliente del sistema
import com.unam.integrador.model.Factura;             // Entidad que representa una factura
import com.unam.integrador.model.Pago;                // Entidad que representa un pago realizado
import com.unam.integrador.model.enums.MetodoPago;    // Enum con los métodos de pago disponibles

// === IMPORTS DEL PROYECTO - SERVICIOS (CAPA DE NEGOCIO) ===
import com.unam.integrador.services.CuentaClienteService;  // Servicio para operaciones con clientes
import com.unam.integrador.services.FacturaService;        // Servicio para operaciones con facturas
import com.unam.integrador.services.PagoService;           // Servicio para operaciones con pagos
import com.unam.integrador.services.ReciboService;         // Servicio para generación de recibos

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * CONTROLADOR WEB: PAGO CONTROLLER
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Controlador de Spring MVC que maneja las peticiones HTTP relacionadas con pagos.
 * Implementa el patrón CONTROLLER del arquitectura MVC (Model-View-Controller).
 * 
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │ RESPONSABILIDADES DE ESTA CLASE:                                             │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 1. Recibir peticiones HTTP (GET/POST) desde el navegador del usuario         │
 * │ 2. Validar los parámetros de entrada recibidos de los formularios            │
 * │ 3. Invocar los servicios de negocio (PagoService, ReciboService, etc.)       │
 * │ 4. Preparar los datos para las vistas (agregar atributos al Model)           │
 * │ 5. Retornar el nombre de la vista Thymeleaf a renderizar                     │
 * │ 6. Manejar redirecciones y mensajes flash entre páginas                      │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │ ENDPOINTS DISPONIBLES:                                                        │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ GET  /pagos                             → Lista todos los pagos/recibos      │
 * │ GET  /pagos/{id}                        → Detalle de un pago específico      │
 * │ GET  /pagos/recibo/{id}                 → Ver recibo por ID de pago          │
 * │ GET  /pagos/recibo/numero/{numero}      → Ver recibo consolidado por número  │
 * │ POST /pagos/buscar-cliente              → Buscar cliente por nombre          │
 * │ GET  /pagos/seleccionar-facturas/{id}   → Seleccionar facturas a pagar       │
 * │ GET  /pagos/nuevo-combinado/{clienteId} → Formulario de pago combinado       │
 * │ POST /pagos/registrar-combinado         → Procesar registro de pago          │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │ RELACIONES CON OTRAS CLASES:                                                 │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ • PagoService: Lógica de negocio para registrar y listar pagos               │
 * │ • FacturaService: Obtención de facturas para validaciones                    │
 * │ • CuentaClienteService: Búsqueda y obtención de clientes                     │
 * │ • ReciboService: Generación de recibos (DTO) para visualización              │
 * │ • Vistas Thymeleaf: templates/pagos/*.html (lista, detalle, formularios)     │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * PATRÓN MVC EN SPRING:
 * <pre>
 *   ┌─────────────┐    HTTP     ┌────────────────┐    invoca    ┌─────────────┐
 *   │  Navegador  │ ──────────► │ PagoController │ ────────────►│ PagoService │
 *   │  (Cliente)  │             │  (Controller)  │              │  (Service)  │
 *   └─────────────┘             └────────────────┘              └─────────────┘
 *          ▲                           │                               │
 *          │         HTML              │    Model                      │
 *          └───────────────────────────┤◄──────────────────────────────┘
 *                                      │
 *                               ┌──────▼──────┐
 *                               │  Thymeleaf  │
 *                               │   (View)    │
 *                               └─────────────┘
 * </pre>
 * 
 * @author Sistema ERP Facturación UNAM
 * @version 2.0
 * @since 2025-01-01
 * @see PagoService
 * @see ReciboService
 * @see Pago
 */
// === ANOTACIONES DE SPRING MVC ===
@Controller  // Indica a Spring que esta clase es un controlador web que retorna vistas
@RequestMapping("/pagos")  // Ruta base: todos los endpoints de esta clase empiezan con /pagos
public class PagoController {
    
    // ═══════════════════════════════════════════════════════════════════════════
    // INYECCIÓN DE DEPENDENCIAS (SERVICIOS)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Servicio para operaciones con pagos.
     * 
     * SINTAXIS - @Autowired:
     * - Indica a Spring que debe inyectar automáticamente una instancia
     * - Spring busca un bean de tipo PagoService en el contenedor
     * - La inyección se hace después de construir el objeto
     * 
     * SEMÁNTICA:
     * - Proporciona métodos para: listar pagos, registrar pagos, buscar por ID
     * - Contiene la lógica de negocio de pagos (no debe estar en el controlador)
     * 
     * LLAMADAS:
     * - listarFiltrados() desde listarPagos()
     * - buscarPorId() desde verDetalle()
     * - listarFacturasImpagasPorCliente() desde mostrarSeleccionFacturas()
     * - registrarPagoCombinado() desde registrarPagoCombinado()
     */
    @Autowired
    private PagoService pagoService;
    
    /**
     * Servicio para operaciones con facturas.
     * 
     * SEMÁNTICA:
     * - Permite obtener información de facturas específicas
     * - Usado para validar y obtener datos de facturas en el flujo de pagos
     * 
     * LLAMADAS:
     * - obtenerFacturaPorId() desde mostrarSeleccionFacturas()
     */
    @Autowired
    private FacturaService facturaService;
    
    /**
     * Servicio para operaciones con clientes.
     * 
     * SEMÁNTICA:
     * - Permite buscar clientes por nombre y obtener por ID
     * - Usado en el flujo de búsqueda y selección de cliente
     * 
     * LLAMADAS:
     * - buscarPorNombre() desde procesarBusquedaCliente()
     * - obtenerClientePorId() desde mostrarSeleccionFacturas() y mostrarFormularioPagoCombinado()
     */
    @Autowired
    private CuentaClienteService cuentaClienteService;
    
    /**
     * Servicio para generación de recibos.
     * 
     * SEMÁNTICA:
     * - Genera objetos ReciboDTO a partir de pagos para visualización
     * - Permite consolidar múltiples pagos en un solo recibo
     * 
     * LLAMADAS:
     * - generarReciboDesdePago() desde listarPagos()
     * - generarReciboDesdeMultiplesPagos() desde listarPagos()
     * - generarReciboPorPagoId() desde verReciboDetalle()
     * - generarReciboConsolidado() desde verReciboDetalleConsolidado()
     */
    @Autowired
    private ReciboService reciboService;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE LISTADO Y VISUALIZACIÓN
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Muestra la lista de todos los pagos con filtros opcionales.
     * 
     * SINTAXIS - @GetMapping:
     * - Mapea peticiones HTTP GET a este método
     * - Sin valor: responde a la ruta base del controlador (/pagos)
     * 
     * SINTAXIS - @RequestParam:
     * - Extrae parámetros del query string de la URL
     * - required = false: El parámetro es opcional
     * - Ejemplo: /pagos?clienteNombre=Juan&desde=2025-01-01
     * 
     * SINTAXIS - Model:
     * - Contenedor para pasar datos a la vista Thymeleaf
     * - model.addAttribute("key", value) → accesible en la vista como ${key}
     * 
     * SEMÁNTICA Y FLUJO DE EJECUCIÓN:
     * 1. Recibe filtros opcionales: nombre cliente, fecha desde, fecha hasta
     * 2. Parsea las fechas de String a LocalDate (con manejo de errores)
     * 3. Consulta los pagos filtrados desde PagoService
     * 4. Agrupa los pagos por número de recibo (evita duplicados)
     * 5. Genera ReciboDTO por cada grupo de pagos
     * 6. Ordena los recibos por número descendente (más recientes primero)
     * 7. Agrega los datos al Model y retorna la vista
     * 
     * ESTRUCTURAS DE DATOS UTILIZADAS:
     * - Map<String, List<Pago>>: Agrupa pagos por número de recibo
     *   Clave: número de recibo o ID del pago
     *   Valor: lista de pagos con ese número de recibo
     * - List<ReciboDTO>: Lista de recibos para mostrar en la vista
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: Navegador al acceder a /pagos o al aplicar filtros
     * - LLAMA A:
     *   • pagoService.listarFiltrados() - obtiene pagos filtrados
     *   • reciboService.generarReciboDesdePago() - genera recibo de un pago
     *   • reciboService.generarReciboDesdeMultiplesPagos() - genera recibo consolidado
     * 
     * @param clienteNombre Filtro opcional por nombre del cliente
     * @param desdeStr Filtro opcional de fecha inicio (formato: yyyy-MM-dd)
     * @param hastaStr Filtro opcional de fecha fin (formato: yyyy-MM-dd)
     * @param model Contenedor para pasar datos a la vista
     * @return String con el nombre de la vista: "pagos/lista"
     */
    @GetMapping
    public String listarPagos(
            @RequestParam(value = "clienteNombre", required = false) String clienteNombre,
            @RequestParam(value = "desde", required = false) String desdeStr,
            @RequestParam(value = "hasta", required = false) String hastaStr,
            Model model) {

        // Inicializar fechas como null (sin filtro)
        LocalDate desde = null;
        LocalDate hasta = null;
        
        // Intentar parsear la fecha "desde" si fue proporcionada
        try {
            if (desdeStr != null && !desdeStr.isBlank()) {
                desde = LocalDate.parse(desdeStr);  // Parsea formato ISO: yyyy-MM-dd
            }
        } catch (DateTimeParseException e) {
            // Si el formato es inválido, agregar mensaje de error al modelo
            model.addAttribute("error", "Fecha 'desde' inválida. Use YYYY-MM-DD.");
        }
        
        // Intentar parsear la fecha "hasta" si fue proporcionada
        try {
            if (hastaStr != null && !hastaStr.isBlank()) {
                hasta = LocalDate.parse(hastaStr);
            }
        } catch (DateTimeParseException e) {
            model.addAttribute("error", "Fecha 'hasta' inválida. Use YYYY-MM-DD.");
        }

        // Pasar los filtros de vuelta a la vista para mantener los valores en el formulario
        model.addAttribute("clienteNombre", clienteNombre);
        model.addAttribute("desde", desdeStr);
        model.addAttribute("hasta", hastaStr);

        // Obtener pagos filtrados desde el servicio
        List<Pago> pagos = pagoService.listarFiltrados(clienteNombre, desde, hasta);
        
        // ═══════════════════════════════════════════════════════════════════
        // AGRUPACIÓN DE PAGOS POR NÚMERO DE RECIBO
        // ═══════════════════════════════════════════════════════════════════
        // Un pago combinado genera múltiples registros de Pago con el mismo
        // número de recibo. Aquí agrupamos para mostrar un solo recibo.
        
        Map<String, List<Pago>> pagosPorRecibo = new java.util.HashMap<>();
        for (Pago pago : pagos) {
            // Usar número de recibo como clave, o el ID del pago si no tiene recibo
            String clave = (pago.getNumeroRecibo() != null) ? pago.getNumeroRecibo() : String.valueOf(pago.getIdPago());
            
            // Crear lista si no existe la clave (computeIfAbsent simplificado)
            if (!pagosPorRecibo.containsKey(clave)) {
                pagosPorRecibo.put(clave, new java.util.ArrayList<>());
            }
            pagosPorRecibo.get(clave).add(pago);
        }
        
        // ═══════════════════════════════════════════════════════════════════
        // GENERACIÓN DE RECIBOS DTO PARA LA VISTA
        // ═══════════════════════════════════════════════════════════════════
        
        List<ReciboDTO> recibos = new java.util.ArrayList<>();
        for (List<Pago> gruposPagos : pagosPorRecibo.values()) {
            ReciboDTO recibo;
            if (gruposPagos.size() == 1) {
                // Un solo pago: generar recibo simple
                recibo = reciboService.generarReciboDesdePago(gruposPagos.get(0));
            } else {
                // Múltiples pagos con mismo recibo: generar recibo consolidado
                String numeroRecibo = gruposPagos.get(0).getNumeroRecibo();
                recibo = reciboService.generarReciboDesdeMultiplesPagos(gruposPagos, numeroRecibo);
            }
            recibos.add(recibo);
        }
        
        // Ordenar recibos por número descendente (más recientes primero)
        // Comparator: r2.compareTo(r1) para orden descendente
        recibos.sort((r1, r2) -> r2.getNumero().compareTo(r1.getNumero()));

        // Agregar lista de recibos al modelo para la vista
        model.addAttribute("recibos", recibos);
        
        // Retornar nombre de la plantilla Thymeleaf: templates/pagos/lista.html
        return "pagos/lista";
    }

    /**
     * Muestra el detalle de un recibo específico por ID de pago.
     * 
     * SINTAXIS - @GetMapping("/recibo/{id}"):
     * - Define la ruta relativa al controlador: /pagos/recibo/{id}
     * - {id} es una variable de ruta (path variable)
     * 
     * SINTAXIS - @PathVariable:
     * - Extrae el valor de la variable de ruta {id}
     * - Automáticamente convierte String a Long
     * - Ejemplo: /pagos/recibo/123 → id = 123L
     * 
     * SEMÁNTICA:
     * - Genera un ReciboDTO dinámicamente desde un pago específico
     * - Útil para ver el detalle de un pago individual
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: Vista lista.html al hacer clic en "Ver Recibo"
     * - LLAMA A: reciboService.generarReciboPorPagoId()
     * 
     * @param id ID del pago del cual generar el recibo
     * @param model Contenedor para pasar el recibo a la vista
     * @return String con el nombre de la vista: "pagos/recibo-detalle"
     */
    @GetMapping("/recibo/{id}")
    public String verReciboDetalle(@PathVariable Long id, Model model) {
        // Generar el ReciboDTO dinámicamente desde el Pago
        ReciboDTO recibo = reciboService.generarReciboPorPagoId(id);
        model.addAttribute("recibo", recibo);
        return "pagos/recibo-detalle";
    }
    
    /**
     * Muestra el detalle de un recibo consolidado por número de recibo.
     * 
     * SINTAXIS:
     * - @GetMapping("/recibo/numero/{numero}"): Ruta con variable String
     * - @PathVariable String numero: Extrae el número de recibo de la URL
     * 
     * SEMÁNTICA:
     * - Un número de recibo puede agrupar múltiples pagos (pago combinado)
     * - Este endpoint genera un recibo consolidado que suma todos los pagos
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: Vista lista.html para recibos de pagos combinados
     * - LLAMA A: reciboService.generarReciboConsolidado()
     * 
     * @param numero Número de recibo (formato: 8 dígitos secuenciales)
     * @param model Contenedor para pasar el recibo a la vista
     * @return String con el nombre de la vista: "pagos/recibo-detalle"
     */
    @GetMapping("/recibo/numero/{numero}")
    public String verReciboDetalleConsolidado(@PathVariable String numero, Model model) {
        // Generar el ReciboDTO consolidado desde múltiples pagos con el mismo número de recibo
        ReciboDTO recibo = reciboService.generarReciboConsolidado(numero);
        model.addAttribute("recibo", recibo);
        return "pagos/recibo-detalle";
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE BÚSQUEDA DE CLIENTE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Procesa la búsqueda de cliente por nombre desde el modal.
     * 
     * SINTAXIS - @PostMapping:
     * - Mapea peticiones HTTP POST (envío de formularios)
     * - Los datos vienen en el cuerpo de la petición
     * 
     * SINTAXIS - RedirectAttributes:
     * - Permite pasar datos entre redirecciones HTTP
     * - addFlashAttribute(): Datos disponibles solo en la siguiente petición
     * - Los flash attributes sobreviven a la redirección
     * 
     * SEMÁNTICA Y FLUJO DE EJECUCIÓN:
     * 1. Valida que el nombre no esté vacío
     * 2. Busca clientes que coincidan con el nombre
     * 3. Si no hay coincidencias, muestra error en el modal
     * 4. Si hay coincidencias, las muestra para selección
     * 5. Redirige de vuelta a /facturas con el modal abierto
     * 
     * NOTA: El flujo de búsqueda de cliente ocurre en un modal dentro
     * de la página de facturas, no en una página separada.
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: Modal de búsqueda en facturas/lista.html
     * - LLAMA A: cuentaClienteService.buscarPorNombre()
     * 
     * @param clienteNombre Nombre del cliente a buscar
     * @param redirectAttributes Atributos para pasar datos en la redirección
     * @return String con la URL de redirección
     */
    @PostMapping("/buscar-cliente")
    public String procesarBusquedaCliente(@RequestParam("clienteNombre") String clienteNombre,
                                          RedirectAttributes redirectAttributes) {
        // Validación: el nombre es obligatorio
        if (clienteNombre == null || clienteNombre.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("modalSearchError", "El nombre del cliente es obligatorio");
            return "redirect:/facturas#buscar-cliente-modal";
        }

        // Buscar clientes que coincidan con el nombre (búsqueda parcial)
        java.util.List<CuentaCliente> clientes = cuentaClienteService.buscarPorNombre(clienteNombre.trim());
        
        if (clientes.isEmpty()) {
            // No se encontraron clientes: mostrar error
            redirectAttributes.addFlashAttribute("modalSearchError", "No se encontraron clientes con ese nombre");
            redirectAttributes.addFlashAttribute("clienteNombre", clienteNombre.trim());
            return "redirect:/facturas#buscar-cliente-modal";
        }
        
        // Mostrar coincidencias en el modal para selección explícita
        // (incluso si solo hay una, el usuario debe confirmar)
        redirectAttributes.addFlashAttribute("modalClients", clientes);
        redirectAttributes.addFlashAttribute("clienteNombre", clienteNombre.trim());
        return "redirect:/facturas#buscar-cliente-modal";
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE SELECCIÓN Y FORMULARIOS DE PAGO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Muestra la pantalla de selección de facturas impagas para un cliente.
     * 
     * SINTAXIS - @GetMapping con @PathVariable y @RequestParam:
     * - @PathVariable Long clienteId: Variable obligatoria de la ruta
     * - @RequestParam(required = false): Parámetro opcional del query string
     * 
     * SEMÁNTICA Y FLUJO DE EJECUCIÓN:
     * 1. Obtiene el cliente por su ID
     * 2. Lista todas las facturas impagas del cliente
     * 3. Prepara los métodos de pago disponibles (excluye SALDO_A_FAVOR)
     * 4. Calcula el total adeudado sumando saldos pendientes
     * 5. Calcula el máximo saldo a favor aplicable
     * 6. Si se pasó facturaId, preselecciona esa factura y sugiere su monto
     * 7. Retorna la vista con todos los datos preparados
     * 
     * DATOS ENVIADOS A LA VISTA:
     * - cliente: Objeto CuentaCliente
     * - facturas: Lista de facturas impagas
     * - metodosPago: Array de MetodoPago disponibles
     * - totalAdeudado: Suma de todos los saldos pendientes
     * - maxSaldoAplicable: Máximo saldo a favor que puede usarse
     * - preselectedFacturaId: ID de factura preseleccionada (opcional)
     * - suggestedMonto: Monto sugerido basado en factura preseleccionada
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: 
     *   • Botón "Registrar Pago" desde detalle de factura
     *   • Selección de cliente en el flujo de pagos
     * - LLAMA A:
     *   • cuentaClienteService.obtenerClientePorId()
     *   • pagoService.listarFacturasImpagasPorCliente()
     *   • facturaService.obtenerFacturaPorId() (si hay facturaId)
     * 
     * @param clienteId ID del cliente (obligatorio en la ruta)
     * @param facturaId ID de factura preseleccionada (opcional)
     * @param model Contenedor para pasar datos a la vista
     * @return String con el nombre de la vista: "pagos/seleccionar-facturas"
     */
    @GetMapping("/seleccionar-facturas/{clienteId}")
    public String mostrarSeleccionFacturas(@PathVariable Long clienteId,
                                           @RequestParam(value = "facturaId", required = false) Long facturaId,
                                           Model model) {
        // Obtener el cliente
        CuentaCliente cliente = cuentaClienteService.obtenerClientePorId(clienteId);
        
        // Obtener facturas impagas del cliente
        java.util.List<Factura> facturasImpagas = pagoService.listarFacturasImpagasPorCliente(clienteId);
        
        model.addAttribute("cliente", cliente);
        model.addAttribute("facturas", facturasImpagas);
        
        // ═══════════════════════════════════════════════════════════════════
        // PREPARAR MÉTODOS DE PAGO DISPONIBLES
        // ═══════════════════════════════════════════════════════════════════
        // Se excluye SALDO_A_FAVOR porque se maneja por separado en el formulario
        
        List<MetodoPago> metodosPagoDisponibles = new java.util.ArrayList<>();
        for (MetodoPago m : MetodoPago.values()) {
            if (m != MetodoPago.SALDO_A_FAVOR) {
                metodosPagoDisponibles.add(m);
            }
        }
        model.addAttribute("metodosPago", metodosPagoDisponibles.toArray(new MetodoPago[0]));
        
        // ═══════════════════════════════════════════════════════════════════
        // CALCULAR TOTALES
        // ═══════════════════════════════════════════════════════════════════
        
        // Calcular total adeudado (suma de saldos pendientes de todas las facturas)
        java.math.BigDecimal totalAdeudado = java.math.BigDecimal.ZERO;
        for (Factura factura : facturasImpagas) {
            totalAdeudado = totalAdeudado.add(factura.getSaldoPendiente());
        }
        model.addAttribute("totalAdeudado", totalAdeudado);
        
        // Calcular máximo saldo a favor aplicable:
        // No debe exceder ni el saldo disponible del cliente ni el total adeudado
        java.math.BigDecimal maxSaldoAplicable = java.math.BigDecimal.ZERO;
        if (cliente != null && cliente.tieneSaldoAFavor()) {
            java.math.BigDecimal saldoAFavor = cliente.getSaldoAFavor();
            // Usar el menor entre saldo disponible y total adeudado
            maxSaldoAplicable = saldoAFavor.min(totalAdeudado);
        }
        model.addAttribute("maxSaldoAplicable", maxSaldoAplicable);
        
        // ═══════════════════════════════════════════════════════════════════
        // PRESELECCIÓN DE FACTURA (OPCIONAL)
        // ═══════════════════════════════════════════════════════════════════
        
        // Si se pasó facturaId, marcarla como preseleccionada en la vista
        model.addAttribute("preselectedFacturaId", facturaId);
        
        // Si se pasó facturaId, usar su saldoPendiente para prellenar el monto sugerido
        if (facturaId != null) {
            try {
                Factura facturaSeleccionada = facturaService.obtenerFacturaPorId(facturaId);
                if (facturaSeleccionada != null && facturaSeleccionada.getSaldoPendiente() != null) {
                    model.addAttribute("suggestedMonto", facturaSeleccionada.getSaldoPendiente());
                }
            } catch (Exception e) {
                // No interrumpir el flujo si no se encuentra la factura
                // El usuario puede seleccionar manualmente
            }
        }
        
        return "pagos/seleccionar-facturas";
    }
    
    /**
     * Muestra el detalle de un pago específico.
     * 
     * SINTAXIS:
     * - @GetMapping("/{id}"): Ruta relativa /pagos/{id}
     * - @PathVariable Long id: Extrae el ID de la ruta
     * 
     * SEMÁNTICA:
     * - Muestra información detallada de un pago individual
     * - Incluye: fecha, monto, método de pago, referencia, facturas asociadas
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: Vista lista.html al hacer clic en un pago
     * - LLAMA A: pagoService.buscarPorId()
     * 
     * @param id ID del pago a visualizar
     * @param model Contenedor para pasar el pago a la vista
     * @return String con el nombre de la vista: "pagos/detalle"
     */
    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Pago pago = pagoService.buscarPorId(id);
        model.addAttribute("pago", pago);
        return "pagos/detalle";
    }
    
    /**
     * Muestra el formulario para registrar un pago combinado (múltiples facturas).
     * 
     * SINTAXIS:
     * - @GetMapping("/nuevo-combinado/{clienteId}"): Ruta con ID de cliente
     * - List<MetodoPago>: Lista de valores del enum MetodoPago
     * 
     * SEMÁNTICA Y FLUJO DE EJECUCIÓN:
     * 1. Obtiene el cliente por ID
     * 2. Lista las facturas impagas del cliente
     * 3. Si no hay facturas impagas, muestra error
     * 4. Calcula el total adeudado
     * 5. Prepara los métodos de pago (excluye SALDO_A_FAVOR)
     * 6. Retorna el formulario con todos los datos
     * 
     * DIFERENCIA CON mostrarSeleccionFacturas():
     * - Este endpoint usa una vista diferente (formulario-combinado.html)
     * - La vista de selección de facturas es más interactiva
     * - Ambos preparan datos similares pero para flujos diferentes
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: Flujo alternativo de pago combinado
     * - LLAMA A:
     *   • cuentaClienteService.obtenerClientePorId()
     *   • pagoService.listarFacturasImpagasPorCliente()
     * 
     * @param clienteId ID del cliente
     * @param model Contenedor para pasar datos a la vista
     * @return String con el nombre de la vista: "pagos/formulario-combinado"
     */
    @GetMapping("/nuevo-combinado/{clienteId}")
    public String mostrarFormularioPagoCombinado(@PathVariable Long clienteId, Model model) {
        // Obtener el cliente
        CuentaCliente cliente = cuentaClienteService.obtenerClientePorId(clienteId);
        
        // Obtener facturas impagas del cliente
        List<Factura> facturasImpagas = pagoService.listarFacturasImpagasPorCliente(clienteId);
        
        // Validar que haya facturas para pagar
        if (facturasImpagas.isEmpty()) {
            model.addAttribute("error", "El cliente no tiene facturas impagas para pagar");
        }
        
        // Calcular el total adeudado (suma de saldos pendientes)
        BigDecimal totalAdeudado = BigDecimal.ZERO;
        for (Factura factura : facturasImpagas) {
            totalAdeudado = totalAdeudado.add(factura.getSaldoPendiente());
        }
        
        // Agregar datos al modelo
        model.addAttribute("cliente", cliente);
        model.addAttribute("facturas", facturasImpagas);
        model.addAttribute("totalAdeudado", totalAdeudado);
        
        // Preparar métodos de pago (excluir SALDO_A_FAVOR que se maneja aparte)
        List<MetodoPago> metodosPagoDisponibles = new java.util.ArrayList<>();
        for (MetodoPago m : MetodoPago.values()) {
            if (m != MetodoPago.SALDO_A_FAVOR) {
                metodosPagoDisponibles.add(m);
            }
        }
        model.addAttribute("metodosPago", metodosPagoDisponibles.toArray(new MetodoPago[0]));
        
        return "pagos/formulario-combinado";
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ENDPOINTS DE PROCESAMIENTO (POST)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Procesa el formulario de pago combinado y registra el pago.
     * 
     * SINTAXIS - @PostMapping:
     * - Mapea peticiones HTTP POST (envío de formularios)
     * - Los datos vienen como parámetros en el cuerpo de la petición
     * 
     * SINTAXIS - @RequestParam con tipos complejos:
     * - List<Long> facturasIds: Spring convierte múltiples parámetros con mismo nombre
     *   Ejemplo en HTML: <input type="checkbox" name="facturasIds" value="1">
     * - BigDecimal montoTotal: Spring convierte el String a BigDecimal automáticamente
     * - MetodoPago metodoPago: Spring convierte el String al valor del enum
     * 
     * SINTAXIS - RedirectAttributes:
     * - addFlashAttribute(): Atributos que sobreviven una redirección
     * - Útil para mostrar mensajes de éxito/error después de procesar
     * 
     * SEMÁNTICA Y FLUJO DE EJECUCIÓN:
     * 1. VALIDACIÓN: Verifica que se haya seleccionado al menos una factura
     * 2. VALORES POR DEFECTO: Si faltan parámetros, usa valores predeterminados
     *    - saldoAFavorAplicar: 0 si no se proporciona
     *    - montoTotal: 0 si no se proporciona (solo saldo a favor)
     *    - metodoPago: SALDO_A_FAVOR si no hay monto
     * 3. VALIDACIÓN DE MÉTODO: Si hay monto, debe haber método de pago
     * 4. PROCESAMIENTO: Llama al servicio para registrar el pago
     * 5. RESULTADO:
     *    - Éxito: Redirige a /pagos con mensaje de confirmación
     *    - Error: Redirige de vuelta con mensaje de error
     * 
     * EXCEPCIONES MANEJADAS:
     * - IllegalStateException: Estado inválido (ej: factura ya pagada)
     * - IllegalArgumentException: Argumentos inválidos (ej: monto negativo)
     * 
     * LLAMADAS:
     * - ES LLAMADO POR: Formulario seleccionar-facturas.html al hacer submit
     * - LLAMA A: pagoService.registrarPagoCombinado()
     * 
     * @param facturasIds Lista de IDs de facturas seleccionadas
     * @param montoTotal Monto total a pagar (puede ser 0 si solo usa saldo a favor)
     * @param saldoAFavorAplicar Monto de saldo a favor a aplicar (opcional)
     * @param metodoPago Método de pago seleccionado (requerido si montoTotal > 0)
     * @param referencia Referencia o comprobante del pago (opcional)
     * @param clienteId ID del cliente (para redirección en caso de error)
     * @param redirectAttributes Atributos para mensajes en la redirección
     * @return String con la URL de redirección
     */
    @PostMapping("/registrar-combinado")
    public String registrarPagoCombinado(
            @RequestParam(value = "facturasIds", required = false) List<Long> facturasIds,
            @RequestParam(value = "montoTotal", required = false) BigDecimal montoTotal,
            @RequestParam(value = "saldoAFavorAplicar", required = false) BigDecimal saldoAFavorAplicar,
            @RequestParam(value = "metodoPago", required = false) MetodoPago metodoPago,
            @RequestParam(value = "referencia", required = false) String referencia,
            @RequestParam(value = "clienteId", required = false) Long clienteId,
            RedirectAttributes redirectAttributes) {
        
        // ═══════════════════════════════════════════════════════════════════
        // VALIDACIÓN: Al menos una factura debe estar seleccionada
        // ═══════════════════════════════════════════════════════════════════
        if (facturasIds == null || facturasIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Seleccione al menos una factura para registrar el pago");
            // Preservar el monto sugerido para que no se pierda en la redirección
            if (montoTotal != null) {
                redirectAttributes.addFlashAttribute("suggestedMonto", montoTotal);
            }
            // Redirigir de vuelta al formulario de selección
            if (clienteId != null) {
                return "redirect:/pagos/seleccionar-facturas/" + clienteId;
            }
            // Si no tenemos cliente, volver al listado de facturas
            return "redirect:/facturas";
        }

        try {
            // ═══════════════════════════════════════════════════════════════
            // VALORES POR DEFECTO
            // ═══════════════════════════════════════════════════════════════
            
            // Si no se proporcionó saldoAFavorAplicar, usar 0
            if (saldoAFavorAplicar == null) {
                saldoAFavorAplicar = BigDecimal.ZERO;
            }
            
            // Si no se proporcionó montoTotal, usar 0 (solo se aplicará saldo a favor)
            if (montoTotal == null) {
                montoTotal = BigDecimal.ZERO;
            }
            
            // ═══════════════════════════════════════════════════════════════
            // VALIDACIÓN DE MÉTODO DE PAGO
            // ═══════════════════════════════════════════════════════════════
            
            // Si hay monto pero no hay método de pago, es un error
            if (metodoPago == null && montoTotal.compareTo(BigDecimal.ZERO) > 0) {
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar un método de pago cuando ingresa un monto");
                if (clienteId != null) {
                    return "redirect:/pagos/seleccionar-facturas/" + clienteId;
                }
                return "redirect:/pagos";
            }
            
            // Si no hay metodoPago (y montoTotal es 0), usar SALDO_A_FAVOR por defecto
            if (metodoPago == null) {
                metodoPago = MetodoPago.SALDO_A_FAVOR;
            }

            // ═══════════════════════════════════════════════════════════════
            // LLAMADA AL SERVICIO DE NEGOCIO
            // ═══════════════════════════════════════════════════════════════
            
            // El servicio orquesta las entidades de dominio y retorna el número de recibo
            String numeroRecibo = pagoService.registrarPagoCombinado(
                facturasIds,
                montoTotal,
                saldoAFavorAplicar,
                metodoPago,
                referencia
            );

            // Éxito: mostrar mensaje de confirmación
            redirectAttributes.addFlashAttribute("mensaje",
                "Pago combinado registrado exitosamente. Recibo N° " + numeroRecibo + " generado.");
            return "redirect:/pagos";
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Error de negocio: mostrar mensaje descriptivo
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (clienteId != null) {
                return "redirect:/pagos/seleccionar-facturas/" + clienteId;
            }
            return "redirect:/pagos";
        }
    }
}