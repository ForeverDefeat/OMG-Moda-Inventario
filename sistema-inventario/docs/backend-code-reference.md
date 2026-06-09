# ClothWise Backend - Registro de Codigo y Arquitectura

Fecha de registro: 2026-06-03

Este documento describe el backend de `sistema-inventario` desde la perspectiva de arquitectura hexagonal, tambien conocida como ports and adapters. Su objetivo es dejar un registro de como esta desarrollado el sistema, que funcion cumple cada pieza principal y que parametros recibe cada contrato, controlador y caso de uso.

## 1. Arquitectura General

El backend esta organizado por modulo funcional:

- `usuario`: autenticacion, JWT, usuarios y roles.
- `producto`: productos, variantes, busquedas y stock bajo.
- `inventario`: movimientos de stock, entradas y ajustes.
- `venta`: registro, consulta y anulacion de ventas.
- `shared`: excepciones, respuestas comunes, validaciones y configuracion transversal.

Cada modulo respeta esta separacion:

- `dominio`: entidades, reglas de negocio, enums y puertos de salida.
- `aplicacion`: DTOs, input ports y casos de uso.
- `infraestructura`: controladores REST, adaptadores JPA, entidades JPA, configuracion Spring, seguridad y transacciones.

Flujo tipico:

```text
Cliente HTTP
  -> Controller REST (adaptador de entrada)
  -> Input Port (contrato de aplicacion)
  -> Use Case (orquestacion)
  -> Output Port (contrato de dominio)
  -> Adapter JPA (adaptador de salida)
  -> Spring Data Repository
  -> Base de datos MySQL
```

Regla de dependencia:

- Dominio no conoce Spring, JPA ni HTTP.
- Aplicacion no conoce controladores ni entidades JPA.
- Infraestructura conoce Spring/JPA y ensambla las implementaciones concretas.

## 2. Arranque y Configuracion Transversal

### `SistemaInventarioApplication`

Funcion: punto de entrada de Spring Boot.

Metodo:

- `main(String[] args)`
  - `args`: argumentos de linea de comandos.
  - Inicia el contexto de Spring Boot.

### `DataInitializer`

Ubicacion: `shared.infraestructura.config`

Funcion: crea usuarios iniciales de desarrollo si no existen.

Metodo:

- `run(ApplicationArguments args)`
  - `args`: argumentos recibidos por Spring al arrancar.
  - Inserta usuarios demo mediante `IUsuarioRepository`.

Dependencias:

- `IUsuarioRepository`: puerto de persistencia de usuarios.
- `PasswordEncoder`: BCrypt de Spring Security.

Nota: esta clase es infraestructura porque depende de Spring Boot y de configuracion de arranque.

### `GlobalExceptionHandler`

Ubicacion: `shared.infraestructura.exception`

Funcion: adaptador transversal que traduce excepciones Java a respuestas HTTP estandarizadas.

Metodos:

- `handleDomainException(DomainException ex, HttpServletRequest request)`
  - `ex`: error de regla de negocio.
  - `request`: solicitud HTTP actual.
  - Responde `400 Bad Request`.

- `handleNotFoundException(NotFoundException ex, HttpServletRequest request)`
  - `ex`: recurso no encontrado.
  - `request`: solicitud HTTP actual.
  - Responde `404 Not Found`.

- `handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request)`
  - `ex`: errores de validacion `@Valid`.
  - `request`: solicitud HTTP actual.
  - Responde `422 Unprocessable Entity`.

- `handleAuthenticationException(AuthenticationException ex, HttpServletRequest request)`
  - `ex`: autenticacion ausente o invalida.
  - `request`: solicitud HTTP actual.
  - Responde `401 Unauthorized`.

- `handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request)`
  - `ex`: usuario autenticado sin permisos.
  - `request`: solicitud HTTP actual.
  - Responde `403 Forbidden`.

- `handleGenericException(Exception ex, HttpServletRequest request)`
  - `ex`: error no contemplado.
  - `request`: solicitud HTTP actual.
  - Responde `500 Internal Server Error`.

### `ErrorResponse`

Ubicacion: `shared.aplicacion.dto`

Funcion: DTO estandar para errores HTTP.

Campos:

- `timestamp`: fecha/hora del error.
- `status`: codigo HTTP.
- `error`: nombre del error.
- `message`: mensaje para cliente.
- `path`: ruta que fallo.
- `details`: detalles opcionales, usado en validacion.

Metodos:

- `of(int status, String error, String message, String path)`
  - Construye una respuesta de error simple.

- `ofValidation(int status, String error, String message, String path, List<String> details)`
  - Construye una respuesta de error con detalles de validacion.

### `PaginationResponse<T>`

Ubicacion: `shared.aplicacion.dto`

Funcion: DTO generico para respuestas paginadas.

Campos:

- `contenido`: lista de elementos.
- `pagina`: numero de pagina.
- `tamanio`: cantidad solicitada.
- `totalElementos`: total de registros.
- `totalPaginas`: total de paginas.
- `ultima`: indica si es la ultima pagina.

Metodo:

- `of(List<T> contenido, int pagina, int tamanio, long totalElementos)`
  - `contenido`: datos de la pagina.
  - `pagina`: indice de pagina.
  - `tamanio`: tamanio de pagina.
  - `totalElementos`: total global.

