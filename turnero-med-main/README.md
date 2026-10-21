# Turnero Med — backend

API de turnos médicos con Java 17, Spring Boot, Spring Data JPA, DTO y Spring Security/JWT.
El flujo básico permite publicar horarios, registrarse, iniciar sesión, elegir turnos, agregarlos a un carrito, confirmar la reserva y cancelarla.

## Iniciar en tu computadora

En VS Code abrí esta carpeta (`turnero-med-main`), donde está `pom.xml`, y ejecutá:

```bash
sh mvnw spring-boot:run
```

En Windows: `mvnw.cmd spring-boot:run`.

El servidor escucha en `http://localhost:8080`. Para comprobarlo, abrí `http://localhost:8080/actuator/health`: debe responder `{"status":"UP"}`. Es una API; las pantallas se construyen en el front. Para detenerlo, usá Ctrl+C.

Por defecto se activa el perfil `local`, con una base H2 en `data/turnero.mv.db`. Conserva los datos al reiniciar y no necesita Docker ni MySQL. La carpeta `data/` está excluida de Git. No ejecutes dos instancias contra el mismo archivo de base de datos.

La primera ejecución crea datos ficticios de prueba:

| Cuenta | Email | Contraseña |
|---|---|---|
| Administrador | admin@turnero.local | AdminDemo123! |
| Médico | medico@turnero.local | MedicoDemo123! |

El médico demo tiene tres horarios para el día siguiente a su creación. No se vuelven a generar al reiniciar; cuando pasen, el administrador o el médico pueden publicar nuevos horarios. Registrá un paciente para probar el carrito. Estas cuentas y la clave JWT local son exclusivamente de demostración y solo se crean en el perfil local.

## Probar el recorrido completo en Postman

Importá `docs/Turnero-Med.postman_collection.json`. Con el back levantado, ejecutá las solicitudes en orden, o usá el Collection Runner:

1. Comprobar el servidor.
2. Iniciar sesión con el administrador demo.
3. Crear un médico de prueba (con datos nuevos en cada ejecución).
4. Publicar un horario para mañana.
5. Registrar un paciente de prueba.
6. Iniciar sesión con ese paciente.
7. Consultar los horarios disponibles y filtrarlos por médico/especialidad/día.
8. Obtener o crear el carrito del paciente autenticado.
9. Agregar el horario al carrito.
10. Consultar el carrito y su total.
11. Confirmar el carrito.
12. Consultar «Mis turnos».
13. Cancelar el turno confirmado.
14. Comprobar que ese horario volvió a estar disponible.

La colección guarda los IDs y los tokens en variables automáticamente. Sus usuarios tienen nombres, emails y DNI ficticios. Cada ejecución crea registros nuevos; usala sobre la base local de pruebas.

## Recorrido y estados

- El **administrador** crea médicos. El administrador o el **médico dueño de la agenda** publica horarios con fecha, hora, dirección y precio. Nacen como `DISPONIBLE`.
- Cualquier visitante puede consultar médicos y horarios disponibles futuros.
- El **paciente** se registra con nombre, apellido, email, contraseña y DNI. Se guarda como paciente real; no puede asignarse a sí mismo el rol ADMIN o MEDICO.
- Registro y login devuelven un JWT, el ID del usuario y el rol. El front envía `Authorization: Bearer TOKEN` en las operaciones privadas.
- `POST /api/carritos` identifica al paciente por su token y devuelve su carrito abierto. Si no tiene uno, lo crea. Hay un solo carrito abierto por paciente.
- Al agregar un turno, pasa de `DISPONIBLE` a `RESERVADO`: otro paciente ya no puede agregarlo. El precio lo toma el servidor de la base; el front no decide el total.
- Se puede quitar un turno, vaciar el carrito o cancelar el carrito. Los horarios se liberan y vuelven a `DISPONIBLE`.
- Confirmar cambia el carrito a `CONFIRMADO` y los turnos a `TOMADO`. `TOMADO` significa reserva confirmada. Se asigna el paciente y se conserva la relación con el carrito para consultar la confirmación después.
- Desde «Mis turnos» el paciente puede cancelar una reserva futura. Ese registro queda `CANCELADO` en su historial y se crea un nuevo turno disponible para el mismo médico, fecha, hora y precio. El horario vuelve a ofrecerse con **otro ID**.
- La baja administrativa (`DELETE /api/turnos/{id}`) retira un horario disponible: lo deja `CANCELADO` y no crea reemplazo. Los horarios ya reservados o confirmados no pueden editarse ni darse de baja por este endpoint.

