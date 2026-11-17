package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.CertificadoEntity
import com.example.data.local.OperadorEntity
import com.example.domain.RoccaCatalogos
import com.example.export.CartonF102PdfGenerator
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.RoccaAzulOscuro
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaBorde
import com.example.ui.theme.RoccaBordeInput
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaRojoPastel
import com.example.ui.theme.RoccaTarjeta
import com.example.ui.theme.RoccaTexto
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTextoMedio

@Composable
fun CartonF102Dialog(
    operador: OperadorEntity,
    certificadoVigente: CertificadoEntity?,
    onDismiss: () -> Unit,
    onNotify: (String) -> Unit,
) {
    val context = LocalContext.current

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destUri ->
        if (destUri != null) {
            runCatching {
                val pdfFile = CartonF102PdfGenerator.generatePdf(context, operador, certificadoVigente)
                context.contentResolver.openOutputStream(destUri)?.use { out ->
                    pdfFile.inputStream().use { it.copyTo(out) }
                }
                onNotify("PDF guardado: F102-${operador.numeroCertificadoF102}.pdf")
            }.onFailure {
                onNotify("Error al guardar PDF: ${it.message}")
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = RoccaFondo,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoccaTarjeta)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(RoccaRojo, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = RoccaBlanco,
                            )
                        }
                        Column {
                            Text(
                                text = "Cartón F 102 — N° ${operador.numeroCertificadoF102}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoccaAzulOscuro,
                            )
                            Text(
                                text = "Formulario Oficial de Inscripción Provincial",
                                style = MaterialTheme.typography.bodySmall,
                                color = RoccaTextoApagado,
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_carton_dialog"),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = RoccaAzulOscuro)
                    }
                }

                HorizontalDivider(color = RoccaBorde)

                // Cuerpo: Réplica oficial del Cartón F 102
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RoccaBlanco)
                            .border(3.dp, Color(0xFFE51923))
                            .padding(14.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Badge F 102 centrado
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Surface(
                                    color = Color.Black,
                                    shape = RoundedCornerShape(3.dp),
                                ) {
                                    Text(
                                        text = "F 102",
                                        color = RoccaBlanco,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    )
                                }
                            }

                            // Logos y Encabezado Institucional
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_m_produccion),
                                        contentDescription = "Logo Ministerio de Producción",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.height(34.dp),
                                    )
                                    Text(
                                        text = "Ministerio de Producción,\nRecursos Naturales,\nForestación y Tierras",
                                        fontSize = 8.sp,
                                        lineHeight = 10.sp,
                                        textAlign = TextAlign.Center,
                                        color = RoccaTexto,
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1.2f),
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_registro_ope_carne),
                                        contentDescription = "Logo Registro Operadores",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.height(32.dp),
                                    )
                                    Text(
                                        text = "Registro Provincial de\nOPERADORES DE CARNE",
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = Color.Black,
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_procarne),
                                        contentDescription = "Logo ProCarne",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.height(30.dp),
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "INSCRIPTO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black,
                                    )
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, Color.Black)
                                            .padding(horizontal = 8.dp, vertical = 3.dp),
                                    ) {
                                        Text(
                                            text = "N° ${operador.numeroCertificadoF102}",
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.Black,
                                        )
                                    }
                                }
                            }

                            // Sección Datos del Operador
                            CartonBar("Datos del Operador")
                            CartonDataRow("Apellido y Nombre:", operador.titular)
                            CartonDataRow("Establecimiento:", operador.nombreEstablecimiento)
                            CartonDataRow("Razón Social:", operador.razonSocial)
                            CartonDataRow("C.U.I.T.:", operador.cuit)
                            CartonDataRow("Estado:", operador.estadoRegistral)

                            // Fechas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    CartonBar("Fecha Inscripción")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = fmtFechaCorto(certificadoVigente?.fechaEmision ?: operador.altaFecha ?: operador.creadoEn),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    CartonBar("Vencimiento")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = fmtFechaCorto(certificadoVigente?.fechaVencimiento),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            // Establecimientos
                            CartonBar("Datos de los Establecimientos")
                            CartonDataRow("Domicilio:", operador.domicilioFormateado)
                            CartonDataRow("Departamento:", operador.departamento.ifBlank { "Capital" })
                            CartonDataRow(
                                "Tipo de Carne:",
                                operador.tiposCarneCsv.ifBlank {
                                    RoccaCatalogos.especiesParaCategoria(operador.categoria)
                                },
                            )
                            if (!operador.habilitacionSanitaria.isNullOrBlank()) {
                                CartonDataRow("Habilitación:", operador.habilitacionSanitaria)
                            }

                            // Actividades AFIP
                            CartonBar("Actividades Económicas (AFIP)")
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.Black),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFD9D9D9))
                                        .padding(6.dp),
                                ) {
                                    Text(
                                        text = "Cód. AFIP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.width(72.dp),
                                    )
                                    Text(
                                        text = "Actividad Habilitada",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                    )
                                }
                                operador.categoriasLista.forEach { catId ->
                                    HorizontalDivider(color = Color.Black, thickness = 0.5.dp)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                    ) {
                                        Text(
                                            text = RoccaCatalogos.codigoAfipParaCategoria(catId),
                                            fontFamily = JetBrainsMono,
                                            fontSize = 10.sp,
                                            modifier = Modifier.width(72.dp),
                                        )
                                        Text(
                                            text = RoccaCatalogos.nombreCategoria(catId),
                                            fontSize = 10.sp,
                                        )
                                    }
                                }
                            }

                            // Firma y Sello
                            Spacer(modifier = Modifier.height(14.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                HorizontalDivider(
                                    color = Color.Black,
                                    modifier = Modifier.width(200.dp),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Firma y Sello de la Autoridad Competente",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                )
                                Text(
                                    text = "Dirección General de Agricultura y Ganadería • Santiago del Estero",
                                    fontSize = 9.sp,
                                    color = RoccaTextoApagado,
                                )
                            }
                        }
                    }
                }

                // Footer acciones
                HorizontalDivider(color = RoccaBorde)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoccaTarjeta)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            savePdfLauncher.launch("F102-${operador.numeroCertificadoF102}.pdf")
                        },
                        border = BorderStroke(1.dp, RoccaBordeInput),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("guardar_pdf_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = RoccaTextoMedio,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar PDF", color = RoccaTextoMedio, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            runCatching {
                                val pdfFile = CartonF102PdfGenerator.generatePdf(context, operador, certificadoVigente)
                                val uri = CartonF102PdfGenerator.getShareUri(context, pdfFile)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Cartón F 102 N° ${operador.numeroCertificadoF102}")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartir Cartón F 102"))
                            }.onFailure {
                                onNotify("Error al generar PDF: ${it.message}")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("compartir_pdf_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir PDF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartonBar(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(RoccaRojoPastel)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RoccaAzulOscuro,
        )
    }
}

@Composable
private fun CartonDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = RoccaTextoMedio,
            modifier = Modifier.width(108.dp),
        )
        Text(
            text = value.ifBlank { "—" },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun fmtFechaCorto(iso: String?): String {
    if (iso.isNullOrBlank()) return "Sin vencimiento"
    return if (iso.length >= 10) {
        val parts = iso.substring(0, 10).split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
    } else iso
}