### `ValidationUtils`

Ubicacion: `shared.aplicacion.validation`

Funcion: utilidades de validacion reutilizables para reglas de aplicacion/dominio.

Metodos:

- `requerirTexto(String valor, String campo)`
  - Valida texto no nulo ni vacio.

- `requerirNoNulo(Object valor, String campo)`
  - Valida objeto obligatorio.

- `requerirPositivo(BigDecimal valor, String campo)`
  - Valida decimal mayor a cero.

- `requerirNoNegativo(int valor, String campo)`
  - Valida entero mayor o igual a cero.

- `requerirMayorACero(int valor, String campo)`
  - Valida entero estrictamente mayor a cero.

- `requerirLongitudMaxima(String valor, String campo, int maxLength)`
  - Valida longitud maxima de texto.

### Excepciones compartidas

- `DomainException`
  - Funcion: representa violaciones de reglas de negocio.
  - Parametro constructor: `String message`.

- `NotFoundException`
  - Funcion: representa recursos no encontrados.
  - Parametro constructor: `String message`.

### `StockStatus`

Ubicacion: `shared.dominio.valueobjects`

Funcion: enum de estado de stock calculado para variantes.

Uso: se retorna en `VarianteResponseDTO` para indicar si la variante esta en estado normal o bajo stock.

## 3. Modulo Usuario

### Dominio

#### `Usuario`

Funcion: aggregate root del modulo usuario. Representa credenciales, correo, nombre y rol.

Constructores:

- `Usuario(String nombre, String correo, String contrasenia, RolUsuario rol)`
  - Crea un usuario nuevo sin id.

- `Usuario(Long id, String nombre, String correo, String contrasenia, RolUsuario rol)`
  - Reconstituye usuario desde persistencia.

Metodos:

- `esAdmin()`
  - Retorna `true` si el rol es `ADMIN`.

- `esVendedor()`
  - Retorna `true` si el rol es `VENDEDOR`.

- `actualizarContrasenia(String nuevaContraseniaHash)`
  - `nuevaContraseniaHash`: password ya codificado.
  - Valida que no sea vacio y actualiza el hash.

- Getters: `getId`, `getNombre`, `getCorreo`, `getContrasenia`, `getRol`.
- `setId(Long id)`: usado por adaptadores al propagar ids generados.

Reglas:

- Nombre obligatorio.
- Correo obligatorio y debe contener `@`.
- Contrasenia obligatoria.
- Rol obligatorio.

#### `RolUsuario`

Funcion: enum de roles del sistema.

Valores:

- `ADMIN`
- `VENDEDOR`

Metodo:

- `toSpringRole()`
  - Retorna el rol con prefijo `ROLE_` para Spring Security.

#### `IUsuarioRepository`

Tipo: output port de dominio.

Funcion: contrato puro para persistencia y consulta de usuarios.

Metodos:

- `save(Usuario usuario)`
  - `usuario`: usuario de dominio a crear o actualizar.

- `findById(Long id)`
  - `id`: identificador de usuario.

- `findByCorreo(String correo)`
  - `correo`: correo usado como login y subject del JWT.

- `existsByCorreo(String correo)`
  - `correo`: correo a verificar.

- `deleteById(Long id)`
  - `id`: identificador a eliminar.

### Aplicacion

#### `LoginDTO`

Funcion: request de login.

Campos:

- `correo`: email del usuario.
- `contrasenia`: password en texto plano recibido del cliente.

#### `AuthResponseDTO`

Funcion: response de autenticacion.

Campos:

- `token`: JWT.
- `tipo`: usualmente `Bearer`.
- `correo`: correo autenticado.
- `rol`: rol del usuario.
- `expiracion`: fecha/hora de expiracion.

Metodo:

- `of(String token, String correo, String rol, LocalDateTime expiracion)`
  - Construye respuesta de login.

#### `IAutenticarUseCase`

Tipo: input port.

Metodo:

- `autenticar(LoginDTO dto)`
  - `dto`: correo y contrasenia.
  - Retorna `AuthResponseDTO`.

#### `AutenticarUseCaseImpl`

Funcion: caso de uso de login.

Metodo:

- `autenticar(LoginDTO dto)`
  - Busca usuario por correo.
  - Verifica password con `IPasswordEncoder`.
  - Genera JWT con `IJwtTokenProvider`.

Dependencias:

- `IUsuarioRepository`
- `IPasswordEncoder`
- `IJwtTokenProvider`

#### `IPasswordEncoder`

Funcion: puerto de aplicacion para codificar/verificar passwords sin depender de Spring.

Metodos:

- `encode(String raw)`
  - `raw`: password plano.

- `matches(String raw, String hashed)`
  - `raw`: password plano recibido.
  - `hashed`: hash almacenado.

#### `IJwtTokenProvider`

Funcion: puerto de aplicacion para generar y leer JWT.

Metodos:

- `generarToken(String correo, String rol)`
  - `correo`: subject del token.
  - `rol`: rol del usuario.

- `extraerCorreo(String token)`
  - `token`: JWT.

- `extraerRol(String token)`
  - `token`: JWT.

- `obtenerExpiracion(String token)`
  - `token`: JWT.

- `esValido(String token)`
  - `token`: JWT.

