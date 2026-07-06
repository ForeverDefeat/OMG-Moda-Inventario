# ClothWise - Evidencia Para Evaluacion Final

Este documento resume como el backend `sistema-inventario` responde a los criterios de la actividad final sin romper la integracion existente con el frontend `omg-moda-front`.

## Compatibilidad Backend Y Frontend

- El frontend consume `http://localhost:8080/api/v1`.
- Se mantienen las rutas publicas actuales: `/auth/login`, `/productos`, `/productos/variantes`, `/movimientos/entrada`, `/movimientos/ajuste` y `/ventas`.
- Se mantiene autenticacion con `Authorization: Bearer <token>`.
- Se mantienen los DTOs consumidos por el frontend; no se cambian nombres de campos ni estructura JSON.
- CORS sigue permitiendo `http://localhost:5173`.

## Matriz De Rubrica

| Criterio | Evidencia en ClothWise | Archivos representativos |
| --- | --- | --- |
| MVC | Los controladores REST reciben requests, validan DTOs y delegan a casos de uso. El modelo de negocio vive en entidades de dominio. | `*RestController`, `dominio/*`, `aplicacion/dto/*` |
| TDD | Se agregan pruebas unitarias de reglas de dominio y casos de uso sin depender de MySQL. | `src/test/java/.../dominio/*Test.java`, `src/test/java/.../usecases/*Test.java` |
| DAO | La persistencia se abstrae con puertos de repositorio y se implementa con adaptadores JPA. | `dominio/ports/*Repository.java`, `infraestructura/adapters/*Adapter.java` |
| SOLID | Los casos de uso dependen de interfaces, las entidades encapsulan reglas y la infraestructura queda separada del dominio. | `aplicacion/usecases/*`, `dominio/*`, `infraestructura/config/*ModuleConfig.java` |
| Seguridad | Login JWT, BCrypt, roles, autorizacion por ruta/metodo, CORS controlado y errores globales sin exponer stack traces. | `SecurityConfig`, `JwtTokenProvider`, `GlobalExceptionHandler`, `UsuarioModuleConfig` |
| Recursos Java | Uso explicito de Guava, Apache POI, Apache Commons Lang, PDFBox y Logback, ademas de records, enums, streams, Optional, BigDecimal, Bean Validation, JUnit 5 y Mockito. | `pom.xml`, `ReportesExcelExportService`, `ReportesDownloadExportService`, `CompraSugerenciaService`, `TextNormalizer`, `logback-spring.xml` |

## Recursos Java De Apoyo

| Libreria | Uso concreto | Archivo representativo | Beneficio | Consideracion de seguridad |
| --- | --- | --- | --- | --- |
| Google Guava | Ordenamiento estable e inmutabilidad de sugerencias de compra con `ComparisonChain` e `ImmutableList`. | `compra/aplicacion/usecases/CompraSugerenciaService.java` | Reduce errores de comparacion manual y evita mutaciones accidentales. | No expone datos sensibles; solo procesa variantes y cantidades sugeridas. |
| Apache POI | Exportacion XLSX de alertas de inventario. | `reportes/aplicacion/usecases/ReportesExcelExportService.java` | Agrega funcionalidad real de reportes descargables. | El Excel incluye solo inventario operativo, sin JWT, contrasenas ni datos personales. |
| PDFBox | Exportacion PDF ejecutiva de resumen, ventas por categoria, rotacion y alertas de stock por periodo diario, semanal o mensual. | `reportes/aplicacion/usecases/ReportesDownloadExportService.java` | Convierte indicadores visuales en documentos presentables para administracion. | Los PDF no incluyen JWT, contrasenas ni payloads completos; solo metricas agregadas e inventario operativo. |
| Apache Commons Lang | Normalizacion centralizada de textos con `StringUtils`. | `shared/aplicacion/utils/TextNormalizer.java` | Evita `trim()` disperso y entradas con espacios duplicados. | Ayuda a validar/limpiar entradas antes del dominio. |
| Logback | Configuracion explicita por perfil para consola, niveles de app, seguridad e Hibernate. | `src/main/resources/logback-spring.xml` | Mejora trazabilidad y control de ruido en logs. | En `prod` reduce SQL/security logs y no registra tokens, contrasenas ni payloads completos. |

