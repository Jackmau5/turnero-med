import { Link, NavLink, Route, Routes, useNavigate } from "react-router-dom";
import Listado from "./components/Listado.jsx";
import DetalleConsulta from "./components/DetalleConsulta.jsx";
import RutaProtegida from "./components/RutaProtegida.jsx";
import { useAuth } from "./context/AuthContext.jsx";
import Acceso from "./pages/Acceso.jsx";
import Carrito from "./pages/Carrito.jsx";
import MisTurnos from "./pages/MisTurnos.jsx";
import Perfil from "./pages/Perfil.jsx";
import Agenda from "./pages/Agenda.jsx";
import MedicosAdmin from "./pages/MedicosAdmin.jsx";
import PacientesAdmin from "./pages/PacientesAdmin.jsx";

function Inicio() {
  return <>
    <section className="introduccion" aria-labelledby="titulo">
      <p className="etiqueta">TURNERO MÉDICO</p>
      <h1 id="titulo">Tu salud, en buenas manos.</h1>
      <p className="descripcion-portada">Encontrá un horario, agregalo al carrito y confirmá tu consulta en pocos pasos.</p>
    </section>
    <Listado />
  </>;
}

function PaginaNoEncontrada() {
  return <section className="pagina"><h1>Página no encontrada</h1><p>La dirección que buscás no existe.</p><Link to="/">Volver al inicio</Link></section>;
}

function Navegacion() {
  const { usuario, cerrarSesion } = useAuth();
  const navigate = useNavigate();
  const enlace = ({ isActive }) => isActive ? "nav-enlace activo" : "nav-enlace";
  function salir() { cerrarSesion(); navigate("/", { replace: true }); }
  return <header className="encabezado"><div className="contenedor cabecera">
    <Link className="marca-enlace" to="/"><span className="marca-icono" aria-hidden="true">+</span><span>Turnero <strong>Med</strong></span></Link>
    <nav className="navegacion" aria-label="Navegación principal">
      <NavLink className={enlace} to="/" end>Horarios</NavLink>
      {usuario?.rol === "PACIENTE" && <>
        <NavLink className={enlace} to="/carrito">Carrito</NavLink>
        <NavLink className={enlace} to="/mis-turnos">Mis turnos</NavLink>
        <NavLink className={enlace} to="/perfil">Mi perfil</NavLink>
      </>}
      {(usuario?.rol === "MEDICO" || usuario?.rol === "ADMIN") && <NavLink className={enlace} to="/agenda">Agenda</NavLink>}
      {usuario?.rol === "ADMIN" && <>
        <NavLink className={enlace} to="/admin/medicos">Médicos</NavLink>
        <NavLink className={enlace} to="/admin/pacientes">Pacientes</NavLink>
      </>}
      {usuario ? <><span className="nav-usuario">{usuario.nombre}</span><button className="salir" onClick={salir}>Salir</button></> : <>
        <NavLink className={enlace} to="/ingresar">Ingresar</NavLink><Link className="boton boton-chico" to="/registro">Registrarme</Link>
      </>}
    </nav>
  </div></header>;
}

export default function App() {
  return <>
    <Navegacion />
    <main className="contenedor contenido">
      <Routes>
        <Route path="/" element={<Inicio />} />
        <Route path="/consultas/:id" element={<DetalleConsulta />} />
        <Route path="/ingresar" element={<Acceso />} />
        <Route path="/registro" element={<Acceso registro />} />
        <Route element={<RutaProtegida roles={["PACIENTE"]} />}>
          <Route path="/carrito" element={<Carrito />} />
          <Route path="/mis-turnos" element={<MisTurnos />} />
          <Route path="/perfil" element={<Perfil />} />
        </Route>
        <Route element={<RutaProtegida roles={["MEDICO", "ADMIN"]} />}><Route path="/agenda" element={<Agenda />} /></Route>
        <Route element={<RutaProtegida roles={["ADMIN"]} />}>
          <Route path="/admin/medicos" element={<MedicosAdmin />} />
          <Route path="/admin/pacientes" element={<PacientesAdmin />} />
        </Route>
        <Route path="*" element={<PaginaNoEncontrada />} />
      </Routes>
    </main>
    <footer className="contenedor pie"><span>Turnero Med</span><span>Proyecto de Aplicaciones Interactivas</span></footer>
  </>;
}
