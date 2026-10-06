# TomaDiaria

Pastillero digital para Android (Kotlin + Compose): el usuario busca sus medicamentos en la API
pública de CIMA (AEMPS), programa recordatorios de toma, confirma cada toma y lleva su
historial. Los datos del usuario viven en Firebase (Auth + Firestore).

Es un fork de MedicApp, que además recomendaba o desrecomendaba medicamentos según las
alergias del usuario. Aquí eso se ha quitado a propósito: ver "Sin zona gris médica".

## Sin zona gris médica

La app se va a publicar, y un software que da información para tomar decisiones terapéuticas
sobre un paciente es un producto sanitario (MDR 2017/745, regla 11). TomaDiaria tiene que
quedarse **fuera** de esa categoría, así que la regla es:

Una funcionalidad es válida si solo hace estas tres cosas:

1. **Guarda** lo que el usuario escribe, tal cual.
2. **Muestra** información oficial sin tocarla, identificada por el número de registro o el
   código nacional exactos.
3. **Avisa** a las horas que el usuario ha elegido.

Queda fuera todo lo que **cruce datos de salud del usuario con conocimiento sobre fármacos
para sacar una conclusión**, aunque parezca inofensivo:

- Alergias, contraindicaciones o advertencias por perfil (embarazo, edad, alcohol...).
- Interacciones o duplicidades ("ya tomas otro con paracetamol").
- Dosis máximas, "has tomado demasiado" o qué hacer si se olvida una dosis.
- Rellenar una pauta con la posología de la ficha técnica.
- Sugerir medicamentos a partir del motivo o de un síntoma.
- Interpretar la adherencia ("tu tratamiento puede no funcionar").
- Textos que prometan juicio clínico ("recomendable para ti").

Si una propuesta toca cualquiera de estos puntos, no se implementa: se avisa y se pregunta.

## Flujo de trabajo

El repo usa **git flow** (`main` / `develop`, prefijo `feature/`).

Toda mejora empieza con su propia rama, nunca commiteando directo en `develop`:

```powershell
git flow feature start <nombre-descriptivo>
git flow feature finish <nombre-descriptivo>
```

Donde no esté instalado `git flow`, el equivalente es crear `feature/<nombre>` desde `develop`
y al terminar fusionarla con `git merge --no-ff`.

**Una rama por asunto.** Si un trabajo mezcla cosas sin relación, van separadas para poder
revisarlas y revertirlas por su cuenta.

Mensajes de commit **en español**, en imperativo, explicando **por qué** se hace el cambio y
no solo qué se toca. Los comentarios y el KDoc del código, también en español.

## Firebase

TomaDiaria tiene **su propio proyecto Firebase**, separado del de MedicApp: los datos de
usuarios reales no pueden mezclarse con los de uso personal. `app/google-services.json` no se
versiona; la plantilla está en `app/google-services.json.example`.

## Firma de release

Google Play recibe un AAB firmado con la **clave de subida**; la clave con la que se firma lo
que llega a los móviles la guarda Google (Play App Signing).

- La clave está en `~/keystores/tomadiaria-upload.jks`, fuera del repo.
- Sus datos van en `keystore.properties`, en la raíz del repo, que **no se versiona**:

  ```properties
  storeFile=/ruta/a/tomadiaria-upload.jks
  storePassword=...
  keyAlias=upload
  keyPassword=...
  ```

- Sin ese fichero el debug compila igual y el release sale sin firmar.
- El AAB para Play se genera con `gradlew bundleRelease` y queda en
  `app/build/outputs/bundle/release/`.
- Nunca subas la clave ni sus contraseñas al repo, ni las pegues en un commit o una issue.

## Privacidad (RGPD y Google Play)

- La política de privacidad y la página de borrado de cuenta están en `hosting/` y se publican
  en `https://tomadiaria-app.web.app` con `firebase deploy --only hosting`.
- Si la política cambia, se sube su versión **a la vez** en `hosting/privacidad/index.html` y en
  `legal/PrivacyPolicy.kt`: esa versión es la que se guarda con el consentimiento de cada usuario.
- **Si añades una colección nueva bajo `users/{uid}`, añádela a `FirebaseAccountRepository`.**
  Firestore no borra las subcolecciones al borrar un documento, así que si no está en esa lista
  sus datos sobrevivirían al "Eliminar mi cuenta". Lo mismo con cualquier dato nuevo que la app
  guarde en el móvil: el borrado de cuenta tiene que limpiarlo.

## Firestore: filtrar en el servidor

Regla aprendida a base de arreglarlo: **nunca leer una colección entera para responder algo
concreto.** Si hace falta un documento, se escucha ese documento; si hace falta un
subconjunto, se filtra en la consulta, no en el móvil.

Cada documento leído se paga y se sincroniza, así que un `whereEqualTo` de más en la consulta
ahorra lecturas, batería y memoria.

Antes de añadir un método al repositorio, mira si ya existe uno acotado que sirva.

## Estructura

- Sin framework de inyección: las dependencias se montan a mano en `di/AppContainer`, que
  cuelga de `TomaDiariaApp`.
- Los repositorios exponen `Flow`; los ViewModels los pasan a `StateFlow` con
  `stateIn(..., WhileSubscribed(5000), ...)`.
- Interfaz de repositorio + implementación Firestore + doble de test en `src/sharedTest`,
  que comparten los tests unitarios y los instrumentados. **Si cambias una interfaz de
  repositorio, actualiza también su Fake.**

## Comprobar el trabajo

```powershell
.\gradlew.bat testDebugUnitTest
```

Para verificarlo en la app de verdad y no solo con tests, hay un skill del proyecto con la
receta del emulador: `.claude/skills/run-tomadiaria/`.

## Fallo conocido

En el historial aparece a veces **"Sin conexión · mostrando datos guardados"** aun con red.
Es intermitente y previo: `isFromCache` sigue en `true` mientras una escritura recién hecha
no está confirmada por el servidor, y la UI lo pinta como falta de conexión. No lo atribuyas
al cambio que estés probando.
