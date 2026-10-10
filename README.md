# TecnoMant: app Android del técnico

App Android para el técnico de campo de TecnoMant. Permite iniciar sesión, ver las órdenes de trabajo aunque no haya internet, registrar la inspección de un equipo con un checklist y guardar ese trabajo en el celular como borrador que se puede editar o borrar.

Versión 0 (avance). Proyecto de estudiante: Kotlin, vistas XML con ViewBinding, Material 3, SQLite con `SQLiteOpenHelper` y Retrofit con `enqueue` y `Callback`.

## Qué incluye

| Funcionalidad | Estado |
|---|---|
| Login contra la API (`POST /api/login`) | Incluido |
| Lista de órdenes con tarjeta personalizada y búsqueda | Incluido |
| Actualizar órdenes (`GET /api/ordenes/tecnico/{idTecnico}`) | Incluido |
| Detalle de la orden | Incluido |
| Formulario de inspección con checklist, guardado en SQLite | Incluido |
| Borradores: listar, editar y borrar | Incluido |
| Fotos de evidencia | Pendiente (la tabla `evidencia` ya está creada, sin uso) |
| Sincronización con el servidor | Pendiente (el botón dice "Próximamente" y está desactivado) |

## Requisitos

| Herramienta | Versión |
|---|---|
| Android Studio | Una versión reciente que soporte Android Gradle Plugin 9.4.1 |
| JDK | El que trae Android Studio (carpeta `jbr`), versión 25 |
| Android SDK | Plataforma `android-37` (se instala desde el SDK Manager de Android Studio) |
| Gradle | 9.6.0. No hay que instalarlo: lo descarga el wrapper (`gradlew`) |
| Celular o emulador | Android 7.0 (API 24) o superior |
| API de TecnoMant | Encendida en el puerto 5080 |

## Dependencias

No hay que instalarlas a mano: Gradle las descarga la primera vez que se compila (hace falta internet). Están declaradas en `app/build.gradle.kts` y `gradle/libs.versions.toml`.

| Librería | Versión | Para qué |
|---|---|---|
| androidx core-ktx | 1.19.1 | Utilidades de Android |
| androidx appcompat | 1.8.0 | Activities compatibles |
| androidx activity-ktx | 1.13.0 | Activities |
| androidx fragment | 1.9.1 | Fragments |
| androidx constraintlayout | 2.2.2 | Diseño de pantallas |
| androidx recyclerview | 1.4.0 | Listas |
| androidx cardview | 1.0.0 | Tarjetas de las listas |
| androidx navigation (fragment-ktx, ui-ktx) | 2.10.2 | Incluidas, sin uso |
| Material Components | 1.14.0 | Tema y componentes Material 3 |
| Glide | 5.0.9 | Imágenes (se usará con las fotos) |
| Retrofit + converter-gson | 3.0.0 | Llamadas a la API |
| JUnit | 4.13.2 | Pruebas unitarias |

## Instalación

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/oyanezc99/tecnomant-app.git
   cd tecnomant-app
   ```

2. Abrir la carpeta en Android Studio (**File > Open**) y esperar a que termine la sincronización de Gradle. Android Studio crea solo el archivo `local.properties`.

3. Si se va a compilar desde la terminal, sin Android Studio, hacer la configuración de la sección siguiente.

## Configuración

La app no usa un archivo `.env`. Hay tres valores que dependen de cada máquina:

| Valor | Dónde se configura | Ejemplo |
|---|---|---|
| Ruta del Android SDK | Archivo `local.properties` en la raíz del proyecto, propiedad `sdk.dir`. No se sube a git. | `sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk` |
| JDK | Variable de entorno `JAVA_HOME`, solo para compilar desde la terminal | `C:\Program Files\Android\Android Studio\jbr` |
| Dirección de la API | Constante `URL_BASE` en `app/src/main/java/pe/edu/cibertec/tecnomantApp/retrofit/ClienteTecnoMantRetrofit.kt` | `http://10.0.2.2:5080/` |

