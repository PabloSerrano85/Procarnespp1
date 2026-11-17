package com.example.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.local.CertificadoEntity
import com.example.data.local.OperadorEntity
import com.example.domain.RoccaCatalogos
import java.io.File
import java.io.FileOutputStream

object CartonF102PdfGenerator {

    fun generatePdf(
        context: Context,
        operador: OperadorEntity,
        certificado: CertificadoEntity? = null,
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Fondo blanco
        canvas.drawColor(Color.WHITE)

        // Borde rojo oficial del template F 102
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E51923")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRect(20f, 22f, 575f, 818f, borderPaint)

        // Badge F 102 negro
        val blackPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(262f, 38f, 333f, 64f), 4f, 4f, blackPaint)
        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("F 102", 297.5f, 56f, badgeTextPaint)

        // Logos institucionales (si existen en drawable)
        drawLogo(context, canvas, R.drawable.logo_m_produccion, Rect(48, 38, 128, 86))
        drawLogo(context, canvas, R.drawable.logo_registro_ope_carne, Rect(200, 74, 232, 114))
        drawLogo(context, canvas, R.drawable.logo_procarne, Rect(458, 38, 548, 80))

        // Textos institucionales de cabecera
        val ministryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Ministerio de Producción,", 88f, 100f, ministryPaint)
        canvas.drawText("Recursos Naturales,", 88f, 111f, ministryPaint)
        canvas.drawText("Forestación y Tierras", 88f, 122f, ministryPaint)

        val regTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 11.5f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("Registro Provincial de", 242f, 92f, regTitlePaint)
        canvas.drawText("OPERADORES DE CARNE", 242f, 107f, regTitlePaint)

        // Caja INSCRIPTO N°
        val inscriptoLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("INSCRIPTO", 495f, 98f, inscriptoLabelPaint)
        val boxStroke = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(435f, 104f, 555f, 126f, boxStroke)
        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("N° ${operador.numeroCertificadoF102}", 444f, 120f, numPaint)

        // Barra "Datos del Operador"
        drawSectionBar(canvas, 34f, 150f, 527f, 20f, "Datos del Operador")

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 10.5f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
        }

        var y = 190f
        drawField(canvas, "Apellido y Nombre:", operador.titular, 38f, 180f, y, labelPaint, valuePaint)
        y += 24f
        drawField(canvas, "Establecimiento:", operador.nombreEstablecimiento, 38f, 180f, y, labelPaint, valuePaint)
        y += 24f
        drawField(canvas, "Razón Social:", operador.razonSocial, 38f, 180f, y, labelPaint, valuePaint)
        y += 24f
        drawField(canvas, "C.U.I.T.:", operador.cuit, 38f, 180f, y, labelPaint, valuePaint)
        y += 24f
        drawField(canvas, "Estado Registral:", operador.estadoRegistral, 38f, 180f, y, labelPaint, valuePaint)
        if (!operador.habilitacionSanitaria.isNullOrBlank()) {
            y += 24f
            drawField(canvas, "Habilitación Sanitaria:", operador.habilitacionSanitaria, 38f, 180f, y, labelPaint, valuePaint)
        }

        // Barra Fechas
        y += 32f
        drawSectionBar(canvas, 34f, y, 255f, 20f, "Fecha de Inscripción")
        drawSectionBar(canvas, 305f, y, 256f, 20f, "Vencimiento Certificado")
        y += 38f
        val fechaEmision = fmtFecha(certificado?.fechaEmision ?: operador.altaFecha ?: operador.creadoEn)
        val fechaVenc = fmtFecha(certificado?.fechaVencimiento)
        canvas.drawText(fechaEmision, 40f, y, valuePaint)
        canvas.drawText(fechaVenc, 311f, y, valuePaint)

        // Barra Establecimientos
        y += 26f
        drawSectionBar(canvas, 34f, y, 527f, 20f, "Datos de los Establecimientos")
        y += 38f
        drawField(
            canvas,
            "Domicilio:",
            operador.domicilioFormateado.ifBlank { "Santiago del Estero" },
            38f,
            140f,
            y,
            labelPaint,
            valuePaint,
        )
        y += 24f
        drawField(
            canvas,
            "Departamento:",
            operador.departamento.ifBlank { "Capital" },
            38f,
            140f,
            y,
            labelPaint,
            valuePaint,
        )
        y += 24f
        drawField(
            canvas,
            "Tipo de Carne:",
            operador.tiposCarneCsv.ifBlank { RoccaCatalogos.especiesParaCategoria(operador.categoria) },
            38f,
            140f,
            y,
            labelPaint,
            valuePaint,
        )

        // Tabla de Actividades AFIP
        y += 32f
        drawSectionBar(canvas, 34f, y, 527f, 20f, "Actividades Económicas Habilitadas (AFIP)")
        y += 32f
        val headPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.FILL
        }
        canvas.drawRect(34f, y - 14f, 561f, y + 6f, headPaint)
        canvas.drawRect(34f, y - 14f, 561f, y + 6f, boxStroke)
        canvas.drawText("Código AFIP", 42f, y, valuePaint)
        canvas.drawText("Descripción de Actividad", 150f, y, valuePaint)

        val cats = operador.categoriasLista
        cats.forEach { catId ->
            y += 22f
            canvas.drawRect(34f, y - 14f, 561f, y + 6f, boxStroke)
            canvas.drawText(RoccaCatalogos.codigoAfipParaCategoria(catId), 42f, y, labelPaint)
            canvas.drawText(RoccaCatalogos.nombreCategoria(catId), 150f, y, labelPaint)
        }

        // Firma y sello al pie
        val sigLineY = 735f
        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
        }
        canvas.drawLine(180f, sigLineY, 415f, sigLineY, linePaint)
        val sigText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Firma y Sello de la Autoridad Competente", 297.5f, sigLineY + 15f, sigText)
        canvas.drawText("Dirección General de Agricultura y Ganadería • Santiago del Estero", 297.5f, sigLineY + 29f, ministryPaint)

        doc.finishPage(page)

        val file = File(context.cacheDir, "F102-${operador.numeroCertificadoF102}.pdf")
        FileOutputStream(file).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        return file
    }

    fun getShareUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun drawLogo(context: Context, canvas: Canvas, resId: Int, dst: Rect) {
        runCatching {
            val bmp: Bitmap? = BitmapFactory.decodeResource(context.resources, resId)
            if (bmp != null) {
                canvas.drawBitmap(bmp, null, dst, null)
            }
        }
    }

    private fun drawSectionBar(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, title: String) {
        val barPaint = Paint().apply {
            color = Color.parseColor("#FCE7E9")
            style = Paint.Style.FILL
        }
        val barAccent = Paint().apply {
            color = Color.parseColor("#8B1425")
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + w, y + h, barPaint)
        canvas.drawRect(x, y, x + 5f, y + h, barAccent)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(title, x + 12f, y + 14f, textPaint)
    }

    private fun drawField(
        canvas: Canvas,
        label: String,
        value: String,
        xLabel: Float,
        xValue: Float,
        y: Float,
        labelPaint: Paint,
        valuePaint: Paint,
    ) {
        canvas.drawText(label, xLabel, y, labelPaint)
        val clipped = if (value.length > 52) value.take(49) + "…" else value
        canvas.drawText(clipped.ifBlank { "—" }, xValue, y, valuePaint)
    }

    private fun fmtFecha(iso: String?): String {
        if (iso.isNullOrBlank()) return "Sin vencimiento"
        return if (iso.length >= 10) {
            val parts = iso.substring(0, 10).split("-")
            if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
        } else iso
    }
}
