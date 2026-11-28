"""
Descarga las 4 fotos reales del zancudo común (Culex quinquefasciatus) del
CDC (Public Health Image Library, dominio público) y las deja en la carpeta
de imágenes de la app, con los nombres que la app busca.

Uso (desde la carpeta de tu proyecto, la que contiene "app"):
    python descargar_fotos_culex.py

Si tu proyecto está en otro lugar:
    python descargar_fotos_culex.py "C:\\ruta\\a\\tu\\proyecto"

No necesita instalar nada. Después: Build > Rebuild Project y ejecutar la app.
Créditos de las fotos: CDC, Public Health Image Library (phil.cdc.gov);
Lauren Bishop y Amy E. Lockwood.
"""
import hashlib
import os
import sys
import urllib.request

FOTOS = [
    ("26026", "zancudo_01_reposo.jpg"),
    ("26027", "zancudo_02_flor.jpg"),
    ("26025", "zancudo_03_picando.jpg"),
    ("26028", "zancudo_04_alas.jpg"),
]
URL = "https://wwwn.cdc.gov/phil/PHIL_Images/{0}/{0}_lores.jpg"

raiz = sys.argv[1] if len(sys.argv) > 1 else "."
destino = os.path.join(raiz, "app", "src", "main", "res", "drawable-nodpi")
if not os.path.isdir(destino):
    print("No encuentro la carpeta:", destino)
    print("Ejecuta el script dentro de la carpeta de tu proyecto (donde está 'app').")
    sys.exit(1)

hashes = {}
for pid, nombre in FOTOS:
    url = URL.format(pid)
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        datos = urllib.request.urlopen(req, timeout=60).read()
    except Exception as e:
        print("FALLÓ", nombre, "->", e)
        print("   Descárgala a mano de:", "https://wwwn.cdc.gov/phil/Details.aspx?pid=" + pid)
        continue
    if len(datos) < 5000 or datos[:2] != b"\xff\xd8":
        print("FALLÓ", nombre, "-> el archivo no parece una foto JPG")
        continue
    h = hashlib.md5(datos).hexdigest()
    if h in hashes:
        print("AVISO:", nombre, "es igual a", hashes[h], "(foto repetida, revísala)")
    hashes[h] = nombre
    with open(os.path.join(destino, nombre), "wb") as f:
        f.write(datos)
    print("OK  ", nombre, "(%d KB)" % (len(datos) // 1024))

print("Listo. En Android Studio: Build > Rebuild Project.")
