import Listado from "./components/Listado.jsx";
import { Link, Route, Routes } from "react-router-dom";
import DetalleConsulta from "./components/DetalleConsulta.jsx";

function Inicio() {
  return (
    <>
      <section className="introduccion" aria-labelledby="titulo">
        <p className="etiqueta">NUESTRAS ESPECIALIDADES</p>
        <h1 id="titulo">Tu salud, en buenas manos.</h1>
        <p className="descripcion-portada">
          Conocé las consultas disponibles y encontrá la atención que necesitás.
        </p>
      </section>
      <Listado />
    </>
  );
}

function PaginaNoEncontrada() {
  return (
    <section className="detalle-consulta">
      <h1>Página no encontrada</h1>
      <p>La dirección que buscás no existe.</p>
      <Link className="enlace-volver" to="/">Volver al inicio</Link>
    </section>
  );
}

export default function App() {
  return (
    <>
      <header className="encabezado">
        <div className="contenedor marca">
          <span className="marca-icono" aria-hidden="true">+</span>
          <Link className="marca-enlace" to="/">Turnero <strong>Med</strong></Link>
          <span className="marca-detalle">Atención médica</span>
        </div>
      </header>

      <main className="contenedor">
        <Routes>
          <Route path="/" element={<Inicio />} />
          <Route path="/consultas/:id" element={<DetalleConsulta />} />
          <Route path="*" element={<PaginaNoEncontrada />} />
        </Routes>
      </main>

      <footer className="contenedor pie">
        <span>Turnero Med</span>
      </footer>
    </>
  );
}