### Infraestructura

#### `AuthRestController`

Funcion: adaptador de entrada REST para autenticacion.

Endpoint:

- `POST /api/v1/auth/login`

Metodo:

- `login(LoginDTO dto)`
  - `dto`: body validado con correo y contrasenia.
  - Retorna token JWT si las credenciales son validas.

#### `UsuarioJpaEntity`

Funcion: entidad JPA que representa tabla `USUARIO`.

Campos:

- `id`: columna `id_usuario`.
- `nombre`
- `correo`
- `contrasenia`
- `rol`

#### `UsuarioJpaRepository`

Funcion: repositorio Spring Data interno.

Metodos:

- `findByCorreo(String correo)`
- `existsByCorreo(String correo)`

#### `JpaUsuarioAdapter`

Funcion: adaptador de salida que implementa `IUsuarioRepository`.

Metodos:

- `save(Usuario usuario)`
  - Mapea dominio a JPA, persiste y propaga id.

- `findById(Long id)`
- `findByCorreo(String correo)`
- `existsByCorreo(String correo)`
- `deleteById(Long id)`

Metodos privados:

- `toEntity(Usuario usuario)`
  - Mapea dominio a JPA.

- `toDomain(UsuarioJpaEntity entity)`
  - Mapea JPA a dominio.

#### `JwtTokenProvider`

Funcion: implementacion JWT con jjwt.

Metodos:

- `generarToken(String correo, String rol)`
  - Genera JWT firmado con subject `correo` y claim `rol`.

- `extraerCorreo(String token)`
  - Lee subject del JWT.

- `extraerRol(String token)`
  - Lee claim `rol`.

- `obtenerExpiracion(String token)`
  - Lee expiracion.

- `esValido(String token)`
  - Valida firma y expiracion.

- `parsearClaims(String token)`
  - Decodifica claims.

#### `JwtAuthenticationFilter`

Funcion: filtro HTTP que autentica requests con `Authorization: Bearer <token>`.

Metodo principal:

- `doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)`
  - `request`: request HTTP.
  - `response`: response HTTP.
  - `filterChain`: cadena de filtros.
  - Si el token es valido, establece `Authentication` con principal `correo`.

Metodo privado:

- `extraerRol(String token)`
  - `token`: JWT.
  - Retorna rol para construir autoridad `ROLE_*`.

#### `UsuarioAutenticadoService`

Funcion: servicio de infraestructura que resuelve el usuario autenticado real desde Spring Security.

Metodos:

- `obtenerCorreoActual()`
  - Lee `SecurityContextHolder`.
  - Espera principal tipo `String` con correo.

- `obtenerUsuarioActual()`
  - Busca el usuario por correo usando `IUsuarioRepository`.

- `obtenerIdUsuarioActual()`
  - Retorna id real del usuario autenticado.

#### `SecurityConfig`

Funcion: configuracion de seguridad HTTP.

Beans:

- `securityFilterChain(HttpSecurity http)`
  - `http`: builder de Spring Security.
  - Configura JWT stateless, CORS, endpoints publicos y autorizaciones.

- `passwordEncoder()`
  - Retorna `BCryptPasswordEncoder`.

- `corsConfigurationSource()`
  - Configura origen permitido `http://localhost:5173`.

#### `UsuarioModuleConfig`

Funcion: ensamblado del modulo usuario.

Beans:

- `usuarioRepository(UsuarioJpaRepository jpaRepository)`
  - Crea `JpaUsuarioAdapter`.

- `usuarioAutenticadoService(IUsuarioRepository usuarioRepository)`
  - Crea servicio de usuario actual.

- `jwtTokenProvider()`
  - Crea proveedor JWT con propiedades.

- `passwordEncoderAdapter(PasswordEncoder bCryptEncoder)`
  - Adapta BCrypt a `IPasswordEncoder`.

- `autenticarUseCase(IUsuarioRepository usuarioRepository, IPasswordEncoder passwordEncoder, IJwtTokenProvider jwtTokenProvider)`
  - Crea caso de uso de login.

- `jwtAuthenticationFilter(IJwtTokenProvider jwtTokenProvider)`
  - Crea filtro JWT.

## 4. Modulo Producto

### Dominio

#### `Producto`

Funcion: aggregate root de producto.

Constructores:

- `Producto(String nombre, String categoria, String marca)`
  - Crea producto nuevo.

- `Producto(Long id, String nombre, String categoria, String marca)`
  - Reconstituye producto persistido.

Metodo:

- `crearVariante(String talla, String color, String material, BigDecimal precioCosto, BigDecimal precioVenta)`
  - `talla`: talla de la variante.
  - `color`: color.
  - `material`: material opcional.
  - `precioCosto`: costo.
  - `precioVenta`: precio de venta.
  - Retorna `VarianteProducto`.

Getters:

- `getId`, `getNombre`, `getCategoria`, `getMarca`, `getVariantes`.

Setter:

- `setId(Long id)`.

#### `VarianteProducto`

Funcion: entidad de dominio que representa SKU/variante con stock.

Constructores:

- Constructor para variante nueva asociada a producto.
- Constructor para reconstitucion desde persistencia.

Metodos:

- `registrarEntrada(int cantidad)`
  - `cantidad`: unidades a incrementar.
  - Valida cantidad mayor a cero.

