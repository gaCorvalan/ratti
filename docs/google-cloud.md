# Google Cloud para Ratti

Ratti entra con tu cuenta de Google y, más adelante, lee la planilla. Esta guía es para crear el proyecto en Google Cloud una sola vez. No hace falta publicar la app ni pasar la revisión de Google: queda en modo prueba y solo entra tu cuenta.

No pegues contraseñas ni el secreto del cliente en el repo. El único valor que va en tu máquina es el ID de cliente web, en `local.properties`. Ese archivo no se sube a git.

Si la consola está en inglés, entre paréntesis está el nombre del botón.

## 1. Crear el proyecto

1. Abrí https://console.cloud.google.com/ con tu cuenta de Google.
2. Arriba, al lado de "Google Cloud", hacé clic en el selector de proyectos.
3. **Proyecto nuevo** (New project).
4. Nombre: `Ratti`. Si pregunta la organización, dejala como está, o "Sin organización".
5. **Crear** (Create).
6. Cuando termine, elegí ese proyecto. Todo lo que sigue es dentro de Ratti.

## 2. Activar la API de planillas

1. Menú ☰ → **APIs y servicios** → **Biblioteca** (APIs & Services → Library).
2. Buscá `Google Sheets API`.
3. Entrá y hacé clic en **Habilitar** (Enable).

## 3. Pantalla de consentimiento, en modo prueba

1. Menú ☰ → **Google Auth Platform**. Si no aparece, usá **APIs y servicios** → **Pantalla de consentimiento de OAuth** (OAuth consent screen).
2. Si dice que no está configurada, **Empezar** (Get started).
3. Completá el asistente:
   - Nombre de la app: `Ratti`. Es el nombre que vas a ver cuando Google pida permiso.
   - Correo de asistencia: tu mail.
   - Público: **Externo** (External). Sirve para una cuenta de Gmail común. La app queda en modo prueba.
   - Correo de contacto: tu mail.
   - Aceptá la política de datos de Google y **Crear** (Create).
4. Entrá a **Público** o **Audiencia** (Audience).
5. El estado tiene que decir **Prueba** (Testing). No lo pases a producción.
6. En **Usuarios de prueba** (Test users), **Agregar usuarios** (Add users), escribí tu mail y guardá. Si tu mail no está en esa lista, Google no deja leer las planillas.

## 4. Permiso para ver planillas

1. En Google Auth Platform, entrá a **Acceso a los datos** (Data Access).
2. **Agregar o quitar permisos** (Add or remove scopes).
3. Agregá este permiso. Solo deja ver planillas, no editarlas:

   `https://www.googleapis.com/auth/spreadsheets.readonly`

   Si no está en la lista, buscá "Google Sheets API", o pegalo en **Agregar permisos manualmente** (Manually add scopes).
4. Si no están, agregá también los de la cuenta, para que la app pueda mostrar tu mail:
   - `openid`
   - `https://www.googleapis.com/auth/userinfo.email`
   - `https://www.googleapis.com/auth/userinfo.profile`
5. **Actualizar** o **Guardar** (Update / Save).

## 5. Cliente Android

Esto le dice a Google que la app instalada desde tu Mac es Ratti. No se copia ningún valor al repo.

1. En Google Auth Platform, entrá a **Clientes** (Clients).
2. **Crear cliente** (Create client).
3. Tipo: **Android**.
4. Nombre: `Ratti Android`.
5. Nombre del paquete: `com.gacorvalan.ratti`
6. Huella SHA-1: la de tu Mac, con el comando de abajo.
7. **Crear**.

### SHA-1 de debug en Mac

En la Terminal:

```
keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android -keypass android
```

En el texto, copiá la línea **SHA1**. Tiene esta forma:

```
SHA1: AB:CD:EF:01:23:45:67:89:AB:CD:EF:01:23:45:67:89:AB:CD:EF:01
```

Pegá el valor con los dos puntos. No uses la línea SHA256.

Si la Terminal dice que no encuentra `keytool`, usá el que trae Android Studio:

```
"/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool" -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android -keypass android
```

La clave de debug es distinta en cada Mac. Si más adelante compilás en otra máquina, sacá el SHA-1 de esa máquina y agregalo a este cliente, o creá otro cliente Android con el mismo paquete.

## 6. Cliente web

El botón "Entrar con Google" necesita este ID. No es una contraseña. Igual no lo subas a git, así el repo no queda atado a tu proyecto.

1. Otra vez **Crear cliente**.
2. Tipo: **Aplicación web** (Web application).
3. Nombre: `Ratti Web`.
4. Dejá vacíos "Orígenes autorizados de JavaScript" y "URI de redireccionamiento autorizados".
5. **Crear**.
6. Copiá el **ID de cliente** (Client ID). Termina en `.apps.googleusercontent.com`.
7. Ignorá el **secreto del cliente** (Client secret). No lo pegues en el proyecto, ni en `local.properties`, ni en un chat.

## 7. Dónde va cada valor

| Valor | Dónde va |
| --- | --- |
| Paquete `com.gacorvalan.ratti` | Solo en el cliente Android de Cloud Console |
| SHA-1 | Solo en el cliente Android de Cloud Console |
| ID de cliente web | En `local.properties`, en tu máquina |
| Secreto del cliente | En ningún lado |

`local.properties` está al lado de `settings.gradle.kts` y ya está en `.gitignore`. Si no existe, crealo. Agregá esta línea, con tu ID en lugar del ejemplo:

```
GOOGLE_WEB_CLIENT_ID=123456789012-abcdefghijklmnop.apps.googleusercontent.com
```

Volvé a correr la app desde Android Studio, o con `./gradlew installDebug`. Gradle lee ese archivo al compilar. Si cambiás el ID, hay que compilar de nuevo.

## 8. Probarlo en el teléfono

1. Instalá la app.
2. En Inicio, tocá **Entrar con Google** y elegí la misma cuenta que agregaste como usuario de prueba.
3. Google puede avisar que la app no está verificada. Es normal en modo prueba. Tocá **Avanzado** (Advanced) y después **Ir a Ratti** (Go to Ratti).
4. Aceptá el permiso para ver las planillas.
5. La pantalla muestra tu mail y el botón **Salir**.
6. **Salir** vuelve a mostrar **Entrar con Google**.

Si falta el ID, la app lo dice. Si el paquete o el SHA-1 no coinciden, dice que Google no reconoció la app.

En modo prueba, el permiso de las planillas vence a los 7 días. Cuando pase, tocá **Entrar con Google** de nuevo y aceptalo otra vez.
