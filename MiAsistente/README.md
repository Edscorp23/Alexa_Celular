# Mi Asistente 0.3 — Android

Nuevo proyecto independiente de Amazon Alexa. No accede a las notificaciones de otras aplicaciones, no tiene Spotify, y **no declara permiso RECORD_AUDIO**: al tocar «Hablar» delega el reconocimiento en el servicio de voz que el usuario tenga instalado. Ese servicio podría requerir Internet y procesar audio de forma remota.

## Funciones
- Voz en español (TextToSpeech de Android).
- Comando por botón o texto: `Recuérdame tomar agua en 10 minutos`, `Recuérdame llamar a mamá a las 19:30`, `Qué recordatorios tengo`.
- Al llegar una alarma, notificación y lectura hablada (dependiente de restricciones de batería del fabricante).
- El botón `Ya lo hice` en la notificación detiene la repetición aproximada cada 2 minutos.
- Los recordatorios se conservan al reiniciar y se reprograman al encender el teléfono.
- Imagen facilitada por el usuario como fondo decorativo en la pantalla principal.

## Limitaciones importantes
- NO tiene escucha permanente ni palabra de activación «Alexa Voice» con pantalla bloqueada. La voz se activa pulsando el botón.
- AlarmManager.setAndAllowWhileIdle es **inexacto** y Android puede retrasar avisos y repeticiones.
- El TTS iniciado desde un receptor de alarma puede ser interrumpido por Android; la notificación permanece como respaldo.
- No se ha compilado ni probado en dispositivo físico. Este ZIP contiene código fuente, no un APK.
- Los recordatorios son internos de la nueva app, NO se sincronizan con Amazon Alexa.
- Google Play Protect puede inspeccionar cualquier APK externo. No lo desactives.

## Compilar online
Crea un repositorio NUEVO y sube el **contenido de `MiAsistente/` a la raíz** del repositorio, incluida `.github`. GitHub Actions genera el artefacto `MiAsistente-debug-apk`. No mezclar con el proyecto antiguo AlexaVoice.


## Novedades v0.2

- Cada tarjeta de recordatorio muestra la cuenta atrás (MM:SS; HH:MM:SS cuando corresponda).
- Botón **Recordar más tarde (5 min)** en la pantalla y en la notificación.
- Botón **Ya lo hice** en ambas ubicaciones.
- Posponer mueve la siguiente alarma y actualiza inmediatamente el reloj.
- Cuando suena un recordatorio, se programa el siguiente aviso en aproximadamente dos minutos, hasta confirmarlo.
- Android puede retrasar las alarmas al entrar en modo reposo; el contador indica el tiempo solicitado, no asegura una entrega exacta.
- El reloj solo se actualiza visualmente con la app abierta. Al bloquear la pantalla, la alarma continúa programada.
- No requiere acceso a notificaciones de otras aplicaciones.

### Instalar desde GitHub
Sube el **contenido interior** de la carpeta `MiAsistente` a la raíz de un repositorio nuevo; asegúrate de que `.github/workflows/build-apk.yml` esté en la raíz. Entra en Actions y descarga el artefacto si la compilación termina correctamente.

## Novedades 0.3: fecha y hora obligatorias
- El botón **Crear recordatorio: elegir fecha y hora** requiere descripción, muestra calendario y selector de hora de 24 horas, y solicita confirmación final.
- Decir «Recuérdame comprar pan» también abre el selector; decir «en 10 minutos» o «a las 18:30» solo propone un horario, que debes confirmar antes de guardar.
- Impide guardar recordatorios con fechas pasadas y muestra fecha/hora de los pendientes.
- Se conservan **Recordar más tarde (5 min)**, **Ya lo hice** y el contador visible.
- La aplicación usa la zona horaria local del teléfono. Los avisos con pantalla apagada dependen de restricciones de Android.
- Proyecto sin compilar ni probar en dispositivo físico.