El carrito vence **15 minutos después de su creación** (`venceEn`). Agregar más elementos no extiende ese plazo. Un proceso revisa cada minuto los carritos vencidos y libera sus turnos; también se libera un carrito vencido al pedir uno activo nuevamente. La confirmación rechaza inmediatamente un carrito vencido, aunque todavía no haya pasado la siguiente limpieza. Un carrito confirmado nunca se cancela por vencimiento.

La confirmación es una transacción: si algún turno ya pasó o no puede confirmarse, no se confirma ninguno. Los bloqueos de base de datos evitan que dos solicitudes tomen el mismo horario o creen dos carritos activos para un paciente. También se impide que un paciente reserve dos turnos exactamente a la misma hora, incluso con médicos diferentes.

## Endpoints para conectar React

Base: `http://localhost:8080/api`. Enviar JSON con `Content-Type: application/json`.

| Método y ruta | Función | Acceso |
|---|---|---|
| POST `/auth/register` | Registrar paciente y obtener token | Público |
| POST `/auth/login` | Iniciar sesión y obtener token | Público |
| GET `/medicos` | Listar médicos | Público |
| GET `/medicos/{id}` | Detalle de médico | Público |
| POST `/medicos` | Alta de médico | ADMIN |
| PUT `/medicos/{id}` | Modificar médico | ADMIN |
| DELETE `/medicos/{id}` | Borrar médico sin registros relacionados | ADMIN |
| GET `/turnos` | Horarios disponibles futuros | Público |
| GET `/turnos?especialidad=CARDIOLOGIA&medicoId=2&dia=2030-10-06&estado=DISPONIBLE` | Filtros combinables (reemplazar fecha e ID) | Público |
| GET `/turnos/{id}` | Detalle | Público si está disponible; si está ocupado, solo dueño, médico o ADMIN |
| POST `/turnos` | Publicar horario | ADMIN o médico dueño |
| PUT `/turnos/{id}` | Editar horario disponible | ADMIN o médico dueño |
| DELETE `/turnos/{id}` | Baja lógica de horario disponible | ADMIN o médico dueño |
| GET `/turnos/mis-turnos` | Reservas confirmadas y canceladas propias | PACIENTE |
| POST `/turnos/{id}/cancelar` | Cancelar reserva futura propia | PACIENTE |
| POST `/carritos` | Obtener o crear carrito activo propio | PACIENTE |
| GET `/carritos/{id}` | Consultar carrito propio | PACIENTE |
| POST `/carritos/{id}/turnos/{turnoId}` | Agregar turno | PACIENTE dueño |
| DELETE `/carritos/{id}/turnos/{turnoId}` | Quitar turno | PACIENTE dueño |
| DELETE `/carritos/{id}/turnos` | Vaciar carrito abierto | PACIENTE dueño |
| POST `/carritos/{id}/confirmar` | Confirmar todos los turnos | PACIENTE dueño |
| DELETE `/carritos/{id}` | Cancelar carrito abierto | PACIENTE dueño |
| GET `/pacientes` | Listar pacientes | ADMIN |
| GET, PUT `/pacientes/{id}` | Consultar o actualizar perfil | Dueño o ADMIN |
| POST, DELETE `/pacientes` o `/pacientes/{id}` | Alta o baja administrativa | ADMIN |

`GET /turnos` sin filtros devuelve disponibles. Si se pide otro estado, se requiere sesión y solo se muestran registros propios, de la agenda del médico o todos para ADMIN. El filtro `pacienteId` solo permite el paciente propio o un ADMIN. `POST /carritos?pacienteId=...` se conserva por compatibilidad, pero únicamente acepta el ID del paciente autenticado; es preferible omitirlo.

### Registro

```json
{
  "nombre": "Lucía",
  "apellido": "Ejemplo",
  "email": "lucia@ejemplo.com",
  "password": "Ejemplo123!",
  "dni": "30123456"
}
```

El login requiere solo `email` y `password`. La respuesta contiene `id`, `nombre`, `apellido`, `email`, `rol` y `token`; nunca devuelve la contraseña.

### Alta de turno

Elegí una fecha futura y un `medicoId` que exista:

```json
{
  "medicoId": 2,
  "fecha": { "dia": "2030-10-06", "hora": "10:00" },
  "direccion": { "calle": "Calle de ejemplo", "altura": 123, "ciudad": "Buenos Aires", "codigoPostal": "1000" },
  "precio": 25000.00
}
```

No envíes `pacienteId`: se asigna al confirmar el carrito. El estado inicial lo determina el servidor. `hora` tiene precisión de minutos, `precio` es no negativo y admite hasta dos decimales. Cada turno representa un horario puntual; todavía no se modela una duración ni intervalos superpuestos.

