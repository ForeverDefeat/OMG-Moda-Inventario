# Auditoria de Seguridad ClothWise

Fecha de ejecucion: 2026-07-02  
Alcance: backend `sistema-inventario` y frontend `omg-moda-front`  
Entorno dinamico: MySQL aislado `inventario_omg_audit`, backend en `localhost:8080`, frontend Vite en `5173`

## Resumen Ejecutivo

Estado general: **Observado con hallazgos criticos**

La aplicacion tiene buenas bases: Spring Security protege endpoints por rol, los DTO principales usan Bean Validation, las consultas revisadas usan Spring Data JPA/JPQL parametrizado y las contrasenas sembradas se almacenan con BCrypt. Sin embargo, la auditoria dinamica encontro errores 500 para solicitudes que deberian resolverse como 400/404/422, un fallo critico al crear productos, credenciales y secretos visibles en codigo/configuracion, sesion JWT persistida en `localStorage`, falta de invalidacion real de logout y advertencias OWASP ZAP por headers de seguridad ausentes.

## Evidencias Generadas

- Resultados HTTP: `target/security-audit-api-results.json`
- Enumeracion de login: `target/security-audit-auth-enumeration.json`
- JSON malformado: `target/security-audit-malformed-json.json`
- Logs backend: `target/security-audit-backend.out.log`
- ZAP frontend: `target/zap/zap-baseline-frontend-ip.html`, `target/zap/zap-baseline-frontend-ip.json`
- ZAP backend API: `target/zap/zap-api-backend.html`, `target/zap/zap-api-backend.json`
- NPM audit frontend: `target/security-audit-npm-audit.json`

Verificaciones automatizadas:

- Backend: `.\mvnw.cmd test` paso 28 tests, 0 fallos.
- Frontend: `npm run build` paso.
- Frontend: `npm run lint` paso.
- Frontend dependencies: `npm audit --audit-level=low` encontro 2 vulnerabilidades conocidas.
- OWASP ZAP Docker: ejecutado correctamente con `ghcr.io/zaproxy/zaproxy:stable`.

## Prueba 1. Validacion de entradas

Estado: **Observado**

Evidencia:

| Caso | Endpoint | Payload | Resultado |
| --- | --- | --- | --- |
| Login con XSS/formato invalido | `POST /api/v1/auth/login` | `<script>alert(1)</script>` | `422`, mensaje de correo invalido |
| Producto vacio | `POST /api/v1/productos` | `{}` | `422`, lista campos obligatorios |
| Producto nombre >100 | `POST /api/v1/productos` | `nombre` con 101 caracteres | `422`, longitud rechazada |
| Producto con caracteres especiales | `POST /api/v1/productos` | `<script>`, `' OR '1'='1`, HTML | `500`, error generico |

Hallazgo principal: crear producto falla con `500` incluso con payload normal. El log muestra:

`TransientPropertyValueException: VarianteJpaEntity references an unsaved transient instance ProductoJpaEntity`

Impacto: el flujo de producto/inventario queda bloqueado y los errores de persistencia se convierten en `500`. No se pudo confirmar persistencia/reflejo XSS por este bug.

Recomendaciones:

- Corregir persistencia de `Producto` y `Variante` para guardar primero el producto o configurar cascade adecuado.
- Agregar validaciones de patron/normalizacion para campos de texto donde aplique: producto, categoria, marca, talla, color, material y motivo.
- Mantener React renderizando texto escapado y evitar cualquier `dangerouslySetInnerHTML`.

## Prueba 2. Inyeccion SQL

Estado: **Aprobado con observacion**

Evidencia:

| Caso | Endpoint | Payload | Resultado |
| --- | --- | --- | --- |
| Login SQL injection | `POST /api/v1/auth/login` | `' OR '1'='1` | `422`, no bypass |
| Filtros variantes | `GET /api/v1/productos/variantes` | `' OR '1'='1` en filtros | `200 []`, no bypass |

La revision estatica encontro Spring Data JPA y JPQL con parametros nombrados en repositorios. Los logs confirman placeholders `?`, no concatenacion SQL.

Observacion: `spring.jpa.show-sql=true` y `logging.level.org.hibernate.SQL=DEBUG` exponen consultas SQL en logs. No se expusieron consultas SQL al cliente.

Recomendaciones:

- Mantener Spring Data/parametros nombrados.
- Desactivar SQL DEBUG/TRACE y `show-sql` fuera de desarrollo local.
- Agregar pruebas de integracion para payloads SQLi en login y filtros.

## Prueba 3. Validacion lado cliente y servidor

Estado: **Critico**

Evidencia:

| Caso | Endpoint/Pantalla | Resultado |
| --- | --- | --- |
| Campos obligatorios producto | Backend | `422`, correcto |
| Longitud `nombre` producto | Backend | `422`, correcto |
| Cantidad negativa entrada | No ejecutable por bloqueo de producto/stock | Bloqueado por bug de producto |
| `estado=INVALIDO` | `GET /api/v1/ventas` | `500`, deberia ser `400` |
| Fecha invalida | `GET /api/v1/ventas` | `500`, deberia ser `400` |
| JSON malformado | `POST /api/v1/auth/login` | `500`, deberia ser `400` |
| Ruta inexistente autenticada | `GET /api/v1/no-existe` | `500`, deberia ser `404` |

Frontend:

- Catalogo e inventario usan `required`, `min` y validacion de imagen.
- Login no usa `required` en inputs y viene precargado con `admin@omgmoda.com/admin123`.
- Faltan `maxLength`/patrones en inputs de texto.
- La navegacion no filtra rutas por rol.

Recomendaciones:

- Agregar handlers globales para `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`, `NoResourceFoundException` y errores de conversion.
- Agregar `@Size` a `LoginDTO.contrasenia` y `CrearVentaDTO.metodoPago`.
- Hacer `RegistrarAjusteDTO.motivo` `@NotBlank` para alinear DTO, Swagger y dominio.
- Usar enums/control cerrado para `metodoPago`.
- En frontend, agregar `required`, `maxLength`, `pattern` donde aplique y ocultar rutas no autorizadas por rol.

## Prueba 4. Autenticacion y autorizacion

Estado: **Observado**

Evidencia:

| Caso | Endpoint | Resultado |
| --- | --- | --- |
| Sin token | `GET /api/v1/productos/variantes` | `403` |
| Token invalido | `GET /api/v1/productos/variantes` | `403` |
| VENDEDOR crea producto | `POST /api/v1/productos` | `403` |
| VENDEDOR lista sus ventas | `GET /api/v1/ventas` | `200 []` |
| Token reutilizado tras logout frontend simulado | `GET /api/v1/ventas` | `200 []` |

Spring Security aplica restricciones por rol. El cierre de sesion del frontend solo elimina `localStorage`; el JWT sigue valido hasta expirar. Esto es esperable en JWT stateless, pero debe documentarse o resolverse con refresh tokens/lista de revocacion si se requiere logout real.

Observaciones:

- Se devuelve `403` para no autenticado/token invalido; puede ser aceptable, pero semantica esperada suele ser `401`.
- Frontend `ProtectedRoute` solo valida existencia de token, no rol ni expiracion.
- Menu frontend muestra rutas potencialmente no permitidas para VENDEDOR.

Recomendaciones:

- Configurar `AuthenticationEntryPoint` para `401` en no autenticado/token invalido.
- Implementar expiracion/validacion local de token en frontend.
- Agregar guardas de rol en frontend para UX, manteniendo el backend como autoridad.
- Para logout real, usar refresh tokens revocables o blacklist temporal de JWT.

## Prueba 5. Manejo de errores

Estado: **Observado**

Evidencia:

| Caso | Resultado |
| --- | --- |
| Errores de validacion `@Valid` | `422`, mensaje adecuado |
| Error de dominio/credenciales incorrectas | `400`, mensaje funcional |
| JSON malformado | `500`, generico |
| Enum/fecha invalida | `500`, generico |
| Ruta inexistente | `500`, generico |
| `/v3/api-docs/` con slash final | `500`, generico |

No se observaron stack traces, rutas internas ni SQL en respuestas HTTP. Si se observaron clases tecnicas y SQL en logs del servidor.

Recomendaciones:

- Mapear errores de cliente a `400`/`404`, no `500`.
- Mantener respuesta generica para 500 reales.
- Reducir logs tecnicos en entornos compartidos.
- Agregar tests `MockMvc`/integracion para errores de conversion, JSON malformado y rutas inexistentes.

## Prueba 6. OWASP ZAP

Estado: **Observado**

ZAP se ejecuto con Docker:

- Imagen: `ghcr.io/zaproxy/zaproxy:stable`
- Frontend baseline target: `http://192.168.18.119:5173`
- Backend API scan target: `http://host.docker.internal:8080/v3/api-docs`

Frontend baseline:

| Severidad ZAP | Alerta | Evidencia |
| --- | --- | --- |
| Medium | Content Security Policy Header Not Set | `GET /`, `robots.txt`, `sitemap.xml` |
| Medium | Missing Anti-clickjacking Header | `GET /`, `robots.txt`, `sitemap.xml` |
| Low | X-Content-Type-Options Header Missing | `GET /`, `favicon.svg`, `src/main.tsx` |
| Low | Permissions Policy Header Not Set | multiples recursos |
| Low | COEP/COOP/CORP missing/invalid | multiples recursos |
| Informational | Modern Web Application | SPA detectada |
| Informational | Storable but Non-Cacheable Content | multiples recursos |

Backend API scan:

| Severidad ZAP | Alerta | Evidencia |
| --- | --- | --- |
| Low | A Server Error response code was returned by the server | `GET /v3/api-docs/` devuelve `500` |
| Low | Cross-Origin-Resource-Policy Header Missing or Invalid | `GET /v3/api-docs`, `POST /api/v1/auth/login` |
| Informational | Client Error response codes | Endpoints protegidos rechazados sin auth |

Recomendaciones:

- Agregar headers de seguridad: CSP, `X-Frame-Options` o `frame-ancestors`, `X-Content-Type-Options`, `Permissions-Policy`, CORP/COOP/COEP segun necesidad.
- Servir frontend production build detras de servidor con headers, no desde Vite dev para validacion final.
- Corregir `/v3/api-docs/` para `404/redirect/200`, no `500`.
- Repetir ZAP autenticado para cubrir flujos internos, ya que el API scan sin token no explora endpoints protegidos con datos reales.

## Prueba 7. Proteccion de credenciales

Estado: **Critico**

Evidencia positiva:

Consulta en `inventario_omg_audit.usuario`:

| Usuario | Hash |
| --- | --- |
| `admin@omgmoda.com` | prefijo `$2a$10$`, longitud 60 |
| `vendedor@omgmoda.com` | prefijo `$2a$10$`, longitud 60 |

Las contrasenas sembradas se guardan con BCrypt.

Hallazgos:

- `application.properties` contiene default `spring.datasource.password=${DB_PASSWORD:RODRIGO30JA}`.
- `application.properties` contiene default `jwt.secret=${JWT_SECRET:ClothWise_Dev_Secret_Key_2024_omgmoda_ica_peru}`.
- `DataInitializer` contiene credenciales seed `admin123` y `venta123`.
- `SistemaInventarioApplication` documenta ejemplo de login con credenciales.
- `LoginPage.tsx` precarga `admin@omgmoda.com/admin123`.
- `httpClient.ts` guarda la sesion completa, incluido JWT, en `localStorage`.
- `npm audit` encontro vulnerabilidad alta en `vite 8.0.0-8.0.15` y baja en `@babel/core <=7.29.0`, ambas con fix disponible.

Recomendaciones:

- Quitar secretos y credenciales por defecto del repo; usar `.env`, variables obligatorias y perfiles `dev/test/prod`.
- Deshabilitar usuarios seed fuera de perfil local.
- No precargar credenciales reales en UI.
- Preferir cookies `HttpOnly Secure SameSite` o reducir vida del access token si se mantiene `localStorage`.
- Ejecutar `npm update vite @babel/core` o `npm audit fix` validando compatibilidad.

## Prueba 8. Proteccion de informacion sensible

Estado: **Observado**

Evidencia:

- OpenAPI esta publico sin token: `GET /v3/api-docs` devuelve `200`.
- Swagger/OpenAPI expone estructura de endpoints, DTOs, ejemplos y contacto `admin@omgmoda.com`.
- `/uploads/**` esta publico por configuracion de seguridad.
- `VentaResponseDTO` y `MovimientoResponseDTO` exponen IDs internos (`idUsuario`, `idVariante`, `idVenta`, etc.).
- VENDEDOR lista solo sus ventas en la prueba dinamica (`200 []`), lo que es correcto.

Recomendaciones:

- Proteger Swagger/OpenAPI en ambientes no locales.
- Revisar si IDs internos son necesarios para el cliente; si no, reducirlos o usar referencias no sensibles.
- Mantener control de acceso por usuario para ventas y movimientos.
- Validar tipo MIME y firma magica en uploads, no solo extension.
- Definir politica de acceso para imagenes publicas y cache headers.

## Priorizacion de Remediacion

1. **Critico:** corregir `POST /api/v1/productos` que devuelve `500` por persistencia de entidad transitoria.
2. **Critico:** retirar credenciales/default secrets del codigo y perfiles compartidos.
3. **Alto:** corregir manejo de errores de cliente para que JSON malformado, enum invalido, fecha invalida y ruta inexistente no devuelvan `500`.
4. **Alto:** actualizar `vite` y `@babel/core` por vulnerabilidades reportadas por `npm audit`.
5. **Medio:** agregar headers de seguridad reportados por ZAP.
6. **Medio:** mejorar sesion/logout: expiracion local, guardas por rol y estrategia de revocacion si aplica.
7. **Medio:** cerrar validaciones faltantes (`metodoPago`, `contrasenia`, `motivo`, patrones/largos de filtros).
8. **Bajo/Medio:** desactivar logs SQL detallados y proteger OpenAPI fuera de local.

## Limitaciones

- El bug de creacion de producto bloqueo pruebas completas de entrada de stock y venta con datos nuevos.
- ZAP API scan fue mayormente no autenticado; se recomienda repetir con contexto autenticado para cobertura profunda.
- ZAP baseline final se ejecuto contra Vite dev por IP local. Para una aceptacion final, repetir contra el build de produccion servido con el mismo servidor/proxy que se usara en despliegue.
