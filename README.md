# ROCCA — Control Cadena Cárnica SDE (Android)

Aplicación nativa Android (**Kotlin + Jetpack Compose + Material Design 3**) del sistema **ROCCA — Registro Provincial de Operadores de la Carne** (Gobierno de Santiago del Estero • Dirección General de Agricultura y Ganadería / ProCarne).

## Stack tecnológico

| Capa | Tecnología |
| --- | --- |
| UI & Navegación | Jetpack Compose + Material 3 + Navigation Compose (type-safe `@Serializable`) + WindowSizeClass |
| Arquitectura | MVVM (`RoccaViewModel` + `StateFlow` + `RoccaRepository`) |
| Persistencia local | Room Database (`rocca_db`, SQLite) + Jetpack DataStore Preferences |
| Red / Georreferenciación | Retrofit + OkHttp + `kotlinx.serialization` contra API pública **Georef AR** (`apis.datos.gob.ar`) |
| Exportación documental | `android.graphics.pdf.PdfDocument` (Cartón Oficial F 102) + Exportación CSV RFC-4180 + `FileProvider` |
| Tipografía | Plus Jakarta Sans, Inter y JetBrains Mono |

## Dashboards por Rol y Casos de Uso (CU-1 a CU-10)

1. **Persona (Titular)** (`titular@lanorita.com` / `Procarne2026!`):
   - Alta de cuenta con usuario, email y número de CUIT (validación Módulo 11 AFIP) que genera/vincula automáticamente la entidad **Persona**.
   - **Dashboard Persona**: administración exclusiva de sus propias carnicerías/sucursales (`CU-5`, `CU-9`), visualización de estado registral y **fechas de vencimiento** de certificados con alertas (`CU-7`), carga de **adjuntos personales** (`dni_frente`, `dni_dorso`, `cuit`, `iibb` — `CU-4`), carga de requisitos/adjuntos por establecimiento (`CU-2`, `CU-3`) y alta de nuevas sucursales (`CU-1`) en estado `Pendiente`.
2. **Supervisor** (`supervisor@procarne.gob.ar` / `Procarne2026!`):
   - **Dashboard Supervisor**: cola priorizada de fiscalización, revisión de requisitos y observaciones (`CU-2`), verificación de adjuntos y control del ciclo de vida del operador (`Pendiente → Activo ↔ Suspendido → Bajado` — `CU-6`).
   - Acceso completo al Padrón (`CU-5`), Jerarquía (`CU-9`) y Catálogo de Actividades Económicas (`CU-8`).
3. **Administrador** (`admin@procarne.gob.ar` / `Procarne2026!`):
   - **Dashboard Administrador**: métricas ejecutivas, padrón completo con filtros por estado y categoría (`CU-5`) + exportación CSV, **autorización y emisión de certificados oficiales F 102** (relevando previos a `RELEVADO`), **suspensión y reactivación de certificados** (`CU-7`), generación de **Cartón Oficial F 102 en PDF**, gestión de F 102, Catálogo `CU-8` y Módulos II–VII.

## Compilación y Pruebas

```bash
gradle assembleDebug
gradle :app:testDebugUnitTest
```
El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.