La respuesta agrega el ID, el estado, `medicoNombre` y `especialidad`. Esto permite renderizar tarjetas en React sin consultar un médico por cada tarjeta. Los importes se manejan en pesos argentinos, sin pasarela de pago: **confirmar reserva los turnos, no realiza un cobro real**. El carrito devuelve `turnos`, `total` y `venceEn`. Si se cancela una reserva confirmada, sigue apareciendo cancelada en ese carrito y ya no cuenta en su total vigente.

### Errores

```json
{ "status": 409, "mensaje": "El turno no está disponible o su fecha ya pasó" }
```

- 400: datos incompletos, formato inválido o fecha pasada.
- 401: falta sesión, contraseña incorrecta o token inválido/vencido.
- 403: el usuario no tiene permiso o el recurso pertenece a otra persona.
- 404: registro inexistente.
- 409: email/DNI duplicado, horario ocupado, carrito cerrado/vacío/vencido o conflicto de reservas.

El token dura 10 horas. Al cerrar sesión, el front descarta el token; si vence, pide login otra vez. CORS permite por defecto `http://localhost:5173` y `http://127.0.0.1:5173`, con los headers `Authorization` y `Content-Type`. Podés cambiar los orígenes con `FRONTEND_ORIGINS` (separados por coma).

El frontend React ya consume estos endpoints y usa los IDs reales de los turnos. Para iniciar ambas partes, conocer las pantallas y recorrer la reserva desde el navegador, consultá el [README del frontend](../front-turnero-med/README.md). Los IDs del antiguo JSON local no equivalían a los de la base y ese archivo se retiró.

## Arquitectura y correcciones

- `controller`: recibe HTTP y devuelve estados/respuestas.
- `service`: aplica reglas y transacciones; `TurnoService` gestiona horarios y cancelaciones, `CarritoService` gestiona la reserva y confirmación.
- `repository`: consultas y persistencia con JPA; ciertos accesos bloquean el registro mientras se modifica para evitar dobles reservas.
- `model`: entidades y enums de estado. Se conservaron los modelos de paciente, médico, turno y carrito.
- `dto`: contratos JSON de entrada/salida. Los precios, el total, el rol y los datos del médico se agregaron para facilitar el front.
- `AuthService`, `SecurityConfig`, `JwtFilter`: registro como paciente, login con BCrypt/JWT, roles, validación de tokens y CORS.
- `AccesoService`: comprueba la identidad del usuario y que el recurso le pertenezca.
- `GlobalExceptionHandler`: devuelve errores JSON consistentes.
- `LimpiezaCarritos`: tarea sencilla que libera reservas abandonadas.

Se corrigió el registro que antes creaba solo `Usuario` y no `Paciente`, la creación de médicos/pacientes sin rol, los filtros de turnos que se ignoraban al combinarse, la pérdida del detalle del carrito después de confirmar y la falta de control de propiedad de los recursos. No se permiten modificaciones de estado arbitrarias desde el JSON.

## Pruebas

```bash
sh mvnw test
```

Usan una base H2 en memoria separada de los datos locales/MySQL. Incluyen registro/login, alta de médico, filtros, alta/modificación/baja de horarios, confirmación y cancelación, quitar/vaciar/cancelar carrito, vencimiento, acceso ajeno, JWT inválido, CORS, rollback de una confirmación y solicitudes simultáneas sobre el mismo turno/carrito.

## Usar MySQL (opcional)

Se conservó `docker-compose.yml` y la base `ecommerce_db3` en el puerto 3307. Con Docker Desktop funcionando:

```bash
docker compose up -d
```

Después definí `JWT_SECRET` (al menos 32 bytes) y ejecutá:

```bash
sh mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Podés cambiar la conexión con `DB_URL`, `DB_USER` y `DB_PASSWORD`. En una base nueva, definí también `ADMIN_EMAIL` y `ADMIN_PASSWORD` (mínimo 8 caracteres): se crea un administrador únicamente si todavía no hay uno. No se crean cuentas ni horarios demo en MySQL. Guardá estos valores en tu entorno local; no los subas a GitHub.

`ddl-auto=update` conserva los datos existentes y agrega los campos nuevos. No convierte usuarios antiguos creados incorrectamente como `Usuario` en pacientes: esas cuentas no poseen DNI/fila de paciente y deben revisarse antes de usar el carrito. Los turnos viejos sin precio se muestran con importe cero; un administrador debe asignar el valor correcto a los disponibles antes de ofrecerlos.

El perfil local es la opción probada para trabajar ahora. La ejecución contra MySQL requiere su servidor activo; no se borraron ni migraron sus datos.
