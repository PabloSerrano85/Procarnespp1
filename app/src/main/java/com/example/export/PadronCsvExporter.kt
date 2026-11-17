package com.example.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.OperadorEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PadronCsvExporter {
    fun exportCsv(context: Context, operadores: List<OperadorEntity>): File {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val file = File(context.cacheDir, "padron_rocca_$dateStr.csv")

        val sb = StringBuilder()
        // UTF-8 BOM para Excel en español
        sb.append('\uFEFF')
        sb.append("N° Certificado,Razón Social,Establecimiento,CUIT,Categoría,Domicilio,Teléfono,Estado\n")

        operadores.forEach { op ->
            val cols = listOf(
                op.numeroCertificadoF102,
                op.razonSocial,
                op.nombreEstablecimiento,
                op.cuit,
                op.categoria,
                op.domicilioFormateado,
                op.telefono.orEmpty(),
                op.estadoRegistral,
            ).map { "\"${it.replace("\"", "\"\"")}\"" }
            sb.append(cols.joinToString(",")).append("\n")
        }

        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }

    fun getShareUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
