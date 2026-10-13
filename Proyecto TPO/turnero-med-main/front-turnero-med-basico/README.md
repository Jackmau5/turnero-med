# Front Turnero Med

Catálogo básico en React, adaptado del ejemplo ProductList.jsx y
CardProductos.jsx del profesor. Las consultas corresponden a especialidades
del TPO Turnero Med. Los precios son ficticios para el trabajo práctico.

## Ejecutar

Requiere Node.js compatible con Vite 7 (20.19+ o 22.12+).

```sh
npm install
npm run dev
```

Abrir la dirección que muestra la terminal. Para compilar: `npm run build`.

## Componentes

- `src/components/Listado.jsx`: importa products.json y recorre los datos con
  `.map()`. Usa el id de cada producto como `key` y pasa el producto por props.
- `src/components/Tarjeta.jsx`: recibe la prop producto y muestra nombre,
  descripción y precio.
- `src/data/products.json`: contiene las consultas del catálogo.

Se conserva la idea y estructura del ejemplo del profesor, adaptando los
productos a consultas médicas. Solo utiliza React y CSS, sin backend.
La consigna práctica no requiere agregar OnOff ni children al catálogo.

## Entrega

Subir el contenido de esta carpeta al repositorio del grupo
`front-turnero-med` (sin node_modules ni dist) y mostrarlo funcionando.
