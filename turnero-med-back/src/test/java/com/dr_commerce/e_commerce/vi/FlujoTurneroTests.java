package com.dr_commerce.e_commerce.vi;

import com.dr_commerce.e_commerce.vi.config.JwtUtil;
import com.dr_commerce.e_commerce.vi.model.*;
import com.dr_commerce.e_commerce.vi.repository.*;
import com.dr_commerce.e_commerce.vi.service.CarritoService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FlujoTurneroTests {
    @Value("${local.server.port}") int port;
    @Autowired UsuarioRepository usuarios;
    @Autowired PacienteRepository pacientes;
    @Autowired MedicoRepository medicos;
    @Autowired TurnoRepository turnos;
    @Autowired CarritoRepository carritos;
    @Autowired CarritoService carritoService;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtil jwt;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private String adminToken;
    private Medico medico;
    private LocalDate dia;

    record Res(int status, JsonNode body, HttpHeaders headers) {}
    record Cuenta(long id, String token) {}

    @BeforeEach
    void preparar() {
        turnos.deleteAll(); carritos.deleteAll(); pacientes.deleteAll(); medicos.deleteAll(); usuarios.deleteAll();
        Usuario admin = new Usuario();
        admin.setNombre("Admin"); admin.setApellido("Test"); admin.setEmail("admin@test.com");
        admin.setPassword(encoder.encode("Password123!")); admin.setRol(Rol.ADMIN);
        usuarios.save(admin); adminToken = jwt.generarToken(admin);
        medico = new Medico();
        medico.setNombre("Ana"); medico.setApellido("Test"); medico.setEmail("medico@test.com");
        medico.setPassword(encoder.encode("Password123!")); medico.setRol(Rol.MEDICO);
        medico.setMatricula("TEST-1"); medico.setEspecialidad(Especialidad.CARDIOLOGIA); medicos.save(medico);
        dia = LocalDate.now().plusDays(3);
    }
    Res request(String method, String path, String token, String body) throws Exception {
        var b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).timeout(Duration.ofSeconds(15));
        if (token != null) b.header("Authorization", "Bearer " + token);
        b.header("Content-Type", "application/json");
        b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        var response = client.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Res(response.statusCode(), response.body().isBlank() ? json.nullNode() : json.readTree(response.body()), response.headers());
    }
    JsonNode ok(int status, Res res) {
        assertEquals(status, res.status(), res.body().toString()); return res.body();
    }
    Cuenta registrar(String name, String dni) throws Exception {
        JsonNode r = ok(201, request("POST", "/api/auth/register", null,
            """
            {"nombre":"%s","apellido":"Test","email":"%s@test.com","password":"Password123!","dni":"%s"}
            """.formatted(name, name, dni)));
        assertEquals("PACIENTE", r.get("rol").asText());
        assertTrue(pacientes.existsById(r.get("id").asLong()));
        return new Cuenta(r.get("id").asLong(), r.get("token").asText());
    }
    String slotBody(long medicoId, LocalDate date, String hora, String precio) {
        return """
            {"medicoId":%d,"fecha":{"dia":"%s","hora":"%s"},"precio":%s,
             "direccion":{"calle":"Prueba","altura":123,"ciudad":"Buenos Aires","codigoPostal":"1000"}}
            """.formatted(medicoId, date, hora, precio);
    }
    long slot(String hora, String precio) throws Exception {
        return ok(201, request("POST", "/api/turnos", adminToken, slotBody(medico.getId(), dia, hora, precio))).get("id").asLong();
    }
    long carrito(Cuenta c) throws Exception {
        return ok(200, request("POST", "/api/carritos", c.token(), null)).get("id").asLong();
    }
    Res agregar(Cuenta c, long cart, long turno) throws Exception {
        return request("POST", "/api/carritos/" + cart + "/turnos/" + turno, c.token(), null);
    }

    @Test
    void flujoCompletoConHistorialYCancelacion() throws Exception {
        Cuenta paciente = registrar("paciente", "30111222");
        ok(401, request("POST", "/api/auth/login", null, "{\"email\":\"paciente@test.com\",\"password\":\"incorrecta\"}"));
        JsonNode login = ok(200, request("POST", "/api/auth/login", null,
                "{\"email\":\"PACIENTE@test.com\",\"password\":\"Password123!\"}"));
        assertEquals(paciente.id(), login.get("id").asLong());
        long t1 = slot("10:00", "25000.50"); long t2 = slot("11:00", "18000");
        JsonNode disponibles = ok(200, request("GET", "/api/turnos?especialidad=CARDIOLOGIA&medicoId=" + medico.getId() + "&dia=" + dia + "&estado=DISPONIBLE", null, null));
        assertEquals(2, disponibles.size());
        assertEquals(0, ok(200, request("GET", "/api/turnos?especialidad=PEDIATRIA&dia=" + dia, null, null)).size());
        long cart = carrito(paciente); assertEquals(cart, carrito(paciente));
        ok(200, agregar(paciente, cart, t1));
        JsonNode conDos = ok(200, agregar(paciente, cart, t2));
        assertEquals(0, new BigDecimal("43000.50").compareTo(conDos.get("total").decimalValue()));
        JsonNode confirmado = ok(200, request("POST", "/api/carritos/" + cart + "/confirmar", paciente.token(), null));
        assertEquals("CONFIRMADO", confirmado.get("estado").asText());
        assertEquals("TOMADO", confirmado.get("turnos").get(0).get("estado").asText());
        assertEquals(2, ok(200, request("GET", "/api/carritos/" + cart, paciente.token(), null)).get("turnos").size());
        assertEquals(2, ok(200, request("GET", "/api/turnos/mis-turnos", paciente.token(), null)).size());
        ok(409, request("POST", "/api/carritos/" + cart + "/confirmar", paciente.token(), null));
        JsonNode cancelado = ok(200, request("POST", "/api/turnos/" + t1 + "/cancelar", paciente.token(), null));
        assertEquals("CANCELADO", cancelado.get("estado").asText());
        JsonNode nuevos = ok(200, request("GET", "/api/turnos", null, null));
        assertEquals(1, nuevos.size()); assertNotEquals(t1, nuevos.get(0).get("id").asLong());
        ok(409, request("POST", "/api/turnos/" + t1 + "/cancelar", paciente.token(), null));
        assertEquals(3, turnos.count());
    }

    @Test
    void permisosTokensYRegistroSeguro() throws Exception {
        ok(400, request("POST", "/api/auth/register", null,
            "{\"nombre\":\"X\",\"apellido\":\"X\",\"email\":\"x@test.com\",\"password\":\"Password123!\",\"dni\":\"30111222\",\"rol\":\"ADMIN\"}"));
        ok(400, request("POST", "/api/auth/register", null, "{}"));
        Cuenta a = registrar("uno", "30111222"); Cuenta b = registrar("dos", "30111223");
        ok(409, request("POST", "/api/auth/register", null,
            "{\"nombre\":\"X\",\"apellido\":\"X\",\"email\":\"UNO@test.com\",\"password\":\"Password123!\",\"dni\":\"30111224\"}"));
        ok(401, request("POST", "/api/carritos", null, null));
        ok(401, request("POST", "/api/carritos", "token-roto", null));
        ok(403, request("POST", "/api/turnos", a.token(), slotBody(medico.getId(), dia, "10:00", "100")));
        long cart = carrito(a); long turno = slot("10:00", "100");
        ok(403, request("GET", "/api/carritos/" + cart, b.token(), null));
        ok(403, agregar(b, cart, turno));
        ok(403, request("POST", "/api/carritos?pacienteId=" + a.id(), b.token(), null));
        ok(403, request("GET", "/api/pacientes/" + a.id(), b.token(), null));
        ok(403, request("GET", "/api/pacientes", a.token(), null));
        ok(200, agregar(a, cart, turno));
        ok(200, request("POST", "/api/carritos/" + cart + "/confirmar", a.token(), null));
        ok(403, request("GET", "/api/turnos/" + turno, b.token(), null));
        ok(403, request("POST", "/api/turnos/" + turno + "/cancelar", b.token(), null));
        ok(403, request("GET", "/api/turnos?pacienteId=" + a.id(), b.token(), null));
        ok(400, request("GET", "/api/turnos?estado=INVENTADO", null, null));
    }

    @Test
    void quitarVaciarCancelarYVencimientoDelCarrito() throws Exception {
        Cuenta c = registrar("paciente", "30111222");
        long cart = carrito(c); long t1 = slot("10:00", "10"); long t2 = slot("11:00", "20");
        ok(409, request("POST", "/api/carritos/" + cart + "/confirmar", c.token(), null));
        ok(200, agregar(c, cart, t1));
        ok(409, agregar(c, cart, t1));
        JsonNode vacio = ok(200, request("DELETE", "/api/carritos/" + cart + "/turnos/" + t1, c.token(), null));
        assertEquals(0, vacio.get("turnos").size());
        ok(200, agregar(c, cart, t1)); ok(200, agregar(c, cart, t2));
        assertEquals(0, ok(200, request("DELETE", "/api/carritos/" + cart + "/turnos", c.token(), null)).get("turnos").size());
        ok(200, agregar(c, cart, t1));
        ok(200, request("DELETE", "/api/carritos/" + cart, c.token(), null));
        assertEquals(EstadoTurno.DISPONIBLE, turnos.findById(t1).orElseThrow().getEstado());
        long nuevo = carrito(c); assertNotEquals(cart, nuevo);
        ok(200, agregar(c, nuevo, t1));
        Carrito vencido = carritos.findById(nuevo).orElseThrow();
        vencido.setVenceEn(LocalDateTime.now().minusMinutes(1)); carritos.save(vencido);
        ok(409, request("POST", "/api/carritos/" + nuevo + "/confirmar", c.token(), null));
        carritoService.liberarVencido(nuevo);
        assertEquals(EstadoCarrito.CANCELADO, carritos.findById(nuevo).orElseThrow().getEstado());
        assertEquals(EstadoTurno.DISPONIBLE, turnos.findById(t1).orElseThrow().getEstado());
    }

    @Test
    void altaModificacionBajaYValidaciones() throws Exception {
        ok(400, request("POST", "/api/turnos", adminToken, "{}"));
        ok(400, request("POST", "/api/turnos", adminToken, slotBody(medico.getId(), dia.minusDays(10), "10:00", "100")));
        ok(400, request("POST", "/api/turnos", adminToken, slotBody(medico.getId(), dia, "10:00", "-1")));
        long t = slot("10:00", "100");
        ok(409, request("POST", "/api/turnos", adminToken, slotBody(medico.getId(), dia, "10:00", "100")));
        ok(200, request("PUT", "/api/turnos/" + t, adminToken, slotBody(medico.getId(), dia, "11:00", "150")));
        ok(204, request("DELETE", "/api/turnos/" + t, adminToken, null));
        assertEquals(EstadoTurno.CANCELADO, turnos.findById(t).orElseThrow().getEstado());
        assertEquals(0, ok(200, request("GET", "/api/turnos", null, null)).size());
        ok(404, request("GET", "/api/turnos/999999", null, null));
    }

    @Test
    void altaDeMedicoYPermisosDeSuAgenda() throws Exception {
        JsonNode m = ok(201, request("POST", "/api/medicos", adminToken, """
          {"nombre":"Luis","apellido":"Test","email":"nuevo@test.com","password":"Password123!",
           "matricula":"TEST-2","especialidad":"PEDIATRIA"}
          """));
        JsonNode login = ok(200, request("POST", "/api/auth/login", null,
                "{\"email\":\"nuevo@test.com\",\"password\":\"Password123!\"}"));
        assertEquals("MEDICO", login.get("rol").asText());
        String token = login.get("token").asText();
        ok(201, request("POST", "/api/turnos", token, slotBody(m.get("id").asLong(), dia, "10:00", "100")));
        ok(403, request("POST", "/api/turnos", token, slotBody(medico.getId(), dia, "10:00", "100")));
        ok(403, request("POST", "/api/carritos", token, null));
    }

    @Test
    void dosPacientesNoPuedenReservarElMismoTurno() throws Exception {
        Cuenta a = registrar("uno", "30111222"); Cuenta b = registrar("dos", "30111223");
        long ca = carrito(a), cb = carrito(b), t = slot("10:00", "100");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            Future<Res> f1 = pool.submit(() -> { inicio.await(); return agregar(a, ca, t); });
            Future<Res> f2 = pool.submit(() -> { inicio.await(); return agregar(b, cb, t); });
            inicio.countDown();
            List<Integer> status = new ArrayList<>(List.of(f1.get().status(), f2.get().status()));
            Collections.sort(status); assertEquals(List.of(200, 409), status);
        } finally { pool.shutdownNow(); }
    }

    @Test
    void mismoPacienteNoPuedeReservarDosMedicosALaMismaHora() throws Exception {
        Cuenta c = registrar("paciente", "30111222"); long cart = carrito(c); long t1 = slot("10:00", "100");
        Medico otro = new Medico(); otro.setNombre("Otro"); otro.setApellido("Test"); otro.setEmail("otro@test.com");
        otro.setPassword(encoder.encode("Password123!")); otro.setRol(Rol.MEDICO); otro.setMatricula("TEST-2");
        otro.setEspecialidad(Especialidad.PEDIATRIA); medicos.save(otro);
        long t2 = ok(201, request("POST", "/api/turnos", adminToken, slotBody(otro.getId(), dia, "10:00", "200"))).get("id").asLong();
        ok(200, agregar(c, cart, t1)); ok(409, agregar(c, cart, t2));
        ok(200, request("POST", "/api/carritos/" + cart + "/confirmar", c.token(), null));
        ok(409, agregar(c, carrito(c), t2));
    }

    @Test
    void confirmacionAtomicaYTurnosOcupadosNoSeEditan() throws Exception {
        Cuenta c = registrar("paciente", "30111222"); long cart = carrito(c);
        long t1 = slot("10:00", "100"), t2 = slot("11:00", "200");
        ok(200, agregar(c, cart, t1)); ok(200, agregar(c, cart, t2));
        ok(409, request("PUT", "/api/turnos/" + t1, adminToken, slotBody(medico.getId(), dia, "12:00", "100")));
        ok(409, request("DELETE", "/api/turnos/" + t1, adminToken, null));
        Turno pasado = turnos.findById(t2).orElseThrow(); pasado.setFecha(new Fecha(LocalDate.now().minusDays(1), LocalTime.NOON)); turnos.save(pasado);
        ok(409, request("POST", "/api/carritos/" + cart + "/confirmar", c.token(), null));
        assertEquals(EstadoTurno.RESERVADO, turnos.findById(t1).orElseThrow().getEstado());
        assertEquals(EstadoCarrito.ABIERTO, carritos.findById(cart).orElseThrow().getEstado());
    }

    @Test
    void solicitudesSimultaneasCreanUnSoloCarritoActivo() throws Exception {
        Cuenta c = registrar("paciente", "30111222");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> carrito(c)); var b = pool.submit(() -> carrito(c));
            assertEquals(a.get(), b.get()); assertEquals(1, carritos.count());
        } finally { pool.shutdownNow(); }
    }

    @Test
    void permitePreflightDelFront() throws Exception {
        var req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/carritos"))
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());
        assertEquals("http://localhost:5173", res.headers().firstValue("access-control-allow-origin").orElse(""));
    }
}
