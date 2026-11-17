# Diagramas de secuencia – Primer avance

Pegue cada bloque en https://mermaid.live para verlo y exportarlo como PNG o SVG.

## 4.1 Inicio de sesión (RF-01, RF-03)

```mermaid
sequenceDiagram
    title Inicio de sesión (RF-01, RF-03)
    actor U as Usuario
    participant N as Navegador
    participant C as Spring Security / LoginController
    participant S as UserDetailsServiceImp
    participant D as UserDao
    participant B as BD PostgreSQL
    U->>N: Escribe correo y contraseña
    N->>+C: POST /login (email, clave)
    C->>+S: loadUserByUsername(email)
    S->>+D: findByEmail(email)
    D->>+B: SELECT * FROM usuarios u JOIN roles r ON u.id_rol = r.id_rol WHERE u.email = ?
    B-->>-D: fila del usuario o vacío
    D-->>-S: Optional<UserEntity>
    alt Usuario no existe o estado_usu = false
        S-->>C: UsernameNotFoundException
        C-->>N: redirect:/login?error
        N-->>U: "Correo o contraseña incorrectos"
    else Usuario activo
        S-->>-C: UserDetails (email, hash BCrypt, rol)
        C->>C: Compara la clave con el hash BCrypt
        alt Clave incorrecta
            C-->>N: redirect:/login?error
            N-->>U: "Correo o contraseña incorrectos"
        else Clave correcta
            C->>C: Crea la sesión con ROLE_ADMINISTRADOR o ROLE_MECANICO
            C-->>-N: redirect:/
            N-->>U: Menú según el rol
        end
    end
```

## 4.2 Registrar un vehículo (RF-12)

```mermaid
sequenceDiagram
    title Registrar un vehículo (RF-12)
    actor A as Administrador
    participant N as Navegador
    participant C as VehiculoController
    participant S as VehiculoServiceImp
    participant D as VehiculoDao / ModeloDao
    participant B as BD PostgreSQL
    A->>N: Llena el formulario del vehículo
    N->>+C: POST /vehiculos (placa, colorPlaca, modelo, datos del dueño)
    C->>C: @Valid VehiculoEntity (Bean Validation)
    alt Campos vacíos o color fuera del catálogo
        C-->>N: vista vehiculo/form con errores junto al campo
    else Datos completos
        C->>+S: save(vehiculo)
        S->>S: validarFormatoPlaca(placa): mayúsculas y ^[A-Z]{3}-[0-9]{3}$
        alt Formato inválido
            S-->>C: PlacaInvalidaException
            C-->>N: vista vehiculo/form, error en el campo placa
        else Formato válido
            S->>+D: existsByPlaca(placa)
            D->>+B: SELECT COUNT(*) FROM vehiculos WHERE placa = ?
            B-->>-D: resultado
            D-->>-S: true / false
            alt Placa repetida
                S-->>C: PlacaDuplicadaException
                C-->>N: vista vehiculo/form, "La placa ya está registrada"
            else Placa nueva
                S->>+D: modeloDao.findById(idModelo)
                D->>+B: SELECT * FROM modelos WHERE id_modelo = ?
                B-->>-D: modelo
                D-->>-S: ModeloEntity activo
                S->>+D: vehiculoDao.save(vehiculo)
                D->>+B: INSERT INTO vehiculos (...) estado_vehiculo = true
                B-->>-D: id_vehiculo
                D-->>-S: VehiculoEntity
                S-->>-C: ok
                C-->>-N: redirect:/vehiculos, "Vehículo creado correctamente"
            end
        end
    end
```

## 4.3 Registrar un repuesto (RF-16)