### Dirección de la API

- **Emulador** (valor que viene en el código): `http://10.0.2.2:5080/`. La dirección `10.0.2.2` es la computadora vista desde el emulador.
- **Celular real**: cambiar `URL_BASE` por la IP de la computadora en la red local, por ejemplo `http://192.168.1.50:5080/`, y volver a compilar. La API debe escuchar en `0.0.0.0:5080` y el celular debe estar en la misma red que la computadora. Si el firewall de Windows pregunta, permitir el acceso en redes privadas.
- La dirección siempre termina en `/`.

Para comprobar que el celular llega a la API, abrir `http://IP_DE_LA_COMPUTADORA:5080/swagger` en el navegador del celular.

Como la API local usa HTTP, el manifiesto lleva `android:usesCleartextTraffic="true"`. Es solo para desarrollo.

## Cómo se corre

### Desde Android Studio

1. Encender la API de TecnoMant en el puerto 5080.
2. Conectar un celular con la depuración USB activada, o crear un emulador en **Device Manager**.
3. Elegir el dispositivo y pulsar **Run**.
4. Iniciar sesión con un usuario técnico de los datos de la API y pulsar **Actualizar órdenes**.

### Desde la terminal

En Git Bash, desde la raíz del proyecto:

```bash
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. Para instalarlo en un dispositivo conectado:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

En PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

## Pruebas unitarias

Son pruebas JUnit 4 locales: no necesitan emulador ni la API.

```bash
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew :app:testDebugUnitTest
```

El informe queda en `app/build/reports/tests/testDebugUnitTest/index.html`.

| Archivo | Qué prueba |
|---|---|
| `ChecklistTest` | Los cinco ítems de cada tipo de equipo y la lista vacía para un tipo desconocido |
| `ReglasInspeccionTest` | Todos los ítems con resultado, observación obligatoria en Malo y estado general |
| `ReglasOrdenTest` | Qué órdenes se borran al actualizar |
| `FechasTest` | Formato de las fechas en pantalla |

## Estructura

```
app/src/main/java/pe/edu/cibertec/tecnomantApp/
├── LoginActivity, HomeActivity, OrdenesFragment, BorradoresFragment,
│   DetalleOrdenActivity, InspeccionActivity          (pantallas)
├── Orden, Inspeccion, ItemInspeccion, Borrador        (modelos)
├── Checklist, ReglasInspeccion, ReglasOrden, Fechas   (reglas, con pruebas)
├── Sesion                                             (SharedPreferences "sesion")
├── adapter/    OrdenAdapter, ChecklistAdapter, BorradorAdapter
├── db/         TecnoMantDbHelper, OrdenDao, InspeccionDao, ResultadoDb
└── retrofit/   ClienteTecnoMantRetrofit, api/, request/, response/
```

Base de datos local `tecnomant.db` con las tablas `orden`, `inspeccion`, `item` y `evidencia`.

## Seguridad

- No hay usuarios ni claves escritos en el código. El usuario y la clave se escriben en la pantalla de login y se validan contra la API.
- La clave no se guarda en el celular. En `SharedPreferences` solo quedan `idTecnico` y `nombre`.
- **Cerrar sesión** borra solo la sesión. Las órdenes y los borradores se conservan, así que un celular lo usa un solo técnico.

## Problemas comunes

| Síntoma | Solución |
|---|---|
| `SDK location not found` | Crear `local.properties` con `sdk.dir`, o abrir el proyecto en Android Studio |
| Error de versión de Java al compilar desde la terminal | Definir `JAVA_HOME` con el JDK de Android Studio |
| El login muestra "Sin conexión" en el emulador | Encender la API en el puerto 5080 |
| El login muestra "Sin conexión" en un celular real | Cambiar `URL_BASE` por la IP de la computadora y comprobar que la API escucha en `0.0.0.0` |
