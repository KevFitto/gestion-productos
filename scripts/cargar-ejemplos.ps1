param([string]$BaseUrl = 'http://localhost:8080')
$ErrorActionPreference = 'Stop'
$api = "$BaseUrl/api"
function Guardar-SiFalta($recurso, $campo, $datos) {
    $actuales = @(Invoke-RestMethod "$api/$recurso")
    $existente = $actuales | Where-Object { $_.$campo -eq $datos[$campo] } | Select-Object -First 1
    if ($existente) { return $existente }
    return Invoke-RestMethod "$api/$recurso" -Method Post -ContentType 'application/json; charset=utf-8' -Body ($datos | ConvertTo-Json -Depth 5)
}
$computadoras = Guardar-SiFalta 'categorias' 'nombre' @{nombre='Computadoras';activa=$true}
$accesorios = Guardar-SiFalta 'categorias' 'nombre' @{nombre='Accesorios';activa=$true}
$null = Guardar-SiFalta 'categorias' 'nombre' @{nombre='Monitores';activa=$true}
$tech = Guardar-SiFalta 'proveedores' 'nombre' @{nombre='Distribuidora Tech';telefono='2222-1000';correo='ventas@tech.example';activo=$true}
$digital = Guardar-SiFalta 'proveedores' 'nombre' @{nombre='Suministros Digitales';telefono='2222-2000';correo='ventas@digital.example';activo=$true}
$null = Guardar-SiFalta 'productos' 'codigo' @{codigo='LAP-001';nombre='Laptop Lenovo';categoria=@{id=$computadoras.id};proveedor=@{id=$tech.id};precioVenta=850;existencia=10;descripcion='Laptop para oficina'}
$null = Guardar-SiFalta 'productos' 'codigo' @{codigo='MOU-001';nombre='Mouse USB';categoria=@{id=$accesorios.id};proveedor=@{id=$digital.id};precioVenta=20;existencia=25;descripcion='Mouse para computadora'}
$evidencias = Join-Path $PSScriptRoot '../docs/evidencias'
New-Item -ItemType Directory -Force $evidencias | Out-Null
foreach ($recurso in @('categorias','proveedores','productos')) {
    $respuesta = Invoke-WebRequest "$api/$recurso"
    $respuesta.Content | Set-Content -Encoding utf8 (Join-Path $evidencias "$recurso.json")
    Write-Output "$recurso HTTP $($respuesta.StatusCode)"
}