```mermaid
sequenceDiagram
    title Registrar un repuesto (RF-16)
    actor A as Administrador
    participant N as Navegador
    participant C as RepuestoController
    participant S as RepuestoServiceImp
    participant D as RepuestoDao / ModeloDao
    participant B as BD PostgreSQL
    A->>N: Llena el formulario y marca los modelos compatibles
    N->>+C: POST /repuestos (nombre, precio, codigoEan, stock, imagen, modelos[])
    C->>C: @Valid RepuestoEntity
    alt EAN sin 13 dígitos, precio <= 0, stock < 0 o sin modelos
        C-->>N: vista repuesto/form con errores junto al campo
    else Datos válidos
        C->>+S: save(repuesto)
        S->>S: validarReglas(repuesto): RN-05 y RN-06
        S->>+D: existsByCodigoEan(codigoEan)
        D->>+B: SELECT COUNT(*) FROM repuestos WHERE codigo_ean = ?
        B-->>-D: resultado
        D-->>-S: true / false
        alt EAN repetido
            S-->>C: EanDuplicadoException
            C-->>N: vista repuesto/form, "Ya existe un repuesto con ese EAN"
        else EAN nuevo
            loop Por cada modelo seleccionado
                S->>+D: modeloDao.findById(idModelo)
                D->>+B: SELECT * FROM modelos WHERE id_modelo = ?
                B-->>-D: modelo
                D-->>-S: ModeloEntity
            end
            S->>+D: repuestoDao.save(repuesto)
            D->>+B: INSERT INTO repuestos (...)
            D->>B: INSERT INTO repuestos_modelos (id_repuesto, id_modelo) por cada modelo
            B-->>-D: id_repuesto
            D-->>-S: RepuestoEntity
            S-->>-C: ok
            C-->>-N: redirect:/repuestos, "Repuesto creado correctamente"
        end
    end
```

## 4.4 Registrar una reparación (RF-20)

```mermaid
sequenceDiagram
    title Registrar una reparación (RF-20)
    actor M as Mecánico
    participant N as Navegador
    participant C as ReparacionController
    participant S as ReparacionServiceImp
    participant D as VehiculoDao / UserDao / ReparacionDao
    participant B as BD PostgreSQL
    M->>N: Selecciona vehículo, tipo, fecha y observaciones
    N->>+C: POST /reparaciones
    C->>C: @Valid ReparacionEntity (tipo del enum TipoReparacion)
    C->>+S: save(reparacion)
    S->>+D: vehiculoDao.findById(idVehiculo)
    D->>+B: SELECT * FROM vehiculos WHERE id_vehiculo = ?
    B-->>-D: vehículo
    D-->>-S: VehiculoEntity
    alt Vehículo inexistente o estado_vehiculo = false
        S-->>C: ReparacionInvalidaException
        C-->>N: vista reparacion/form, "El vehículo está dado de baja"
    else Vehículo activo
        S->>+D: userDao.findById(idUsuario)
        D->>+B: SELECT * FROM usuarios u JOIN roles r ON u.id_rol = r.id_rol WHERE u.id_usuario = ?
        B-->>-D: usuario y rol
        D-->>-S: UserEntity
        alt No tiene rol MECANICO o está inactivo
            S-->>C: ReparacionInvalidaException
            C-->>N: vista reparacion/form, "No es un mecánico activo"
        else Mecánico válido
            S->>S: setEstado(EN_PROCESO)
            S->>+D: reparacionDao.save(reparacion)
            D->>+B: INSERT INTO reparaciones (..., estado = 'EN_PROCESO')
            B-->>-D: id_reparacion
            D-->>-S: ReparacionEntity
            S-->>-C: ok
            C-->>-N: redirect:/reparaciones, "Reparación registrada"
        end
    end
```

## 4.5 Buscar repuestos compatibles con un modelo (RF-24)

```mermaid
sequenceDiagram
    title Buscar repuestos compatibles con un modelo (RF-24)
    actor U as Usuario
    participant N as Navegador
    participant C as BusquedaController
    participant S as RepuestoServiceImp
    participant D as RepuestoDao
    participant B as BD PostgreSQL
    U->>N: Selecciona un modelo de la lista
    N->>+C: GET /busquedas/repuestos?idModelo=5
    C->>+S: findCompatibles(idModelo)
    S->>+D: findByModelos_IdModeloAndEstadoRepuestoTrue(idModelo)
    D->>+B: SELECT r.* FROM repuestos r JOIN repuestos_modelos rm ON r.id_repuesto = rm.id_repuesto WHERE rm.id_modelo = ? AND r.estado_repuesto = true
    B-->>-D: filas
    D-->>-S: List<RepuestoEntity>
    S-->>-C: lista
    alt Lista vacía
        C-->>N: vista busqueda/repuestos, "No hay repuestos compatibles con este modelo"
    else Hay resultados
        C-->>-N: vista busqueda/repuestos con nombre, EAN, precio y stock
    end
```

