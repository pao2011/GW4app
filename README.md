# GW4App – Webhooks para Galaxy Watch 4 (Wear OS)

App independiente para Wear OS que guarda una lista de webhooks y los envía con un toque.
Pensada para disparar macros de **MacroDroid** mediante su URL de webhook.

## Uso
1. En MacroDroid crea una macro con el disparador **Webhook (URL)** y copia el enlace
   (`https://trigger.macrodroid.com/<id-dispositivo>/<identificador>`).
2. En el reloj abre la app → **+ Añadir** → escribe nombre y URL (teclado del reloj, o dictado) → **Guardar**.
3. Toca el botón del webhook para enviarlo. Muestra `✓ 200` si salió bien o `✗` si falló.


Soporta GET y POST. Toca un webhook para enviarlo (vibra al terminar); mantén pulsado para editarlo o borrarlo.

## Compilar
Requiere Android Studio (o Android SDK + JDK 17) y abrir la carpeta como proyecto.

```
gradle :app:assembleDebug        # o ./gradlew si generas el wrapper con `gradle wrapper`
adb connect <ip-del-reloj>:<puerto>
adb install app/build/outputs/apk/debug/app-debug.apk
```
(Activa Opciones de desarrollador → Depuración por Wi‑Fi en el reloj.)