- `registrarSalida(int cantidad)`
  - `cantidad`: unidades a descontar.
  - Valida cantidad mayor a cero y stock suficiente.

- `ajustarStock(int cantidad, String motivo)`
  - `cantidad`: nuevo stock absoluto.
  - `motivo`: justificacion obligatoria.

- `esBajoStock()`
  - Retorna si `stockActual <= stockMinimo`.

- `getStockStatus()`
  - Retorna estado de stock para respuesta.

Getters:

- `getId`, `getProducto`, `getTalla`, `getColor`, `getMaterial`, `getPrecioCosto`, `getPrecioVenta`, `getStockActual`, `getStockMinimo`.

Setter:

- `setId(Long id)`.

#### `IVarianteRepository`

Tipo: output port.

Metodos:

- `save(VarianteProducto variante)`
  - Crea o actualiza variante.

- `findById(Long id)`
  - Busca variante por id.

- `findByProductoId(Long idProducto)`
  - Lista variantes de un producto.

- `findByFiltros(String talla, String color, String categoria)`
  - Filtros opcionales; `null` significa sin filtro.

- `findBajoStock()`
  - Lista variantes con bajo stock.

- `deleteById(Long id)`
  - Elimina variante por id.

### Aplicacion

#### `CrearProductoDTO`

Funcion: request para crear producto con variantes.

Campos principales:

- `nombre`
- `categoria`
- `marca`
- `variantes`: lista de variantes iniciales.

DTO interno:

- `VarianteDTO`
  - `talla`
  - `color`
  - `material`
  - `precioCosto`
  - `precioVenta`

#### `VarianteResponseDTO`

Funcion: response de variante.

Campos:

- `idVariante`
- `idProducto`
- `nombreProducto`
- `categoria`
- `marca`
- `talla`
- `color`
- `material`
- `precioCosto`
- `precioVenta`
- `stockActual`
- `stockMinimo`
- `stockStatus`

#### `IRegistrarProductoUseCase`

Tipo: input port.

Metodo:

- `registrar(CrearProductoDTO dto)`
  - `dto`: producto con variantes.
  - Retorna lista de variantes persistidas.

#### `RegistrarProductoUseCaseImpl`

Funcion: crea producto y variantes, luego persiste variantes mediante puerto.

Metodo:

- `registrar(CrearProductoDTO dto)`
  - Crea `Producto`.
  - Invoca `producto.crearVariante(...)`.
  - Persiste con `IVarianteRepository.save(...)`.
  - Retorna `List<VarianteResponseDTO>`.

#### `IBuscarVariantesUseCase`

Tipo: input port.

Metodos:

- `buscar(String talla, String color, String categoria)`
  - Busca variantes con filtros opcionales.

- `buscarBajoStock()`
  - Busca variantes con bajo stock.

#### `BuscarVariantesUseCaseImpl`

Funcion: consulta variantes y las mapea a DTO.

Metodos:

- `buscar(String talla, String color, String categoria)`
- `buscarBajoStock()`

### Infraestructura

#### `ProductoRestController`

Funcion: adaptador REST de producto.

Endpoints:

- `POST /api/v1/productos`
  - Metodo: `crearProducto(CrearProductoDTO dto)`
  - Acceso: `ADMIN`.
  - Body: producto con variantes.

- `GET /api/v1/productos/variantes`
  - Metodo: `buscarVariantes(String talla, String color, String categoria)`
  - Acceso: `ADMIN`, `VENDEDOR`.
  - Query params opcionales: `talla`, `color`, `categoria`.

- `GET /api/v1/productos/variantes/bajo-stock`
  - Metodo: `buscarBajoStock()`
  - Acceso: `ADMIN`.

#### Entidades JPA

- `ProductoJpaEntity`
  - Tabla `PRODUCTO`.
  - Campos: `id`, `nombre`, `categoria`, `marca`.

- `VarianteJpaEntity`
  - Tabla `VARIANTE_PRODUCTO`.
  - Campos: `id`, `producto`, `talla`, `color`, `material`, `precioCosto`, `precioVenta`, `stockActual`, `stockMinimo`.

#### `VarianteJpaRepository`

Funcion: repositorio Spring Data interno.

Metodos:

- `findByProductoId(Long idProducto)`
- `findByFiltros(String talla, String color, String categoria)`
- `findBajoStock()`

#### `JpaVarianteAdapter`

Funcion: adaptador de salida de producto.

Metodos:

- `save(VarianteProducto variante)`
- `findById(Long id)`
- `findByProductoId(Long idProducto)`
- `findByFiltros(String talla, String color, String categoria)`
- `findBajoStock()`
- `deleteById(Long id)`

Metodos privados:

- `toEntity(VarianteProducto v)`
- `toDomain(VarianteJpaEntity e)`

#### `ProductoModuleConfig`

Funcion: ensamblado del modulo producto.

Beans:

- `varianteRepository(VarianteJpaRepository jpaRepository)`
- `registrarProductoUseCase(IVarianteRepository repo)`
- `buscarVariantesUseCase(IVarianteRepository repo)`

## 5. Modulo Inventario

### Dominio

#### `Movimiento`

Funcion: entidad de dominio que registra una transaccion de stock.

Constructores:

- `Movimiento(Long idVariante, Long idUsuario, TipoMovimiento tipo, int cantidad, String motivo)`
  - Crea movimiento nuevo.

- `Movimiento(Long id, Long idVariante, Long idUsuario, TipoMovimiento tipo, int cantidad, String motivo, LocalDateTime fecha)`
  - Reconstituye movimiento persistido.

Metodo:

- `validarTipo()`
  - Valida reglas segun tipo:
    - `ENTRADA` y `SALIDA`: cantidad mayor a cero.
    - `AJUSTE`: cantidad no negativa y motivo obligatorio.

Getters:

- `getId`, `getIdVariante`, `getIdUsuario`, `getTipo`, `getCantidad`, `getMotivo`, `getFecha`.

Setter:

- `setId(Long id)`.

#### `TipoMovimiento`

Funcion: enum de movimiento de inventario.

Valores:

- `ENTRADA`
- `SALIDA`
- `AJUSTE`

#### `IMovimientoRepository`

Tipo: output port.

Metodos:

- `save(Movimiento movimiento)`
- `findById(Long id)`
- `findByVarianteId(Long idVariante)`
- `findByVarianteIdAndTipo(Long idVariante, TipoMovimiento tipo)`
- `findByFechaEntre(LocalDateTime desde, LocalDateTime hasta)`
- `findByUsuarioId(Long idUsuario)`

### Aplicacion

#### `RegistrarEntradaDTO`

Funcion: request publico seguro para entrada de stock.

Campos:

- `idVariante`: variante afectada.
- `cantidad`: cantidad a incrementar, minimo 1.
- `motivo`: observacion opcional, maximo 150 caracteres.

No incluye:

- `idUsuario`: se toma del JWT.
- `tipo`: se fija como `ENTRADA`.

#### `RegistrarAjusteDTO`

Funcion: request publico seguro para ajuste manual.

Campos:

- `idVariante`: variante afectada.
- `cantidad`: nuevo stock absoluto, minimo 0.
- `motivo`: justificacion, validada por dominio para `AJUSTE`.

No incluye:

- `idUsuario`: se toma del JWT.
- `tipo`: se fija como `AJUSTE`.

#### `MovimientoResponseDTO`

Funcion: response de movimiento.

Campos:

- `idMovimiento`
- `idVariante`
- `idUsuario`
- `tipo`
- `cantidad`
- `motivo`
- `fecha`
- `stockResultante`

#### `IRegistrarEntradaUseCase`

Tipo: input port.

Metodo:

- `registrar(RegistrarEntradaDTO dto, Long idUsuario)`
  - `dto`: datos operativos de entrada.
  - `idUsuario`: usuario autenticado resuelto en infraestructura.

#### `RegistrarEntradaUseCaseImpl`

Funcion: registra entrada de mercaderia.

Metodo:

- `registrar(RegistrarEntradaDTO dto, Long idUsuario)`
  - Busca variante por `dto.idVariante`.
  - Ejecuta `variante.registrarEntrada(dto.cantidad)`.
  - Persiste variante.
  - Crea `Movimiento` con tipo `ENTRADA`.
  - Persiste movimiento.
  - Retorna `MovimientoResponseDTO`.

#### `IRegistrarAjusteUseCase`

Tipo: input port.

Metodo:

- `ajustar(RegistrarAjusteDTO dto, Long idUsuario)`
  - `dto`: datos operativos del ajuste.
  - `idUsuario`: usuario autenticado.

#### `RegistrarAjusteUseCaseImpl`

Funcion: registra ajuste manual de stock.

Metodo:

- `ajustar(RegistrarAjusteDTO dto, Long idUsuario)`
  - Busca variante.
  - Ejecuta `variante.ajustarStock(dto.cantidad, dto.motivo)`.
  - Persiste variante.
  - Crea `Movimiento` con tipo `AJUSTE`.
  - Persiste movimiento.
  - Retorna `MovimientoResponseDTO`.

### Infraestructura

#### `InventarioRestController`

Funcion: adaptador REST de movimientos.

Endpoints:

- `POST /api/v1/movimientos/entrada`
  - Metodo: `registrarEntrada(RegistrarEntradaDTO dto)`
  - Acceso: `ADMIN`.
  - Obtiene `idUsuario` desde `UsuarioAutenticadoService`.

- `POST /api/v1/movimientos/ajuste`
  - Metodo: `registrarAjuste(RegistrarAjusteDTO dto)`
  - Acceso: `ADMIN`.
  - Obtiene `idUsuario` desde `UsuarioAutenticadoService`.

#### `MovimientoJpaEntity`

Funcion: entidad JPA de tabla `MOVIMIENTO`.

Campos:

- `id`
- `idVariante`
- `idUsuario`
- `tipo`
- `cantidad`
- `motivo`
- `fecha`

#### `MovimientoJpaRepository`

Funcion: repositorio Spring Data interno.

Metodos:

- `findByIdVariante(Long idVariante)`
- `findByIdVarianteAndTipo(Long idVariante, TipoMovimiento tipo)`
- `findByFechaBetween(LocalDateTime desde, LocalDateTime hasta)`
- `findByIdUsuario(Long idUsuario)`

#### `JpaMovimientoAdapter`

Funcion: adaptador de salida de inventario.

