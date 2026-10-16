# GW4App – Webhooks para Galaxy Watch 4 (Wear OS)

App independiente para Wear OS que guarda una lista de webhooks y los envía con un toque.
Pensada para disparar macros de **MacroDroid** mediante su URL de webhook.

## Uso
1. En MacroDroid crea una macro con el disparador **Webhook (URL)** y copia el enlace
   (`https://trigger.macrodroid.com/<id-dispositivo>/<identificador>`).
2. En el reloj abre la app → **+ Añadir** → escribe nombre y URL (teclado del reloj, o dictado) → **Guardar**.
3. Toca el botón del webhook para enviarlo. Muestra `✓ 200` si salió bien o `✗` si falló.


Soporta GET y POST. Toca un webhook para enviarlo (vibra al terminar); mantén pulsado para editarlo o borrarlo.

## Instalar en el reloj (sin compilar)
Cada push compila el APK con GitHub Actions y lo publica en
**Releases → "GW4App (última versión)" → `GW4App.apk`**.

Instalación en el Galaxy Watch 4 (no tiene instalador de archivos propio):
1. Reloj: Ajustes → Acerca del reloj → Software → toca 5 veces "Versión de software" → Opciones de desarrollador → activa **Depuración ADB** y **Depurar por Wi‑Fi**.
2. Anota la IP y puerto (y el código de emparejamiento si lo pide).
3. Desde PC: `adb pair <ip>:<puerto-emparejar>` (si pide código), `adb connect <ip>:<puerto>` y `adb install -r GW4App.apk`.
   Alternativa desde el móvil Android: app **Wear Installer 2** o **Bugjaeger** (elige el APK y se instala por Wi‑Fi).

## Compilar a mano
Android Studio, o `gradle :app:assembleDebug` con Android SDK y JDK 17.