## Fases Implementadas

### Fase 1: Verificacion Y Evidencia

- Se corrigio `mvnw.cmd` para evitar un fallo de PowerShell al leer `.m2` sin target simbolico.
- Se cambio la prueba de contexto por una prueba liviana de entrada Spring Boot, evitando que la suite basica requiera MySQL.
- Se documento la matriz academica de evaluacion.

### Fase 2: TDD Sin Base De Datos

- Pruebas de dominio:
  - `VarianteProductoTest`: entradas, salidas, ajuste y estado de stock.
  - `VentaTest`: ciclo de vida, total y anulacion.
  - `DetalleVentaTest`: subtotal y validaciones.
- Pruebas de casos de uso:
  - `RegistrarProductoUseCaseImplTest`.
  - `RegistrarInventarioUseCaseImplTest`.
  - `VentaUseCaseImplTest`.

### Fase 3: MVC, DAO Y SOLID

- La arquitectura se defiende como hexagonal y compatible con MVC academico.
- Los repositorios funcionan como contratos DAO desde el dominio.
- Los casos de uso no dependen de Spring Data ni entidades JPA.

### Fase 4: Seguridad Con Compatibilidad

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` y `JWT_EXPIRACION_HORAS` se pueden configurar con variables de entorno.
- Los valores por defecto de desarrollo se mantienen para no romper ejecucion local.
- El contrato de login y token Bearer se mantiene intacto.
- `GET /api/v1/ventas/{id}` valida propiedad de la venta para usuarios `VENDEDOR`; `ADMIN` conserva acceso global.
- Errores de cliente como JSON malformado, parametros invalidos y rutas inexistentes se mapean a `400`/`404`, evitando falsos `500`.
- DTOs criticos refuerzan validaciones de longitud, patrones y metodo de pago permitido.

### Fase 5: Recursos Java De Apoyo

- Se agregaron dependencias explicitas en `pom.xml`: Guava, Apache Commons Lang, Apache POI, PDFBox y Logback.
- Se expuso `GET /api/v1/reportes/export/inventario` para descargar alertas de stock en formato `.xlsx`.
- Se expusieron reportes descargables `CSV` y `PDF` para `resumen`, `ventas-categoria`, `rotacion` y `stock-alertas` usando `periodo=today|7d|30d`.
- Se agregaron pruebas de lectura del Excel generado, normalizacion de texto y errores HTTP de cliente.
- La suite de pruebas confirma 61 tests exitosos sin requerir MySQL.

## Comandos De Verificacion

Desde `sistema-inventario`:

```bash
mvnw.cmd test
```

Empaquetado sin repetir pruebas:

```bash
mvnw.cmd clean package -DskipTests
```

Prueba manual del exportador, usando token `ADMIN`:

```bash
curl -H "Authorization: Bearer <TOKEN_ADMIN>" ^
  -o alertas-inventario.xlsx ^
  http://localhost:8080/api/v1/reportes/export/inventario
```

Prueba manual de reportes por periodo:

```bash
curl -H "Authorization: Bearer <TOKEN_ADMIN>" ^
  -o reporte-resumen-7d.pdf ^
  "http://localhost:8080/api/v1/reportes/export/resumen.pdf?periodo=7d"

curl -H "Authorization: Bearer <TOKEN_ADMIN>" ^
  -o reporte-rotacion-today.csv ^
  "http://localhost:8080/api/v1/reportes/export/rotacion.csv?periodo=today"
```

Para levantar backend en Windows:

```bash
mvnw.cmd spring-boot:run
```

Para probar la integracion completa:

1. Crear la base MySQL `inventario_omg`.
2. Levantar backend en `http://localhost:8080`.
3. Levantar frontend en `http://localhost:5173`.
4. Validar login, productos, movimientos y ventas.

## Riesgos Controlados

- MySQL sigue siendo necesario para ejecutar la aplicacion completa.
- Las pruebas unitarias nuevas no dependen de MySQL, por lo que sirven como evidencia TDD estable.
- Los secretos siguen teniendo defaults de desarrollo, pero ya pueden reemplazarse por variables de entorno.
- Los logs de produccion usan niveles reducidos para evitar exposicion accidental de SQL, tokens o payloads sensibles.