Metodos:

- `save(Movimiento movimiento)`
- `findById(Long id)`
- `findByVarianteId(Long idVariante)`
- `findByVarianteIdAndTipo(Long idVariante, TipoMovimiento tipo)`
- `findByFechaEntre(LocalDateTime desde, LocalDateTime hasta)`
- `findByUsuarioId(Long idUsuario)`

Metodos privados:

- `toEntity(Movimiento movimiento)`
- `toDomain(MovimientoJpaEntity entity)`

#### Decoradores transaccionales

Funcion: aplicar `@Transactional` en infraestructura sin contaminar la capa de aplicacion.

- `TransactionalRegistrarEntradaUseCase`
  - Metodo: `registrar(RegistrarEntradaDTO dto, Long idUsuario)`.
  - Garantiza atomicidad entre actualizacion de variante y movimiento.

- `TransactionalRegistrarAjusteUseCase`
  - Metodo: `ajustar(RegistrarAjusteDTO dto, Long idUsuario)`.
  - Garantiza atomicidad entre ajuste de variante y movimiento.

#### `InventarioModuleConfig`

Funcion: ensamblado del modulo inventario.

Beans:

- `movimientoRepository(MovimientoJpaRepository jpaRepository)`
- `registrarEntradaUseCase(IVarianteRepository varianteRepository, IMovimientoRepository movimientoRepository)`
- `registrarAjusteUseCase(IVarianteRepository varianteRepository, IMovimientoRepository movimientoRepository)`

Los beans de entrada y ajuste retornan decoradores transaccionales.

## 6. Modulo Venta

### Dominio

#### `Venta`

Funcion: aggregate root de venta.

Constructores:

- `Venta(Long idUsuario, String metodoPago)`
  - Crea venta nueva en estado pendiente.

- `Venta(Long id, Long idUsuario, EstadoVenta estado, String metodoPago, LocalDateTime fecha, List<DetalleVenta> detalles)`
  - Reconstituye venta persistida.

Metodos:

- `agregarDetalle(Long idVariante, int cantidad, BigDecimal precioUnitario)`
  - Agrega detalle y retorna `DetalleVenta`.

- `completar()`
  - Cambia estado a `COMPLETADA` si se permite.

- `anular()`
  - Cambia estado a `ANULADA` si se permite.

- `calcularTotal()`
  - Suma subtotales de detalles.

Getters:

- `getId`, `getIdUsuario`, `getEstado`, `getMetodoPago`, `getFecha`, `getDetalles`.

Setter:

- `setId(Long id)`.

#### `DetalleVenta`

Funcion: entidad de dominio hija de `Venta`.

Constructores:

- `DetalleVenta(Long idVariante, int cantidad, BigDecimal precioUnitario)`
- `DetalleVenta(Long id, Long idVariante, int cantidad, BigDecimal precioUnitario)`

Metodo:

- `calcularSubtotal()`
  - Retorna `cantidad * precioUnitario`.

Getters:

- `getId`, `getIdVariante`, `getCantidad`, `getPrecioUnitario`.

Setter:

- `setId(Long id)`.

#### `EstadoVenta`

Funcion: enum de estado de venta.

Valores:

- `PENDIENTE`
- `COMPLETADA`
- `ANULADA`

Metodos:

- `puedeCompletar()`
  - Retorna si el estado puede pasar a completada.

- `puedeAnular()`
  - Retorna si el estado puede pasar a anulada.

#### `IVentaRepository`

Tipo: output port.

Metodos:

- `save(Venta venta)`
- `findById(Long id)`
- `findAll()`
- `findByUsuarioId(Long idUsuario)`
- `findByEstado(EstadoVenta estado)`
- `findByUsuarioIdAndEstado(Long idUsuario, EstadoVenta estado)`
- `findByFechaEntre(LocalDateTime desde, LocalDateTime hasta)`
- `findByUsuarioIdAndFechaEntre(Long idUsuario, LocalDateTime desde, LocalDateTime hasta)`

### Aplicacion

#### `CrearVentaDTO`

Funcion: request para registrar venta.

Campos:

- `items`: lista de items vendidos.
- `metodoPago`: metodo de pago.

DTO interno `ItemVentaDTO`:

- `idVariante`: variante vendida.
- `cantidad`: unidades vendidas.
- `precioUnitario`: precio aplicado.

No incluye `idUsuario`; se obtiene desde JWT.

#### `VentaResponseDTO`

Funcion: response de venta.

Campos:

- `idVenta`
- `idUsuario`
- `estado`
- `metodoPago`
- `fecha`
- `detalles`
- `total`

DTO interno `DetalleVentaResponseDTO`:

- `idDetalle`
- `idVariante`
- `cantidad`
- `precioUnitario`
- `subtotal`

#### `IRegistrarVentaUseCase`

Tipo: input port.

Metodo:

- `registrar(CrearVentaDTO dto, Long idUsuario)`
  - `dto`: items y metodo de pago.
  - `idUsuario`: usuario autenticado.

#### `RegistrarVentaUseCaseImpl`

Funcion: registra venta completa y descuenta stock.

Metodo:

