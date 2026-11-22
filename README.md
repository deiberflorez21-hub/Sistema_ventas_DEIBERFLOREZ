# 🏪 CastilloVentas — Sistema de Ventas e Inventario

<div align="center">

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=java)
![Swing](https://img.shields.io/badge/GUI-Java%20Swing-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)
![Version](https://img.shields.io/badge/Versión-2.0-purple?style=for-the-badge)

**Sistema integral de ventas e inventario con interfaz gráfica moderna en Java Swing.**

</div>

---

## ✨ Características y Módulos (v2.0)

| Módulo | Descripción y Mejoras |
|---|---|
| 📊 **Dashboard** | 8 tarjetas KPI interactivas, gráfica de ventas de los últimos 6 meses con degradados y panel de alertas de stock bajo (≤ 5 unidades). |
| 📦 **Productos** | Gestión completa (CRUD), filtrado dinámico por categoría y diálogos de edición personalizados con validaciones de datos. |
| 👥 **Clientes** | Registro y administración con documento, teléfono y nuevo campo para correo electrónico. |
| 🛒 **Nueva Venta** | Carrito de compras en tiempo real, descuentos seleccionables por ítem (0% al 30%), desglose de Subtotal, Descuento, IVA (19%) y Total. |
| 📈 **Reportes** | Historial de transacciones con filtros por rango de fechas y búsqueda por cliente. Exportación directa a archivo CSV. |
| 🔧 **Usuarios** | Control de acceso por roles (*Administrador* y *Vendedor*), con almacenamiento seguro de contraseñas mediante **SHA-256**. |

---

## 🚀 Cómo Ejecutar el Sistema

### Opción 1: Un solo clic en Windows
Haz doble clic en el archivo **`EJECUTAR_SISTEMA.bat`** ubicado en la raíz del proyecto.

### Opción 2: Desde la terminal
```bash
cd SISTEMA_DE_VENTAS-main
javac -encoding UTF-8 CastilloVentas.java
java CastilloVentas
```

---

## 🔑 Credenciales por Defecto

| Usuario | Contraseña | Rol | Acceso |
|---|---|---|---|
| `admin` | `admin123` | Administrador | Acceso total (incluye gestión de usuarios) |
| `vendedor` | `1234` | Vendedor | Módulos de ventas, inventario, clientes y reportes |

---

## 📁 Estructura del Repositorio

```
Sistema_ventas_DEIBERFLOREZ/
├── EJECUTAR_SISTEMA.bat              ← Lanzador con doble clic para Windows
├── FIGMA_BOSQUEJO_SISTEMA            ← Enlace al diseño en Figma
├── Propuesta_ProductoAula_FlorezDeiber.docx ← Documento de propuesta
├── README.md                         ← Documentación general
└── SISTEMA_DE_VENTAS-main/
    ├── CastilloVentas.java           ← Código fuente v2.0
    ├── CastilloVentas.class          ← Archivo compilado
    ├── EJECUTAR_SISTEMA.bat          ← Lanzador alternativo
    └── castilloventas.dat            ← Base de datos local
```

---

## 🎨 Prototipo en Figma

Diseño de interfaz disponible en:  
🔗 [Ver bosquejo en Figma](https://www.figma.com/design/5AS3TXYrQng8i0ayWMHQGy/BOSQUEJO_FIGMA?node-id=1-2058&t=k0BZAcvIk7KA7SBM-1)
