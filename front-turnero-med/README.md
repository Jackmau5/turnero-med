# Turnero Med — frontend

Interfaz en React, React Router y Vite para usar el [backend de Turnero Med](../turnero-med-main/README.md). Muestra datos reales de la API: médicos, horarios, reservas y perfiles. No usa un catálogo local de turnos.

## Iniciar el proyecto

Abrí dos terminales de VS Code:

1. En la carpeta `turnero-med-main` (la que contiene `pom.xml`): `sh mvnw spring-boot:run`. Esperá a que el backend esté en `http://localhost:8080`. La prueba rápida es `http://localhost:8080/actuator/health`.
2. En esta carpeta `front-turnero-med` (la que contiene `package.json`): `npm install` si todavía faltan dependencias y luego `npm run dev`.
3. Abrí `http://127.0.0.1:5173` en el navegador. Para detener cada servidor, Ctrl+C en su terminal.

Vite reenvía las solicitudes que empiezan con `/api` al backend en `127.0.0.1:8080` desde `vite.config.js`. Si el backend está en otra dirección, definí `VITE_API_URL` con la URL completa de la API antes de iniciar Vite, por ejemplo `VITE_API_URL=http://localhost:8080/api npm run dev`. El backend permite CORS para localhost y 127.0.0.1 en el puerto 5173. `npm run build` genera una versión de producción en `dist/`; para servirla fuera de Vite hay que configurar el servidor para que envíe las rutas de React a `index.html` y haga llegar `/api` al backend.

La base local H2 y los datos demo del backend se explican en su README. Las cuentas disponibles en una base local nueva son `admin@turnero.local` / `AdminDemo123!` y `medico@turnero.local` / `MedicoDemo123!`. Para probar como paciente, usá **Registrarme**: se crea un paciente real y se inicia sesión.

## Recorrido desde la pantalla

1. **Administrador:** ingresar con la cuenta demo, abrir **Médicos** para crear profesionales y **Agenda** para publicar o dar de baja horarios con fecha, hora, consultorio y precio.
2. **Médico:** ingresar con la cuenta demo y abrir **Agenda**. Puede publicar y administrar sus propios horarios; también ve las reservas de su agenda.
3. **Visitante:** en **Horarios** ve los disponibles que vienen del backend. Puede filtrar por especialidad, médico y día; cada tarjeta abre `/consultas/:id` con el detalle de un horario real.
4. **Paciente:** registrarse o ingresar, abrir el horario y tocar **Agregar al carrito**. Si intenta reservar sin sesión, se lo lleva al login y luego vuelve al detalle.
5. En **Carrito** ve el vencimiento, los turnos y el total calculados por el servidor. Puede quitar un turno, vaciar o cancelar el carrito, o confirmar la reserva.
6. Después de confirmar, **Mis turnos** muestra la reserva. Puede cancelar una cita futura; queda en el historial como cancelada y el backend vuelve a ofrecer el horario con otro ID.
7. En **Mi perfil** el paciente puede cambiar sus datos. Después de guardar vuelve a iniciar sesión.

Los horarios de prueba creados inicialmente por el backend son para el día posterior a la primera ejecución. Cuando esa fecha pasa, un médico o el administrador debe publicar horarios futuros nuevos. Confirmar el carrito **reserva turnos; no cobra dinero**.

## Pantallas y rutas

| Ruta | Pantalla | Acceso |
|---|---|---|
| `/` | Horarios y filtros | Todos |
| `/consultas/:id` | Detalle de un horario y botón para reservar | Todos |
| `/ingresar`, `/registro` | Login y alta de paciente | Todos |
| `/carrito` | Ver, modificar y confirmar carrito | Paciente |
| `/mis-turnos` | Reservas propias y cancelación | Paciente |
| `/perfil` | Datos propios | Paciente |
| `/agenda` | Alta, edición, baja y consulta de horarios | Médico o administrador |
| `/admin/medicos` | Alta, edición y baja de médicos | Administrador |
| `/admin/pacientes` | Alta, edición y baja de pacientes | Administrador |

`BrowserRouter` está en `src/main.jsx`; `Routes`, `Route`, `Link` y `NavLink` están en `src/App.jsx`. El detalle lee el ID de la URL con `useParams`. Tras login o reserva se navega con `useNavigate`. `RutaProtegida.jsx` limita las pantallas por rol; el backend vuelve a verificar permisos en cada solicitud, que es el control efectivo.

## Cómo se conecta al backend

- `src/lib/api.js` centraliza `fetch`, JSON, token `Bearer` y mensajes de error. Las páginas llaman a `/api/auth`, `/api/medicos`, `/api/turnos`, `/api/carritos` y `/api/pacientes`. La lista exacta de operaciones y las reglas de cada estado están en el [README del backend](../turnero-med-main/README.md).
- `src/context/AuthContext.jsx` guarda en `localStorage` el usuario, rol y JWT del login. El JWT se quita al salir o si vence. En el servidor se comprueba otra vez. No se guarda la contraseña.
- `Listado.jsx` pide los turnos disponibles y `Tarjeta.jsx` usa `.map()` para mostrar una tarjeta por horario. `DetalleConsulta.jsx` agrega el turno al carrito mediante la API.
- `Carrito.jsx` usa los importes, estado y fecha de vencimiento devueltos por el backend; `MisTurnos.jsx` obtiene el historial propio y envía la cancelación.
- `Agenda.jsx`, `MedicosAdmin.jsx` y `PacientesAdmin.jsx` presentan formularios simples para gestionar los datos permitidos al rol.
- Cada pantalla muestra estados de carga, confirmación y errores. Si el backend está apagado, aparece un mensaje de conexión. Un horario ocupado, un carrito vencido o un formulario inválido muestra el error enviado por la API.

El archivo de datos de ejemplo `products.json` se retiró porque sus IDs no eran los IDs de los turnos de la base. El backend conserva los datos entre reinicios en su H2 local; el frontend solo conserva la sesión en el navegador.