- `registrar(CrearVentaDTO dto, Long idUsuario)`
  - Valida stock de todas las variantes.
  - Crea `Venta`.
  - Agrega detalles.
  - Descuenta stock de cada variante.
  - Persiste variantes.
  - Completa venta.
  - Persiste venta.
  - Retorna `VentaResponseDTO`.

Metodo estatico:

- `toDTO(Venta venta)`
  - Mapea venta de dominio a response.

#### `IConsultarVentaUseCase`

Tipo: input port.

Metodos:

- `buscarPorId(Long idVenta)`
- `buscarTodas()`
- `buscarPorUsuario(Long idUsuario)`
- `buscarPorEstado(EstadoVenta estado)`
- `buscarPorUsuarioYEstado(Long idUsuario, EstadoVenta estado)`
- `buscarPorFechas(LocalDateTime desde, LocalDateTime hasta)`
- `buscarPorUsuarioYFechas(Long idUsuario, LocalDateTime desde, LocalDateTime hasta)`

#### `ConsultarVentaUseCaseImpl`

Funcion: consulta ventas y mapea resultados.

Metodos:

- `buscarPorId(Long idVenta)`
- `buscarTodas()`
- `buscarPorUsuario(Long idUsuario)`
- `buscarPorEstado(EstadoVenta estado)`
- `buscarPorUsuarioYEstado(Long idUsuario, EstadoVenta estado)`
- `buscarPorFechas(LocalDateTime desde, LocalDateTime hasta)`
- `buscarPorUsuarioYFechas(Long idUsuario, LocalDateTime desde, LocalDateTime hasta)`

#### `IAnularVentaUseCase`

Tipo: input port.

Metodo:

- `anular(Long idVenta, Long idUsuario)`
  - `idVenta`: venta a anular.
  - `idUsuario`: usuario autenticado que ejecuta la anulacion.

#### `AnularVentaUseCaseImpl`

Funcion: anula venta completada y revierte stock.

Metodo:

- `anular(Long idVenta, Long idUsuario)`
  - Busca venta.
  - Ejecuta `venta.anular()`.
  - Para cada detalle, repone stock en variante.
  - Persiste variantes.
  - Persiste venta anulada.
  - Retorna `VentaResponseDTO`.

### Infraestructura

#### `VentaRestController`

Funcion: adaptador REST de ventas.

Endpoints:

- `POST /api/v1/ventas`
  - Metodo: `crearVenta(CrearVentaDTO dto)`
  - Acceso: `ADMIN`, `VENDEDOR`.
  - Obtiene `idUsuario` real desde `UsuarioAutenticadoService`.

- `GET /api/v1/ventas/{id}`
  - Metodo: `obtenerVenta(Long id)`
  - Acceso: `ADMIN`, `VENDEDOR`.

- `GET /api/v1/ventas`
  - Metodo: `listarVentas(EstadoVenta estado, LocalDateTime desde, LocalDateTime hasta)`
  - Acceso: `ADMIN`, `VENDEDOR`.
  - Regla:
    - `ADMIN`: lista todas o filtra globalmente.
    - `VENDEDOR`: lista solo ventas propias, tambien cuando filtra.

- `PATCH /api/v1/ventas/{id}/anular`
  - Metodo: `anularVenta(Long id)`
  - Acceso: `ADMIN`.
  - Usa usuario autenticado real.

Metodo privado:

- `listarVentasComoAdmin(EstadoVenta estado, LocalDateTime desde, LocalDateTime hasta)`
  - Aplica filtros globales o retorna todas.

#### Entidades JPA

- `VentaJpaEntity`
  - Tabla `VENTA`.
  - Campos: `id`, `idUsuario`, `estado`, `metodoPago`, `fecha`, `detalles`.

- `DetalleVentaJpaEntity`
  - Tabla `DETALLE_VENTA`.
  - Campos: `id`, `venta`, `idVariante`, `cantidad`, `precioUnitario`.

#### `VentaJpaRepository`

Funcion: repositorio Spring Data interno.

Metodos:

- `findByIdUsuario(Long idUsuario)`
- `findByEstado(EstadoVenta estado)`
- `findByIdUsuarioAndEstado(Long idUsuario, EstadoVenta estado)`
- `findByFechaBetween(LocalDateTime desde, LocalDateTime hasta)`
- `findByIdUsuarioAndFechaBetween(Long idUsuario, LocalDateTime desde, LocalDateTime hasta)`

#### `JpaVentaAdapter`

Funcion: adaptador de salida de ventas.

Metodos:

- `save(Venta venta)`
- `findById(Long id)`
- `findAll()`
- `findByUsuarioId(Long idUsuario)`
- `findByEstado(EstadoVenta estado)`
- `findByUsuarioIdAndEstado(Long idUsuario, EstadoVenta estado)`
- `findByFechaEntre(LocalDateTime desde, LocalDateTime hasta)`
- `findByUsuarioIdAndFechaEntre(Long idUsuario, LocalDateTime desde, LocalDateTime hasta)`

Metodos privados:

- `toEntity(Venta venta)`
- `toDomain(VentaJpaEntity e)`

#### Decoradores transaccionales

- `TransactionalRegistrarVentaUseCase`
  - Metodo: `registrar(CrearVentaDTO dto, Long idUsuario)`.
  - Garantiza atomicidad entre descuento de stock y persistencia de venta.