## 4.6 Buscar modelo por placa (RF-25)

```mermaid
sequenceDiagram
    title Buscar modelo por placa (RF-25)
    actor U as Usuario
    participant N as Navegador
    participant C as BusquedaController
    participant S as VehiculoServiceImp
    participant D as VehiculoDao
    participant B as BD PostgreSQL
    U->>N: Escribe la placa
    N->>+C: GET /busquedas/placa?placa=abc-123
    C->>+S: findByPlaca(placa)
    S->>S: validarFormatoPlaca: "abc-123" a "ABC-123"
    alt Formato inválido
        S-->>C: PlacaInvalidaException (no se consulta la BD)
        C-->>N: vista busqueda/placa, "Formato de placa inválido. Use ABC-123"
    else Formato válido
        S->>+D: findByPlaca("ABC-123")
        D->>+B: SELECT v.*, m.* FROM vehiculos v JOIN modelos m ON v.id_modelo = m.id_modelo WHERE v.placa = ?
        B-->>-D: fila o vacío
        D-->>-S: Optional<VehiculoEntity>
        S-->>-C: VehiculoEntity o null
        alt null
            C-->>N: vista busqueda/placa, "Placa no registrada"
        else Encontrado
            C-->>-N: vista busqueda/placa con modelo, marca y color de placa
        end
    end
```

## 4.7 Funcionalidad de negocio: internacionalización (RF-26, RF-29)

```mermaid
sequenceDiagram
    title Cambiar el idioma y ver mensajes traducidos (RF-26, RF-29)
    actor U as Usuario
    participant N as Navegador
    participant I as LocaleChangeInterceptor / CookieLocaleResolver
    participant C as ModeloController
    participant S as ModeloServiceImp
    participant D as ModeloDao
    participant B as BD PostgreSQL
    participant MS as MessageSource (messages_es / messages_en)
    U->>N: Pulsa "English" en el selector de idioma
    N->>+I: GET /modelos?lang=en
    I->>I: setLocale(en) y guarda la cookie "idioma=en"
    I->>+C: listar(model)
    C->>+S: findAll()
    S->>+D: findAll()
    D->>+B: SELECT * FROM modelos
    B-->>-D: filas
    D-->>-S: List<ModeloEntity>
    S-->>-C: lista de modelos
    C->>C: model.addAttribute("title", "modelo.titulo.lista")
    C-->>-I: vista modelo/list
    I->>+MS: Resuelve #{modelo.titulo.lista}, #{modelo.marca}... con locale en
    MS-->>-I: "Models", "Brand"...
    I-->>-N: HTML en inglés (Set-Cookie: idioma=en)
    U->>N: Envía el formulario de modelo con la marca vacía
    N->>+I: POST /modelos (cookie idioma=en)
    I->>I: resolveLocale: en
    I->>+C: crear(@Valid modelo)
    C->>+MS: Interpola {modelo.marca.obligatoria} con locale en
    MS-->>-C: "Brand is required"
    alt Hay errores de validación
        C-->>I: vista modelo/form con el error junto al campo
        I-->>N: Formulario con "Brand is required"
    else Datos válidos
        C->>+S: save(modelo)
        S->>+D: existsByMarcaIgnoreCaseAndNombreModeloIgnoreCase(marca, nombre)
        D->>+B: SELECT COUNT(*) FROM modelos WHERE lower(marca) = ? AND lower(nombre_modelo) = ?
        B-->>-D: 0
        D-->>-S: false
        S->>+D: save(modelo)
        D->>+B: INSERT INTO modelos (...)
        B-->>-D: id_modelo
        D-->>-S: ModeloEntity
        S-->>-C: ok
        C-->>-I: redirect:/modelos con la clave "modelo.creado"
        I-->>-N: Muestra "Model created successfully"
    end
```