- `TransactionalAnularVentaUseCase`
  - Metodo: `anular(Long idVenta, Long idUsuario)`.
  - Garantiza atomicidad entre reposicion de stock y anulacion de venta.

#### `VentaModuleConfig`

Funcion: ensamblado del modulo venta.

Beans:

- `ventaRepository(VentaJpaRepository jpaRepository)`
- `registrarVentaUseCase(IVentaRepository ventaRepository, IVarianteRepository varianteRepository)`
- `consultarVentaUseCase(IVentaRepository ventaRepository)`
- `anularVentaUseCase(IVentaRepository ventaRepository, IVarianteRepository varianteRepository)`

Los beans de registro y anulacion retornan decoradores transaccionales.

## 7. Endpoints Publicos del Backend

Documentacion interactiva:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

La documentacion usa el esquema de seguridad `bearer-jwt`. Para probar endpoints protegidos desde Swagger UI:

1. Ejecutar `POST /api/v1/auth/login`.
2. Copiar el token de la respuesta.
3. Usar el boton `Authorize`.
4. Ingresar el token como Bearer JWT.

### Autenticacion

| Metodo | Ruta | Acceso | Body / Params | Funcion |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/auth/login` | Publico | `LoginDTO` | Autentica usuario y retorna JWT |

### Producto

| Metodo | Ruta | Acceso | Body / Params | Funcion |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/productos` | ADMIN | `CrearProductoDTO` | Crea producto y variantes |
| GET | `/api/v1/productos/variantes` | ADMIN, VENDEDOR | `talla`, `color`, `categoria` | Busca variantes |
| GET | `/api/v1/productos/variantes/bajo-stock` | ADMIN | ninguno | Lista variantes en bajo stock |

### Inventario

| Metodo | Ruta | Acceso | Body / Params | Funcion |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/movimientos/entrada` | ADMIN | `RegistrarEntradaDTO` | Incrementa stock y registra movimiento ENTRADA |
| POST | `/api/v1/movimientos/ajuste` | ADMIN | `RegistrarAjusteDTO` | Ajusta stock y registra movimiento AJUSTE |

### Venta

| Metodo | Ruta | Acceso | Body / Params | Funcion |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/ventas` | ADMIN, VENDEDOR | `CrearVentaDTO` | Registra venta y descuenta stock |
| GET | `/api/v1/ventas/{id}` | ADMIN, VENDEDOR | `id` | Obtiene detalle de venta |
| GET | `/api/v1/ventas` | ADMIN, VENDEDOR | `estado`, `desde`, `hasta` | Lista ventas segun rol y filtros |
| PATCH | `/api/v1/ventas/{id}/anular` | ADMIN | `id` | Anula venta y repone stock |

## 8. Decisiones Arquitectonicas Importantes

- El correo del usuario es el `subject` del JWT.
- El `idUsuario` real no se toma del cliente; se resuelve desde `UsuarioAutenticadoService`.
- Los movimientos de inventario no aceptan `tipo` ni `idUsuario` desde el body.
- Los vendedores no pueden consultar ventas de otros usuarios.
- La transaccionalidad vive en infraestructura mediante decoradores.
- Los casos de uso no usan anotaciones Spring.
- Los adaptadores JPA son los unicos que conocen entidades JPA.
- Los controladores REST no contienen reglas de dominio; solo validan entrada HTTP, resuelven usuario actual y delegan a input ports.

## 9. Pruebas Registradas

Pruebas existentes y agregadas:

- `SistemaInventarioApplicationTests`
  - Verifica que el contexto Spring arranca.

- `UsuarioAutenticadoServiceTest`
  - Verifica que el servicio obtiene el usuario real desde el correo autenticado.

- `VentaRestControllerTest`
  - Verifica que `ADMIN` lista todas las ventas.
  - Verifica que `VENDEDOR` filtra solo sus propias ventas.

- `InventarioRestControllerTest`
  - Verifica que inventario usa el `idUsuario` autenticado.

- `TransactionalUseCaseDecoratorsTest`
  - Verifica que los decoradores criticos exponen metodos con `@Transactional`.

Comando de validacion:

```powershell
mvn test
```

Nota local: en esta maquina `mvnw.cmd` fallo por el wrapper de Windows, por eso se valido con el Maven descargado en `.m2`.

## 10. Guia para Mantener la Arquitectura

Al agregar una nueva funcionalidad:

1. Crear o extender entidades/reglas en `dominio` si hay logica de negocio.
2. Definir input ports en `aplicacion/ports`.
3. Implementar casos de uso en `aplicacion/usecases`.
4. Definir output ports en `dominio/ports` si se necesita persistencia.
5. Implementar adaptadores JPA en `infraestructura/adapters`.
6. Exponer endpoints en `infraestructura/controllers`.
7. Registrar beans en `infraestructura/config`.
8. Si la operacion modifica mas de un agregado o tabla, envolverla con decorador transaccional en `infraestructura/transaction`.

Regla practica:

- Si una clase importa `org.springframework`, debe vivir en infraestructura.
- Si una clase importa `jakarta.persistence`, debe vivir en infraestructura.
- Si una clase representa una regla de negocio, debe vivir en dominio o aplicacion.
- Si un dato viene del cliente y puede afectar auditoria o seguridad, no confiar en el body; derivarlo desde backend.
